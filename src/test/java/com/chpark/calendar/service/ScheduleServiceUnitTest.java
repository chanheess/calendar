package com.chpark.calendar.service;import com.chpark.calendar.dto.ScheduleDto;import com.chpark.calendar.dto.ScheduleNotificationDto;import com.chpark.calendar.dto.ScheduleRepeatDto;import com.chpark.calendar.entity.ScheduleEntity;import com.chpark.calendar.entity.UserEntity;import com.chpark.calendar.enumClass.ScheduleRepeatType;import com.chpark.calendar.repository.schedule.ScheduleNotificationRepository;import com.chpark.calendar.repository.schedule.ScheduleRepeatRepository;import com.chpark.calendar.repository.schedule.ScheduleRepository;import jakarta.persistence.EntityNotFoundException;import org.junit.jupiter.api.BeforeEach;import org.junit.jupiter.api.Test;import org.junit.jupiter.api.extension.ExtendWith;import org.junit.jupiter.params.ParameterizedTest;import org.junit.jupiter.params.provider.CsvSource;import org.mockito.InjectMocks;import org.mockito.Mock;import org.mockito.junit.jupiter.MockitoExtension;import java.time.LocalDateTime;import java.util.List;import java.util.Optional;import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.ArgumentMatchers.any;import static org.mockito.Mockito.*;@ExtendWith(MockitoExtension.class)class ScheduleServiceUnitTest {    @Mock    private ScheduleRepository scheduleRepository;    @Mock    private ScheduleNotificationRepository scheduleNotificationRepository;    @Mock    private ScheduleRepeatRepository scheduleRepeatRepository;    @Mock    private ScheduleNotificationService scheduleNotificationService;    @Mock    private ScheduleRepeatService scheduleRepeatService;    @InjectMocks    private ScheduleService scheduleService;    private UserEntity mockUser;    private ScheduleEntity mockSchedule;    ScheduleDto.Request mockRequestDto;    @BeforeEach    void setUp() {        // 유저 및 일정 기본값 설정        mockUser = new UserEntity(1, "test@example.com");        mockSchedule = new ScheduleEntity();        mockSchedule.setId(1);        mockSchedule.setTitle("Test Schedule");        mockSchedule.setUserId(mockUser.getId());        mockSchedule.setStartAt(LocalDateTime.now());        mockSchedule.setEndAt(LocalDateTime.now().plusDays(1));        mockSchedule.setRepeatId(null);        mockRequestDto = new ScheduleDto.Request();        ScheduleDto scheduleDto = new ScheduleDto();        scheduleDto.setTitle("Test Form Schedule");        scheduleDto.setDescription("Form Desc");        scheduleDto.setStartAt(LocalDateTime.now());        scheduleDto.setEndAt(LocalDateTime.now().plusDays(1));        ScheduleNotificationDto notificationDto = new ScheduleNotificationDto();        notificationDto.setNotificationAt(LocalDateTime.now().plusHours(1));        ScheduleRepeatDto repeatDto = new ScheduleRepeatDto();        repeatDto.setRepeatType(ScheduleRepeatType.d);        repeatDto.setRepeatInterval(1);        mockRequestDto.setScheduleDto(scheduleDto);        mockRequestDto.setNotificationDto(List.of(notificationDto));        mockRequestDto.setRepeatDto(repeatDto);    }    @Test    void create() {        when(scheduleRepository.save(any(ScheduleEntity.class)))                .thenAnswer(invocation -> invocation.<ScheduleEntity>getArgument(0));        ScheduleDto createdSchedule = scheduleService.create(mockRequestDto.getScheduleDto(), mockUser.getId());        assertNotNull(createdSchedule);        assertEquals("Test Form Schedule", createdSchedule.getTitle());    }    @Test    void createByForm() {        //모킹        when(scheduleRepository.save(any(ScheduleEntity.class)))                .thenAnswer(invocation -> invocation.<ScheduleEntity>getArgument(0));        when(scheduleNotificationService.create(anyInt(), anyList()))                .thenReturn(mockRequestDto.getNotificationDto());        when(scheduleRepeatService.create(anyInt(), eq(mockRequestDto.getRepeatDto()), eq(mockSchedule.getUserId())))                .thenReturn(mockRequestDto.getRepeatDto());        ScheduleDto.Response result = scheduleService.createByForm(mockRequestDto, mockSchedule.getUserId());        // 검증 (일정 + 알림 + 반복)        assertNotNull(result.getRepeatDto(), "Repeat should not be null.");        assertEquals(ScheduleRepeatType.d, result.getRepeatDto().getRepeatType());        assertFalse(result.getNotificationDto().isEmpty(), "Notifications should not be empty.");    }    @Test    void findSchedulesByTitle() {        when(scheduleRepository.findByTitleContainingAndUserId("Test", mockUser.getId()))                .thenReturn(List.of(mockSchedule));        List<ScheduleDto> result = scheduleService.findSchedulesByTitle("Test", mockUser.getId());        assertFalse(result.isEmpty(), "Result should not be empty");        assertEquals("Test Schedule", result.get(0).getTitle(), "Schedule title should be 'Test Schedule'");    }    @Test    void update() {        when(scheduleRepository.findByIdAndUserId(mockSchedule.getId(), mockUser.getId()))                .thenReturn(Optional.of(mockSchedule));        ScheduleDto scheduleDto = new ScheduleDto();        scheduleDto.setTitle("Updated Schedule");        when(scheduleRepository.save(any(ScheduleEntity.class)))                .thenAnswer(invocation -> invocation.<ScheduleEntity>getArgument(0));        ScheduleDto updatedSchedule = scheduleService.update(mockSchedule.getId(), scheduleDto, mockUser.getId());        assertEquals("Updated Schedule", updatedSchedule.getTitle());    }    @Test    void updateSchedule() {        when(scheduleRepository.getRepeatId(mockSchedule.getId(), mockUser.getId()))                .thenReturn(Optional.empty());        when(scheduleRepository.findByIdAndUserId(mockSchedule.getId(), mockUser.getId()))                .thenReturn(Optional.of(mockSchedule));        when(scheduleRepository.save(any(ScheduleEntity.class)))                .thenAnswer(invocation -> invocation.<ScheduleEntity>getArgument(0));        when(scheduleNotificationService.update(anyInt(), anyList()))                .thenReturn(mockRequestDto.getNotificationDto());        when(scheduleRepeatService.create(anyInt(), any(ScheduleRepeatDto.class), anyInt()))                .thenReturn(mockRequestDto.getRepeatDto());        ScheduleDto.Request requestDto = new ScheduleDto.Request();        requestDto.setScheduleDto(new ScheduleDto("Update Schedule", "Desc", LocalDateTime.now(), LocalDateTime.now().plusDays(1)));        ScheduleDto.Response response = scheduleService.updateSchedule(mockSchedule.getId(), true, requestDto, mockUser.getId());        assertNotNull(response.getScheduleDto());        assertEquals("Update Schedule", response.getScheduleDto().getTitle());    }    @Test    void deleteById() {        when(scheduleRepository.getRepeatId(mockSchedule.getId(), mockUser.getId()))                .thenReturn(Optional.empty());        doNothing().when(scheduleNotificationRepository).deleteByScheduleId(mockSchedule.getId());        doNothing().when(scheduleRepository).deleteByIdAndUserId(mockSchedule.getId(), mockUser.getId());        when(scheduleRepository.findById(mockSchedule.getId()))                .thenReturn(Optional.empty());        assertDoesNotThrow(() -> scheduleService.deleteById(mockSchedule.getId(), mockUser.getId()));        verify(scheduleRepository, times(1)).deleteByIdAndUserId(mockSchedule.getId(), mockUser.getId());        Optional<ScheduleEntity> deletedSchedule = scheduleRepository.findById(mockSchedule.getId());        assertTrue(deletedSchedule.isEmpty(), "The schedule should no longer exist after deletion");    }    @Test    void deleteCurrentOnlyRepeatSchedule() {        // Given        int scheduleId = mockSchedule.getId();        int userId = mockUser.getId();        int repeatId = 1;        mockSchedule.setRepeatId(repeatId);        when(scheduleRepository.findByIdAndUserId(scheduleId, userId))                .thenReturn(Optional.of(mockSchedule));        when(scheduleRepository.isLastRemainingRepeatSchedule(repeatId))                .thenReturn(true);        doNothing().when(scheduleRepeatRepository).deleteById(repeatId);        // When        scheduleService.deleteCurrentOnlyRepeatSchedule(scheduleId, userId);        // Then        verify(scheduleRepository).findByIdAndUserId(scheduleId, userId);        verify(scheduleRepository).isLastRemainingRepeatSchedule(repeatId);        verify(scheduleRepeatRepository).deleteById(repeatId);    }    @Test    void deleteCurrentOnlyRepeatSchedule_ScheduleNotFound() {        // Given        int scheduleId = mockSchedule.getId();        int userId = mockUser.getId();        when(scheduleRepository.findByIdAndUserId(scheduleId, userId))                .thenReturn(Optional.empty());        assertThrows(EntityNotFoundException.class,                () -> scheduleService.deleteCurrentOnlyRepeatSchedule(scheduleId, userId));        verify(scheduleRepository).findByIdAndUserId(scheduleId, userId);        verifyNoMoreInteractions(scheduleRepository, scheduleRepeatRepository);    }    @Test    void findById() {        when(scheduleRepository.findByIdAndUserId(mockSchedule.getId(), mockUser.getId()))                .thenReturn(Optional.of(mockSchedule));        Optional<ScheduleDto> result = scheduleService.findById(mockSchedule.getId(), mockSchedule.getUserId());        assertTrue(result.isPresent());        assertEquals(mockSchedule.getTitle(), result.get().getTitle());    }    @Test    void findAll() {        when(scheduleRepository.findAll()).thenReturn(List.of(mockSchedule));        List<ScheduleDto> result = scheduleService.findAll();        assertFalse(result.isEmpty());        assertEquals(mockSchedule.getTitle(), result.get(0).getTitle());    }    @Test    void findByUserId() {        when(scheduleRepository.findByUserId(mockUser.getId()))                .thenReturn(List.of(mockSchedule));        List<ScheduleDto> result = scheduleService.findByUserId(mockUser.getId());        ScheduleDto expectedSchedule = new ScheduleDto(mockSchedule);        assertThat(result)                .isNotEmpty()                .hasSize(1)                .usingRecursiveComparison()                .isEqualTo(List.of(expectedSchedule));    }    @ParameterizedTest    @CsvSource({            "true",  // Schedule이 존재할 때            "false"  // Schedule이 존재하지 않을 때    })    void existsById(boolean scheduleExists) {        // Given        when(scheduleRepository.existsById(mockSchedule.getId()))                .thenReturn(scheduleExists);        // When        Boolean hasSchedule = scheduleService.existsById(mockSchedule.getId());        // Then        assertThat(hasSchedule).isEqualTo(scheduleExists);    }    @Test    void getSchedulesByDateRange() {        List<ScheduleEntity> mockSchedules = List.of(mockSchedule);        when(scheduleRepository.findSchedules(mockSchedule.getStartAt(), mockSchedule.getEndAt(), mockUser.getId()))                .thenReturn(mockSchedules);        List<ScheduleDto> result = scheduleService.getSchedulesByDateRange(mockSchedule.getStartAt(), mockSchedule.getEndAt(), mockUser.getId());        ScheduleDto expectedSchedule = new ScheduleDto(mockSchedule);        assertThat(result)                .isNotEmpty()                .hasSize(1)                .usingRecursiveComparison()                .isEqualTo(List.of(expectedSchedule));    }}