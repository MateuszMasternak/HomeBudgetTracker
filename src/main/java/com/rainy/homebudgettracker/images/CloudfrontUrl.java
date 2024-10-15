package com.rainy.homebudgettracker.images;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"key", "url"})
})
public class CloudfrontUrl {
    @Id
    @GeneratedValue
    private Long id;
    private String key;
    @Column(length = 8192)
    private String url;
    private Instant expirationTime;
}
