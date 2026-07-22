package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.incident.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByIncident_IncidentIdOrderByCreatedAtAsc(Long incidentId);
}
