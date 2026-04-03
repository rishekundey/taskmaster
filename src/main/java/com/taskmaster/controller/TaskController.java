package com.taskmaster.controller;

import com.taskmaster.dto.request.TaskRequest;
import com.taskmaster.dto.response.ApiResponse;
import com.taskmaster.dto.response.TaskResponse;
import com.taskmaster.entity.Task;
import com.taskmaster.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tasks", description = "Task CRUD, filtering, sorting, search, and assignment")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create a new task")
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created",
                        taskService.createTask(userDetails.getUsername(), request)));
    }

    @Operation(summary = "Get a single task by ID")
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task found", taskService.getTaskById(taskId)));
    }

    @Operation(summary = "Get my assigned tasks with optional status filter and sorting")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success("My tasks",
                taskService.getMyTasks(userDetails.getUsername(), status,
                        PageRequest.of(page, size, sort))));
    }

    @Operation(summary = "Get all tasks for a team with optional status filter")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTeamTasks(
            @PathVariable Long teamId,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Team tasks",
                taskService.getTeamTasks(teamId, status, PageRequest.of(page, size,
                        Sort.by("createdAt").descending()))));
    }

    @Operation(summary = "Search tasks by keyword in title or description")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> searchTasks(
            @RequestParam String keyword,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Search results",
                taskService.searchTasks(keyword, teamId, PageRequest.of(page, size))));
    }

    @Operation(summary = "Update an existing task")
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated",
                taskService.updateTask(userDetails.getUsername(), taskId, request)));
    }

    @Operation(summary = "Mark a task as COMPLETED")
    @PatchMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success("Task completed",
                taskService.completeTask(userDetails.getUsername(), taskId)));
    }

    @Operation(summary = "Delete a task permanently")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted"));
    }
}
