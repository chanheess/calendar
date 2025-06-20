package com.chpark.chcalendar.repository.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarMemberDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chpark.chcalendar.entity.QUserEntity.userEntity;
import static com.chpark.chcalendar.entity.calendar.QCalendarEntity.calendarEntity;
import static com.chpark.chcalendar.entity.calendar.QCalendarMemberEntity.calendarMemberEntity;
import static com.chpark.chcalendar.entity.calendar.QCalendarProviderEntity.calendarProviderEntity;
import static com.chpark.chcalendar.entity.calendar.QCalendarSettingEntity.calendarSettingEntity;

@RequiredArgsConstructor
@Repository
public class CalendarQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CalendarDto.Response> findGroupCalendarsByUserId(Long userId) {
        return queryFactory
                .select(Projections.constructor(
                        CalendarDto.Response.class,
                        calendarEntity.title,
                        calendarEntity.id,
                        calendarSettingEntity.color,
                        calendarEntity.category,
                        calendarSettingEntity.checked
                        ))
                .from(calendarMemberEntity)
                .join(calendarMemberEntity.calendar, calendarEntity)
                .leftJoin(calendarSettingEntity)
                .on(
                        calendarSettingEntity.calendar.id.eq(calendarEntity.id)
                                .and(calendarSettingEntity.userId.eq(userId))
                )
                .where(calendarMemberEntity.user.id.eq(userId))
                .fetch();
    }

    public List<CalendarDto.Response> findCalendarsByUserId(Long userId) {
        return queryFactory
                .select(Projections.constructor(
                        CalendarDto.Response.class,
                        calendarEntity.title,
                        calendarEntity.id,
                        calendarSettingEntity.color,
                        calendarEntity.category,
                        calendarSettingEntity.checked
                ))
                .from(calendarEntity)
                .leftJoin(calendarSettingEntity)
                .on(
                        calendarSettingEntity.calendar.id.eq(calendarEntity.id)
                                .and(calendarSettingEntity.userId.eq(userId))
                )
                .where(calendarEntity.userId.eq(userId))
                .fetch();
    }

    public List<CalendarMemberDto> findByCalendarId(Long calendarId) {
        return queryFactory
                .select(Projections.constructor(
                        CalendarMemberDto.class,
                        calendarMemberEntity.calendar.id,
                        calendarMemberEntity.user.id,
                        calendarMemberEntity.role,
                        userEntity.nickname
                ))
                .from(calendarMemberEntity)
                .join(calendarMemberEntity.user, userEntity)
                .where(calendarMemberEntity.calendar.id.eq(calendarId))
                .fetch();
    }

    public Map<String, CalendarEntity> findExternalCalendar(Long userId, CalendarCategory category) {
        List<Tuple> results = queryFactory
                .select(calendarProviderEntity.providerId, calendarEntity)
                .from(calendarProviderEntity)
                .join(calendarProviderEntity.calendar, calendarEntity)
                .where(
                        calendarEntity.userId.eq(userId),
                        calendarEntity.category.eq(category)
                )
                .fetch();

        // externalId를 key로, CalendarEntity를 value로 map에 넣기
        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(calendarProviderEntity.providerId),
                        tuple -> tuple.get(calendarEntity)
                ));
    }

    public List<CalendarDto.Response> findExternalCalendarsByUserId(Long userId, CalendarCategory category) {
        return queryFactory
                .select(Projections.constructor(
                        CalendarDto.Response.class,
                        calendarEntity.title,
                        calendarEntity.id,
                        calendarSettingEntity.color,
                        calendarEntity.category,
                        calendarSettingEntity.checked
                ))
                .from(calendarEntity)
                .leftJoin(calendarSettingEntity)
                .on(
                        calendarSettingEntity.calendar.id.eq(calendarEntity.id)
                                .and(calendarSettingEntity.userId.eq(userId))
                )
                .where(
                        calendarEntity.userId.eq(userId)
                                .and(calendarEntity.category.eq(category))
                )
                .fetch();
    }
}
