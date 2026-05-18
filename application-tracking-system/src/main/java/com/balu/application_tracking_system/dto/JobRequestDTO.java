package com.balu.application_tracking_system.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobRequestDTO {

    @NotBlank(message = "Job title is required.")
    @Size(min = 4, message = "Title must be 4 to 100 characters.")
    private String title;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Location is required.")
    private String location;

    @NotBlank(message = "Department is required.")
    private String department;

    @NotBlank(message = "Job type is required.")
    private String jobType;

    @NotNull(message = "Experience years is required.")
    private Integer experienceRequired;

    @NotBlank(message = "Skills is required.")
    private String skills;

    @NotNull(message = "Minimum salary is required.")
    private Double minSalary;

    @NotNull(message = "Maximum salary is required.")
    private Double maxSalary;
}
