package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.enumClass.ScheduleRepeatScope;
import com.chpark.calendar.exception.CustomException;
import com.chpark.calendar.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/schedules")
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
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

        if(schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        }
    }

    @GetMapping("/date")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByDate(@RequestParam("year") Integer year,
                                                                @RequestParam(value = "month", required = false) Integer month,
                                                                @RequestParam(value = "day", required = false) Integer day) {

        log.info("Fetching schedules for year: {}, month: {}, day: {}", year, month, day);

        List<ScheduleDto> schedules;

        if (month != null && day != null) {
            //day의 범위를 잡기 위함
            LocalDate.of(year, month, day);

            schedules =  scheduleService.getSchedulesForDate(year, month, day);
        } else if (month != null) {
            if (month < 1 || month > 12) {
                throw new CustomException("Month parameter must be between 1 and 12.");
            }

            schedules =  scheduleService.getSchedulesForMonth(year, month);
        } else {
            schedules =  scheduleService.getSchedulesForYear(year);
        }

        if(schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable("id") int id,
                                                       @RequestParam("repeat_id") Integer repeatId) {

        ScheduleDto scheduleDto = scheduleService.findById(id);

        return new ResponseEntity<>(scheduleDto, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(@RequestBody ScheduleDto schedule) {
        Optional<ScheduleDto> createDto = scheduleService.create(schedule);

        return createDto.map(scheduleDto -> new ResponseEntity<>(scheduleDto, HttpStatus.CREATED)).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(@PathVariable("id") int id, @RequestBody ScheduleDto scheduleDto) {
        ScheduleDto updateDto = scheduleService.update(id, scheduleDto);

        return new ResponseEntity<>(updateDto, HttpStatus.OK);
    }

    @PutMapping("/{id}/{update-scope}")
    public ResponseEntity<ScheduleDto.repeatResponse> updateRepeatSchedule(@PathVariable("id") int id,
                                                                   @PathVariable("update-scope") ScheduleRepeatScope scheduleRepeatScope,
                                                                   @RequestBody ScheduleDto.repeatRequest scheduleDto) {

        Optional<ScheduleDto.repeatResponse> updateDto = Optional.empty();

        //일정의 수정 범위가 어떻게 되는가
        switch (scheduleRepeatScope) {
            case currentonly -> {
                updateDto = Optional.of(scheduleService.repeatCurrentOnlyScheduleUpdate(id, scheduleDto.getScheduleDto()));
            }
            case currentandfuture -> {
                //반복되는 일정 수정
                updateDto = Optional.of(scheduleService.repeatCurrentAndFutureScheduleUpdate(id, scheduleDto));
            }
        }

        return updateDto.map(repeatResponse -> new ResponseEntity<>(repeatResponse, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable("id") int id) {

        scheduleService.deleteById(id);
        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }

    @DeleteMapping("/{id}/{delete-scope}")
    public ResponseEntity<String> deleteRepeatSchedule(@PathVariable("id") int id,
                                                       @PathVariable("delete-scope") ScheduleRepeatScope scheduleRepeatScope) {

        //삭제할 범위
        switch (scheduleRepeatScope){
            case currentonly -> {
                scheduleService.deleteCurrentOnlyRepeatSchedule(id);
            }
            case currentandfuture -> {
                scheduleService.deleteCurrentAndFutureRepeatSchedule(id);
            }
        }

        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }
}
