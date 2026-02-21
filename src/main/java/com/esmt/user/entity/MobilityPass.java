package com.esmt.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobility_passes")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MobilityPass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pass_number", unique = true, nullable = false)
    private String passNumber;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_type")
    private PassType passType;

    @Enumerated(EnumType.STRING)
    private PassStatus status;

    @Column(name = "issued_at", updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @PrePersist
    public void prePersist() {
        this.issuedAt = LocalDateTime.now();
        if (this.status == null) this.status = PassStatus.ACTIVE;
        if (this.passType == null) this.passType = PassType.STANDARD;
    }

    public boolean isValid() {
        return status == PassStatus.ACTIVE &&
                (expiresAt == null || expiresAt.isAfter(LocalDate.now()));
    }
}