package me.unp0wnable.groupeo.model.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Calendar;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friendshipstatus")
public class FriendshipStatus {
    @EmbeddedId
    private FriendshipStatusPK friendshipStatusPK;
    
    @OneToOne(fetch = FetchType.EAGER,
            cascade = CascadeType.PERSIST,
            optional = false,
            orphanRemoval = true)
    @JoinColumn(name = "specifierid", nullable = false)
    private User specifier;
    
    @OneToOne(fetch = FetchType.EAGER,
            orphanRemoval = true)
    @JoinColumn(name = "statusid")
    private FriendshipStatusCode statusCode;
    
    
    public FriendshipStatus(UUID requesterID, UUID targetID, User specifier, FriendshipStatusCode statusCode) {
        this.specifier = specifier;
        this.statusCode = statusCode;
        this.friendshipStatusPK = new FriendshipStatusPK(requesterID, targetID, Calendar.getInstance().getTime());
    }
    
    @Override
    public int hashCode() {
        return this.friendshipStatusPK.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return this.friendshipStatusPK.equals(obj);
    }
}
