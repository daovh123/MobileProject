package com.mobileproject.mobileprojectbackend.moment;

import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class MomentService {
    private final MomentRepository momentRepository;

    public MomentService(MomentRepository momentRepository) {
        this.momentRepository = momentRepository;
    }

    public Moment saveMoment(String coupleId, String title, String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            throw new IllegalArgumentException("Image data is required");
        }

        String cleanBase64 = base64Image;
        String contentType = "image/jpeg";
        if (base64Image.contains(",")) {
            String[] parts = base64Image.split(",");
            String header = parts[0];
            cleanBase64 = parts[1];
            if (header.contains("image/png")) contentType = "image/png";
            else if (header.contains("image/webp")) contentType = "image/webp";
            else if (header.contains("image/gif")) contentType = "image/gif";
        }

        Base64.getDecoder().decode(cleanBase64);
        String imageDataUri = "data:" + contentType + ";base64," + cleanBase64;
        Moment moment = new Moment(coupleId, title == null ? "" : title, imageDataUri);
        return momentRepository.save(moment);
    }
    
    public List<Moment> getMoments(String coupleId) {
        return momentRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId);
    }
}
