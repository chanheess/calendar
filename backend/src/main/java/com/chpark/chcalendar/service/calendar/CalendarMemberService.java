package com.chpark.chcalendar.service.calendar;

import com.chpark.chcalendar.dto.calendar.CalendarMemberDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.entity.calendar.CalendarEntity;
import com.chpark.chcalendar.entity.calendar.CalendarMemberEntity;
import com.chpark.chcalendar.entity.calendar.CalendarSettingEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.CalendarMemberRole;
import com.chpark.chcalendar.exception.authorization.GroupAuthorizationException;
import com.chpark.chcalendar.repository.calendar.CalendarMemberRepository;
import com.chpark.chcalendar.repository.calendar.CalendarQueryRepository;
import com.chpark.chcalendar.repository.calendar.CalendarRepository;
import com.chpark.chcalendar.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CalendarMemberService {

    private final CalendarRepository calendarRepository;
    private final CalendarMemberRepository calendarMemberRepository;
    private final CalendarQueryRepository calendarQueryRepository;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(CalendarMemberService.class);

    public CalendarMemberDto create(CalendarEntity calendarEntity, long userId, CalendarMemberRole calendarMemberRole) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("등록된 유저가 없습니다.")
        );

        CalendarMemberEntity calendarMemberEntity = new CalendarMemberEntity(calendarEntity, userEntity, calendarMemberRole);
        calendarMemberRepository.save(calendarMemberEntity);

        return CalendarMemberDto.builder()
                .calendarId(calendarMemberEntity.getCalendar().getId())
                .userId(calendarMemberEntity.getUser().getId())
                .role(calendarMemberEntity.getRole())
                .userNickname(calendarMemberEntity.getUser().getNickname())
                .build();
    }

    public List<CalendarMemberDto> findCalendarMemberList(long userId, long calendarId) {
        getCalendarMember(userId, calendarId);
        return calendarQueryRepository.findByCalendarId(calendarId);
    }

    public List<Long> findCalendarIdList(long userId) {
        return calendarMemberRepository.findCalendarIdByUserId(userId);
    }

    public CalendarMemberEntity getCalendarMember(long userId, long calendarId) {
        return calendarMemberRepository.findByUserIdAndCalendarId(userId, calendarId).orElseThrow(
            () -> new GroupAuthorizationException("캘린더에 대한 권한이 없습니다.")
        );
    }

    public CalendarMemberEntity checkCalendarMemberAuthority(long userId, long calendarId, CalendarMemberRole role) {
        CalendarMemberEntity result = this.getCalendarMember(userId, calendarId);

        if (result.getRole().compareTo(role) >= 0) {
            throw new GroupAuthorizationException("캘린더에 대한 권한이 없습니다.");
        }

        return result;
    }

    public void checkCalendarMemberExists(long userId, long calendarId) {
        if (calendarMemberRepository.findByUserIdAndCalendarId(userId, calendarId).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 사용자입니다.");
        }
    }

    @Transactional
    public void addUser(long userId, long calendarId) {
        checkCalendarMemberExists(userId, calendarId);

        CalendarEntity calendar = calendarRepository.findByIdAndCategory(calendarId, CalendarCategory.GROUP).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 그룹입니다.")
        );

        //기존 그룹 캘린더에 캘린더 설정 추가
        CalendarSettingEntity newCalendar = new CalendarSettingEntity(userId);
        calendar.addCalendarSetting(newCalendar);

        create(calendar, userId, CalendarMemberRole.USER);
    }

    public List<Long> getUserList(long calendarId) {
        return calendarMemberRepository.findUserIdByCalendarId(calendarId);
    }

    @Transactional
    public void OwnershipTransfer(long userId, long calendarId, CalendarMemberEntity calendarMemberEntity) {
        List<CalendarMemberEntity> memberList = calendarMemberRepository.findByNextOwner(calendarId, userId);

        if (memberList.isEmpty() || !isCalendarOwner(calendarMemberEntity)) {
            return;
        }

        for (CalendarMemberEntity member : memberList) {
            UserEntity user = member.getUser();
            CalendarEntity calendar = member.getCalendar();

            if (user != null) {
                member.setRole(calendarMemberEntity.getRole());
                calendar.setUserId(user.getId());
                break;
            }
        }
    }

    public boolean isCalendarOwner(CalendarMemberEntity calendarMemberEntity) {
        if (calendarMemberEntity == null) {
            return false;
        }

        return CalendarMemberRole.ADMIN.equals(calendarMemberEntity.getRole());
    }

    @Transactional
    public long getMemberCount(long calendarId) {
        return calendarMemberRepository.countByCalendarId(calendarId);
    }

    @Transactional
    public CalendarMemberDto findMyRole(long userId, long calendarId) {
        CalendarMemberEntity calendarMember = getCalendarMember(userId, calendarId);
        
        return CalendarMemberDto.builder()
                .calendarId(calendarMember.getCalendar().getId())
                .userId(calendarMember.getUser().getId())
                .role(calendarMember.getRole())
                .userNickname(calendarMember.getUser().getNickname())
                .build();
    }

    @Transactional
    public void deleteMember(CalendarMemberEntity calendarMemberEntity) {
        calendarMemberRepository.delete(calendarMemberEntity);
    }

    @Transactional
    public void removeGroupMembership(long userId, long calendarId) {
        CalendarMemberEntity calendarMemberEntity = getCalendarMember(userId, calendarId);

        if (isCalendarOwner(calendarMemberEntity)) {
            if (getMemberCount(calendarId) > 1) {
                OwnershipTransfer(userId, calendarId, calendarMemberEntity);
            }
        }

        deleteMember(calendarMemberEntity);
    }

}
