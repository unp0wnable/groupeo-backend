package me.unp0wnable.groupeo.rest.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockDTO<T> {
    @NotNull
    private List<T> items;
    
    @NotNull
    private boolean hasMoreItems;
}
