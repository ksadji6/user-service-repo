package com.esmt.user.repository;

import com.esmt.user.entity.MobilityPass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MobilityPassRepository extends JpaRepository<MobilityPass, Long> {
    Optional<MobilityPass> findByPassNumber(String passNumber);
    Optional<MobilityPass> findByUserId(Long userId);
    boolean existsByPassNumber(String passNumber);
}