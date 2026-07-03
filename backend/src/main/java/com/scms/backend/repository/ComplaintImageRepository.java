package com.scms.backend.repository;

import com.scms.backend.entity.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintImageRepository extends JpaRepository<ComplaintImage, Long> {
}
