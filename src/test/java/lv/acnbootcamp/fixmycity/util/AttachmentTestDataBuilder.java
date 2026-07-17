package lv.acnbootcamp.fixmycity.util;

import lv.acnbootcamp.fixmycity.entity.incident.Attachment;
import lv.acnbootcamp.fixmycity.entity.incident.Incident;

import java.time.LocalDateTime;

/**
 * Fluent builder for constructing Attachment test fixtures with sensible defaults.
 */
public class AttachmentTestDataBuilder {

    private Long attachmentId = 1L;
    private Incident incident;
    private String fileName = "test-image.jpg";
    private String filePath = "/uploads/test-image.jpg";
    private String fileType = "image/jpeg";
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private AttachmentTestDataBuilder() {
        this.incident = IncidentTestDataBuilder.anIncident().build();
    }

    public static AttachmentTestDataBuilder anAttachment() {
        return new AttachmentTestDataBuilder();
    }

    public AttachmentTestDataBuilder withAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
        return this;
    }

    public AttachmentTestDataBuilder withIncident(Incident incident) {
        this.incident = incident;
        return this;
    }

    public AttachmentTestDataBuilder withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public AttachmentTestDataBuilder withFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public AttachmentTestDataBuilder withFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public AttachmentTestDataBuilder withUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
        return this;
    }

    public Attachment build() {
        return Attachment.builder()
                .attachmentId(attachmentId)
                .incident(incident)
                .fileName(fileName)
                .filePath(filePath)
                .fileType(fileType)
                .uploadedAt(uploadedAt)
                .build();
    }
}