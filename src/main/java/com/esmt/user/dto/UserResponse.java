package com.esmt.user.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String uuid;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String status;
    private int totalTrips;
    private PassResponse mobilityPass;
}

