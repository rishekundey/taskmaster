package com.taskmaster.controller;

import com.taskmaster.dto.request.CommentRequest;
import com.taskmaster.dto.response.ApiResponse;
import com.taskmaster.dto.response.CommentResponse;
import com.taskmaster.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comments", description = "Add, update, delete comments on tasks")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added",
                        commentService.addComment(userDetails.getUsername(), taskId, request)));
    }

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success("Comments",
                commentService.getTaskComments(taskId)));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Comment updated",
                commentService.updateComment(userDetails.getUsername(), commentId, request)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId) {
        commentService.deleteComment(userDetails.getUsername(), commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted"));
    }
}
