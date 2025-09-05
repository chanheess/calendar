package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.DotenvInitializer;
import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
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
import java.time.temporal.ChronoUnit;
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
        당일_일정_생성_n간격(1000, 1, 10, ChronoUnit.SECONDS, 0);
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

    public void 당일_일정_생성_n간격(
            int scheduleCountPerBatch, // 한 배치에 몇 개? (예: 1000)
            int testCount,             // 배치를 몇 번? (예: 3)
            int intervalValue,        // 배치 간 울리는 간격 (예: 1, 5 등)
            ChronoUnit intervalType,    // 간격 타입 (예: 시, 분, 초 등)
            int jitterSeconds          // 각 일정에 줄 지터(0~n초, 분산용; 0이면 없음)
    ) {
        Random rand = new Random();

        for (int t = 1; t <= testCount; t++) {
            // 배치 t가 울릴 기준 시각: now + (초기지연 + t * 간격)
            LocalDateTime batchFireTime =
                    LocalDateTime.now().plus((long) t * intervalValue, intervalType);

            for (int i = 0; i < scheduleCountPerBatch; i++) {
                // (선택) 배치 내부에서 약간의 분산을 주고 싶으면 지터 몇 초를 랜덤으로 더함
                LocalDateTime start = jitterSeconds > 0
                        ? batchFireTime.plusSeconds(rand.nextInt(jitterSeconds + 1))
                        : batchFireTime;

                LocalDateTime end = start.plusMinutes(1);

                long userId = 5094L;
                long calendarId = 3264L;

                String title = "LoadTest-" + t + "-" + i;

                ScheduleDto s = new ScheduleDto();
                s.setTitle(title);
                s.setStartAt(start);
                s.setEndAt(end);
                s.setUserId(userId);
                s.setCalendarId(calendarId);

                // 알림 시각 = 일정 시작 시각(또는 원하는 리드타임으로 조절)
                Set<ScheduleNotificationDto> sn = new HashSet<>();
                sn.add(new ScheduleNotificationDto(start));

                ScheduleDto.Request request = new ScheduleDto.Request();
                request.setScheduleDto(s);
                request.setNotificationDto(sn);

                scheduleService.createByForm(request, userId);
            }
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
