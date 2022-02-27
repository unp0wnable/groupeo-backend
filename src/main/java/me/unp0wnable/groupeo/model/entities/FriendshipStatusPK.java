package me.unp0wnable.groupeo.model.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FriendshipStatusPK implements Serializable {
    private static final long serialVersionUID = -774242906897515405L;
    private UUID requesterID;
    
    private UUID targetID;

    @Temporal(TemporalType.TIMESTAMP)
    
    private Date lastUpdated;
    
}
