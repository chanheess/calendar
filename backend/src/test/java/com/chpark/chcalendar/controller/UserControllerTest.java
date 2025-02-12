package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.security.JwtAuthenticationFilter;
import com.chpark.chcalendar.security.JwtTokenProvider;
import com.chpark.chcalendar.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() throws Exception {
        // Mocking JWT 필터 설정
        Mockito.doNothing().when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        // Mocking JWT Token Provider 설정
        when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class)))
                .thenReturn("mockedToken");
        when(jwtTokenProvider.getUserIdFromToken("mockedToken"))
                .thenReturn(1234);

        // MockMvc 설정
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(username = "testUser")
    void updatePassword() throws Exception {
        // given
        UserDto.ChangePassword changePasswordDto = new UserDto.ChangePassword("currPassword", "newPassword");

        // when & then
        mockMvc.perform(patch("/api/user/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully."));
    }

    @Test
    @WithMockUser(username = "testUser")
    void updatePassword_wrongPassword() throws Exception {
        // given
        UserDto.ChangePassword changePasswordDto = new UserDto.ChangePassword("wrongPassword", "newPassword");

        doThrow(new IllegalArgumentException("Incorrect password"))
                .when(userService).updatePassword(anyLong(), any(UserDto.ChangePassword.class));

        // when & then
        mockMvc.perform(patch("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect password"))
                .andDo(print());
    }

}