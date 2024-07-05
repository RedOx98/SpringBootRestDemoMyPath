package com.olahammed.SpringRestDemo.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthoritiesDTO {

    @NotBlank(message = "can not be blank")
    @Schema(description = "Authorities", example = "USER", requiredMode = RequiredMode.REQUIRED)
    private String authorities;
}
