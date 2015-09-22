package com.toyknight.aeii.utils;

import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Tile;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author toyknight 9/21/2015.
 */
public class TileValidator {

    private static HashMap<TileSurround, Short> water_mapping;

    private static int W;
    private static int L;

    public static void initialize() {
        water_mapping = new HashMap<TileSurround, Short>();
        W = Tile.TYPE_WATER;
        L = Tile.TYPE_LAND;
    }

    private static TileSurround createTileSurround(Map map, int x, int y) {
        TileSurround surround = new TileSurround();
        Arrays.fill(surround.data, W);
        int i = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                Tile tile = map.getTile(x + dx, y + dy);
                if (tile != null && tile.getType() != Tile.TYPE_WATER) {
                    surround.data[i] = L;
                }
                i++;
            }
        }
        return surround;
    }

    public static void validate(Map map, int x, int y) {
        if (map.isWithinMap(x, y)) {
            int index = map.getTileIndex(x, y);
            if (TileFactory.getTile(index).getType() == Tile.TYPE_WATER) {
                TileSurround surround = createTileSurround(map, x, y);
                Short validated_index = water_mapping.get(surround);
                if (validated_index != null) {
                    map.setTile(validated_index, x, y);
                }
            }
        }
    }

    private static class TileSurround {

        public final int[] data = new int[9];

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else {
                if (obj instanceof TileSurround) {
                    for (int i = 0; i < 3; i++) {
                        if (((TileSurround) obj).data[i] != data[i]) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }

    }

}
