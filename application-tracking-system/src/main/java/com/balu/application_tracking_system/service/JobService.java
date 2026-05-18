package com.balu.application_tracking_system.service;

import com.balu.application_tracking_system.dto.JobRequestDTO;
import com.balu.application_tracking_system.dto.JobResponseDTO;
import com.balu.application_tracking_system.dto.PagedResponseDTO;
import com.balu.application_tracking_system.entity.Job;
import com.balu.application_tracking_system.entity.User;
import com.balu.application_tracking_system.exception.ResourceNotFoundException;
import com.balu.application_tracking_system.repository.JobRepository;
import com.balu.application_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    // postJob (HR only):
    public JobResponseDTO postJob(JobRequestDTO dto) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR user by email
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("HR not found with email: " + hrEmail));

        // 3. Validate HR role
        if (user.getRole() != User.Role.HR) {
            throw new ResourceNotFoundException("Only HR role will post job.");
        }

        // 4. Create Job — set all fields + postedBy = HR user
        //  User hrUser = new User();
        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setDepartment(dto.getDepartment());
        job.setJobType(Job.JobType.valueOf(dto.getJobType()));
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setSkills(dto.getSkills());
        job.setMinSalary(dto.getMinSalary());
        job.setMaxSalary(dto.getMaxSalary());
        job.setPostedBy(user);

        // 5. Set status as OPEN by default
        job.setStatus(Job.JobStatus.OPEN);

        // 6. Save and return DTO
        Job savedJob = jobRepository.save(job);
        return mapToDto(savedJob);
    }

    // getAllJobs (paginated):
//    public List<JobResponseDTO> getAllJobs() {
//        // 1. Build pageable with page, size, sort
//
//        // 3. Map to DTO and return paged response
//        return jobRepository.findAll()
//                .stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }

    public JobResponseDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return mapToDto(job);
    }

    // updateJobStatus (HR/Admin):
    public JobResponseDTO updateJob(Long id, JobRequestDTO dto) {

        // 1. Find job by id — throw if not found
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        // 2. Validate new status is valid
        if (job.getStatus() == Job.JobStatus.CLOSED) {
            throw new RuntimeException("Job is already closed. We cannot update job status.");
        }

        // 3. Update status
//        job.setStatus(Job.JobStatus.valueOf(job.getStatus().name().toUpperCase()));
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setDepartment(dto.getDepartment());
        job.setJobType(Job.JobType.valueOf(dto.getJobType()));
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setSkills(dto.getSkills());
        job.setMinSalary(dto.getMinSalary());
        job.setMaxSalary(dto.getMaxSalary());

        // 4. Save and return
        Job updatedJob = jobRepository.save(job);
        return mapToDto(updatedJob);
    }

    public JobResponseDTO updateJobStatus(String email, Long jobId, String status) {

        // 1. Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Validate role is HR or ADMIN
        if (user.getRole() != User.Role.HR && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only HR or Admin can update job status");
        }

        // 3. Find job by id
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // 4. Validate current status
        if (job.getStatus() == Job.JobStatus.CLOSED) {
            throw new RuntimeException("Job is already closed.");
        }

        // 5. Update status from parameter
        job.setStatus(Job.JobStatus.valueOf(status.toUpperCase()));

        // 6. Save and return
        Job updated = jobRepository.save(job);
        return mapToDto(updated);
    }

    public void deleteJob(Long id) {
        // 1. Find job — throw if not found
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        // 2. Delete
        jobRepository.delete(job);
    }

    // Update service method
    public PagedResponseDTO<JobResponseDTO> getAllJobs(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<Job> jobPage = jobRepository.findByStatus(Job.JobStatus.OPEN, pageable);
        List<JobResponseDTO> content = jobPage.getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return new PagedResponseDTO<>(
                content,
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements(),
                jobPage.getTotalPages(),
                jobPage.isLast()
        );
    }

    private JobResponseDTO mapToDto(Job job) {
        return new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getDepartment(),
                job.getJobType().name(),
                job.getStatus().name(),
                job.getExperienceRequired(),
                job.getSkills(),
                job.getMinSalary(),
                job.getMaxSalary(),
                job.getPostedBy().getFullName(),
                job.getPostedBy().getEmail(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

}
