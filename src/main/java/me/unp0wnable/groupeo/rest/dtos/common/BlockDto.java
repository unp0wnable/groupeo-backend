package me.unp0wnable.groupeo.rest.dtos.common;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockDto<T> {
    @NotNull
    private List<T> items;
    
    @NotNull
    private boolean hasMoreItems;
}
