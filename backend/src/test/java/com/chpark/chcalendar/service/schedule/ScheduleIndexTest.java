package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.DotenvInitializer;
import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Disabled
@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
public class ScheduleIndexTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleNotificationRepository scheduleNotificationRepository;

    @Autowired
    private ScheduleService scheduleService;

    @Test
    void create() {
        당일_일정_생성(1000);
        //랜덤_일정_생성(1000);
    }

    public void 랜덤_일정_생성(int count) {
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

    public void 당일_일정_생성(int count) {
        List<ScheduleEntity> schedules = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            String title = getRandomTitle(rand.nextInt(5) + 4); // 4~8글자
            LocalDateTime start = LocalDateTime.now().plusMinutes(2);
            LocalDateTime end = start.plusMinutes(1);
            long userId = 5094;
            long calendarId = 3264;

            ScheduleDto s = new ScheduleDto();
            s.setTitle(title);
            s.setStartAt(start);
            s.setEndAt(end);
            s.setUserId(userId);
            s.setCalendarId(calendarId);

            Set<ScheduleNotificationDto> sn = new HashSet<>();
            sn.add(new ScheduleNotificationDto(LocalDateTime.now().plusMinutes(2)));

            ScheduleDto.Request request = new ScheduleDto.Request();
            request.setScheduleDto(s);
            request.setNotificationDto(sn);

            scheduleService.createByForm(request, 5094);
        }
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
