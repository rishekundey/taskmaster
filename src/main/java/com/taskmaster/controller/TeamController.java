package com.taskmaster.controller;

import com.taskmaster.dto.request.TeamRequest;
import com.taskmaster.dto.response.ApiResponse;
import com.taskmaster.dto.response.TeamResponse;
import com.taskmaster.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Teams", description = "Team/Project creation, invite codes, and member management")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Create a new team/project")
    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created",
                        teamService.createTeam(userDetails.getUsername(), request)));
    }

    @Operation(summary = "Get all teams I belong to")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getMyTeams(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("My teams",
                teamService.getMyTeams(userDetails.getUsername())));
    }

    @Operation(summary = "Get team details by ID")
    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success("Team found", teamService.getTeamById(teamId)));
    }

    @Operation(summary = "Join a team using invite code")
    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<ApiResponse<TeamResponse>> joinTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String inviteCode) {
        return ResponseEntity.ok(ApiResponse.success("Joined team successfully",
                teamService.joinTeamByInviteCode(userDetails.getUsername(), inviteCode)));
    }

    @Operation(summary = "Add member to team (owner only)")
    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<ApiResponse<TeamResponse>> addMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Member added",
                teamService.addMember(userDetails.getUsername(), teamId, userId)));
    }

    @Operation(summary = "Remove member from team (owner only)")
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeMember(userDetails.getUsername(), teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed"));
    }

    @Operation(summary = "Regenerate invite code (owner only)")
    @PostMapping("/{teamId}/regenerate-code")
    public ResponseEntity<ApiResponse<TeamResponse>> regenerateCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success("Invite code regenerated",
                teamService.regenerateInviteCode(userDetails.getUsername(), teamId)));
    }
}
