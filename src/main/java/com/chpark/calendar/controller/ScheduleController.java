package com.chpark.calendar.controller;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.enumClass.ScheduleRepeatScope;
import com.chpark.calendar.service.ScheduleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
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
    public ResponseEntity<ScheduleDto.Response> createSchedule(@Valid @RequestBody ScheduleDto.Request schedule) {
        if(schedule.getScheduleDto() == null) {
            new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ScheduleDto.Response result = scheduleService.createByForm(schedule);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(@PathVariable("id") int id, @Valid @RequestBody ScheduleDto scheduleDto) {
        ScheduleDto updateDto = scheduleService.update(id, scheduleDto);

        return new ResponseEntity<>(updateDto, HttpStatus.OK);
    }

    @PutMapping("/{id}/{update-scope}")
    public ResponseEntity<ScheduleDto.Response> updateRepeatSchedule(@PathVariable("id") int id,
                                                                   @PathVariable("update-scope") ScheduleRepeatScope scheduleRepeatScope,
                                                                   @Valid @RequestBody ScheduleDto.Request scheduleDto) {

        Optional<ScheduleDto.Response> updateDto = Optional.empty();

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

        return updateDto.map(response -> new ResponseEntity<>(response, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
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
