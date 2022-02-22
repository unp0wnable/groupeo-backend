package me.unp0wnable.groupeo.model.entities;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "UserProfile")
public class UserProfile {
    public enum UserRoles {ADMIN, USER}
    
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "userProfileID", nullable = false)
    private UUID userProfileID;
    
    @Column(name = "firstName", length = 30, nullable = false)
    private String firstName;
    
    @Column(name = "surname1", length = 50)
    private String surname1;
    
    @Column(name = "surname2", length = 50)
    private String surname2;
    
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "birthDate", nullable = false)
    private Date birthDate;
    
    @Column(name = "joinDate", nullable = false)
    private Date joinDate;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "nickName", length = 50, nullable = false, unique = true)
    private String nickName;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Lob
    @Column(name = "avatarPath")
    private String imageB64;
    
    @Column(name = "score")
    private Float score;
    
    @Column(name = "role")
    private UserRoles role;
    
    @OneToOne(mappedBy = "userProfile",
            fetch = FetchType.LAZY,
            cascade = CascadeType.PERSIST,
            orphanRemoval = true)
    private UserAddress address;
}
