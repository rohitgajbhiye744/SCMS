package com.scms.backend.repository;

import com.scms.backend.entity.LocationQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationQRRepository extends JpaRepository<LocationQR, Long> {
    Optional<LocationQR> findByQrCodeUuid(String qrCodeUuid);
}
