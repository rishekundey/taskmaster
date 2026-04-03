package com.taskmaster.service;

import com.taskmaster.dto.request.TaskRequest;
import com.taskmaster.dto.response.AttachmentResponse;
import com.taskmaster.dto.response.CommentResponse;
import com.taskmaster.dto.response.TaskResponse;
import com.taskmaster.entity.*;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TeamService teamService;
    private final NotificationService notificationService;

    @Transactional
    public TaskResponse createTask(String username, TaskRequest request) {
        User creator = userService.findByUsername(username);
        Task.TaskBuilder builder = Task.builder()
                .title(request.getTitle()).description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Task.Status.OPEN)
                .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                .dueDate(request.getDueDate()).creator(creator);

        if (request.getAssigneeId() != null) builder.assignee(userService.findById(request.getAssigneeId()));
        if (request.getTeamId() != null) builder.team(teamService.findById(request.getTeamId()));

        Task saved = taskRepository.save(builder.build());

        if (saved.getAssignee() != null && !saved.getAssignee().getId().equals(creator.getId())) {
            notificationService.sendNotification(saved.getAssignee(),
                    creator.getUsername() + " assigned you task: " + saved.getTitle(),
                    Notification.Type.TASK_ASSIGNED, saved.getId());
        }
        return mapToResponse(saved);
    }

    public TaskResponse getTaskById(Long taskId) {
        return mapToResponse(findById(taskId));
    }

    public Page<TaskResponse> getMyTasks(String username, Task.Status status, Pageable pageable) {
        User user = userService.findByUsername(username);
        Page<Task> tasks = status != null
                ? taskRepository.findByAssigneeAndStatus(user, status, pageable)
                : taskRepository.findByAssignee(user, pageable);
        return tasks.map(this::mapToResponse);
    }

    public Page<TaskResponse> getTeamTasks(Long teamId, Task.Status status, Pageable pageable) {
        Team team = teamService.findById(teamId);
        if (status != null) {
            return taskRepository.findByTeamAndStatus(team, status, pageable).map(this::mapToResponse);
        }
        List<Task> list = taskRepository.findByTeam(team);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size()).map(this::mapToResponse);
    }

    public Page<TaskResponse> searchTasks(String keyword, Long teamId, Pageable pageable) {
        if (teamId != null) {
            Team team = teamService.findById(teamId);
            return taskRepository.searchByKeywordInTeam(team, keyword, pageable).map(this::mapToResponse);
        }
        return taskRepository.searchByKeyword(keyword, pageable).map(this::mapToResponse);
    }

    @Transactional
    public TaskResponse updateTask(String username, Long taskId, TaskRequest request) {
        Task task = findById(taskId);
        User currentUser = userService.findByUsername(username);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == Task.Status.COMPLETED) task.setCompletedAt(LocalDateTime.now());
        }

        if (request.getAssigneeId() != null) {
            User newAssignee = userService.findById(request.getAssigneeId());
            boolean reassigned = task.getAssignee() == null ||
                    !task.getAssignee().getId().equals(newAssignee.getId());
            task.setAssignee(newAssignee);
            if (reassigned && !newAssignee.getId().equals(currentUser.getId())) {
                notificationService.sendNotification(newAssignee,
                        currentUser.getUsername() + " assigned you task: " + task.getTitle(),
                        Notification.Type.TASK_ASSIGNED, task.getId());
            }
        }

        if (task.getAssignee() != null && !task.getAssignee().getId().equals(currentUser.getId())) {
            notificationService.sendNotification(task.getAssignee(),
                    "Task updated: " + task.getTitle(), Notification.Type.TASK_UPDATED, task.getId());
        }
        return mapToResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse completeTask(String username, Long taskId) {
        Task task = findById(taskId);
        task.setStatus(Task.Status.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        User currentUser = userService.findByUsername(username);
        if (task.getCreator() != null && !task.getCreator().getId().equals(currentUser.getId())) {
            notificationService.sendNotification(task.getCreator(),
                    username + " completed task: " + task.getTitle(),
                    Notification.Type.TASK_COMPLETED, task.getId());
        }
        return mapToResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId))
            throw new ResourceNotFoundException("Task", "id", taskId);
        taskRepository.deleteById(taskId);
    }

    public Task findById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
    }

    public TaskResponse mapToResponse(Task task) {
        List<CommentResponse> comments = task.getComments().stream()
                .map(c -> CommentResponse.builder().id(c.getId()).content(c.getContent())
                        .author(AuthService.mapToUserResponse(c.getAuthor()))
                        .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build())
                .collect(Collectors.toList());

        List<AttachmentResponse> attachments = task.getAttachments().stream()
                .map(a -> AttachmentResponse.builder().id(a.getId()).fileName(a.getFileName())
                        .originalName(a.getOriginalName()).fileType(a.getFileType())
                        .fileSize(a.getFileSize())
                        .downloadUrl("/api/attachments/" + a.getId() + "/download")
                        .uploader(AuthService.mapToUserResponse(a.getUploader()))
                        .createdAt(a.getCreatedAt()).build())
                .collect(Collectors.toList());

        return TaskResponse.builder()
                .id(task.getId()).title(task.getTitle()).description(task.getDescription())
                .status(task.getStatus().name()).priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .creator(AuthService.mapToUserResponse(task.getCreator()))
                .assignee(task.getAssignee() != null ? AuthService.mapToUserResponse(task.getAssignee()) : null)
                .team(task.getTeam() != null ? teamService.mapToResponse(task.getTeam()) : null)
                .comments(comments).attachments(attachments)
                .createdAt(task.getCreatedAt()).updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt()).build();
    }
}
