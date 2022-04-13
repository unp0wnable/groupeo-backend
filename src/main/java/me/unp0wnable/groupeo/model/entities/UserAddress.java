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
@Table(name = "UserAddress")
public class UserAddress {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "userProfileID", nullable = false)
    private UUID userAddressID;
    
    @Column(name = "city", length = 50)
    private String city;
    
    @Column(name = "region", length = 50)
    private String region;
    
    @Column(name = "postalCode", length = 10)
    private String postalCode;
    
    @Column(name = "country", length = 50)
    private String country;
    
    @OneToOne(optional = false,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "userProfileID")
    private User user;
}
