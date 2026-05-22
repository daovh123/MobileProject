package com.mobileproject.mobileprojectbackend.notifications;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpecialDayReminderScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecialDayReminderScheduler.class);
    private static final ZoneId REMINDER_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final CoupleInfoRepository coupleInfoRepository;
    private final NotificationService notificationService;

    public SpecialDayReminderScheduler(
            CoupleInfoRepository coupleInfoRepository,
            NotificationService notificationService) {
        this.coupleInfoRepository = coupleInfoRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void notifyUpcomingSpecialDays() {
        LocalDate today = LocalDate.now(REMINDER_ZONE);
        List<CoupleInfo> couples = coupleInfoRepository.findAll();
        for (CoupleInfo couple : couples) {
            String user1 = trimOrNull(couple.getIdUser1());
            String user2 = trimOrNull(couple.getIdUser2());
            if (user1 == null || user2 == null) {
                continue;
            }

            LocalDate startDate = parseStartDate(couple.getStartAt());
            List<SpecialDayEvent> events = buildUpcomingSpecialDays(startDate, today, 24);
            for (SpecialDayEvent event : events) {
                long daysUntil = ChronoUnit.DAYS.between(today, event.date());
                if (daysUntil != 7L) {
                    continue;
                }

                String title = "Upcoming special day: " + event.title();
                String body = event.title() + " is in 7 days (" + formatDate(event.date()) + ").";
                notificationService.createAndPushIfAbsentToday(
                        user1,
                        NotificationType.SPECIAL_DAY,
                        title,
                        body,
                        today,
                        REMINDER_ZONE
                );
                notificationService.createAndPushIfAbsentToday(
                        user2,
                        NotificationType.SPECIAL_DAY,
                        title,
                        body,
                        today,
                        REMINDER_ZONE
                );
            }
        }
        LOGGER.debug("Special day reminder job finished for {} couples", couples.size());
    }

    private List<SpecialDayEvent> buildUpcomingSpecialDays(
            LocalDate relationshipStartDate,
            LocalDate today,
            int limit) {
        List<SpecialDayEvent> events = new ArrayList<>();
        int currentYear = today.getYear();

        addHoliday(events, LocalDate.of(currentYear, 3, 8), "International Women's Day");
        addHoliday(events, LocalDate.of(currentYear, 11, 19), "International Men's Day");
        addHoliday(events, LocalDate.of(currentYear, 12, 25), "Christmas");
        addHoliday(events, LocalDate.of(currentYear + 1, 3, 8), "International Women's Day");
        addHoliday(events, LocalDate.of(currentYear + 1, 11, 19), "International Men's Day");
        addHoliday(events, LocalDate.of(currentYear + 1, 12, 25), "Christmas");

        if (relationshipStartDate != null) {
            events.add(new SpecialDayEvent(relationshipStartDate.plusDays(100), "100 days together"));
            events.add(new SpecialDayEvent(relationshipStartDate.plusDays(200), "200 days together"));
            events.add(new SpecialDayEvent(relationshipStartDate.plusDays(300), "300 days together"));
            events.add(new SpecialDayEvent(relationshipStartDate.plusYears(1), "1 year anniversary"));
            events.add(new SpecialDayEvent(relationshipStartDate.plusYears(2), "2 year anniversary"));
        }

        return events.stream()
                .filter(event -> !event.date().isBefore(today))
                .distinct()
                .sorted(Comparator.comparing(SpecialDayEvent::date))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void addHoliday(List<SpecialDayEvent> events, LocalDate date, String title) {
        events.add(new SpecialDayEvent(date, title));
    }

    private LocalDate parseStartDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String text = raw.trim();
        try {
            return Instant.parse(text).atZone(REMINDER_ZONE).toLocalDate();
        } catch (Exception ignore) {
            try {
                return LocalDate.parse(text.length() >= 10 ? text.substring(0, 10) : text);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatDate(LocalDate date) {
        return String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private record SpecialDayEvent(LocalDate date, String title) {
    }
}
