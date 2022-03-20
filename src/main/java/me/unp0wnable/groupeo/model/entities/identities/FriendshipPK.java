package me.unp0wnable.groupeo.model.entities.identities;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FriendshipPK implements Serializable {
    private static final long serialVersionUID = -774242906897515405L;
    
    //@ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    //@JoinColumn(name = "requesterid", referencedColumnName = "requesterid", nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID requesterID;
    
    //@ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    //@JoinColumn(name = "targetid", referencedColumnName = "targetid", nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID targetID;
    
    
    public FriendshipPK(UUID requesterID, UUID targetID) {
        this.requesterID = requesterID;
        this.targetID = targetID;
    }
}
