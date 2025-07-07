package com.chpark.chcalendar.repository.schedule;

import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.chpark.chcalendar.entity.schedule.QScheduleEntity.scheduleEntity;
import static com.chpark.chcalendar.entity.schedule.QScheduleGroupEntity.scheduleGroupEntity;

@RequiredArgsConstructor
@Repository
public class ScheduleQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ScheduleEntity> findSchedulesByCalendarIdAndUser(
            Long userId,
            List<Long> calendarIds,
            LocalDateTime start,
            LocalDateTime end,
            LocalDateTime cursorStartAt,
            Long cursorId,
            Pageable pageable) {

        BooleanBuilder where = new BooleanBuilder();

        where.andAnyOf(
                scheduleGroupEntity.isNull(),
                scheduleEntity.userId.eq(userId),
                scheduleGroupEntity.userId.eq(userId)
        );
        where.and(scheduleEntity.calendarId.in(calendarIds));
        // s.startAt <= :end
        where.and(scheduleEntity.startAt.loe(end));
        // s.endAt >= :start
        where.and(scheduleEntity.endAt.goe(start));

        // (s.startAt > :cursorStartAt OR (s.startAt = :cursorStartAt AND s.id > :cursorId))
        where.andAnyOf(
                scheduleEntity.startAt.gt(cursorStartAt),
                scheduleEntity.startAt.eq(cursorStartAt).and(scheduleEntity.id.gt(cursorId))
        );

        // 쿼리 생성
        JPQLQuery<ScheduleEntity> query = queryFactory
                .selectDistinct(scheduleEntity)
                .from(scheduleEntity)
                .leftJoin(scheduleGroupEntity).on(scheduleGroupEntity.scheduleId.eq(scheduleEntity.id))
                .where(where)
                .orderBy(scheduleEntity.startAt.asc(), scheduleEntity.id.asc());

        // 페이징 처리
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        return query.fetch();
    }

    public List<ScheduleEntity> findSchedulesByCalendarIdAndUser(
            Long userId,
            List<CalendarEntity> calendarEntityList,
            LocalDateTime cursorStartAt,
            Long cursorId,
            Pageable pageable) {

        List<Long> calendarIds = calendarEntityList.stream()
                .map(CalendarEntity::getId)
                .toList();

        BooleanBuilder where = new BooleanBuilder();

        where.andAnyOf(
                scheduleGroupEntity.isNull(),
                scheduleEntity.userId.eq(userId),
                scheduleGroupEntity.userId.eq(userId)
        );
        where.and(scheduleEntity.calendarId.in(calendarIds));

        // 커서가 null이 아닐 때만 커서 조건 추가
        if (cursorStartAt != null && cursorId != null) {
            where.andAnyOf(
                    scheduleEntity.startAt.gt(cursorStartAt),
                    scheduleEntity.startAt.eq(cursorStartAt).and(scheduleEntity.id.gt(cursorId))
            );
        }

        JPQLQuery<ScheduleEntity> query = queryFactory
                .selectDistinct(scheduleEntity)
                .from(scheduleEntity)
                .leftJoin(scheduleGroupEntity).on(scheduleGroupEntity.scheduleId.eq(scheduleEntity.id))
                .where(where)
                .orderBy(scheduleEntity.startAt.asc(), scheduleEntity.id.asc());

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        return query.fetch();
    }
}
