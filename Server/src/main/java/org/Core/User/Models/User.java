package org.Core.User.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    private String id;


    @CreatedDate
    private Instant created_at;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String avatarUrl;
    @Builder.Default
    private int elo=1200;

}
