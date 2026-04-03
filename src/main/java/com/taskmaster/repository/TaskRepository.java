package com.taskmaster.repository;

import com.taskmaster.entity.Task;
import com.taskmaster.entity.Team;
import com.taskmaster.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByAssignee(User assignee);

    List<Task> findByCreator(User creator);

    List<Task> findByTeam(Team team);

    Page<Task> findByAssignee(User assignee, Pageable pageable);

    Page<Task> findByAssigneeAndStatus(User assignee, Task.Status status, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.team = :team AND t.status = :status")
    Page<Task> findByTeamAndStatus(@Param("team") Team team,
                                   @Param("status") Task.Status status,
                                   Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.team = :team AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchByKeywordInTeam(@Param("team") Team team,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);
}
