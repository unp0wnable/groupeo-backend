package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.User;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends PagingAndSortingRepository<User, UUID> {
    
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
    Optional<User> findByNickNameIgnoreCase(String nickName);
    
    
}
