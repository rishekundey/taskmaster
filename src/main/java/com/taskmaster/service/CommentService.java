package com.taskmaster.service;

import com.taskmaster.dto.request.CommentRequest;
import com.taskmaster.dto.response.CommentResponse;
import com.taskmaster.entity.Comment;
import com.taskmaster.entity.Notification;
import com.taskmaster.entity.Task;
import com.taskmaster.entity.User;
import com.taskmaster.exception.BadRequestException;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse addComment(String username, Long taskId, CommentRequest request) {
        User author = userService.findByUsername(username);
        Task task = taskService.findById(taskId);
        Comment saved = commentRepository.save(Comment.builder()
                .content(request.getContent()).task(task).author(author).build());

        if (task.getAssignee() != null && !task.getAssignee().getId().equals(author.getId())) {
            notificationService.sendNotification(task.getAssignee(),
                    author.getUsername() + " commented on task: " + task.getTitle(),
                    Notification.Type.COMMENT_ADDED, taskId);
        }
        return mapToResponse(saved);
    }

    public List<CommentResponse> getTaskComments(Long taskId) {
        return commentRepository.findByTaskOrderByCreatedAtDesc(taskService.findById(taskId))
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(String username, Long commentId, CommentRequest request) {
        Comment comment = findById(commentId);
        if (!comment.getAuthor().getUsername().equals(username))
            throw new BadRequestException("You can only edit your own comments");
        comment.setContent(request.getContent());
        return mapToResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(String username, Long commentId) {
        Comment comment = findById(commentId);
        if (!comment.getAuthor().getUsername().equals(username))
            throw new BadRequestException("You can only delete your own comments");
        commentRepository.delete(comment);
    }

    private Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }

    private CommentResponse mapToResponse(Comment c) {
        return CommentResponse.builder().id(c.getId()).content(c.getContent())
                .author(AuthService.mapToUserResponse(c.getAuthor()))
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }
}
