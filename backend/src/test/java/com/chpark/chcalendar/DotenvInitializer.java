package com.chpark.chcalendar;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // 프로젝트 루트의 .env 파일을 읽어옵니다.
            List<String> lines = Files.readAllLines(Paths.get(".env"));
            Map<String, Object> props = new HashMap<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    props.put(parts[0].trim(), parts[1].trim());
                }
            }
            // "dotenv"라는 이름으로 프로퍼티 소스를 추가합니다.
            applicationContext.getEnvironment().getPropertySources()
                    .addFirst(new MapPropertySource("dotenv", props));
        } catch (Exception e) {
            // .env 파일이 없으면 예외를 무시합니다.
            e.printStackTrace();
        }
    }
}
