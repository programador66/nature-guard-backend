package com.natureguard.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ElementCollection
    @CollectionTable(name = "report_tags", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Embedded
    private Location location;

    @ElementCollection
    @CollectionTable(name = "report_images", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "image_url")
    private List<String> images;

    private LocalDateTime createdAt;

    private String userId;
}