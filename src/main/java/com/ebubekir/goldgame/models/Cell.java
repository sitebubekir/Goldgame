package com.ebubekir.goldgame.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    private int x;
    private int y;
    private boolean hasGold;
    private int gold;
}
