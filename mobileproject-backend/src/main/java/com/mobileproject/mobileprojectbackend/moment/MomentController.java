package com.mobileproject.mobileprojectbackend.moment;

import com.mobileproject.mobileprojectbackend.moment.dto.MomentRequest;
import com.mobileproject.mobileprojectbackend.moment.dto.MomentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moments")
public class MomentController {

    private final MomentService momentService;

    public MomentController(MomentService momentService) {
        this.momentService = momentService;
    }

    @GetMapping
    public ResponseEntity<List<Moment>> getMoments(@RequestParam String coupleId) {
        return ResponseEntity.ok(momentService.getMoments(coupleId));
    }

    @PostMapping
    public ResponseEntity<MomentResponse> createMoment(@RequestBody MomentRequest request) {
        try {
            Moment moment = momentService.saveMoment(
                request.coupleId(),
                request.title(),
                request.base64Image()
            );
            return ResponseEntity.ok(new MomentResponse(true, "Moment created successfully", moment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MomentResponse(false, e.getMessage(), null));
        }
    }
}
