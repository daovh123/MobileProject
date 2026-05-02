package com.mobileproject.mobileprojectbackend.moment;

import com.mobileproject.mobileprojectbackend.storage.FirebaseStorageService;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class MomentService {
    private final MomentRepository momentRepository;
    private final FirebaseStorageService firebaseStorageService;

    public MomentService(MomentRepository momentRepository, FirebaseStorageService firebaseStorageService) {
        this.momentRepository = momentRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    public Moment saveMoment(String coupleId, String title, String base64Image) {
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
        
        byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
        String imageUrl = firebaseStorageService.uploadMomentImage(coupleId, imageBytes, contentType);
        
        Moment moment = new Moment(coupleId, title, imageUrl);
        return momentRepository.save(moment);
    }
    
    public List<Moment> getMoments(String coupleId) {
        return momentRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId);
    }
}
