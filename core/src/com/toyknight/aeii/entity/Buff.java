package com.toyknight.aeii.entity;

/**
 * Created by toyknight on 4/3/2015.
 */
public class Buff {

    public static final int POISONED = 0x1;

    private final int type;
    private int remaining_turn;

    public Buff(int type, int turn) {
        this.type = type;
        this.remaining_turn = turn;
    }

    public void update() {
        this.remaining_turn--;
    }

    public int getType() {
        return type;
    }

    public int getRemainingTurn() {
        return remaining_turn;
    }

    @Override
    public boolean equals(Object buff) {
        if (buff instanceof Buff) {
            return ((Buff) buff).getType() == this.type;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.type;
        return hash;
    }

}
