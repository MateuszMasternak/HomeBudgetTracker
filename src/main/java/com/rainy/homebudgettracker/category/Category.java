package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "user_id"})
        })
public class Category {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToOne
    private User user;
}
