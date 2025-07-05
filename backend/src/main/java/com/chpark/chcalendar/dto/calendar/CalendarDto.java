package com.chpark.chcalendar.dto.calendar;

import com.chpark.chcalendar.enumClass.CalendarCategory;
import com.chpark.chcalendar.enumClass.FileAuthority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDto {

    @Size(min = 1, max = 20)
    private String title;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request extends CalendarDto {

        @NotNull
        private CalendarCategory category;
    }

    @Getter
    @NoArgsConstructor
    public static class Response extends CalendarDto {

        private long id;
        private String color;
        private CalendarCategory category;
        private Boolean checked;
        private FileAuthority fileAuthority;

        public Response(String title, long id, String color, CalendarCategory category, Boolean checked) {
            super(title);
            this.id = id;
            this.color = color;
            this.category = category;
            this.checked = checked;
            this.fileAuthority = null;
        }

        @Builder
        public Response(String title, long id, String color, CalendarCategory category, Boolean checked, String fileAuthority) {
            super(title);
            this.id = id;
            this.color = color;
            this.category = category;
            this.checked = checked;
            this.fileAuthority = FileAuthority.parseFileAuthority(fileAuthority);
        }
    }
}
