package com.example.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String account; // username/email/mobile 皆可

    @NotBlank
    private String code;

    @NotBlank
    @Size(min = 6, max = 32)
    private String newPassword;
}




