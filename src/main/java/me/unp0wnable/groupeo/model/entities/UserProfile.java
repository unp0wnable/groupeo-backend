package me.unp0wnable.groupeo.model.entities;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "UserProfile")
public class UserProfile {
    @Id
    @Column(name = "userProfileID", nullable = false)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    private UUID userProfileID;
    
    @Column(name = "firstName", length = 30, nullable = false)
    private String firstName;
    
    @Column(name = "surname1", length = 50)
    private String surname1;
    
    @Column(name = "surname2", length = 50)
    private String surname2;
    
    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;
    
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
    
    @Column(name = "avatarPath")
    private String avatarPath;
    
    @Column(name = "score")
    private Float score;
}
