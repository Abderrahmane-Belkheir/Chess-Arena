package org.Core.Social.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.Core.User.Models.User;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_one_id", "user_two_id"})
})
public class FriendShip {
    @Id
    @GeneratedValue
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User sender;


    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn
    private User recipient;

    @CreatedDate
    private Instant createdAt;

    private int gamePlayed;
}
