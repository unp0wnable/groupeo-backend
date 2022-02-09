package me.unp0wnable.groupeo.model.entities;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

/**
 * Datos de la dirección de un usuario
 */
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "UserAddress")
public class UserAddress {
    @Id
    @Column(name = "userAddressID", nullable = false)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    private UUID userAddressID;
    
    @Column(name = "city", length = 50)
    private String city;
    
    @Column(name = "region", length = 50)
    private String region;
    
    @Column(name = "postalCode", length = 10)
    private String postalCode;
    
    @Column(name = "country", length = 50)
    private String country;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true, optional = false)
    @JoinColumn(name = "userProfileID")
    private UserProfile userProfile;
}
