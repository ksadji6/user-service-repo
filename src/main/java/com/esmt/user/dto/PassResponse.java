package com.esmt.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PassResponse {
    private Long id;
    private String passNumber;
    private String passType;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDate expiresAt;
    private boolean valid;
}
