package com.mobileproject.mobileprojectbackend.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private static final int PAGE_SIZE = 20;

    private final AppNotificationRepository notificationRepository;
    private final CoupleInfoRepository coupleInfoRepository;
    private final FcmPushService fcmPushService;
    private final MongoTemplate mongoTemplate;

    public NotificationService(
            AppNotificationRepository notificationRepository,
            CoupleInfoRepository coupleInfoRepository,
            FcmPushService fcmPushService,
            MongoTemplate mongoTemplate) {
        this.notificationRepository = notificationRepository;
        this.coupleInfoRepository = coupleInfoRepository;
        this.fcmPushService = fcmPushService;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Creates a notification for a single user and sends FCM push.
     */
    public void createAndPush(String userId, NotificationType type, String title, String body) {
        if (userId == null || userId.isBlank())
            return;
        AppNotification notification = new AppNotification(userId, type, title, body);
        notificationRepository.save(notification);
        fcmPushService.sendGeneralPush(userId, type.name().toLowerCase(), title, body);
        LOGGER.debug("Notification created for user={} type={}", userId, type);
    }

    /**
     * Creates notifications for both users in a couple and sends FCM push.
     */
    public void createAndPushForCouple(String coupleId, NotificationType type, String title, String body) {
        if (coupleId == null || coupleId.isBlank())
            return;
        CoupleInfo couple = coupleInfoRepository.findById(coupleId).orElse(null);
        if (couple == null)
            return;
        String user1 = couple.getIdUser1();
        String user2 = couple.getIdUser2();
        if (user1 != null && !user1.isBlank())
            createAndPush(user1, type, title, body);
        if (user2 != null && !user2.isBlank())
            createAndPush(user2, type, title, body);
    }

    /**
     * Creates a notification for the partner (the other user) only.
     */
    public void createAndPushForPartner(String coupleId, String senderUserId, NotificationType type, String title,
            String body) {
        if (coupleId == null || coupleId.isBlank() || senderUserId == null)
            return;
        CoupleInfo couple = coupleInfoRepository.findById(coupleId).orElse(null);
        if (couple == null)
            return;
        String user1 = couple.getIdUser1();
        String user2 = couple.getIdUser2();
        String partnerId = senderUserId.equals(user1) ? user2 : user1;
        if (partnerId != null && !partnerId.isBlank()) {
            createAndPush(partnerId, type, title, body);
        }
    }

    public Page<AppNotification> getNotifications(String userId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markAllRead(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("read").is(false));
        Update update = new Update().set("read", true);
        mongoTemplate.updateMulti(query, update, AppNotification.class);
    }

    public void markRead(String userId, String notificationId) {
        Query query = new Query(Criteria.where("id").is(notificationId).and("userId").is(userId));
        Update update = new Update().set("read", true);
        mongoTemplate.updateFirst(query, update, AppNotification.class);
    }
}
