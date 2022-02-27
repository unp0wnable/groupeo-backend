package me.unp0wnable.groupeo.model.entities;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friendshipstatuscode")
public class FriendshipStatusCode {
    @Id
    @Column(name = "statusid")
    private Character statusID;
    
    private String name;
}
