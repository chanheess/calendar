package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.repository.schedule.ScheduleGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ScheduleGroupService {

    private final ScheduleGroupRepository scheduleGroupRepository;

//    @Transactional
//    public void createGroupSchedule(long userId, List<ScheduleGroupDto> scheduleGroupList) {
//        userId
//
//
//        scheduleGroupList.forEach(scheduleGroup -> {
//
//
//            scheduleGroupRepository.save(scheduleGroupEntity);
//
//        });
//    }

}
