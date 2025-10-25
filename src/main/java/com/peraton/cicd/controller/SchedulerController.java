package com.peraton.cicd.controller;

import com.peraton.cicd.service.ScheduledTasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerController {

    private final ScheduledTasksService scheduledTasksService;

    /**
     * Manually trigger GitHub sync
     * POST /api/scheduler/trigger-sync
     */
    @PostMapping("/trigger-sync")
    public ResponseEntity<Map<String, String>> triggerSync() {
        log.info("Manual sync triggered via API");
        try {
            scheduledTasksService.triggerManualSync();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "GitHub sync triggered successfully. Check logs for details."
            ));
        } catch (Exception e) {
            log.error("Error triggering manual sync: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to trigger sync: " + e.getMessage()
            ));
        }
    }
}
