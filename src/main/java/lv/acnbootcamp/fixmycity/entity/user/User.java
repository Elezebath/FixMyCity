package lv.acnbootcamp.fixmycity.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lv.acnbootcamp.fixmycity.entity.incident.Comment;
import lv.acnbootcamp.fixmycity.entity.Company;
import lv.acnbootcamp.fixmycity.entity.incident.Incident;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentAssignment;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentStatusHistory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Represents an application user stored in the database.
// Used both as the JPA entity and as the source of truth for
// Spring Security authentication (via UserDetailsServiceImpl).
@Entity
@Table(name = "users") // "user" is a reserved word in MySQL, so the table is named "users"
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Stores the BCrypt-hashed password, never plain text.
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING) // stores role as "CITIZEN" etc., not as an ordinal number
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "user")
    private Company company;

    @OneToMany(mappedBy = "citizen")
    @Builder.Default
    private List<Incident> reportedIncidents = new ArrayList<>();

    @OneToMany(mappedBy = "assignedBy")
    @Builder.Default
    private List<IncidentAssignment> createdAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "changedBy")
    @Builder.Default
    private List<IncidentStatusHistory> statusChanges = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
}