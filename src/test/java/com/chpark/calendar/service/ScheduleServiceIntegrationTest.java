package com.chpark.calendar.service;

import com.chpark.calendar.repository.schedule.ScheduleRepository;
import com.chpark.calendar.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableTransactionManagement
@Slf4j
@AutoConfigureMockMvc
public class ScheduleServiceIntegrationTest {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    private String loginCookie;
    private long userId;

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


    //통합테스트로 진행
    //updateRepeatSchedule
    //updateRepeatCurrentOnlySchedule
    //updateRepeatCurrentAndFutureSchedules
    //deleteFutureRepeatSchedules

}
