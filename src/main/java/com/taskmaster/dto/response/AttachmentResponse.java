package com.taskmaster.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private UserResponse uploader;
    private LocalDateTime createdAt;
}
