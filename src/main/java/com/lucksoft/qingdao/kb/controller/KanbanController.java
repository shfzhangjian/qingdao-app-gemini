package com.lucksoft.qingdao.kb.controller;

import com.lucksoft.qingdao.kb.dto.CompleteRequest;
import com.lucksoft.qingdao.kb.dto.ScoreRequest;
import com.lucksoft.qingdao.kb.dto.Task;
import com.lucksoft.qingdao.kb.dto.TaskQuery;
import com.lucksoft.qingdao.kb.service.KanbanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kb")
public class KanbanController {

    private static final Logger log = LoggerFactory.getLogger(KanbanController.class);
    private final KanbanService kanbanService;

    public KanbanController(KanbanService kanbanService) {
        this.kanbanService = kanbanService;
    }

    @GetMapping("/machines")
    public ResponseEntity<List<String>> getMachines() {
        log.info("Request received for getMachines");
        return ResponseEntity.ok(kanbanService.getMachines());
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getTasks(TaskQuery query) {
        log.info("Request received for getTasks with query: {}", query);
        if ("history".equals(query.getView())) {
            return ResponseEntity.ok(kanbanService.getHistoryTasks(query.getMachine()));
        }
        return ResponseEntity.ok(kanbanService.getTasks(query));
    }

    @PostMapping("/tasks/batch-complete")
    public ResponseEntity<Map<String, Object>> batchCompleteTasks(@RequestBody CompleteRequest request, @RequestParam String machine) {
        log.info("Request received for batchCompleteTasks on machine {}: {}", machine, request.getTaskIds());
        long count = kanbanService.batchCompleteTasks(machine, request.getTaskIds());
        return ResponseEntity.ok(Collections.singletonMap("message", "Successfully completed " + count + " tasks."));
    }

    @PostMapping("/tasks/{id}/abnormal")
    public ResponseEntity<Map<String, Object>> markAsAbnormal(@PathVariable String id, @RequestBody CompleteRequest request, @RequestParam String machine) {
        log.info("Request received to mark task {} as abnormal on machine {}: {}", id, machine, request.getAbnormalReason());
        boolean success = kanbanService.completeTask(machine, id, true, request.getAbnormalReason());
        if (success) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Task " + id + " marked as abnormal."));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/tasks/batch-score")
    public ResponseEntity<Map<String, Object>> batchScoreTasks(@RequestBody ScoreRequest request, @RequestParam String machine) {
        log.info("Request received for batchScoreTasks on machine {} by checker {}: {}", machine, request.getChecker(), request.getScores());
        long count = kanbanService.batchScoreTasks(machine, request.getScores(), request.getChecker());
        return ResponseEntity.ok(Collections.singletonMap("message", "Successfully scored " + count + " tasks."));
    }

    @PutMapping("/tasks/{id}/score")
    public ResponseEntity<Map<String, Object>> updateScore(@PathVariable String id, @RequestBody Map<String, Integer> payload, @RequestParam String machine) {
        int newScore = payload.get("score");
        log.info("Request to update score for task {} on machine {} to {}", id, machine, newScore);
        boolean success = kanbanService.updateScore(machine, id, newScore);
        if(success) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Score updated successfully."));
        }
        return ResponseEntity.status(404).body(Collections.singletonMap("error", "Task not found or not scored yet."));
    }
}
