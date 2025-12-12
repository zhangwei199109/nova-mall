package com.example.user.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "用户数据传输对象")
@Data
public class UserDTO {

    @Schema(description = "用户ID", example = "1")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "用户名", example = "张三")
    @JsonProperty("username")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @JsonProperty("email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "年龄", example = "25")
    @JsonProperty("age")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄必须小于150")
    private Integer age;

    public UserDTO() {
    }

    public UserDTO(Long id, String username, String email, Integer age) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.age = age;
    }
}



