package com.taskmaster.controller;

import com.taskmaster.dto.response.ApiResponse;
import com.taskmaster.dto.response.AttachmentResponse;
import com.taskmaster.service.AttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Attachments", description = "Upload, download, and manage task file attachments")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded",
                        attachmentService.uploadAttachment(userDetails.getUsername(), taskId, file)));
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success("Attachments",
                attachmentService.getTaskAttachments(taskId)));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) throws Exception {
        Resource resource = attachmentService.downloadAttachment(attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long attachmentId) throws IOException {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted"));
    }
}
