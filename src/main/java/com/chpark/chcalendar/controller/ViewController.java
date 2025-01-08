package com.chpark.chcalendar.controller;

import com.chpark.chcalendar.dto.UserDto;
import com.chpark.chcalendar.service.RedisService;
import com.chpark.chcalendar.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final UserService userService;
    private final RedisService redisService;

    @GetMapping("/auth/login")
    public String login() {
        return "login";
    }

    @GetMapping("/auth/register")
    public String register(Model model) {
        model.addAttribute("userRequest", new UserDto.RegisterRequest());
        return "register";
    }

    @PostMapping("/auth/register")
    public String createUser(@Validated @ModelAttribute UserDto.RegisterRequest userRequest,
                             RedirectAttributes redirectAttributes, Model model) {
        try {
            redisService.verificationEmail(userRequest.getEmail(), userRequest.getEmailCode());
            userService.create(userRequest);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("userRequest", userRequest);
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
        return "redirect:/auth/login";
    }

    @GetMapping("/user/profile")
    public String userProfile() {
        return "userProfile";
    }
}


//async function getCalendarList(category, listElementId) {
//    try {
//        // 카테고리를 쿼리 파라미터로 전달
//                const response = await fetch(`/calendars?category=${category}`, {
//            method: 'GET',
//                    credentials: 'include',
//                    headers: {
//                'Content-Type': 'application/json',
//            }
//        });
//
//        if (response.ok) {
//                    const calendars = await response.json();
//            console.log(`Calendars for category ${category}:`, calendars);
//
//            // 특정 리스트 요소에 캘린더 렌더링
//            renderCalendars(calendars, listElementId);
//        } else {
//            console.error(`Failed to fetch calendars for category ${category}`);
//        }
//    } catch (error) {
//        console.error(`Error fetching calendars for category ${category}:`, error);
//    }
//}
//
//function renderCalendars(calendars, listElementId) {
//            const calendarListElement = document.getElementById(listElementId);
//    calendarListElement.innerHTML = ''; // 초기화
//
//    calendars.forEach(calendar => {
//            addCalendarToList(calendar, listElementId); // 단일 캘린더 추가
//            });
//}
//
//function addCalendarToList(calendar, listElementId) {
//            const calendarListElement = document.getElementById(listElementId);
//
//            const listItem = document.createElement('li');
//    listItem.innerHTML = `
//                <label>
//                    <input type="checkbox" checked data-id="${calendar.id}" class="calendar-checkbox" />
//                    <span style="color: gray">${calendar.title}</span>
//                </label>
//            `;
//
//    calendarListElement.appendChild(listItem);
//
//    // 중복 방지: 추가 전에 체크 상태 확인
//    if (!checkedState.has(calendar.id)) {
//        checkedState.add(calendar.id);
//    }
//
//            const checkbox = listItem.querySelector('.calendar-checkbox');
//    checkbox.addEventListener('change', (event) => handleCalendarToggle(event, calendar.id));
//}
//
//
//function handleCalendarToggle(event, calendarId) {
//    if (event.target.checked) {
//        checkedState.add(calendarId); // 체크 상태 추가
//    } else {
//        checkedState.delete(calendarId); // 체크 상태 제거
//    }
//
//    // `checkedState` 업데이트 후 바로 FullCalendar 이벤트 다시 로드
//    calendar.refetchEvents();
//}
//
//
//
//function getSelectedCalendarIds() {
//    return Array.from(checkedState); // 선택된 ID 반환
//}


