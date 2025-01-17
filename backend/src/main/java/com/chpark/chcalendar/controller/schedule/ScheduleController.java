package com.chpark.chcalendar.controller.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.enumClass.ScheduleRepeatScope;
import com.chpark.chcalendar.exception.ValidGroup;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.schedule.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/schedules")
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTitle(@RequestParam(value = "title", required = false) String title,
                                                                 HttpServletRequest request) {
        List<ScheduleDto> schedules;

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        if (title == null) {
            schedules = scheduleService.findByUserId(userId);
        } else {
            schedules = scheduleService.findSchedulesByTitle(title, userId);
        }

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/date")
    public ResponseEntity<Map<Long, List<ScheduleDto>>> getSchedulesByDateRangeAndCalendarId(@RequestParam("start") String startAt,
                                                                                             @RequestParam("end") String endAt,
                                                                                             HttpServletRequest request) {
        // Parsing the start and end dates to LocalDateTime
        LocalDateTime startDate = LocalDate.parse(startAt).atStartOfDay(); // 00:00:00
        LocalDateTime endDate = LocalDate.parse(endAt).atTime(LocalTime.MAX); // 23:59:59.999999999

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        Map<Long, List<ScheduleDto>> schedules = scheduleService.getScheduleByDateRangeAndCalendarId(startDate, endDate, userId);

        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable("id") long id,
                                                       HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        Optional<ScheduleDto> scheduleDto = scheduleService.findById(id, userId);

        return scheduleDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ScheduleDto.Response> createSchedule(@Validated(ValidGroup.CreateGroup.class) @RequestBody ScheduleDto.Request schedule,
                                                               HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        ScheduleDto.Response result = scheduleService.createByForm(schedule, userId);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleDto.Response> updateSchedule(@PathVariable("id") long id,
                                                               @RequestParam("repeat") boolean isRepeatChecked,
                                                               @Validated @RequestBody ScheduleDto.Request scheduleDto,
                                                               HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        ScheduleDto.Response response = scheduleService.updateSchedule(id, isRepeatChecked, scheduleDto, userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{id}/{update-scope}")
    public ResponseEntity<ScheduleDto.Response> updateRepeatSchedule(@PathVariable("id") long id,
                                                                     @PathVariable("update-scope") String repeatStringScope,
                                                                     @RequestParam("repeat") boolean isRepeatChecked,
                                                                     @Validated @RequestBody ScheduleDto.Request scheduleDto,
                                                                     HttpServletRequest request) {
        ScheduleRepeatScope scheduleRepeatScope = ScheduleRepeatScope.fromValue(repeatStringScope);
        ScheduleDto.Response response = null;

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        switch (scheduleRepeatScope) {
            case CURRENT -> {
                response = scheduleService.updateRepeatCurrentOnlySchedule(id, scheduleDto, userId);
            }
            case FUTURE -> {
                response = scheduleService.updateRepeatSchedule(id, isRepeatChecked, scheduleDto, userId);
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/calendars/{calendar-id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable("id") long id,
                                                 @PathVariable("calendar-id") long calendarId,
                                                 HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        scheduleService.deleteById(id, calendarId, userId);
        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }

    @DeleteMapping("/{id}/{delete-scope}/calendars/{calendar-id}")
    public ResponseEntity<String> deleteRepeatSchedule(@PathVariable("id") long id,
                                                       @PathVariable("delete-scope") String repeatStringScope,
                                                       @PathVariable("calendar-id") long calendarId,
                                                       HttpServletRequest request) {
        ScheduleRepeatScope scheduleRepeatScope = ScheduleRepeatScope.fromValue(repeatStringScope);

        String token = jwtTokenProvider.resolveToken(request);
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        //삭제할 범위
        switch (scheduleRepeatScope){
            case CURRENT -> {
                scheduleService.deleteCurrentOnlyRepeatSchedule(id, userId);
                scheduleService.update(id, new ScheduleDto(), true, userId);
                scheduleService.deleteById(id, calendarId, userId);
            }
            case FUTURE -> {
                scheduleService.deleteFutureRepeatSchedules(id, userId);
                scheduleService.update(id, new ScheduleDto(), true, userId);
                scheduleService.deleteById(id, calendarId, userId);
            }
        }

        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }
}