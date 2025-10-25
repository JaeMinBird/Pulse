package com.peraton.cicd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncRequest {

    @NotBlank(message = "Repository owner is required")
    private String owner;

    @NotBlank(message = "Repository name is required")
    private String repo;

    @NotNull(message = "Repository ID is required")
    private Long repositoryId;
}
