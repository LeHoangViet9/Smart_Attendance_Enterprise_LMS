package edufit_com_lms.module.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "phone", unique = true)
    private String phone;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    @Column(name = "address")
    private String address;
    @Column(name = "avatar_url", unique = true)
    private String avatarUrl;
    @Column(name = "face_embedding", columnDefinition = "TEXT")
    // @org.hibernate.annotations.ColumnTransformer(read = "face_embedding::text",
    // write = "?::vector")
    private String faceEmbedding;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private StudentProfile studentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LecturerProfile lecturerProfile;

    /**
     * Chỉ so sánh theo userId để tránh vòng lặp vô tận khi Hibernate
     * duyệt qua các quan hệ Lazy (studentProfile, lecturerProfile).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return userId != null && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", email='" + email + "', role=" + role + "}";
    }
}
