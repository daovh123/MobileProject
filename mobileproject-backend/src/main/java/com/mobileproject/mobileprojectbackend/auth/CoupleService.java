package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.CoupleCodeResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CouplePartnerProfileResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestActionResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestCreateRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestDecisionRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý nghiệp vụ ghép đôi (couple) giữa hai người dùng.
 *
 * <p>Nghiệp vụ chính:</p>
 * <ul>
 *   <li><b>Tạo mã ghép đôi</b> - Sinh mã 6 số (XXX-XXX) có hiệu lực 15 phút, lưu trên Redis</li>
 *   <li><b>Gửi yêu cầu ghép đôi</b> - Người dùng nhập mã đối tác, tạo CoupleRequest PENDING</li>
 *   <li><b>Chấp nhận/Từ chối</b> - Đối tác quyết định, nếu chấp nhận thì liên kết 2 user
 *       và tạo CoupleInfo</li>
 *   <li><b>Xem trạng thái</b> - Trả về thông tin ghép đôi, mã cá nhân, yêu cầu đang chờ,
 *       số ngày bên nhau, kỷ niệm</li>
 *   <li><b>Xem hồ sơ đối tác</b> - Lấy thông tin cá nhân của partner đã ghép đôi</li>
 * </ul>
 *
 * <p>Logic tính ngày bên nhau:</p>
 * <ul>
 *   <li>Ngày bắt đầu được suy từ CoupleInfo.startAt hoặc CoupleRequest.accepted time</li>
 *   <li>{@code daysTogether} = số ngày từ startAt đến hôm nay + 1 (tính cả ngày đầu)</li>
 *   <li>{@code anniversaryTomorrow} = true nếu hôm nay là ngày kỷ niệm (cùng ngày/tháng)</li>
 * </ul>
 *
 * <p>CoupleInfo ID được tạo canonical: {@code couple:{min(userId1,userId2)}:{max(userId1,userId2)}}</p>
 */
