package com.ebubekir.goldgame.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private char id;
    private int x;
    private int y;
    private int gold;
    private boolean eliminated;
    private int steps;
    private int goldSpent;
    private int goldCollected;

    public boolean move(int newX, int newY, int moveCost) {
        if (gold < moveCost) {
            eliminated = true;
            return false;
        }
        gold -= moveCost;
        goldSpent += moveCost;
        x = newX;
        y = newY;
        steps++;
        return true;
    }

    public boolean collectGold(Cell cell) {
        if (cell.isHasGold()) {
            goldCollected += cell.getGold();
            gold += cell.getGold();
            cell.setHasGold(false);
            return true;
        }
        return false;
    }
}
