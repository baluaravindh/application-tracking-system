package com.balu.application_tracking_system.dto;

import com.balu.application_tracking_system.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String location;
    private String department;
    private String jobType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    private String status; // OPEN, CLOSED, DRAFT
    private Integer experienceRequired;
    private String skills;
    private Double minSalary;
    private Double maxSalary;
    private String postedByName;
    private String postedByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