@Service
public class CoupleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoupleService.class);

    private final AuthIdentityService authIdentityService;
    private final AuthUserCacheService authUserCacheService;
    private final CoupleRequestRepository coupleRequestRepository;
    private final CoupleCodeCacheService coupleCodeCacheService;
    private final CoupleInfoRepository coupleInfoRepository;

    public CoupleService(AuthIdentityService authIdentityService,
            AuthUserCacheService authUserCacheService,
            CoupleRequestRepository coupleRequestRepository,
            CoupleCodeCacheService coupleCodeCacheService,
            CoupleInfoRepository coupleInfoRepository) {
        this.authIdentityService = authIdentityService;
        this.authUserCacheService = authUserCacheService;
        this.coupleRequestRepository = coupleRequestRepository;
        this.coupleCodeCacheService = coupleCodeCacheService;
        this.coupleInfoRepository = coupleInfoRepository;
    }

    /**
     * Tạo hoặc lấy mã ghép đôi của người dùng hiện tại.
     * Yêu cầu profile đã hoàn thành và chưa ghép đôi.
     *
     * @param authorizationHeader header Authorization
     * @return CoupleCodeResponse chứa mã và thời gian hết hạn
     */
    public CoupleCodeResponse generateMyCode(String authorizationHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        ensureProfileCompleted(user);
        ensureNotPaired(user);

        CoupleCodeCacheService.CoupleCodeLease lease = coupleCodeCacheService.getOrCreateCode(user.getId());

        return CoupleCodeResponse.success("Couple code is ready", lease.code(), lease.expiresAt().toString());
    }

    /**
     * Tạo yêu cầu ghép đôi mới bằng mã đối tác.
     * Kiểm tra: profile hoàn thành, chưa ghép, không có yêu cầu đang chờ,
     * mã đối tác hợp lệ, đối tác chưa ghép.
     *
     * @param authorizationHeader header Authorization
     * @param request             chứa partnerCode (mã 6 số)
     * @return CoupleRequestActionResponse với thông tin yêu cầu đã tạo
     */
    public CoupleRequestActionResponse createRequest(String authorizationHeader, CoupleRequestCreateRequest request) {
        AuthUser requester = authIdentityService.requireCurrentUser(authorizationHeader);
        ensureProfileCompleted(requester);
        ensureNotPaired(requester);
        ensureNoPendingRequestForUser(requester.getId(), "You already have a pending couple request");

        String normalizedPartnerCode = normalizeCode(request.partnerCode());
        String recipientUserId = coupleCodeCacheService.findUserIdByCode(normalizedPartnerCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner code does not exist"));
        AuthUser recipient = authUserCacheService.findById(recipientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner code does not exist"));

        if (requester.getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot connect with your own code");
        }

        ensureProfileCompleted(recipient);
        ensureNotPaired(recipient);
        ensureNoPendingRequestForUser(recipient.getId(), "Partner currently has another pending request");

        String now = Instant.now().toString();

        CoupleRequest coupleRequest = new CoupleRequest();
        coupleRequest.setRequesterUserId(requester.getId());
        coupleRequest.setRequesterUsername(requester.getUsername());
        coupleRequest.setRequesterDisplayName(displayNameOf(requester));
        coupleRequest.setRecipientUserId(recipient.getId());
        coupleRequest.setRecipientUsername(recipient.getUsername());
        coupleRequest.setStatus(CoupleRequestStatus.PENDING);
        coupleRequest.setCreatedAt(now);
        coupleRequest.setUpdatedAt(now);

        CoupleRequest savedRequest = coupleRequestRepository.save(coupleRequest);

        return CoupleRequestActionResponse.success(
                "Couple request sent. Waiting for partner confirmation",
                savedRequest.getId(),
                savedRequest.getStatus().name(),
                savedRequest.getRequesterUsername(),
                savedRequest.getRecipientUsername());
    }

    /**
     * Quyết định (chấp nhận/từ chối) yêu cầu ghép đôi.
     * Khi chấp nhận: liên kết 2 user, tạo CoupleInfo, vô hiệu hóa mã ghép đôi của cả 2.
     *
     * @param authorizationHeader header Authorization
     * @param requestId           ID của yêu cầu ghép đôi
     * @param request             chứa accept (true/false)
     * @return CoupleRequestActionResponse với trạng thái mới
     */
    public CoupleRequestActionResponse decideRequest(String authorizationHeader,
            String requestId,
            CoupleRequestDecisionRequest request) {
        AuthUser recipient = authIdentityService.requireCurrentUser(authorizationHeader);
        ensureProfileCompleted(recipient);

        CoupleRequest coupleRequest = coupleRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple request not found"));

        if (!recipient.getId().equals(coupleRequest.getRecipientUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recipient can decide this request");
        }
        if (coupleRequest.getStatus() != CoupleRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Couple request is already processed");
        }

        AuthUser requester = authUserCacheService.findById(coupleRequest.getRequesterUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Requester no longer exists"));

        String now = Instant.now().toString();
        if (request.accept()) {
            ensureNotPaired(requester);
            ensureNotPaired(recipient);

            requester.setPartnerUserId(recipient.getId());
            recipient.setPartnerUserId(requester.getId());

            authUserCacheService.save(requester);
            authUserCacheService.save(recipient);
            coupleCodeCacheService.invalidateCodeByUserId(requester.getId());
            coupleCodeCacheService.invalidateCodeByUserId(recipient.getId());

            String user1 = canonicalUser1(requester.getId(), recipient.getId());
            String user2 = canonicalUser2(requester.getId(), recipient.getId());
            String coupleId = coupleIdForUsers(user1, user2);
            CoupleInfo coupleInfo = new CoupleInfo();
            coupleInfo.setId(coupleId);
            coupleInfo.setIdCouple(coupleId);
            coupleInfo.setIdUser1(user1);
            coupleInfo.setIdUser2(user2);
            coupleInfo.setStartAt(now);
            try {
                coupleInfoRepository.save(coupleInfo);
            } catch (Exception exception) {
                LOGGER.warn(
                        "Failed to persist couple_info for requester={} recipient={}.",
                        requester.getId(),
                        recipient.getId(),
                        exception);
            }

            coupleRequest.setStatus(CoupleRequestStatus.ACCEPTED);
            coupleRequest.setUpdatedAt(now);
            CoupleRequest savedRequest = coupleRequestRepository.save(coupleRequest);

            return CoupleRequestActionResponse.success(
                    "Couple request accepted",
                    savedRequest.getId(),
                    savedRequest.getStatus().name(),
                    savedRequest.getRequesterUsername(),
                    savedRequest.getRecipientUsername());
        }

        coupleRequest.setStatus(CoupleRequestStatus.REJECTED);
        coupleRequest.setUpdatedAt(now);
        CoupleRequest savedRequest = coupleRequestRepository.save(coupleRequest);

        return CoupleRequestActionResponse.success(
                "Couple request rejected",
                savedRequest.getId(),
                savedRequest.getStatus().name(),
                savedRequest.getRequesterUsername(),
                savedRequest.getRecipientUsername());
    }

    /**
     * Lấy trạng thái ghép đôi tổng hợp của người dùng hiện tại.
     * Bao gồm: mã cá nhân, thông tin partner, CoupleInfo, yêu cầu đang chờ, ngày bên nhau.
     *
     * @param authorizationHeader header Authorization
     * @return CoupleStatusResponse với toàn bộ trạng thái ghép đôi
     */
    public CoupleStatusResponse getStatus(String authorizationHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);

        String myCode = null;
        String myCodeExpiresAt = null;
        if (user.isProfileCompleted() && !isPaired(user)) {
            CoupleCodeCacheService.CoupleCodeLease lease = coupleCodeCacheService.getOrCreateCode(user.getId());
            myCode = lease.code();
            myCodeExpiresAt = lease.expiresAt().toString();
        }

        AuthUser partner = null;
        if (isPaired(user)) {
            partner = authUserCacheService.findById(user.getPartnerUserId()).orElse(null);
        }

        String coupleId = null;
        String startAt = null;
        Long daysTogether = null;
        Boolean anniversaryTomorrow = null;

        if (isPaired(user) && partner != null) {
            CoupleInfo coupleInfo = getOrCreateCoupleInfo(user.getId(), partner.getId());

            if (coupleInfo != null) {
                coupleId = coupleInfo.getId();
                startAt = coupleInfo.getStartAt();
                DaysTogetherResult daysTogetherResult = computeDaysTogether(startAt);
                if (daysTogetherResult != null) {
                    daysTogether = daysTogetherResult.daysTogether();
                    anniversaryTomorrow = daysTogetherResult.anniversaryToday();
                }
            }
        }

        CoupleRequest incoming = null;
        if (!isPaired(user)) {
            incoming = coupleRequestRepository
                    .findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), CoupleRequestStatus.PENDING)
                    .orElse(null);
        }

        CoupleRequest outgoing = coupleRequestRepository
                .findFirstByRequesterUserIdOrderByUpdatedAtDesc(user.getId())
                .orElse(null);

        return CoupleStatusResponse.success(
                "Couple status fetched successfully",
                user.isProfileCompleted(),
                partner != null,
                partner == null ? null : partner.getUsername(),
                myCode,
                myCodeExpiresAt,
                incoming == null ? null : incoming.getId(),
                incoming == null ? null : incoming.getRequesterUsername(),
                incoming == null ? null : incoming.getRequesterDisplayName(),
                incoming == null ? null : incoming.getCreatedAt(),
                outgoing == null ? null : outgoing.getId(),
                outgoing == null ? null : outgoing.getRecipientUsername(),
                outgoing == null ? null : outgoing.getStatus().name(),
                outgoing == null ? null : outgoing.getUpdatedAt(),
                coupleId,
                startAt,
                daysTogether,
                anniversaryTomorrow);
    }

    /**
     * Lấy thông tin hồ sơ của đối tác đã ghép đôi.
     * Trả về thông tin rỗng nếu chưa ghép đôi hoặc đối tác không tồn tại.
     *
     * @param authorizationHeader header Authorization
     * @return CouplePartnerProfileResponse với thông tin đối tác
     */
    public CouplePartnerProfileResponse getPartnerProfile(String authorizationHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);

        if (!isPaired(user)) {
            return CouplePartnerProfileResponse.success(
                    "User is not connected to a partner",
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        AuthUser partner = authUserCacheService.findById(user.getPartnerUserId()).orElse(null);
        if (partner == null) {
            return CouplePartnerProfileResponse.success(
                    "Partner profile is currently unavailable",
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        String startAt = null;
        Long daysTogether = null;
        CoupleInfo coupleInfo = getOrCreateCoupleInfo(user.getId(), partner.getId());
        if (coupleInfo != null) {
            startAt = coupleInfo.getStartAt();
            DaysTogetherResult result = computeDaysTogether(startAt);
            if (result != null) {
                daysTogether = result.daysTogether();
            }
        }

        return CouplePartnerProfileResponse.success(
                "Partner profile fetched successfully",
                true,
                partner.getUsername(),
                partner.getFullName(),
                partner.getNickName(),
                partner.getAvatarUrl(),
                startAt,
                daysTogether,
                partner.getBirthDate(),
                partner.getGender(),
                partner.getPhoneNumber());
    }

    private DaysTogetherResult computeDaysTogether(String startAt) {
        if (isBlank(startAt)) {
            return null;
        }

        try {
            LocalDate startDate = Instant.parse(startAt).atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate today = LocalDate.now(ZoneOffset.UTC);

            long days = ChronoUnit.DAYS.between(startDate, today) + 1;
            if (days < 1) {
                days = 1;
            }

            boolean anniversaryToday = today.getMonthValue() == startDate.getMonthValue()
                    && today.getDayOfMonth() == startDate.getDayOfMonth();

            return new DaysTogetherResult(days, anniversaryToday);
        } catch (Exception ignored) {
            return null;
        }
    }

    private record DaysTogetherResult(long daysTogether, boolean anniversaryToday) {
    }

    private CoupleInfo getOrCreateCoupleInfo(String userId, String partnerId) {
        String user1 = canonicalUser1(userId, partnerId);
        String user2 = canonicalUser2(userId, partnerId);
        String coupleId = coupleIdForUsers(user1, user2);

        CoupleInfo stableExisting = coupleInfoRepository.findById(coupleId).orElse(null);
        if (stableExisting != null) {
            return stableExisting;
        }

        List<CoupleInfo> legacy = new ArrayList<>();
        try {
            legacy.addAll(coupleInfoRepository.findByIdUser1AndIdUser2(user1, user2));
            legacy.addAll(coupleInfoRepository.findByIdUser1AndIdUser2(user2, user1));
        } catch (Exception exception) {
            LOGGER.warn("Failed to query legacy couple_info for user1={} user2={}", user1, user2, exception);
        }

        String derivedStartAt = pickEarliestStartAt(legacy);
        if (isBlank(derivedStartAt)) {
            derivedStartAt = deriveStartAtFromAcceptedRequest(userId, partnerId);
        }
        if (isBlank(derivedStartAt)) {
            derivedStartAt = Instant.now().toString();
        }

        CoupleInfo coupleInfo = new CoupleInfo();
        coupleInfo.setId(coupleId);
        coupleInfo.setIdCouple(coupleId);
        coupleInfo.setIdUser1(user1);
        coupleInfo.setIdUser2(user2);
        coupleInfo.setStartAt(derivedStartAt);

        try {
            coupleInfoRepository.save(coupleInfo);
        } catch (Exception exception) {
            LOGGER.warn(
                    "Failed to backfill couple_info for user1={} user2={}.",
                    user1,
                    user2,
                    exception);
            return null;
        }

        if (!legacy.isEmpty()) {
            try {
                for (CoupleInfo legacyInfo : legacy) {
                    if (legacyInfo == null || isBlank(legacyInfo.getId())) {
                        continue;
                    }
                    if (coupleId.equals(legacyInfo.getId())) {
                        continue;
                    }
                    coupleInfoRepository.deleteById(legacyInfo.getId());
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to cleanup legacy couple_info for coupleId={}", coupleId, exception);
            }
        }

        return coupleInfo;
    }

    private String canonicalUser1(String userId, String partnerId) {
        return userId.compareTo(partnerId) <= 0 ? userId : partnerId;
    }

    private String canonicalUser2(String userId, String partnerId) {
        return userId.compareTo(partnerId) <= 0 ? partnerId : userId;
    }

    private String coupleIdForUsers(String user1, String user2) {
        return "couple:" + user1 + ":" + user2;
    }

    private String pickEarliestStartAt(List<CoupleInfo> legacy) {
        if (legacy == null || legacy.isEmpty()) {
            return null;
        }

        Instant best = null;
        String bestValue = null;

        for (CoupleInfo info : legacy) {
            if (info == null) {
                continue;
            }
            String value = info.getStartAt();
            Instant parsed = parseInstantOrNull(value);
            if (parsed == null) {
                continue;
            }
            if (best == null || parsed.isBefore(best)) {
                best = parsed;
                bestValue = value;
            }
        }

        if (!isBlank(bestValue)) {
            return bestValue;
        }

        for (CoupleInfo info : legacy) {
            if (info == null) {
                continue;
            }
            if (!isBlank(info.getStartAt())) {
                return info.getStartAt();
            }
        }

        return null;
    }

    private String deriveStartAtFromAcceptedRequest(String userId, String partnerId) {
        CoupleRequest a = coupleRequestRepository
                .findFirstByRequesterUserIdAndRecipientUserIdAndStatusOrderByUpdatedAtDesc(
                        userId,
                        partnerId,
                        CoupleRequestStatus.ACCEPTED)
                .orElse(null);

        CoupleRequest b = coupleRequestRepository
                .findFirstByRequesterUserIdAndRecipientUserIdAndStatusOrderByUpdatedAtDesc(
                        partnerId,
                        userId,
                        CoupleRequestStatus.ACCEPTED)
                .orElse(null);

        CoupleRequest latest = latestByUpdatedAt(a, b);
        if (latest == null) {
            return null;
        }

        if (!isBlank(latest.getUpdatedAt())) {
            return latest.getUpdatedAt();
        }
        if (!isBlank(latest.getCreatedAt())) {
            return latest.getCreatedAt();
        }
        return null;
    }

    private CoupleRequest latestByUpdatedAt(CoupleRequest a, CoupleRequest b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }

        Instant aInstant = parseInstantOrNull(a.getUpdatedAt());
        Instant bInstant = parseInstantOrNull(b.getUpdatedAt());

        if (aInstant == null && bInstant == null) {
            return a;
        }
        if (aInstant == null) {
            return b;
        }
        if (bInstant == null) {
            return a;
        }

        return aInstant.isAfter(bInstant) ? a : b;
    }

    private Instant parseInstantOrNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void ensureProfileCompleted(AuthUser user) {
        if (!user.isProfileCompleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please complete profile first");
        }
    }

    private void ensureNotPaired(AuthUser user) {
        if (isPaired(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already connected to a partner");
        }
    }

    private void ensureNoPendingRequestForUser(String userId, String conflictMessage) {
        Optional<CoupleRequest> outgoingPending = coupleRequestRepository
                .findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc(userId, CoupleRequestStatus.PENDING);
        Optional<CoupleRequest> incomingPending = coupleRequestRepository
                .findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc(userId, CoupleRequestStatus.PENDING);

        if (outgoingPending.isPresent() || incomingPending.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMessage);
        }
    }

    private boolean isPaired(AuthUser user) {
        return !isBlank(user.getPartnerUserId());
    }

    private String displayNameOf(AuthUser user) {
        if (!isBlank(user.getNickName())) {
            return user.getNickName();
        }
        if (!isBlank(user.getFullName())) {
            return user.getFullName();
        }
        return user.getUsername();
    }

    private String normalizeCode(String rawCode) {
        if (rawCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Partner code is required");
        }

        String digitsOnly = rawCode.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Partner code must have 6 digits");
        }

        return digitsOnly.substring(0, 3) + "-" + digitsOnly.substring(3);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
