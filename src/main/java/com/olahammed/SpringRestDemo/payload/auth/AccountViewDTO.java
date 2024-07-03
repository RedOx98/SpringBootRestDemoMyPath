package com.olahammed.SpringRestDemo.payload.auth;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountViewDTO {
    
    private Long id;

    private String email;

    private String role;
}
