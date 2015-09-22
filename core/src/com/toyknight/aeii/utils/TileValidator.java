package com.toyknight.aeii.utils;

import com.badlogic.gdx.utils.ObjectMap;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Tile;

import java.util.Arrays;

/**
 * @author toyknight 9/21/2015.
 */
public class TileValidator {

    private static ObjectMap<TileSurround, Short> water_mapping;

    private static byte W;
    private static byte L;
    private static byte N;

    public static void initialize() {
        water_mapping = new ObjectMap<TileSurround, Short>();
        W = 0;
        L = 1;
        N = -1;
        TileSurround t0 = new TileSurround(new byte[]{
                W, W, W,
                W, W, W,
                W, W, W
        });
        water_mapping.put(t0, (short) 0);
        TileSurround t3 = new TileSurround(new byte[]{
                W, W, N,
                W, W, L,
                W, W, N
        });
        water_mapping.put(t3, (short) 3);
        TileSurround t4 = new TileSurround(new byte[]{
                W, W, N,
                W, W, L,
                N, L, N
        });
        water_mapping.put(t4, (short) 4);
        TileSurround t5 = new TileSurround(new byte[]{
                W, W, W,
                W, W, W,
                W, W, L
        });
        water_mapping.put(t5, (short) 5);
        TileSurround t6 = new TileSurround(new byte[]{
                W, W, W,
                W, W, W,
                N, L, N
        });
        water_mapping.put(t6, (short) 6);
        TileSurround t7 = new TileSurround(new byte[]{
                W, W, W,
                W, W, W,
                L, W, W
        });
        water_mapping.put(t7, (short) 7);
        TileSurround t8 = new TileSurround(new byte[]{
                N, W, W,
                L, W, W,
                N, L, N
        });
        water_mapping.put(t8, (short) 8);
        TileSurround t9 = new TileSurround(new byte[]{
                N, W, W,
                L, W, W,
                N, W, W
        });
        water_mapping.put(t9, (short) 9);
        TileSurround t10 = new TileSurround(new byte[]{
                W, W, L,
                W, W, W,
                W, W, W
        });
        water_mapping.put(t10, (short) 10);
        TileSurround t11 = new TileSurround(new byte[]{
                N, L, N,
                W, W, W,
                W, W, W
        });
        water_mapping.put(t11, (short) 11);
        TileSurround t12 = new TileSurround(new byte[]{
                L, W, W,
                W, W, W,
                W, W, W
        });
        water_mapping.put(t12, (short) 12);
        TileSurround t13 = new TileSurround(new byte[]{
                N, L, N,
                W, W, L,
                W, W, N
        });
        water_mapping.put(t13, (short) 13);
        TileSurround t14 = new TileSurround(new byte[]{
                N, L, N,
                L, W, W,
                N, W, W
        });
        water_mapping.put(t14, (short) 14);
        TileSurround t46 = new TileSurround(new byte[]{
                L, W, W,
                W, W, W,
                L, W, W
        });
        water_mapping.put(t46, (short) 46);
        TileSurround t47 = new TileSurround(new byte[]{
                W, W, W,
                W, W, W,
                L, W, L
        });
        water_mapping.put(t47, (short) 47);
        TileSurround t48 = new TileSurround(new byte[]{
                W, W, L,
                W, W, W,
                W, W, L
        });
        water_mapping.put(t48, (short) 48);
        TileSurround t49 = new TileSurround(new byte[]{
                L, W, L,
                W, W, W,
                W, W, W
        });
        water_mapping.put(t49, (short) 49);
        TileSurround t50 = new TileSurround(new byte[]{
                L, W, W,
                W, W, W,
                W, W, L
        });
        water_mapping.put(t50, (short) 50);
        TileSurround t51 = new TileSurround(new byte[]{
                W, W, L,
                W, W, W,
                L, W, W
        });
        water_mapping.put(t51, (short) 51);
        TileSurround t52 = new TileSurround(new byte[]{
                L, W, L,
                W, W, W,
                L, W, W
        });
        water_mapping.put(t52, (short) 52);
        TileSurround t53 = new TileSurround(new byte[]{
                L, W, L,
                W, W, W,
                W, W, L
        });
        water_mapping.put(t53, (short) 53);
        TileSurround t54 = new TileSurround(new byte[]{
                W, W, L,
                W, W, W,
                L, W, L
        });
        water_mapping.put(t54, (short) 54);
        TileSurround t55 = new TileSurround(new byte[]{
                L, W, W,
                W, W, W,
                L, W, L
        });
        water_mapping.put(t55, (short) 55);
        TileSurround t56 = new TileSurround(new byte[]{
                L, W, L,
                W, W, W,
                L, W, L
        });
        water_mapping.put(t56, (short) 56);
        TileSurround t57 = new TileSurround(new byte[]{
                N, L, N,
                W, W, W,
                L, W, L
        });
        water_mapping.put(t57, (short) 57);
        TileSurround t58 = new TileSurround(new byte[]{
                L, W, L,
                W, W, W,
                N, L, N
        });
        water_mapping.put(t58, (short) 58);
        TileSurround t59 = new TileSurround(new byte[]{
                L, W, N,
                W, W, L,
                L, W, N
        });
        water_mapping.put(t59, (short) 59);
        TileSurround t60 = new TileSurround(new byte[]{
                N, W, L,
                L, W, W,
                N, W, L
        });
        water_mapping.put(t60, (short) 60);
        TileSurround t61 = new TileSurround(new byte[]{
                N, L, N,
                W, W, W,
                W, W, L
        });
        water_mapping.put(t61, (short) 61);
        TileSurround t62 = new TileSurround(new byte[]{
                N, L, N,
                W, W, W,
                L, W, W
        });
        water_mapping.put(t62, (short) 62);
        TileSurround t63 = new TileSurround(new byte[]{
                L, W, W,
                W, W, W,
                N, L, N
        });
        water_mapping.put(t63, (short) 63);
        TileSurround t64 = new TileSurround(new byte[]{
                W, W, L,
                W, W, W,
                N, L, N
        });
        water_mapping.put(t64, (short) 64);
        TileSurround t65 = new TileSurround(new byte[]{
                L, W, N,
                W, W, L,
                W, W, N
        });
        water_mapping.put(t65, (short) 65);
        TileSurround t66 = new TileSurround(new byte[]{
                W, W, N,
                W, W, L,
                L, W, N
        });
        water_mapping.put(t66, (short) 66);
        TileSurround t67 = new TileSurround(new byte[]{
                N, W, L,
                L, W, W,
                N, W, W
        });
        water_mapping.put(t67, (short) 67);
        TileSurround t68 = new TileSurround(new byte[]{
                N, W, W,
                L, W, W,
                N, W, L
        });
        water_mapping.put(t68, (short) 68);
        TileSurround t69 = new TileSurround(new byte[]{
                N, L, N,
                W, W, L,
                L, W, N
        });
        water_mapping.put(t69, (short) 69);
        TileSurround t70 = new TileSurround(new byte[]{
                L, W, N,
                W, W, L,
                N, L, N
        });
        water_mapping.put(t70, (short) 70);
        TileSurround t71 = new TileSurround(new byte[]{
                N, W, L,
                L, W, W,
                N, L, N
        });
        water_mapping.put(t71, (short) 71);
        TileSurround t72 = new TileSurround(new byte[]{
                N, L, N,
                L, W, W,
                N, W, L
        });
        water_mapping.put(t72, (short) 72);
        TileSurround t73 = new TileSurround(new byte[]{
                N, L, N,
                W, W, W,
                N, L, N
        });
        water_mapping.put(t73, (short) 73);
        TileSurround t74 = new TileSurround(new byte[]{
                N, W, N,
                L, W, L,
                N, W, N
        });
        water_mapping.put(t74, (short) 74);
        TileSurround t75 = new TileSurround(new byte[]{
                N, L, N,
                W, W, L,
                N, L, N
        });
        water_mapping.put(t75, (short) 75);
        TileSurround t76 = new TileSurround(new byte[]{
                N, L, N,
                L, W, W,
                N, L, N
        });
        water_mapping.put(t76, (short) 76);
        TileSurround t77 = new TileSurround(new byte[]{
                N, L, N,
                L, W, L,
                N, W, N
        });
        water_mapping.put(t77, (short) 77);
        TileSurround t78 = new TileSurround(new byte[]{
                N, W, N,
                L, W, L,
                N, L, N
        });
        water_mapping.put(t78, (short) 78);
        TileSurround t79 = new TileSurround(new byte[]{
                N, L, N,
                L, W, L,
                N, L, N
        });
        water_mapping.put(t79, (short) 79);
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
        for (TileSurround key : water_mapping.keys()) {
            if (surround.equals(key)) {
                return key;
            }
        }
        return surround;
    }

    private static boolean isCostTile(int index) {
        return (3 <= index && index <= 14) || (46 <= index && index <= 79);
    }

    private static boolean isBridge(int index) {
        return index == 28 || index == 29;
    }

    public static void validate(Map map, int x, int y) {
        short index = map.getTileIndex(x, y);
        if (TileFactory.getTile(index).getType() == Tile.TYPE_WATER) {
            TileSurround surround = createTileSurround(map, x, y);
            Short validated_index = water_mapping.get(surround);
            if (validated_index != null && !isBridge(index) && (isCostTile(index) || isCostTile(validated_index))) {
                map.setTile(validated_index, x, y);
            }
        }
    }

    private static class TileSurround {

        public final byte[] data;

        public TileSurround() {
            data = new byte[9];
        }

        public TileSurround(byte[] data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else {
                if (obj instanceof TileSurround) {
                    for (int i = 0; i < 9; i++) {
                        TileSurround surround = (TileSurround) obj;
                        if (data[i] != N && surround.data[i] != N && surround.data[i] != data[i]) {
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
