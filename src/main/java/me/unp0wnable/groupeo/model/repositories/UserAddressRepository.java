package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.UserAddress;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends PagingAndSortingRepository<UserAddress, UUID> {
    
    /** Busca la dirección correspondiente al usuario recibido */
    Optional<UserAddress> findByUserProfile_UserProfileID(UUID userProfileID);
    
}
