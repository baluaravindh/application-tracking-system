package com.balu.application_tracking_system.repository;

import com.balu.application_tracking_system.dto.ApplicationResponseDTO;
import com.balu.application_tracking_system.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobId(Long jobId);
}
