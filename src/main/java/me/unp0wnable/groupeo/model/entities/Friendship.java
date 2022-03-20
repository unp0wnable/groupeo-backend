package me.unp0wnable.groupeo.model.entities;

import lombok.*;
import me.unp0wnable.groupeo.model.constants.FriendshipStatusCodes;
import me.unp0wnable.groupeo.model.entities.identities.FriendshipPK;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friendship")
public class Friendship {
    @EmbeddedId
    private FriendshipPK id;
    
    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "groupid")
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specifierid", referencedColumnName = "userid", nullable = false)
    private User specifier;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastupdate", nullable = false)
    private Date lastUpdate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FriendshipStatusCodes status;
    
    
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
