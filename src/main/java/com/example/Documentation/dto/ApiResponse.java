package com.example.Documentation.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class ApiResponse {
    private String message;
    private boolean success;
    private Object object;


    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}
