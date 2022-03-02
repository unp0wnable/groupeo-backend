package me.unp0wnable.groupeo.model.entities;

import lombok.*;
import me.unp0wnable.groupeo.model.constants.FriendshipStatusCodes;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipStatusPK;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friendshipstatus")
public class FriendshipStatus {
    @EmbeddedId
    private FriendshipStatusPK id;
    
    private FriendshipStatusCodes status;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastupdated")
    private Date lastUpdated = Calendar.getInstance().getTime();
    
    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumns( {
        @JoinColumn(name = "requesterid", referencedColumnName = "requesterid", nullable = false),
        @JoinColumn(name = "targetid", referencedColumnName = "targetid", nullable = false)
    })
    private Friendship friendship;
    
    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "specifierID", nullable = false)
    private User specifier;
    
}
