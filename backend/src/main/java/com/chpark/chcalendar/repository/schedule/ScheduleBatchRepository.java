package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.schedule.*;
import com.chpark.chcalendar.utility.ScheduleUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ScheduleBatchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ScheduleNotificationRepository scheduleNotificationRepository;
    private final ScheduleGroupRepository scheduleGroupRepository;

    @Transactional
    public ScheduleBatchEntity saveRepeatAll(ScheduleEntity schedule, ScheduleRepeatEntity repeat) {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            // 각 단계별로 메서드 분리
            Map<Long, ScheduleEntity> generatedSchedules = insertSchedules(connection, schedule, repeat);
            List<ScheduleNotificationEntity> generatedNotifications = insertNotifications(connection, schedule, repeat, generatedSchedules);
            insertGroups(connection, schedule, generatedSchedules);

            connection.commit();
            return new ScheduleBatchEntity(generatedSchedules, generatedNotifications);

        } catch (Exception e) {
            handleError(connection, e);
            throw new RuntimeException("Batch insert failed", e);
        } finally {
            closeConnection(connection);
        }
    }

    private Connection getConnection() throws SQLException {
        return Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
    }

    private Map<Long, ScheduleEntity> insertSchedules(Connection connection, ScheduleEntity schedule, ScheduleRepeatEntity repeat) throws SQLException {
        String scheduleSql = "INSERT INTO schedule (title, description, start_at, end_at, repeat_id, user_id, calendar_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        int repeatCount = ScheduleUtility.calculateRepeatCount(
                schedule.getStartAt(), repeat.getEndAt(), repeat.getRepeatInterval(), repeat.getRepeatType());

        Map<Long, ScheduleEntity> generatedSchedules = new LinkedHashMap<>();

        try (PreparedStatement ps = connection.prepareStatement(scheduleSql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 1; i <= repeatCount; i++) {
                setScheduleParameters(ps, schedule, repeat, i);
                ps.addBatch();
            }
            ps.executeBatch();

            collectGeneratedSchedules(ps, schedule, repeat, generatedSchedules);
        }

        return generatedSchedules;
    }

    private void setScheduleParameters(PreparedStatement ps, ScheduleEntity schedule, ScheduleRepeatEntity repeat, int index) throws SQLException {
        ps.setString(1, schedule.getTitle());
        ps.setString(2, schedule.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(ScheduleUtility.calculateRepeatPlusDate(
                schedule.getStartAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * index)));
        ps.setTimestamp(4, Timestamp.valueOf(ScheduleUtility.calculateRepeatPlusDate(
                schedule.getEndAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * index)));
        ps.setLong(5, repeat.getId());
        ps.setLong(6, schedule.getUserId());
        ps.setLong(7, schedule.getCalendarId());
    }

    private void collectGeneratedSchedules(PreparedStatement ps, ScheduleEntity schedule, ScheduleRepeatEntity repeat,
                                           Map<Long, ScheduleEntity> generatedSchedules) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            int index = 0;
            while (rs.next()) {
                long scheduleId = rs.getLong(1);
                ScheduleEntity repeatedSchedule = buildRepeatedSchedule(schedule, repeat, scheduleId, index + 1);
                generatedSchedules.put(scheduleId, repeatedSchedule);
                index++;
            }
        }
    }

    private ScheduleEntity buildRepeatedSchedule(ScheduleEntity schedule, ScheduleRepeatEntity repeat, long scheduleId, int repeatNumber) {
        return ScheduleEntity.builder()
                .id(scheduleId)
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startAt(ScheduleUtility.calculateRepeatPlusDate(
                        schedule.getStartAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * repeatNumber))
                .endAt(ScheduleUtility.calculateRepeatPlusDate(
                        schedule.getEndAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * repeatNumber))
                .repeatId(repeat.getId())
                .userId(schedule.getUserId())
                .calendarId(schedule.getCalendarId())
                .build();
    }

    private List<ScheduleNotificationEntity> insertNotifications(Connection connection, ScheduleEntity schedule,
                                                                 ScheduleRepeatEntity repeat, Map<Long, ScheduleEntity> generatedSchedules) throws SQLException {
        String notificationSql = "INSERT INTO schedule_notification (notification_at, schedule_id) VALUES (?, ?)";
        List<ScheduleNotificationEntity> notificationEntities = scheduleNotificationRepository.findByScheduleId(schedule.getId());

        if (notificationEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScheduleNotificationEntity> generatedNotifications = new ArrayList<>();
        int scheduleIndex = 0;

        try (PreparedStatement ps = connection.prepareStatement(notificationSql, Statement.RETURN_GENERATED_KEYS)) {
            for (Long scheduleId : generatedSchedules.keySet()) {
                for (ScheduleNotificationEntity notificationEntity : notificationEntities) {
                    setNotificationParameters(ps, notificationEntity, repeat, scheduleIndex + 1, scheduleId);
                    ps.addBatch();
                }
                scheduleIndex++;
            }
            ps.executeBatch();

            collectGeneratedNotifications(ps, notificationEntities, generatedSchedules, generatedNotifications, repeat);
        }

        return generatedNotifications;
    }

    private void setNotificationParameters(PreparedStatement ps, ScheduleNotificationEntity notificationEntity,
                                           ScheduleRepeatEntity repeat, int repeatNumber, long scheduleId) throws SQLException {
        LocalDateTime repeatedTime = ScheduleUtility.calculateRepeatPlusDate(
                notificationEntity.getNotificationAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * repeatNumber);
        ps.setTimestamp(1, Timestamp.valueOf(repeatedTime));
        ps.setLong(2, scheduleId);
    }

    private void collectGeneratedNotifications(PreparedStatement ps, List<ScheduleNotificationEntity> notificationEntities,
                                               Map<Long, ScheduleEntity> generatedSchedules, List<ScheduleNotificationEntity> generatedNotifications, 
                                               ScheduleRepeatEntity repeat) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            int currentIndex = 0;
            while (rs.next()) {
                long notificationId = rs.getLong(1);
                int repeatOrder = currentIndex / notificationEntities.size();
                int notificationIndex = currentIndex % notificationEntities.size();

                Long scheduleId = generatedSchedules.keySet().toArray(new Long[0])[repeatOrder];
                ScheduleNotificationEntity repeatedNotification = buildRepeatedNotification(
                        notificationId, notificationEntities.get(notificationIndex), repeat, repeatOrder + 1, scheduleId);

                generatedNotifications.add(repeatedNotification);
                currentIndex++;
            }
        }
    }

    private ScheduleNotificationEntity buildRepeatedNotification(long notificationId, ScheduleNotificationEntity original,
                                                                 ScheduleRepeatEntity repeat, int repeatNumber, long scheduleId) {
        LocalDateTime repeatedTime = ScheduleUtility.calculateRepeatPlusDate(
                original.getNotificationAt(), repeat.getRepeatType(), repeat.getRepeatInterval() * repeatNumber);

        return ScheduleNotificationEntity.builder()
                .id(notificationId)
                .notificationAt(repeatedTime)
                .scheduleId(scheduleId)
                .build();
    }

    private void insertGroups(Connection connection, ScheduleEntity schedule, Map<Long, ScheduleEntity> generatedSchedules) throws SQLException {
        String groupSql = "INSERT INTO schedule_group (authority, status, schedule_id, user_id, user_nickname) VALUES (?, ?, ?, ?, ?)";
        List<ScheduleGroupEntity> groupEntities = scheduleGroupRepository.findByScheduleId(schedule.getId());

        if (groupEntities.isEmpty()) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(groupSql)) {
            for (Long scheduleId : generatedSchedules.keySet()) {
                for (ScheduleGroupEntity group : groupEntities) {
                    setGroupParameters(ps, group, scheduleId);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void setGroupParameters(PreparedStatement ps, ScheduleGroupEntity group, long scheduleId) throws SQLException {
        ps.setString(1, group.getAuthority().name());
        ps.setString(2, "PENDING");
        ps.setLong(3, scheduleId);
        ps.setLong(4, group.getUserId());
        ps.setString(5, group.getUserNickname());
    }

    private void handleError(Connection connection, Exception e) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("Rollback failed", rollbackEx);
            }
        }
        log.error("Batch insert failed", e);
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException closeEx) {
                log.error("Failed to close connection", closeEx);
            }
        }
    }
}