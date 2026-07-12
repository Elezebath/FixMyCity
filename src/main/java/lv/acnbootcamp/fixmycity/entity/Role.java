package lv.acnbootcamp.fixmycity.entity;

// Represents the fixed set of application roles.
// "Anonymous" is intentionally not included here - it represents
// an unauthenticated user and is handled by Spring Security itself,
// not stored as a role in the database.
public enum Role {
    CITIZEN,
    MANAGER,
    EMPLOYEE,
    ADMIN
}