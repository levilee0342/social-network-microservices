package com.example.post_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "type_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypeContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "type_name", unique = true, nullable = false)
    private String typeName;

    @OneToMany(mappedBy = "typeContent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-type-preference")
    private List<UserTypePreference> userTypePreferences = new ArrayList<>();
}
