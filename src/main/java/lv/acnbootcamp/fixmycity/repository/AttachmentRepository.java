package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
