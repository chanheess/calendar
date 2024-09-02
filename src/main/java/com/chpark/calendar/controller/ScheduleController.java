package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.enumClass.ScheduleRepeatScope;
import com.chpark.calendar.exception.ValidGroup;
import com.chpark.calendar.service.ScheduleNotificationService;
import com.chpark.calendar.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/schedules")
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService, ScheduleNotificationService scheduleNotificationService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTitle(@RequestParam(value = "title", required = false) String title) {

        List<ScheduleDto> schedules;

        if(title == null) {
            schedules = scheduleService.findAll();
        } else {
            schedules = scheduleService.findSchedulesByTitle(title);
        }

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/date")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByDateRange(@RequestParam("start") String startDateStr,
                                                                     @RequestParam("end") String endDateStr) {
        // Parsing the start and end dates to LocalDateTime
        LocalDateTime startDate = LocalDate.parse(startDateStr).atStartOfDay(); // 00:00:00
        LocalDateTime endDate = LocalDate.parse(endDateStr).atTime(LocalTime.MAX); // 23:59:59.999999999

        List<ScheduleDto> schedules = scheduleService.getSchedulesByDateRange(startDate, endDate);

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable("id") int id) {
        Optional<ScheduleDto> scheduleDto = scheduleService.findById(id);

        return scheduleDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ScheduleDto.Response> createSchedule(@Validated(ValidGroup.CreateGroup.class) @RequestBody ScheduleDto.Request schedule) {
        if(schedule.getScheduleDto() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ScheduleDto.Response result = scheduleService.createByForm(schedule);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleDto.Response> updateSchedule(@PathVariable("id") int id,
                                                               @RequestParam("repeat") boolean isRepeatChecked,
                                                               @Validated @RequestBody ScheduleDto.Request scheduleDto) {
        ScheduleDto.Response response = scheduleService.updateSchedule(id, isRepeatChecked, scheduleDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{id}/{update-scope}")
    public ResponseEntity<ScheduleDto.Response> updateRepeatSchedule(@PathVariable("id") int id,
                                                                     @PathVariable("update-scope") String repeatStringScope,
                                                                     @RequestParam("repeat") boolean isRepeatChecked,
                                                                     @Validated @RequestBody ScheduleDto.Request scheduleDto) {
        ScheduleRepeatScope scheduleRepeatScope = ScheduleRepeatScope.fromValue(repeatStringScope);

        ScheduleDto.Response response = null;

        switch (scheduleRepeatScope) {
            case CURRENT -> {
                response = scheduleService.updateRepeatCurrentOnlySchedule(id, scheduleDto);
            }
            case FUTURE -> {
                response = scheduleService.updateRepeatSchedule(id, isRepeatChecked, scheduleDto);
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable("id") int id) {
        scheduleService.deleteById(id);
        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }

    @DeleteMapping("/{id}/{delete-scope}")
    public ResponseEntity<String> deleteRepeatSchedule(@PathVariable("id") int id,
                                                       @PathVariable("delete-scope") String repeatStringScope) {
        ScheduleRepeatScope scheduleRepeatScope = ScheduleRepeatScope.fromValue(repeatStringScope);

        //삭제할 범위
        switch (scheduleRepeatScope){
            case CURRENT -> {
                scheduleService.deleteCurrentOnlyRepeatSchedule(id);
                scheduleService.deleteById(id);
            }
            case FUTURE -> {
                scheduleService.deleteFutureRepeatSchedules(id);
                scheduleService.deleteById(id);
            }
        }

        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }
}
