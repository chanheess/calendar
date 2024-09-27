package com.chpark.calendar.service;

import com.chpark.calendar.dto.ScheduleDto;
import com.chpark.calendar.dto.UserDto;
import com.chpark.calendar.entity.ScheduleEntity;
import com.chpark.calendar.repository.schedule.ScheduleRepository;
import com.chpark.calendar.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableTransactionManagement
@Slf4j
@AutoConfigureMockMvc
public class ScheduleServiceTest {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    private String loginCookie;
    private int userId;

    @BeforeEach
    void userSignUpAndLogin() throws Exception {

        mockMvc.perform(post("/register/users")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "test@example.com")
                        .param("password", "password")
                        .param("nickname", "testUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        MvcResult result = mockMvc.perform(post("/login/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();

        loginCookie = result.getResponse().getHeader("Set-Cookie");

        String token = Arrays.stream(loginCookie.split(";"))
                .filter(c -> c.trim().startsWith("jwtToken="))
                .findFirst()
                .map(c -> c.substring("jwtToken=".length()))
                .orElseThrow(() -> new IllegalArgumentException("JWT Token not found in cookie"));
        userId = jwtTokenProvider.getUserIdFromToken(token);
    }

    @Test
    @Transactional
    void join() {
        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setTitle("hello world");
        scheduleDto.setDescription("just test");
        scheduleDto.setStartAt(LocalDateTime.now());
        scheduleDto.setEndAt(LocalDateTime.now().plusDays(3));

        ScheduleDto createDto = scheduleService.create(scheduleDto, userId);
        assertNotNull(createDto, "Not created");

        log.info("Created schedule info: {}", createDto);
    }

    @Test
    @Transactional
    void testFindSchedulesByTitle() {
        ScheduleEntity scheduleEntity1 = new ScheduleEntity();
        scheduleEntity1.setTitle("나 찾아봐라~");
        scheduleEntity1.setDescription("이게 머고?");
        scheduleEntity1.setStartAt(LocalDateTime.now());
        scheduleEntity1.setEndAt(LocalDateTime.now().plusDays(3));
        scheduleRepository.save(scheduleEntity1);

        ScheduleEntity scheduleEntity2 = new ScheduleEntity();
        scheduleEntity2.setTitle("나는 못  찾겠지ㅋㅋ");
        scheduleEntity2.setDescription("이건 맞지^^");
        scheduleEntity2.setStartAt(LocalDateTime.now());
        scheduleEntity2.setEndAt(LocalDateTime.now().plusDays(4));
        scheduleRepository.save(scheduleEntity2);

        //TODO:
//        List<ScheduleDto> result = scheduleService.findSchedulesByTitle("나", );
//        assertFalse(result.isEmpty());
//
//        result.forEach(schedule -> log.info("Found Schedule: {}", schedule));
    }

    @Test
    @Transactional
    void updateTest() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("hello world");
        scheduleEntity.setDescription("just test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(3));
        scheduleRepository.save(scheduleEntity);

        scheduleEntity.setTitle("hi cloud");
        scheduleEntity.setDescription("update?");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        //TODO:
//        ScheduleDto updateDto = scheduleService.update(scheduleEntity.getId(), new ScheduleDto(scheduleEntity));
//
//        assertNotNull(updateDto);
//        log.info("Updated Schedule: {}", updateDto);
    }

    @Test
    @Transactional
    void deleteTest() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("delete test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        scheduleRepository.save(scheduleEntity);

        int deletedId = scheduleEntity.getId();
        //TODO:
//        scheduleService.deleteById(scheduleEntity.getId());

        Optional<ScheduleEntity> result = scheduleRepository.findById(deletedId);
        assertFalse(result.isPresent(), "Not deleted.");

        log.info("Schedule with ID {} was successfully deleted.", deletedId);
    }

    @Test
    @Transactional
    void scheduleSearching() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("schedule searching test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        scheduleRepository.save(scheduleEntity);
//TODO:
//        List<ScheduleDto> dateList = scheduleService.getSchedulesByDateRange(LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
//        assertFalse(dateList.isEmpty());
//        dateList.forEach(schedule -> log.info("Found Date Schedule: {}", schedule));
    }

    @Test
    @Transactional
    void scheduleExistsById() {
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setTitle("schedule exists test");
        scheduleEntity.setStartAt(LocalDateTime.now());
        scheduleEntity.setEndAt(LocalDateTime.now().plusDays(8));

        scheduleRepository.save(scheduleEntity);

        assertFalse(!scheduleService.existsById(scheduleEntity.getId()), "Not Found");
        log.info("Found Schedule: {}", scheduleEntity);
    }

}
