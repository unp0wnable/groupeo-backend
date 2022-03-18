package me.unp0wnable.groupeo.model.repositories;

import me.unp0wnable.groupeo.model.constants.FriendshipStatusCodes;
import me.unp0wnable.groupeo.model.entities.User;
import org.springframework.data.domain.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

public class CustomFriendshipRepositoryImpl implements CustomFriendshipRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Slice<User> getFriendsByUser(UUID userID, Pageable pageable) {
        // Consultas a realizar
        // Obtener usuarios que son amigos del usuario actual
        String currentUserFriendsQuery =
            "SELECT f.id.targetID FROM Friendship f WHERE f.id.requesterID = :userID AND f.status = :acceptedStatus";
        // Obtener gente que tiene al usuario actual como amigo
        String friendsWithCurrentUserQuery =
            "SELECT f.id.requesterID FROM Friendship f WHERE f.id.targetID = :userID AND f.status = :acceptedStatus";
        String unionQuery = currentUserFriendsQuery
                + " UNION DISTINCT "
                + friendsWithCurrentUserQuery
                + " ORDER BY nickName ASC";
    
        // Construir consulta y substituir parámetros
        Query query = createPaginatedQuery(unionQuery, pageable)
                .setParameter("userID", userID.toString())
                .setParameter("acceptedStatus", FriendshipStatusCodes.ACCEPTED.toString());
        
        // Ejecutar consulta y obtener resultados
        List<User> queryItems = query.getResultList();
        
        return paginateResultsFromQuery(queryItems, pageable);
    }
    
    @Override
    public Slice<User> getFriendsByGroup(UUID groupID, Pageable pageable) {
        // Consultas a realizar
        String selectQuery =
            "SELECT f.id.targetID FROM Friendship f GROUP BY f.group.groupID HAVING f.group.groupID = :groupID";
        
        // Construir consulta y substituir parámetros
        Query query = createPaginatedQuery(selectQuery, pageable)
            .setParameter("groupID", groupID.toString());
    
        // Ejecutar consulta y obtener resultados
        List<User> queryItems = query.getResultList();
    
        return paginateResultsFromQuery(queryItems, pageable);
    }
    
    @Override
    public Slice<User> getBlockedUsersByUserID(UUID userID, Pageable pageable) {
        // Consultas a realizar
        String selectQuery =
            "SELECT f.id.targetID FROM Friendship f WHERE f.id.requesterID = ?1 AND f.status = 'BLOCKED' ORDER BY f.id.targetID.nickName ASC";
    
        // Construir consulta y substituir parámetros
        Query query = createPaginatedQuery(selectQuery, pageable)
            .setParameter("", null);
    
        // Ejecutar consulta y obtener resultados
        List<User> queryItems = query.getResultList();
    
        return paginateResultsFromQuery(queryItems, pageable);
    }
    
    
    
    /* ****************************** AUX FUNCTIONS ****************************** */
    /** Crea la consulta paginada a partir del string con la consulta SQL y los datos de paginación */
    private Query createPaginatedQuery(String queryString, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        
        Query query = entityManager.createQuery(queryString)
                .setFirstResult(pageNumber * pageSize)
                .setMaxResults(pageSize + 1);
        
        return query;
    }
    
    /** Devuelve los resultados de la consulta SQL paginados */
    private <T> Slice<T> paginateResultsFromQuery(List<T> results, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        boolean hasMoreItems = results.size() == ( pageSize + 1);
    
        // Eliminar el último elemento que se usa para saber si hay más elementos en los resultados
        if (hasMoreItems)
            results.remove(results.size() - 1);
    
        return new SliceImpl<>(results, PageRequest.of(pageNumber, pageSize), hasMoreItems);
    }
}
