package com.taskmaster.service;

import com.taskmaster.dto.response.AttachmentResponse;
import com.taskmaster.entity.Attachment;
import com.taskmaster.entity.Notification;
import com.taskmaster.entity.Task;
import com.taskmaster.entity.User;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Value("${app.file-upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public AttachmentResponse uploadAttachment(String username, Long taskId,
                                                MultipartFile file) throws IOException {
        User uploader = userService.findByUsername(username);
        Task task = taskService.findById(taskId);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = Paths.get(uploadDir).resolve(fileName);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Attachment saved = attachmentRepository.save(Attachment.builder()
                .fileName(fileName).originalName(file.getOriginalFilename())
                .fileType(file.getContentType()).fileSize(file.getSize())
                .filePath(targetPath.toString()).task(task).uploader(uploader).build());

        if (task.getAssignee() != null && !task.getAssignee().getId().equals(uploader.getId())) {
            notificationService.sendNotification(task.getAssignee(),
                    uploader.getUsername() + " added attachment to: " + task.getTitle(),
                    Notification.Type.ATTACHMENT_ADDED, taskId);
        }
        return mapToResponse(saved);
    }

    public Resource downloadAttachment(Long attachmentId) throws MalformedURLException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));
        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) throw new ResourceNotFoundException("File not found on disk");
        return resource;
    }

    public List<AttachmentResponse> getTaskAttachments(Long taskId) {
        return attachmentRepository.findByTask(taskService.findById(taskId))
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));
        Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        attachmentRepository.delete(attachment);
    }

    private AttachmentResponse mapToResponse(Attachment a) {
        return AttachmentResponse.builder().id(a.getId()).fileName(a.getFileName())
                .originalName(a.getOriginalName()).fileType(a.getFileType()).fileSize(a.getFileSize())
                .downloadUrl("/api/attachments/" + a.getId() + "/download")
                .uploader(AuthService.mapToUserResponse(a.getUploader()))
                .createdAt(a.getCreatedAt()).build();
    }
}
