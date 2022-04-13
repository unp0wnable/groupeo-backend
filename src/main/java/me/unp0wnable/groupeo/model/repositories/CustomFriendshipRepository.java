package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface CustomFriendshipRepository {
    /** Obtiene los amigos del usuario indicado */
    Slice<User> getFriendsByUser(UUID userID, Pageable pageable);
    
    /** Obtie todos los amigos que pertenecen al grupo recibido */
    Slice<User> getFriendsByGroup(UUID groupID, Pageable pageable);
    
    /** Obtiene los usuarios bloqueados por el usuario recibido */
    Slice<User> getBlockedUsersByUserID(UUID userID, Pageable pageable);
}
