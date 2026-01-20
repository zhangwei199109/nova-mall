package com.example.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendResetCodeRequest {
    @NotBlank
    private String account; // username/email/mobile 皆可
}






