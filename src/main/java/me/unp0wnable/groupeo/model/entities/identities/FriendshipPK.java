package me.unp0wnable.groupeo.model.entities.identities;

import lombok.*;
import me.unp0wnable.groupeo.model.entities.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FriendshipPK implements Serializable {
    private static final long serialVersionUID = -774242906897515405L;
    
    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "requesterid", nullable = false)
    private UUID requesterID;
    
    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "targetid")
    private UUID targetID;
    
}
