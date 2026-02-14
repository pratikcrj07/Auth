package com.auth.Dto;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    private String phone;
    private String otp;
}
