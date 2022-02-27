package me.unp0wnable.groupeo.model.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friendship")
public class Friendship {
    @EmbeddedId
    private FriendshipPK friendshipPK;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    
    public Friendship(UUID requesterID, UUID targetID) {
        this.friendshipPK = new FriendshipPK(requesterID, targetID);
    }
    
    @Override
    public int hashCode( ) {
        return this.friendshipPK.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return this.friendshipPK.equals(obj);
    }
}
