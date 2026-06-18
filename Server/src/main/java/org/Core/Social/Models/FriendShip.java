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
    private User userOne;


    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn
    private User userTwo;

    @CreatedDate
    private Instant createdAt;

    private int gamePlayed;

    public FriendShip(String userOneId,String userTwoId){
        this.userOne=new User(userOneId);
        this.userTwo=new User(userTwoId);
    }

     record score(int userOneWin,int userTwoWin,int draw){}
}
