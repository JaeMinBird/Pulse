package com.peraton.cicd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {

    private boolean success;
    private int syncedCount;
    private String message;
    private String repositoryName;
}
