package com.taskmaster.service;

import com.taskmaster.dto.request.TeamRequest;
import com.taskmaster.dto.response.TeamResponse;
import com.taskmaster.dto.response.UserResponse;
import com.taskmaster.entity.Notification;
import com.taskmaster.entity.Team;
import com.taskmaster.entity.User;
import com.taskmaster.exception.BadRequestException;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public TeamResponse createTeam(String username, TeamRequest request) {
        User owner = userService.findByUsername(username);
        Team team = Team.builder()
                .name(request.getName()).description(request.getDescription())
                .owner(owner).inviteCode(generateInviteCode()).build();
        team.getMembers().add(owner);
        return mapToResponse(teamRepository.save(team));
    }

    public List<TeamResponse> getMyTeams(String username) {
        User user = userService.findByUsername(username);
        return teamRepository.findAllByUserMemberOrOwner(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TeamResponse getTeamById(Long teamId) {
        return mapToResponse(findById(teamId));
    }

    @Transactional
    public TeamResponse joinTeamByInviteCode(String username, String inviteCode) {
        User user = userService.findByUsername(username);
        Team team = teamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with invite code: " + inviteCode));
        if (team.getMembers().contains(user))
            throw new BadRequestException("You are already a member of this team");
        team.getMembers().add(user);
        teamRepository.save(team);
        notificationService.sendNotification(team.getOwner(),
                user.getUsername() + " joined your team: " + team.getName(),
                Notification.Type.TEAM_JOINED, null);
        return mapToResponse(team);
    }

    @Transactional
    public TeamResponse addMember(String ownerUsername, Long teamId, Long userId) {
        User owner = userService.findByUsername(ownerUsername);
        Team team = findById(teamId);
        if (!team.getOwner().getId().equals(owner.getId()))
            throw new BadRequestException("Only the team owner can add members");
        User newMember = userService.findById(userId);
        team.getMembers().add(newMember);
        notificationService.sendNotification(newMember,
                "You have been added to team: " + team.getName(),
                Notification.Type.TEAM_INVITE, null);
        return mapToResponse(teamRepository.save(team));
    }

    @Transactional
    public void removeMember(String ownerUsername, Long teamId, Long userId) {
        User owner = userService.findByUsername(ownerUsername);
        Team team = findById(teamId);
        if (!team.getOwner().getId().equals(owner.getId()))
            throw new BadRequestException("Only the team owner can remove members");
        team.getMembers().remove(userService.findById(userId));
        teamRepository.save(team);
    }

    @Transactional
    public TeamResponse regenerateInviteCode(String ownerUsername, Long teamId) {
        User owner = userService.findByUsername(ownerUsername);
        Team team = findById(teamId);
        if (!team.getOwner().getId().equals(owner.getId()))
            throw new BadRequestException("Only the team owner can regenerate the invite code");
        team.setInviteCode(generateInviteCode());
        return mapToResponse(teamRepository.save(team));
    }

    public Team findById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    public TeamResponse mapToResponse(Team team) {
        Set<UserResponse> members = team.getMembers().stream()
                .map(AuthService::mapToUserResponse).collect(Collectors.toSet());
        return TeamResponse.builder()
                .id(team.getId()).name(team.getName()).description(team.getDescription())
                .owner(AuthService.mapToUserResponse(team.getOwner()))
                .members(members).inviteCode(team.getInviteCode())
                .createdAt(team.getCreatedAt()).build();
    }
}
