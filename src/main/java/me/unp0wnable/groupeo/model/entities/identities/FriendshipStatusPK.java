package me.unp0wnable.groupeo.model.entities.identities;

import lombok.*;
import me.unp0wnable.groupeo.model.entities.Friendship;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FriendshipStatusPK implements Serializable {
    @Embedded
    private FriendshipPK friendshipPK;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int entryNumber;
    
}
