package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.incident.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
