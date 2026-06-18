package org.Core.Social.Models;

import jakarta.persistence.*;
import lombok.Data;
import org.Core.User.Models.User;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
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
    private User sender;


    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn
    private User recipient;

    @CreatedDate
    private Instant createdAt;

    public FriendShip_Request(String sender,String recipient){
        this.sender=new User(sender);
        this.recipient=new User(sender);
    }
}
