package com.balu.application_tracking_system.repository;

import com.balu.application_tracking_system.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);
}
