package me.unp0wnable.groupeo.model.entities;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "grouptable")
public class Group {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "groupid", nullable = false)
    private UUID groupID;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "creatorID", nullable = false)
    private User creator;
    
}
