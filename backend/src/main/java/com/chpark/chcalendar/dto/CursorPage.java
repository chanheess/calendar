package com.chpark.chcalendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CursorPage<T> {
    private List<T> content;
    private String nextCursor;
}
