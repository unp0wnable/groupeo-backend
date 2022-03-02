package me.unp0wnable.groupeo.model.entities;

import lombok.*;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipPK;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friendship")
public class Friendship {
    @EmbeddedId
    private FriendshipPK id;
    
    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "groupID")
    private Group group;
    
    @OneToMany(mappedBy = "friendship", orphanRemoval = true, cascade = CascadeType.PERSIST)
    @OrderBy("lastUpdated ASC")
    private List<FriendshipStatus> friendshipStatus = new ArrayList<>();
    
    @Override
    public int hashCode( ) {
        return Objects.hash(id);
    }
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Friendship that = ( Friendship ) o;
        
        return id.equals(that.getId());
    }
}
