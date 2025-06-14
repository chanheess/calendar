package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarDto;
import com.chpark.chcalendar.dto.group.GroupUserDto;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.GroupAuthority;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.repository.CalendarRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.user.GroupUserService;
import com.chpark.chcalendar.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupCalendarService implements CalendarService {

    private final JwtTokenProvider jwtTokenProvider;
    private final CalendarRepository calendarRepository;
    private final GroupUserService groupUserService;
    private final UserService userService;

    @Override
    public CalendarDto.Response create(long userId, String title) {
        int maxAdminCount = 10;

        if (maxAdminCount <= calendarRepository.countAdminGroups(userId)) {
            throw new IllegalArgumentException("You have reached the maximum limit for creating groups.");
        }

        CalendarEntity groupEntity = new CalendarEntity(title, userId, CalendarCategory.GROUP);

        calendarRepository.save(groupEntity);

        String nickname = userService.findNickname(userId);

        groupUserService.create(new GroupUserDto(
                groupEntity.getTitle(),
                groupEntity.getId(),
                nickname,
                groupEntity.getUserId(),
                GroupAuthority.ADMIN,
                groupEntity.getCalendarSetting().getColor())
        );

        return new CalendarDto.Response(groupEntity);
    }

    @Override
    public List<CalendarDto.Response> findCalendarList(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        return groupUserService.findMyGroup(userId);
    }

    @Override
    public CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto) {
        GroupUserEntity groupUser = groupUserService.updateGroupColor(userId, calendarColorDto.getCalendarId(), calendarColorDto.getColor());
        return new CalendarColorDto(groupUser.getGroupId(), groupUser.getColor(), CalendarCategory.GROUP);
    }

    public List<CalendarDto.Response> findGroup(String title) {
        List<CalendarEntity> groupEntity = calendarRepository.findByTitleAndCategory(title, CalendarCategory.GROUP);

        return CalendarDto.Response.fromCalendarEntityList(groupEntity);
    }
}
