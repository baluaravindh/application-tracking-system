package com.balu.application_tracking_system.service;

import com.balu.application_tracking_system.dto.InterviewNotificationDTO;
import com.balu.application_tracking_system.dto.InterviewRequestDTO;
import com.balu.application_tracking_system.dto.InterviewResponseDTO;
import com.balu.application_tracking_system.entity.Application;
import com.balu.application_tracking_system.entity.Interview;
import com.balu.application_tracking_system.entity.User;
import com.balu.application_tracking_system.exception.ResourceNotFoundException;
import com.balu.application_tracking_system.messaging.NotificationPublisher;
import com.balu.application_tracking_system.repository.ApplicationRepository;
import com.balu.application_tracking_system.repository.InterviewRepository;
import com.balu.application_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationPublisher notificationPublisher;

    // scheduleInterview (HR only):
    public InterviewResponseDTO scheduleInterview(Long applicationId, InterviewRequestDTO dto) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR by email — throw if not found
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate HR role — throw if not HR
        if (user.getRole() != User.Role.HR) {
            throw new RuntimeException("You are not allowed to schedule interviews. Only HR can schedule interviews.");
        }

        // 4. Find application by applicationId — throw if not found
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // 5. Check application status is INTERVIEW — throw if not
        //    (only candidates in INTERVIEW stage get scheduled)
        if (application.getStatus() != Application.ApplicationStatus.INTERVIEW) {
            throw new RuntimeException("Only candidates in INTERVIEW stage get scheduled.");
        }

        // 6. Check interview not already scheduled for this application
        //    — throw if already exists
        if (interviewRepository.existsByApplicationId(applicationId)) {
            throw new ResourceNotFoundException("Interview already scheduled for this application.");
        }

        // 7. Create Interview — set application, scheduledAt, meetingLink,
        //    venue, durationMinutes, notes, status=SCHEDULED
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setScheduledAt(dto.getScheduledAt());
        interview.setMeetingLink(dto.getMeetingLink());
        interview.setVenue(dto.getVenue());
        interview.setDurationMinutes(dto.getDurationMinutes());
        interview.setNotes(dto.getNotes());
        interview.setStatus(Interview.InterviewStatus.SCHEDULED);

        // 8. Save and return DTO
        Interview saved = interviewRepository.save(interview);

        InterviewNotificationDTO notification = new InterviewNotificationDTO(
                saved.getId(),
                saved.getApplication().getId(),
                saved.getApplication().getCandidate().getFullName(),
                saved.getApplication().getCandidate().getEmail(),
                saved.getApplication().getJob().getTitle(),
                saved.getScheduledAt(),
                saved.getMeetingLink(),
                saved.getVenue(),
                saved.getStatus().name()
        );

        notificationPublisher.publishInterviewNotification(notification);
        return mapToDto(saved);
    }

    // getInterviewByApplication (HR only):
    public InterviewResponseDTO getInterviewByApplication(Long applicationId) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR by email — throw if not found
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate HR role — throw if not HR
        if (user.getRole() != User.Role.HR) {
            throw new RuntimeException("You are not allowed to view the application. Only HR can.");
        }

        // 4. Find application by applicationId — throw if not found
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // 5. Find interview by applicationId — throw if not found
        Interview interview = interviewRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found for this application."));

        // 6. Return mapped DTO
        return mapToDto(interview);
    }

    // rescheduleInterview (HR only):
    public InterviewResponseDTO rescheduleInterview(Long interviewId, InterviewRequestDTO dto) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR by email — throw if not found
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate HR role — throw if not HR
        if (user.getRole() != User.Role.HR) {
            throw new RuntimeException("You are not allowed to view the application. Only HR can.");
        }

        // 4. Find interview by id — throw if not found
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found."));

        // 5. Check interview status is not CANCELLED or COMPLETED
        //    — throw if so (cannot reschedule cancelled/completed)
        if (interview.getStatus() == Interview.InterviewStatus.CANCELLED) {
            throw new RuntimeException("Sorry! this interview is cancelled. Only scheduled interviews can reschedule.");
        }

        if (interview.getStatus() == Interview.InterviewStatus.COMPLETED) {
            throw new RuntimeException("Sorry! this interview is completed. Only scheduled interviews can reschedule.");
        }

        // 6. Update scheduledAt, meetingLink, venue, durationMinute from request DTO
        interview.setScheduledAt(dto.getScheduledAt());
        interview.setMeetingLink(dto.getMeetingLink());
        interview.setVenue(dto.getVenue());
        interview.setDurationMinutes(dto.getDurationMinutes());

        // 7. Set status to RESCHEDULED
        interview.setStatus(Interview.InterviewStatus.RESCHEDULED);

        // 8. Save and return DTO
        Interview reScheduled = interviewRepository.save(interview);
        return mapToDto(reScheduled);
    }

    // updateInterviewStatus (HR only):
    public InterviewResponseDTO updateInterviewStatus(Long interviewId, String status) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR by email — throw if not found
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate HR role — throw if not HR
        if (user.getRole() != User.Role.HR) {
            throw new RuntimeException("You are not allowed to view the application. Only HR can.");
        }

        // 4. Find interview by id — throw if not found
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found."));

        // 5. Check status is not already CANCELLED or COMPLETED
        //    — throw if so (cannot change final status)
        if (interview.getStatus() == Interview.InterviewStatus.CANCELLED) {
            throw new RuntimeException("Sorry! this interview is cancelled. Cannot change interview status.");
        }

        if (interview.getStatus() == Interview.InterviewStatus.COMPLETED) {
            throw new RuntimeException("Sorry! this interview is completed. Cannot change interview status.");
        }

        // 6. Update status from request parameter
        interview.setStatus(Interview.InterviewStatus.valueOf(status));

        // 7. Save and return DTO
        Interview updated = interviewRepository.save(interview);
        return mapToDto(updated);
    }

    // cancelInterview (HR only):
    public InterviewResponseDTO cancelInterview(Long interviewId) {

        // 1. Get logged in HR email from SecurityContext
        String hrEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2. Find HR by email — throw if not found
        User user = userRepository.findByEmail(hrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Validate HR role — throw if not HR
        if (user.getRole() != User.Role.HR) {
            throw new RuntimeException("You are not allowed to view the application. Only HR can.");
        }

        // 4. Find interview by id — throw if not found
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found."));

        // 5. Check status is not already CANCELLED
        //    — throw "Interview already cancelled"
        if (interview.getStatus() == Interview.InterviewStatus.CANCELLED) {
            throw new RuntimeException("Sorry! this interview is already cancelled. " +
                    "Cannot change interview status to CANCELLED.");
        }

        // 6. Set status to CANCELLED
        interview.setStatus(Interview.InterviewStatus.CANCELLED);

        // 7. Save and return DTO
        Interview cancelled = interviewRepository.save(interview);
        return mapToDto(cancelled);
    }

    //---MAPPER---
    private InterviewResponseDTO mapToDto(Interview interview) {
        return new InterviewResponseDTO(
                interview.getId(),
                interview.getApplication().getId(),
                interview.getScheduledAt(),
                interview.getMeetingLink(),
                interview.getVenue(),
                interview.getDurationMinutes(),
                interview.getNotes(),
                interview.getStatus().name(),
                interview.getCreatedAt(),
                interview.getUpdatedAt()
        );
    }

}
