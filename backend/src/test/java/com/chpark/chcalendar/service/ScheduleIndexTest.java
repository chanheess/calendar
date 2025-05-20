package com.chpark.chcalendar.service;

import com.chpark.chcalendar.DotenvInitializer;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Disabled
@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
public class ScheduleIndexTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void create() {
        generateSchedules(1000);
    }

    public void generateSchedules(int count) {
        List<ScheduleEntity> schedules = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            String title = getRandomTitle(rand.nextInt(5) + 4); // 4~8글자
            LocalDateTime start = getRandomDateTimeInCurrentMonth();
            LocalDateTime end = start.plusMinutes(rand.nextInt(120) + 1); // 1~120분 후 종료
            long userId = 1; // userId 1~100
            long calendarId = 12; // calendarId 1~10

            ScheduleEntity s = new ScheduleEntity();
            s.setTitle(title);
            s.setStartAt(start);
            s.setEndAt(end);
            s.setUserId(userId);
            s.setCalendarId(calendarId);
            schedules.add(s);
        }
        scheduleRepository.saveAll(schedules);
    }

    public static String getRandomTitle(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static LocalDateTime getRandomDateTimeInCurrentMonth() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.of(now.getYear(), now.getMonth());
        int minDay = 1;
        int maxDay = currentMonth.lengthOfMonth();

        int day = new Random().nextInt(maxDay - minDay + 1) + minDay;
        int hour = new Random().nextInt(24);
        int minute = new Random().nextInt(60);

        return LocalDateTime.of(now.getYear(), now.getMonth(), day, hour, minute);
    }


}
