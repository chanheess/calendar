package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleNotificationEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleRepeatEntity;
import com.chpark.chcalendar.utility.ScheduleUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ScheduleBatchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ScheduleNotificationRepository scheduleNotificationRepository;

    @Transactional
    public void saveRepeatAll(ScheduleEntity schedule, ScheduleRepeatEntity repeat) {
        String scheduleSql = "INSERT INTO schedule (title, description, start_at, end_at, repeat_id, user_id, calendar_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String notificationSql = "INSERT INTO schedule_notification (notification_at, schedule_id) VALUES (?, ?)";

        // try-with-resources로 Connection을 자동 닫히게 함
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            connection.setAutoCommit(false); // 수동 커밋 모드

            List<Integer> generatedKeys = new ArrayList<>();

            // 반복 횟수 계산
            int repeatCount = ScheduleUtility.calculateRepeatCount(
                    schedule.getStartAt(),
                    repeat.getEndAt(),
                    repeat.getRepeatInterval(),
                    repeat.getRepeatType()
            );

            // 일정 배치 삽입
            try (PreparedStatement psSchedule = connection.prepareStatement(scheduleSql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 1; i <= repeatCount; i++) {
                    psSchedule.setString(1, schedule.getTitle());
                    psSchedule.setString(2, schedule.getDescription());
                    psSchedule.setTimestamp(3, Timestamp.valueOf(ScheduleUtility.calculateRepeatPlusDate(
                            schedule.getStartAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * i)));
                    psSchedule.setTimestamp(4, Timestamp.valueOf(ScheduleUtility.calculateRepeatPlusDate(
                            schedule.getEndAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * i)));
                    psSchedule.setLong(5, repeat.getId());
                    psSchedule.setLong(6, schedule.getUserId());
                    psSchedule.setLong(7, schedule.getCalendarId());
                    psSchedule.addBatch();
                }
                psSchedule.executeBatch();

                // 생성된 일정들의 키를 가져오기
                try (ResultSet rs = psSchedule.getGeneratedKeys()) {
                    while (rs.next()) {
                        generatedKeys.add(rs.getInt(1));
                    }
                }
            }

            // 알림 배치 삽입 (알림 데이터가 있는 경우)
            List<ScheduleNotificationEntity> notificationEntities = scheduleNotificationRepository.findByScheduleId(schedule.getId());
            if (!notificationEntities.isEmpty()) {
                try (PreparedStatement psNotification = connection.prepareStatement(notificationSql)) {
                    for (int i = 0; i < repeatCount; i++) {
                        for (ScheduleNotificationEntity notificationEntity : notificationEntities) {
                            psNotification.setTimestamp(1, Timestamp.valueOf(ScheduleUtility.calculateRepeatPlusDate(notificationEntity.getNotificationAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * (i + 1))));
                            psNotification.setInt(2, generatedKeys.get(i));
                            psNotification.addBatch();
                        }
                    }
                    psNotification.executeBatch();
                }
            }

            // 모든 작업 성공 시 commit
            connection.commit();

        } catch (SQLException e) {
            log.error("Batch insert failed due to SQLException", e);
            throw new RuntimeException("Batch insert failed", e);
        } catch (Exception e) {
            log.error("Batch insert failed due to an unexpected error", e);
            throw new RuntimeException("Batch insert failed", e);
        }
    }
}