package com.balu.application_tracking_system.repository;

import com.balu.application_tracking_system.entity.Job;
import com.balu.application_tracking_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(Job.JobStatus status);

    List<Job> findByPostedById(Long userId);

    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);
}
