package org.Core.Social.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.Core.User.Models.User;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Data
@Table(name = "friend_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id", "recipient_id"})
})
@NoArgsConstructor
public class FriendShip_Request {
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

    public FriendShip_Request(User sender,User recipient){
        this.sender=sender;
        this.recipient=recipient;
    }
}
