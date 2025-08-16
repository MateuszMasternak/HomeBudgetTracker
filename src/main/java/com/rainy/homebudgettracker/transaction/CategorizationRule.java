package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorizationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String keyword;
    @ManyToOne()
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(name = "user_sub", nullable = false)
    private String userSub;
}
