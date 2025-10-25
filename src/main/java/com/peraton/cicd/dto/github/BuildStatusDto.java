package com.peraton.cicd.dto.github;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildStatusDto {

    private Long runId;
    private String repositoryName;
    private String branch;
    private String commitSha;
    private String status;
    private String conclusion;
    private Integer runNumber;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    private String htmlUrl;
    private String commitMessage;
    private String authorName;
}
