package com.balu.application_tracking_system.service;

import com.balu.application_tracking_system.dto.ApplicationNotificationDTO;
import com.balu.application_tracking_system.dto.ApplicationRequestDTO;
import com.balu.application_tracking_system.dto.ApplicationResponseDTO;
import com.balu.application_tracking_system.entity.Application;
import com.balu.application_tracking_system.entity.Job;
import com.balu.application_tracking_system.entity.User;
import com.balu.application_tracking_system.exception.ResourceNotFoundException;
import com.balu.application_tracking_system.messaging.NotificationPublisher;
import com.balu.application_tracking_system.repository.ApplicationRepository;
import com.balu.application_tracking_system.repository.JobRepository;
import com.balu.application_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final NotificationPublisher notificationPublisher;
    private final ResumeService resumeService;

    // applyForJob (CANDIDATE only):
    public ApplicationResponseDTO applyForJob(
            Long jobId,
            String coverLetter,
            MultipartFile resumeFile) throws IOException {

        // 1. Get logged in candidate email from SecurityContext
        String candidateEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find candidate by email — throw if not found
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate candidate role — throw if not CANDIDATE
        if (user.getRole() != User.Role.CANDIDATE) {
            throw new RuntimeException("You are not allowed to apply for this application");
        }

        // 4. Find job by jobId — throw if not found
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // 5. Check job status is OPEN — throw if CLOSED or DRAFT
        if (job.getStatus() == Job.JobStatus.DRAFT) {
            throw new RuntimeException("Sorry! this job is not open yet.");
        }

        if (job.getStatus() == Job.JobStatus.CLOSED) {
            throw new RuntimeException("Sorry! this job is closed. Cannot apply.");
        }

        // 6. Check candidate hasn't already applied — throw if duplicate
        boolean alreadyApplied = applicationRepository.existsByCandidateIdAndJobId(user.getId(), jobId);

        if (alreadyApplied) {
            throw new RuntimeException("You have already applied for this job.");
        }

        // 7. Create Application — set candidate, job, coverLetter, status=APPLIED
        Application application = new Application();
        application.setCandidate(user);
        application.setJob(job);
        application.setCoverLetter(coverLetter);
        application.setStatus(Application.ApplicationStatus.APPLIED);

        // Extract resume text and calculate score
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String resumeText = resumeService.extractTextFromPdf(resumeFile);
            int score = resumeService.calculateMatchScore(resumeText, job.getSkills());
            application.setMatchScore(score);
            application.setResumeUrl(resumeFile.getOriginalFilename());
        }

        // 8. Save and return DTO
        Application saved = applicationRepository.save(application);

        ApplicationNotificationDTO notification = new ApplicationNotificationDTO(
                saved.getId(),
                saved.getCandidate().getFullName(),
                saved.getCandidate().getEmail(),
                saved.getJob().getTitle(),
                saved.getStatus().name(),
                saved.getAppliedAt()
        );

        notificationPublisher.publishApplicationNotification(notification);
        return mapToDto(saved);
    }

    // getMyApplications (CANDIDATE only):
    public List<ApplicationResponseDTO> getMyApplications() {

        // 1. Get logged in candidate email from SecurityContext
        String candidateEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find candidate by email — throw if not found
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate candidate role — throw if not CANDIDATE
        if (user.getRole() != User.Role.CANDIDATE) {
            throw new RuntimeException("Only CANDIDATES will only access to view the applications.");
        }

        // 4. Fetch all applications by candidateId
        //  applicationRepository.findByCandidateId(user.getId());

        // 5. Map to DTO list and return
        return applicationRepository.findByCandidateId(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // getApplicationsByJob (HR only):
    public List<ApplicationResponseDTO> getApplicationsByJob(Long jobId) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR user by email — throw if not found
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate HR role — throw if not HR
        if (user.getRole() != User.Role.HR) {
            throw new RuntimeException("You are not allowed to view this application.");
        }

        // 4. Find job by jobId — throw if not found
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // 5. Fetch all applications for that jobId
        //  applicationRepository.findByJobId(jobId);

        // 6. Map to DTO list and return
        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // updateApplicationStatus (HR only):
    public ApplicationResponseDTO updateApplicationStatus(Long id, String status) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR user by email — throw if not found
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate HR role — throw if not HR
        if (user.getRole() != User.Role.HR) {
            throw new RuntimeException("You are not allowed to view this application.");
        }

        // 4. Find application by id — throw if not found
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        // 5. Check current status is not WITHDRAWN or REJECTED — throw if so
        if (application.getStatus() == Application.ApplicationStatus.WITHDRAWN) {
            throw new RuntimeException("Sorry! this application is withdrawn so not able to update.");
        }
        if (application.getStatus() == Application.ApplicationStatus.REJECTED) {
            throw new RuntimeException("Sorry! this application is rejected so not able to update.");
        }

        // 6. Update status from parameter
        application.setStatus(Application.ApplicationStatus.valueOf(status.toUpperCase()));

        // 7. Save and return DTO
        Application updated = applicationRepository.save(application);
        return mapToDto(updated);
    }

    // withdrawApplication (CANDIDATE only):
    public ApplicationResponseDTO withdrawApplication(Long id) {

        // 1. Get logged in candidate email from SecurityContext
        String candidateEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find candidate by email — throw if not found
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate candidate role — throw if not CANDIDATE
        if (user.getRole() != User.Role.CANDIDATE) {
            throw new RuntimeException("You are not allowed to view this application.");
        }

        // 4. Find application by id — throw if not found
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        // 5. Validate this application belongs to this candidate — throw if application.candidate.id != candidate.id
        if (!application.getCandidate().getId().equals(user.getId())) {
            throw new RuntimeException("Sorry! this application is not belongs to this candidate.");
        }

        // 6. Check status is not already WITHDRAWN — throw if so
        if (application.getStatus() == Application.ApplicationStatus.WITHDRAWN) {
            throw new RuntimeException("Sorry! this application is already withdrawn.");
        }

        // 7. Set status to WITHDRAWN
        application.setStatus(Application.ApplicationStatus.WITHDRAWN);

        // 8. Save and return DTO
        Application withdrawnApplication = applicationRepository.save(application);
        return mapToDto(withdrawnApplication);
    }

    // HR views applications ranked by match score
    public List<ApplicationResponseDTO> getApplicationsByJobRanked(Long jobId) {
        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(this::mapToDto)
                .sorted(Comparator.comparingInt(
                        ApplicationResponseDTO::getMatchScore)
                        .reversed())
                .collect(Collectors.toList());
    }


    //---MAPPER---
    private ApplicationResponseDTO mapToDto(Application application) {
        return new ApplicationResponseDTO(
                application.getId(),
                application.getCandidate().getId(),
                application.getCandidate().getFullName(),
                application.getCandidate().getEmail(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getDescription(),
                application.getJob().getSkills(),
                application.getStatus().name(),
                application.getCoverLetter(),
                application.getResumeUrl(),
                application.getMatchScore(),
                application.getAppliedAt()
        );
    }

}
