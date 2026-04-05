package com.mobileproject.mobileprojectbackend.map;

import com.mobileproject.mobileprojectbackend.auth.AuthIdentityService;
import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import com.mobileproject.mobileprojectbackend.auth.AuthUserRepository;
import com.mobileproject.mobileprojectbackend.map.dto.LocationDto;
import com.mobileproject.mobileprojectbackend.map.dto.MapLastLocationsResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth/map")
public class MapController {

    private final AuthIdentityService authIdentityService;
    private final AuthUserRepository authUserRepository;
    private final MapLocationService mapLocationService;

    public MapController(
            AuthIdentityService authIdentityService,
            AuthUserRepository authUserRepository,
            MapLocationService mapLocationService
    ) {
        this.authIdentityService = authIdentityService;
        this.authUserRepository = authUserRepository;
        this.mapLocationService = mapLocationService;
    }

    @GetMapping("/last")
    public ResponseEntity<MapLastLocationsResponse> getLastLocations(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);

        if (isBlank(user.getPartnerUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is not connected to a partner");
        }

        AuthUser partner = authUserRepository.findById(user.getPartnerUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Partner not found"));

        String coupleId = coupleIdForUsers(user.getId(), partner.getId());

        UserLocation my = mapLocationService.findUserLocation(coupleId, user.getId());
        UserLocation partnerLoc = mapLocationService.findUserLocation(coupleId, partner.getId());

        LocationDto myDto = toDto(my);
        LocationDto partnerDto = toDto(partnerLoc);

        return ResponseEntity.ok(MapLastLocationsResponse.success(
                "Last known locations fetched successfully",
                coupleId,
                myDto,
                partnerDto
        ));
    }

    private LocationDto toDto(UserLocation location) {
        if (location == null || location.getLocation() == null) {
            return null;
        }

        return new LocationDto(
                location.getLocation().getY(),
                location.getLocation().getX(),
                location.getUpdatedAt()
        );
    }

    private String coupleIdForUsers(String a, String b) {
        String user1 = a.compareTo(b) <= 0 ? a : b;
        String user2 = a.compareTo(b) <= 0 ? b : a;
        return "couple:" + user1 + ":" + user2;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
