package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarColorDto;
import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.dto.group.GroupUserDto;
import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.GroupAuthority;
import com.chpark.chcalendar.repository.CalendarInfoRepository;
import com.chpark.chcalendar.service.user.GroupUserService;
import com.chpark.chcalendar.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupCalendarService implements CalendarService {

    private final CalendarInfoRepository calendarInfoRepository;
    private final GroupUserService groupUserService;
    private final UserService userService;

    @Override
    public CalendarInfoDto.Response create(long userId, String title) {
        int maxAdminCount = 10;

        if (maxAdminCount <= calendarInfoRepository.countAdminGroups(userId)) {
            throw new IllegalArgumentException("You have reached the maximum limit for creating groups.");
        }

        CalendarInfoEntity groupEntity = new CalendarInfoEntity(title, userId, CalendarCategory.GROUP);

        calendarInfoRepository.save(groupEntity);

        String nickname = userService.findNickname(userId);

        groupUserService.create(new GroupUserDto(
                groupEntity.getTitle(),
                groupEntity.getId(),
                nickname,
                groupEntity.getAdminId(),
                GroupAuthority.ADMIN,
                groupEntity.getColor())
        );

        return new CalendarInfoDto.Response(groupEntity);
    }

    @Override
    public List<CalendarInfoDto.Response> findCalendarList(long userId) {
        return groupUserService.findMyGroup(userId);
    }

    @Override
    public CalendarColorDto changeColor(long userId, CalendarColorDto calendarColorDto) {
        GroupUserEntity groupUser = groupUserService.updateGroupColor(userId, calendarColorDto.getCalendarId(), calendarColorDto.getColor());
        return new CalendarColorDto(groupUser.getGroupId(), groupUser.getColor());
    }

    public List<CalendarInfoDto.Response> findGroup(String title) {
        List<CalendarInfoEntity> groupEntity = calendarInfoRepository.findByTitleAndCategory(title, CalendarCategory.GROUP);

        return CalendarInfoDto.Response.fromCalendarEntityList(groupEntity);
    }
}
