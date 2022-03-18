package me.unp0wnable.groupeo.model.entities;

import lombok.Data;

import java.util.List;

@Data
public class Block<T> {
    private List<T> items;
    private int itemsCount;
    private boolean hasMoreItems;
}
