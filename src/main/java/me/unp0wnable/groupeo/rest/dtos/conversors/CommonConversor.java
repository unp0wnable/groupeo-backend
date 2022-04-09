package me.unp0wnable.groupeo.rest.dtos.conversors;

import lombok.experimental.UtilityClass;
import me.unp0wnable.groupeo.model.entities.Block;
import me.unp0wnable.groupeo.rest.dtos.common.BlockDto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class CommonConversor {
    /* ******************** Convertir a DTO ******************** */
    
    /** Crea un BlockDto<D> a partir de un Block<E>.
     * Para ello mapea las entidades de tipo <E> al tipo <D> mediante la función recibida.
     * <E>: Entidad a mapear.
     * <D>: DTO al que se desea mapear la entidad <E>
     */
    public static <E, D> BlockDto<D> toBlockDTO(Block<E> block, Function<E, D> mappingFunction) {
        BlockDto<D> dto = new BlockDto<>();
        
        // Mapea las entidades a su correspondiente DTO empleando la función recibida
        List<D> mappedEntities = block.getItems().stream()
                .map(mappingFunction)
                .collect(Collectors.toList());
        
        // Asigna los campos del Block al BlockDto
        dto.setItems(mappedEntities);
        dto.setHasMoreItems(block.hasMoreItems());
        dto.setItemsCount(block.getItemsCount());
        
        return dto;
    }
    /* ******************** Convertir a Entidad ******************** */
    
}
