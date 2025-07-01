package com.chpark.chcalendar.controller.schedule;

import com.chpark.chcalendar.dto.CursorPage;
import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.entity.calendar.CalendarProviderEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.JwtTokenType;
import com.chpark.chcalendar.enumClass.ScheduleRepeatScope;
import com.chpark.chcalendar.exception.ValidGroup;
import com.chpark.chcalendar.repository.calendar.CalendarProviderRepository;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.schedule.GoogleScheduleService;
import com.chpark.chcalendar.service.schedule.ScheduleService;
import com.chpark.chcalendar.utility.CookieUtility;
import jakarta.persistence.EntityNotFoundException;
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
@RequestMapping("/api/schedules")
@Slf4j
public class ScheduleController {

    private final JwtTokenProvider jwtTokenProvider;
    private final Map<CalendarCategory, ScheduleService> scheduleServiceMap;
    private final CalendarProviderRepository calendarProviderRepository;

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTitle(@RequestParam(value = "title", required = false) String title,
                                                                 HttpServletRequest request) {
        List<ScheduleDto> schedules;

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        if (title == null) {
            schedules = scheduleServiceMap.get(CalendarCategory.USER).findByUserId(userId);
        } else {
            schedules = scheduleServiceMap.get(CalendarCategory.USER).findSchedulesByTitle(title, userId);
        }

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/date")
    public ResponseEntity<CursorPage<ScheduleDto>> getNextSchedules(
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam(name = "cursor-time", required = false) String cursorTime,
            @RequestParam(name = "cursor-id", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "50") int size,
            HttpServletRequest request) {

        LocalDateTime startTime = LocalDate.parse(start).atStartOfDay();
        LocalDateTime endTime = LocalDate.parse(end).atTime(LocalTime.MAX);
        String trimmedCursor = (cursorTime != null) ? cursorTime.trim() : "";
        LocalDateTime targetCursorTime = (!trimmedCursor.isEmpty()
                && !"null".equalsIgnoreCase(trimmedCursor)
                && !"undefined".equalsIgnoreCase(trimmedCursor))
                ? LocalDateTime.parse(trimmedCursor)
                : LocalDateTime.of(1000, 1, 1, 0, 0);
        long targetCursorId = (cursorId != null) ? cursorId : 0L;

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        CursorPage<ScheduleDto> result = scheduleServiceMap.get(CalendarCategory.USER).getNextSchedules(userId, startTime, endTime, targetCursorTime, targetCursorId, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable("id") long id,
                                                       HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        Optional<ScheduleDto> scheduleDto = scheduleServiceMap.get(CalendarCategory.USER).findById(id, userId);

        return scheduleDto.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ScheduleDto.Response> createSchedule(@Validated(ValidGroup.CreateGroup.class) @RequestBody ScheduleDto.Request schedule,
                                                               HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        //여기서 provider검색해서 처리해주자.
        Optional<CalendarProviderEntity> calendarProvider = calendarProviderRepository.findByCalendarId(schedule.getScheduleDto().getCalendarId());
        ScheduleDto.Response result = null;

        if (calendarProvider.isEmpty()) {
            result = scheduleServiceMap.get(CalendarCategory.USER).createByForm(schedule, userId);
        } else {
            String googleAccessToken = CookieUtility.getToken(request, JwtTokenType.GOOGLE_ACCESS);

            GoogleScheduleService googleScheduleService = (GoogleScheduleService) scheduleServiceMap.get(CalendarCategory.GOOGLE);
            result = googleScheduleService.createByForm(schedule, userId, googleAccessToken, calendarProvider.get().getProviderId());
        }


        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleDto.Response> updateSchedule(@PathVariable("id") long id,
                                                               @RequestParam("repeat") boolean isRepeatChecked,
                                                               @Validated @RequestBody ScheduleDto.Request scheduleDto,
                                                               HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        ScheduleDto.Response response = scheduleServiceMap.get(CalendarCategory.USER).updateSchedule(id, isRepeatChecked, scheduleDto, userId);

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

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        switch (scheduleRepeatScope) {
            case CURRENT -> {
                response = scheduleServiceMap.get(CalendarCategory.USER).updateRepeatCurrentOnlySchedule(id, scheduleDto, userId);
            }
            case FUTURE -> {
                response = scheduleServiceMap.get(CalendarCategory.USER).updateRepeatSchedule(id, isRepeatChecked, scheduleDto, userId);
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{schedule-id}/calendars/{calendar-id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable("schedule-id") long scheduleId,
                                                 @PathVariable("calendar-id") long calendarId,
                                                 HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        scheduleServiceMap.get(CalendarCategory.USER).deleteById(scheduleId, calendarId, userId);

        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }

    @DeleteMapping("/{schedule-id}/{delete-scope}/calendars/{calendar-id}")
    public ResponseEntity<String> deleteRepeatSchedule(@PathVariable("schedule-id") long scheduleId,
                                                       @PathVariable("delete-scope") String repeatStringScope,
                                                       @PathVariable("calendar-id") long calendarId,
                                                       HttpServletRequest request) {
        ScheduleRepeatScope scheduleRepeatScope = ScheduleRepeatScope.fromValue(repeatStringScope);

        String token = jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS.getValue());
        long userId = jwtTokenProvider.getUserIdFromToken(token);

        //삭제할 범위
        switch (scheduleRepeatScope){
            case CURRENT -> {
                scheduleServiceMap.get(CalendarCategory.USER).deleteCurrentOnlyRepeatSchedule(scheduleId);
                //repeat를 지워주기 위한 update
                scheduleServiceMap.get(CalendarCategory.USER).update(scheduleId, new ScheduleDto(), true, userId);
                scheduleServiceMap.get(CalendarCategory.USER).deleteById(scheduleId, calendarId, userId);
            }
            case FUTURE -> {
                scheduleServiceMap.get(CalendarCategory.USER).deleteFutureRepeatSchedules(scheduleId, userId);
                //repeat를 지워주기 위한 update
                scheduleServiceMap.get(CalendarCategory.USER).update(scheduleId, new ScheduleDto(), true, userId);
                scheduleServiceMap.get(CalendarCategory.USER).deleteById(scheduleId, calendarId, userId);
            }
        }

        return new ResponseEntity<>("Schedule deleted successfully.", HttpStatus.OK);
    }
}
