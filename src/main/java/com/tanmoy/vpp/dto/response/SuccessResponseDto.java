package com.tanmoy.vpp.dto.response;

public class SuccessResponseDto {

    private String message;

    public SuccessResponseDto(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
