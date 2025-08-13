package com.chpark.chcalendar.service.user;

import com.chpark.chcalendar.DotenvInitializer;
import com.chpark.chcalendar.dto.user.UserDto;
import com.chpark.chcalendar.entity.UserEntity;
import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.repository.user.UserRepository;
import com.chpark.chcalendar.service.calendar.UserCalendarService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

@Disabled
@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
public class UserServiceJMeterTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserCalendarService userCalendarService;

    String password = "qwer1234!";

    @Test
    void createUser() {
        int count = 10000;
        UserDto.RegisterRequest userDto = new UserDto.RegisterRequest("","","","", "");
        for (int i = 0; i < count; i++) {
            String randomId = getRandom(4) + i;

            userDto.setEmail("test" + randomId + "@gmail.com");
            userDto.setPassword(password);
            userDto.setNickname("tester" + randomId);

            userService.create(userDto);
        }
    }

    @Test
    void 유저생성() {
        UserDto.RegisterRequest requestUser  = new UserDto.RegisterRequest(
                "test1",
                "qwer",
                "test1",
                "",
                "local"
        );

        UserEntity user = userRepository.save(UserEntity.createWithEncodedPassword(requestUser, passwordEncoder));

        //기본 캘린더 생성
        userCalendarService.create(user.getId(), "내 캘린더");
    }

    @Test
    void generateUserCsvForJMeter() throws IOException {
        String outputFile = "/Users/chpark/users.csv";
        int userCount = 1000;

        List<UserEntity> userList = userRepository.findByNicknameContaining("tester");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("email,password");
            writer.newLine();

            for (int i = 0; i <= userCount; i++) {
                if (userList.size() <= i) {
                    break;
                }

                String email = userList.get(i).getEmail();
                String password = this.password;    //평문으로 넣어야 테스트 가능

                writer.write(email + "," + password);
                writer.newLine();
            }
        }

        System.out.println("users.csv 파일 생성 완료: " + outputFile);
    }

    public static String getRandom(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
