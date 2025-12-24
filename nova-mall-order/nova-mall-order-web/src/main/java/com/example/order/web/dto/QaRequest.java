package com.example.order.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QaRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}


