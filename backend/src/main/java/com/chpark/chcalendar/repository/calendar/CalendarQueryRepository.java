package com.chpark.chcalendar.repository.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.calendar.CalendarMemberDto;
import com.chpark.chcalendar.entity.QUserEntity;
import com.chpark.chcalendar.entity.calendar.*;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


import java.util.List;

@RequiredArgsConstructor
@Repository
public class CalendarQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CalendarDto.Response> findGroupCalendarsByUserId(Long userId) {
        QCalendarMemberEntity calendarMemberEntity = QCalendarMemberEntity.calendarMemberEntity;
        QCalendarEntity calendarEntity = QCalendarEntity.calendarEntity;
        QCalendarSettingEntity calendarSettingEntity = QCalendarSettingEntity.calendarSettingEntity;

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
        QCalendarEntity calendarEntity = QCalendarEntity.calendarEntity;
        QCalendarSettingEntity calendarSettingEntity = QCalendarSettingEntity.calendarSettingEntity;

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
        QUserEntity userEntity = QUserEntity.userEntity;
        QCalendarMemberEntity calendarMemberEntity = QCalendarMemberEntity.calendarMemberEntity;

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
}
