package com.taskmaster.service;

import com.taskmaster.dto.response.NotificationResponse;
import com.taskmaster.entity.Notification;
import com.taskmaster.entity.User;
import com.taskmaster.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for creating, storing, and pushing real-time notifications via WebSocket.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @Transactional
    public void sendNotification(User recipient, String message,
                                  Notification.Type type, Long taskId) {
        Notification notification = Notification.builder()
                .recipient(recipient).message(message).type(type).taskId(taskId).isRead(false).build();
        Notification saved = notificationRepository.save(notification);
        // Push real-time via WebSocket to the user's private queue
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(), "/queue/notifications", mapToResponse(saved));
        log.info("Notification sent to '{}': {}", recipient.getUsername(), message);
    }

    public List<NotificationResponse> getNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).message(n.getMessage()).type(n.getType().name())
                .taskId(n.getTaskId()).isRead(n.getIsRead()).createdAt(n.getCreatedAt()).build();
    }
}
