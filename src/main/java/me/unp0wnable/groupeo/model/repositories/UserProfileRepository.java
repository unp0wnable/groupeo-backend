package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.UserProfile;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, UUID> {
    
    /**
     * Comprueba si existe un usuario por su nombre
     * @param nickName Nombre del usuario
     * @return <c>True</c> si usuario existe
     */
    boolean existsByNickName(String nickName);
    
    /**
     * Obtiene un usuario a partir de su nickName ignorando mayúsculas
     * @param nickName Nickname del usuario a buscar
     * @return Usuario encontrado
     */
    Optional<UserProfile> findByNickNameIgnoreCase(String nickName);
    
    
}
