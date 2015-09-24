package com.toyknight.aeii.entity;

import java.io.Serializable;

/**
 * @author toyknight 4/3/2015.
 */
public class Status implements Serializable {

    private static final long serialVersionUID = 4032015L;

    public static final int POISONED = 0x1;
    public static final int PETRIFACTED = 0x2;
    public static final int INSPIRED = 0x3;
    public static final int BLINDED = 0x4;

    private final int type;
    private int remaining_turn;

    public Status(int type, int turn) {
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
    public boolean equals(Object status) {
        return status instanceof Status && ((Status) status).getType() == this.type;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.type;
        return hash;
    }

}
