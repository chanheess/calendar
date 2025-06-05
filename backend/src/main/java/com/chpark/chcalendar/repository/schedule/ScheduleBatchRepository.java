package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
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
    private final ScheduleGroupRepository scheduleGroupRepository;

    @Transactional
    public void saveRepeatAll(ScheduleEntity schedule, ScheduleRepeatEntity repeat) {
        String scheduleSql = "INSERT INTO schedule (title, description, start_at, end_at, repeat_id, user_id, calendar_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String notificationSql = "INSERT INTO schedule_notification (notification_at, schedule_id) VALUES (?, ?)";
        String groupSql = "INSERT INTO schedule_group (authority, status, schedule_id, user_id, user_nickname) VALUES (?, ?, ?, ?, ?)";

        // Connection을 명시적으로 관리하여 rollback 및 close를 확실히 처리
        Connection connection = null;
        try {
            connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
            connection.setAutoCommit(false); // 수동 커밋 모드

            List<Long> generatedKeys = new ArrayList<>();

            // 반복 횟수 계산
            int repeatCount = ScheduleUtility.calculateRepeatCount(
                    schedule.getStartAt(),
                    repeat.getEndAt(),
                    repeat.getRepeatInterval(),
                    repeat.getRepeatType()
            );

            // 일정 배치 삽입
            try (PreparedStatement psSchedule = connection.prepareStatement(scheduleSql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 1; i <= repeatCount; i++) { //곱셈을 위해 1부터 시작
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
                        generatedKeys.add(rs.getLong(1));
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
                            psNotification.setLong(2, generatedKeys.get(i));
                            psNotification.addBatch();
                        }
                    }
                    psNotification.executeBatch();
                }
            }

            // 기존 일정에 연결된 그룹 정보 조회 및 배치 삽입
            List<ScheduleGroupEntity> groupEntities = scheduleGroupRepository.findByScheduleId(schedule.getId());
            if (!groupEntities.isEmpty()) {
                try (PreparedStatement psGroup = connection.prepareStatement(groupSql)) {
                    for (int i = 0; i < repeatCount; i++) {
                        for (ScheduleGroupEntity group : groupEntities) {
                            psGroup.setString(1, group.getAuthority().name());
                            psGroup.setString(2, "PENDING");
                            psGroup.setLong(3, generatedKeys.get(i));
                            psGroup.setLong(4, group.getUserId());
                            psGroup.setString(5, group.getUserNickname());
                            psGroup.addBatch();
                        }
                    }
                    psGroup.executeBatch();
                }
            }

            // 모든 작업 성공 시 commit
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Rollback failed", rollbackEx);
                }
            }
            log.error("Batch insert failed due to SQLException", e);
            throw new RuntimeException("Batch insert failed", e);
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Rollback failed", rollbackEx);
                }
            }
            log.error("Batch insert failed due to an unexpected error", e);
            throw new RuntimeException("Batch insert failed", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    log.error("Failed to close connection", closeEx);
                }
            }
        }
    }
}