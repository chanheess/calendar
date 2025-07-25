package com.chpark.chcalendar.service.schedule;

import com.chpark.chcalendar.dto.schedule.ScheduleDto;
import com.chpark.chcalendar.dto.schedule.ScheduleGroupDto;
import com.chpark.chcalendar.dto.schedule.ScheduleNotificationDto;
import com.chpark.chcalendar.dto.schedule.ScheduleRepeatDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.entity.schedule.ScheduleEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.ScheduleRepeatType;
import com.chpark.chcalendar.repository.schedule.ScheduleNotificationRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepeatRepository;
import com.chpark.chcalendar.repository.schedule.ScheduleRepository;
import com.chpark.chcalendar.service.calendar.CalendarMemberService;
import com.chpark.chcalendar.service.calendar.CalendarService;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceUnitTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleNotificationRepository scheduleNotificationRepository;

    @Mock
    private ScheduleRepeatRepository scheduleRepeatRepository;

    @Mock
    private ScheduleNotificationService scheduleNotificationService;

    @Mock
    private ScheduleRepeatService scheduleRepeatService;

    @Mock
    private CalendarMemberService calendarMemberService;

    @Mock
    private UserCalendarService userCalendarService;

    @Mock
    private ScheduleGroupService scheduleGroupService;

    @Mock
    private Map<CalendarCategory, CalendarService> calendarServiceMap;

    @InjectMocks
    private ScheduleService scheduleService;

    private UserEntity mockUser;
    private ScheduleEntity mockSchedule;
    ScheduleDto.Request mockRequestDto;

    @BeforeEach
    void setup() {
        // 유저 및 일정 기본값 설정
        mockUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        mockSchedule = new ScheduleEntity();
        mockSchedule.setId(1);
        mockSchedule.setTitle("Test Schedule");
        mockSchedule.setUserId(mockUser.getId());
        mockSchedule.setStartAt(LocalDateTime.now());
        mockSchedule.setEndAt(LocalDateTime.now().plusDays(1));
        mockSchedule.setRepeatId(null);
        mockSchedule.setCalendarId(1L);

        mockRequestDto = new ScheduleDto.Request();
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("Test Form Schedule");
        scheduleDto.setDescription("Form Desc");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(1));
        scheduleDto.setCalendarId(1L);

        ScheduleNotificationDto notificationDto = new ScheduleNotificationDto();
        notificationDto.setNotificationAt(LocalDateTime.now().plusHours(1));

        ScheduleRepeatDto repeatDto = new ScheduleRepeatDto();
        repeatDto.setRepeatType(ScheduleRepeatType.DAY);
        repeatDto.setRepeatInterval(1);

        Set<ScheduleGroupDto> scheduleGroupDto = new HashSet<>();

        mockRequestDto.setScheduleDto(scheduleDto);
        mockRequestDto.setNotificationDto(Set.of(notificationDto));
        mockRequestDto.setRepeatDto(repeatDto);
        mockRequestDto.setGroupDto(scheduleGroupDto);
    }

    @Test
    void create() {
        when(scheduleRepository.save(any(ScheduleEntity.class)))
                .thenAnswer(invocation -> invocation.<ScheduleEntity>getArgument(0));

        ScheduleDto createdSchedule = scheduleService.create(mockRequestDto.getScheduleDto(), mockUser.getId());

        assertNotNull(createdSchedule);
        assertEquals("Test Form Schedule", createdSchedule.getTitle());
    }

    @Test
    void findSchedulesByTitle() {
        when(scheduleRepository.findByTitleContainingAndUserId("Test", mockUser.getId()))
                .thenReturn(List.of(mockSchedule));

        List<ScheduleDto> result = scheduleService.findSchedulesByTitle("Test", mockUser.getId());

        assertFalse(result.isEmpty(), "Result should not be empty");
        assertEquals("Test Schedule", result.get(0).getTitle(), "Schedule title should be 'Test Schedule'");
    }

    @Test
    void update() {
        when(scheduleRepository.findById(mockSchedule.getId()))
                .thenReturn(Optional.of(mockSchedule));

        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(1));
        scheduleDto.setTitle("Updated Schedule");
        scheduleDto.setCalendarId(1L);

        when(scheduleRepository.save(any(ScheduleEntity.class)))
                .thenAnswer(invocation -> invocation.<ScheduleEntity>getArgument(0));

        ScheduleDto updatedSchedule = scheduleService.update(mockSchedule.getId(), scheduleDto, mockUser.getId());

        assertEquals("Updated Schedule", updatedSchedule.getTitle());
    }

    @Test
    void deleteCurrentOnlyRepeatSchedule() {
        // Given
        long scheduleId = mockSchedule.getId();
        long repeatId = 1;

        // mockSchedule에 반복 id 설정
        mockSchedule.setRepeatId(repeatId);

        // 반복 일정이 마지막 남은 일정임을 반환하도록 설정
        when(scheduleRepository.countByRepeatId(repeatId))
                .thenReturn(1L);

        // 반복 일정 삭제와 그룹 알림 삭제는 아무 동작 없이 처리하도록 설정
        doNothing().when(scheduleRepeatRepository).deleteById(repeatId);

        // When
        scheduleService.deleteCurrentOnlyRepeatSchedule(mockSchedule);

        // Then
        verify(scheduleRepository, times(1)).countByRepeatId(repeatId);
        verify(scheduleRepeatRepository, times(1)).deleteById(repeatId);
    }

    @Test
    void findById() {
        when(scheduleRepository.findByIdAndUserId(mockSchedule.getId(), mockUser.getId()))
                .thenReturn(Optional.of(mockSchedule));

        Optional<ScheduleDto> result = scheduleService.findById(mockSchedule.getId(), mockSchedule.getUserId());
        assertTrue(result.isPresent());
        assertEquals(mockSchedule.getTitle(), result.get().getTitle());
    }

    @Test
    void findAll() {
        when(scheduleRepository.findAll()).thenReturn(List.of(mockSchedule));

        List<ScheduleDto> result = scheduleService.findAll();
        assertFalse(result.isEmpty());
        assertEquals(mockSchedule.getTitle(), result.get(0).getTitle());
    }

    @Test
    void findByUserId() {
        when(scheduleRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockSchedule));

        List<ScheduleDto> result = scheduleService.findByUserId(mockUser.getId());

        ScheduleDto expectedSchedule = new ScheduleDto(mockSchedule);

        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expectedSchedule));
    }

    @ParameterizedTest
    @CsvSource({
            "true",  // Schedule이 존재할 때
            "false"  // Schedule이 존재하지 않을 때
    })
    void existsById(boolean scheduleExists) {
        // Given
        when(scheduleRepository.existsById(mockSchedule.getId()))
                .thenReturn(scheduleExists);

        // When
        Boolean hasSchedule = scheduleService.existsById(mockSchedule.getId());

        // Then
        assertThat(hasSchedule).isEqualTo(scheduleExists);
    }

    @Test
    void getSchedulesByDateRange() {
        List<ScheduleEntity> mockSchedules = List.of(mockSchedule);

        when(scheduleRepository.findSchedules(mockSchedule.getStartAt(), mockSchedule.getEndAt(), mockUser.getId()))
                .thenReturn(mockSchedules);

        List<ScheduleDto> result = scheduleService.getSchedulesByDateRange(mockSchedule.getStartAt(), mockSchedule.getEndAt(), mockUser.getId());

        ScheduleDto expectedSchedule = new ScheduleDto(mockSchedule);

        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expectedSchedule));
    }
}