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
@Table(name = "friend_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id", "recipient_id"})
})
public class FriendShip_Request {
    @Id
    @GeneratedValue
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User userOne;


    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn
    private User userTwo;

    @CreatedDate
    private Instant createdAt;
}
