package com.peraton.cicd.controller;

import com.peraton.cicd.model.Pipeline;
import com.peraton.cicd.service.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    @GetMapping
    public ResponseEntity<List<Pipeline>> getAllPipelines() {
        List<Pipeline> pipelines = pipelineService.getAllPipelines();
        return ResponseEntity.ok(pipelines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pipeline> getPipelineById(@PathVariable Long id) {
        return pipelineService.getPipelineById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Pipeline> getPipelineByName(@PathVariable String name) {
        return pipelineService.getPipelineByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Pipeline>> getPipelinesByStatus(@PathVariable Pipeline.PipelineStatus status) {
        List<Pipeline> pipelines = pipelineService.getPipelinesByStatus(status);
        return ResponseEntity.ok(pipelines);
    }

    @PostMapping
    public ResponseEntity<Pipeline> createPipeline(@RequestBody Pipeline pipeline) {
        Pipeline createdPipeline = pipelineService.createPipeline(pipeline);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPipeline);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pipeline> updatePipeline(@PathVariable Long id, @RequestBody Pipeline pipeline) {
        try {
            Pipeline updatedPipeline = pipelineService.updatePipeline(id, pipeline);
            return ResponseEntity.ok(updatedPipeline);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePipeline(@PathVariable Long id) {
        pipelineService.deletePipeline(id);
        return ResponseEntity.noContent().build();
    }
}
