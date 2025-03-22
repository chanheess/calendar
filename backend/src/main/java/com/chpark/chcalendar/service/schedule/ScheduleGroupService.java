package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.notification.NotificationScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleGroupEntity;
import com.chpark.chcalendar.enumClass.FileAuthority;
import com.chpark.chcalendar.enumClass.NotificationCategory;
import com.chpark.chcalendar.exception.ScheduleException;
import com.chpark.chcalendar.repository.schedule.ScheduleGroupRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.notification.NotificationScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ScheduleGroupService {

    private final ScheduleGroupRepository scheduleGroupRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationScheduleService notificationScheduleService;

    @Transactional
    public List<ScheduleGroupDto> createScheduleGroup(ScheduleDto scheduleDto, Set<ScheduleGroupDto> scheduleGroupList) {
        if (scheduleGroupList.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScheduleGroupEntity> scheduleGroupEntityList = ScheduleGroupEntity
                .fromScheduleGroupDtoList(scheduleDto.getId(), scheduleGroupList.stream().toList());
        scheduleGroupRepository.saveAll(scheduleGroupEntityList);

        //알림 보내기
        notificationScheduleService.sendInviteNotification(
                scheduleDto.getUserId(),
                scheduleDto.getId(),
                new NotificationScheduleDto(scheduleDto.getCalendarId(), scheduleGroupList.stream().toList()),
                NotificationCategory.SCHEDULE
        );

        return ScheduleGroupDto.fromScheduleGroupEntityList(scheduleGroupEntityList);
    }

    @Transactional
    public List<ScheduleGroupDto> getScheduleGroupUserList(long userId, long scheduleId) {
        Optional<ScheduleGroupEntity> scheduleGroupEntity = scheduleGroupRepository.findByScheduleIdAndUserId(scheduleId, userId);
        ScheduleEntity scheduleEntity = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ScheduleException("Not found schedule")
        );

        if (scheduleEntity.getUserId() != userId && scheduleGroupEntity.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScheduleGroupEntity> scheduleGroupEntityList = scheduleGroupRepository.findByScheduleId(scheduleId);
        List<ScheduleGroupDto> result = null;

        if (scheduleEntity.getUserId() == userId || scheduleGroupEntity.get().getAuthority() == FileAuthority.ADMIN) {
            result = ScheduleGroupDto.fromScheduleGroupEntityList(scheduleGroupEntityList);
        } else {
            result = ScheduleGroupDto.fromUnauthorizedUserEntityList(scheduleGroupEntityList);
        }

        return result;
    }

    @Transactional
    public List<ScheduleGroupDto> updateScheduleGroup(long userId, ScheduleDto scheduleDto, List<ScheduleGroupDto> requestNewGroupList) {

        Optional<ScheduleGroupEntity> targetUserInfo = scheduleGroupRepository.findByScheduleIdAndUserId(scheduleDto.getId(), userId);

        if (targetUserInfo.isEmpty()) {
            return createScheduleGroup(scheduleDto, new HashSet<>(requestNewGroupList));
        }

        if (userId == scheduleDto.getUserId() ||
            targetUserInfo.get().getAuthority() == FileAuthority.ADMIN) {
            List<ScheduleGroupEntity> currentGroupList = scheduleGroupRepository.findByScheduleId(scheduleDto.getId());

            // DTO 목록에서 ID가 있는 항목들을 Map으로 만듭니다.
            Map<Long, ScheduleGroupDto> dtoMap = requestNewGroupList.stream()
                    .filter(dto -> dto.getId() != null)
                    .collect(Collectors.toMap(ScheduleGroupDto::getId, Function.identity()));

            // 기존 엔티티들을 순회하며 수정 또는 삭제 처리
            for (ScheduleGroupEntity entity : currentGroupList) {
                if (dtoMap.containsKey(entity.getId())) {
                    ScheduleGroupDto dto = dtoMap.get(entity.getId());
                    entity.setAuthority(dto.getAuthority());
                    entity.setStatus(dto.getStatus());
                    scheduleGroupRepository.save(entity);
                } else {
                    notificationScheduleService.deleteScheduleNotification(userId, scheduleDto.getId());
                    scheduleGroupRepository.delete(entity);
                }
            }

            //없는 것은 생성
            List<ScheduleGroupEntity> newList = requestNewGroupList.stream()
                    .filter(dto -> dto.getId() == null)
                    .map(dto -> new ScheduleGroupEntity(scheduleDto.getId(), dto))
                    .collect(Collectors.toList());
            if (!newList.isEmpty()) {
                scheduleGroupRepository.saveAll(newList);
            }

            // 최종적으로 업데이트된 전체 그룹 목록을 반환합니다.
            List<ScheduleGroupEntity> updatedList = scheduleGroupRepository.findByScheduleId(scheduleDto.getId());
            return ScheduleGroupDto.fromScheduleGroupEntityList(updatedList);
        }

        return requestNewGroupList;
    }

    @Transactional
    public ScheduleGroupDto updateStatus(long scheduleId, ScheduleGroupDto requestNewGroup) {
        ScheduleGroupEntity scheduleGroupEntity = scheduleGroupRepository.findByScheduleIdAndUserId(scheduleId, requestNewGroup.getUserId()).orElseThrow(
                () -> new ScheduleException("Not found user")
        );

        scheduleGroupEntity.setStatus(requestNewGroup.getStatus());
        scheduleGroupRepository.save(scheduleGroupEntity);

        return new ScheduleGroupDto(scheduleGroupEntity);
    }

    //그룹 일정인지 판별이 우선, 그룹 일정이라면 권한 확인
    @Transactional
    public void checkScheduleGroupAuth(long userId, long createdUserId, Long scheduleId) {
        if (scheduleId == null ||
            scheduleGroupRepository.findByScheduleId(scheduleId).isEmpty() ||
            createdUserId == userId) {
            return;
        }

        Optional<ScheduleGroupEntity> scheduleGroupEntity = scheduleGroupRepository.findByScheduleIdAndUserId(scheduleId, userId);

        if (scheduleGroupEntity.isEmpty() || scheduleGroupEntity.get().getAuthority() == FileAuthority.READ) {
            throw new ScheduleException("You do not have permission to access the schedule.");
        }
    }

    @Transactional
    public void deleteScheduleGroup(long scheduleId) {
        scheduleGroupRepository.deleteByScheduleId(scheduleId);
        deleteScheduleNotification(scheduleId);
    }

    @Transactional
    public void deleteScheduleNotification(long scheduleId) {
        notificationScheduleService.deleteScheduleNotifications(scheduleId);
    }

}