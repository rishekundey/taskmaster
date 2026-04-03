package com.taskmaster.repository;

import com.taskmaster.entity.Team;
import com.taskmaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByOwner(User owner);

    @Query("SELECT t FROM Team t JOIN t.members m WHERE m = :user")
    List<Team> findByMember(@Param("user") User user);

    Optional<Team> findByInviteCode(String inviteCode);

    @Query("SELECT t FROM Team t WHERE t.owner = :user OR :user MEMBER OF t.members")
    List<Team> findAllByUserMemberOrOwner(@Param("user") User user);
}
