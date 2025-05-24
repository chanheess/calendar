package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.dto.calendar.CalendarInfoDto;
import com.chpark.chcalendar.dto.group.GroupUserDto;
import com.chpark.chcalendar.entity.CalendarInfoEntity;
import com.chpark.chcalendar.entity.GroupUserEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.GroupAuthority;
import com.chpark.chcalendar.exception.authentication.GroupAuthenticationException;
import com.chpark.chcalendar.repository.CalendarInfoRepository;
import com.chpark.chcalendar.repository.GroupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupUserService {

    private final GroupUserRepository groupUserRepository;
    private final CalendarInfoRepository calendarInfoRepository;

    public GroupUserDto create(GroupUserDto groupUserDto) {
        GroupUserEntity groupUserEntity = new GroupUserEntity(groupUserDto);
        groupUserRepository.save(groupUserEntity);

        return new GroupUserDto(groupUserEntity);
    }

    public List<CalendarInfoDto.Response> findMyGroup(long userId) {
        List<GroupUserEntity> result = groupUserRepository.findByUserId(userId);

        return CalendarInfoDto.Response.fromGroupUserEntityList(result);
    }

    public List<Long> findMyGroupsId(long userId) {
        return groupUserRepository.findGroupIdByUserId(userId);
    }

    public List<GroupUserDto> findGroupUserList(long userId, long groupId) {
        getGroupUser(userId, groupId);
        List<GroupUserEntity> userEntityList = groupUserRepository.findByGroupId(groupId);

        return GroupUserDto.fromGroupUserEntityList(userEntityList);
    }

    public GroupUserEntity getGroupUser(long userId, long groupId) {
        return groupUserRepository.findByUserIdAndGroupId(userId, groupId).orElseThrow(
            () -> new GroupAuthenticationException("그룹에 대한 권한이 없습니다.")
        );
    }

    public GroupUserEntity checkGroupUserAuthority(long userId, long groupId) {
        GroupUserEntity result = this.getGroupUser(userId, groupId);

        if (result.getRole().compareTo(GroupAuthority.USER) >= 0) {
            throw new GroupAuthenticationException("그룹에 대한 권한이 없습니다.");
        }

        return result;
    }

    public void checkGroupUserExists(long userId, long groupId) {
        if (groupUserRepository.findByUserIdAndGroupId(userId, groupId).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 사용자입니다.");
        }
    }

    @Transactional
    public void addUser(long userId, String nickname, long groupId) {
        CalendarInfoEntity entity = calendarInfoRepository.findByIdAndCategory(groupId, CalendarCategory.GROUP).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 그룹입니다.")
        );

        //그룹 가입 최대 유저 수 제한 필요

        GroupUserDto groupUserDto = new GroupUserDto(
                entity.getTitle(),
                groupId,
                nickname,
                userId,
                GroupAuthority.USER,
                "blue"
        );

        this.create(groupUserDto);
    }

    @Transactional
    public void updateGroupUserNickname(long userId, String nickname) {
        List<GroupUserEntity> result = groupUserRepository.findByUserId(userId);

        result.forEach(groupUserEntity -> {
            groupUserEntity.setUserNickname(nickname);
        });
    }

    @Transactional
    public GroupUserEntity updateGroupColor(long userId, long groupId, String color) {
        GroupUserEntity groupUser = getGroupUser(userId, groupId);
        groupUser.setColor(color);
        return groupUser;
    }

    public List<Long> getUserList(long calendarId) {
        return groupUserRepository.findUserIdByGroupId(calendarId);
    }

}
