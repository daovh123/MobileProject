package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceImportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PlaceStartupImporter implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PlaceStartupImporter.class);

    private final PlaceRepository placeRepository;
    private final PlaceImportService placeImportService;

    @Value("${app.place.import.auto-startup:false}")
    private boolean autoStartup;

    @Value("${app.place.import.skip-when-data-exists:true}")
    private boolean skipWhenDataExists;

    @Value("${app.place.import.file-path:D:/data.json}")
    private String filePath;

    public PlaceStartupImporter(PlaceRepository placeRepository, PlaceImportService placeImportService) {
        this.placeRepository = placeRepository;
        this.placeImportService = placeImportService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!autoStartup) {
            return;
        }

        long existingCount = placeRepository.count();
        if (skipWhenDataExists && existingCount > 0) {
            logger.info("Skip startup import because places collection already has {} documents", existingCount);
            return;
        }

        try {
            PlaceImportResponse response = placeImportService.importFromFile(filePath, false);
            logger.info("Startup import completed: imported={}, totalInDatabase={}",
                    response.importedCount(), response.totalInDatabase());
        } catch (ResponseStatusException responseStatusException) {
            logger.warn("Startup import skipped: {}", responseStatusException.getReason());
        } catch (Exception exception) {
            logger.error("Unexpected startup import failure", exception);
        }
    }
}