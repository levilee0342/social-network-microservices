package com.example.post_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "user_type_preference",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "type_id"})
)
@Data
public class UserTypePreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "type_id")
    @JsonBackReference
    private TypeContent typeContent;

    @Column(name = "score")
    private Integer score;
}
