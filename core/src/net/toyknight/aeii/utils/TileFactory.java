package net.toyknight.aeii.utils;

import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.entity.Tile;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author toyknight 4/3/2015.
 */
public class TileFactory {

    private static Tile[] tile_list;

    private TileFactory() {
    }

    public static void loadTileData() throws AEIIException {
        Scanner din = new Scanner(TileFactory.class.getResourceAsStream("/data/tiles/tile_config.dat"));
        int tile_count = din.nextInt();
        din.close();
        tile_list = new Tile[tile_count];
        for (int index = 0; index < tile_count; index++) {
            InputStream tile_input =
                    TileFactory.class.getResourceAsStream("/data/tiles/tile_" + index + ".dat");
            loadTileData(tile_input, index);
        }
    }

    private static void loadTileData(InputStream is, int index) throws AEIIException {
        try {
            Scanner din = new Scanner(is);
            int defence_bonus = din.nextInt();
            int step_cost = din.nextInt();
            int hp_recovery = din.nextInt();
            int type = din.nextInt();
            int top_tile_index = din.nextInt();
            int team = din.nextInt();
            tile_list[index] = new Tile(defence_bonus, step_cost, type);
            tile_list[index].setTopTileIndex(top_tile_index);
            tile_list[index].setHpRecovery(hp_recovery);
            tile_list[index].setTeam(team);
            int access_tile_count = din.nextInt();
            if (access_tile_count > 0) {
                int[] access_tile_list = new int[access_tile_count];
                for (int n = 0; n < access_tile_count; n++) {
                    access_tile_list[n] = din.nextInt();
                }
                tile_list[index].setAccessTileList(access_tile_list);
            }
            boolean is_capturable = din.nextBoolean();
            tile_list[index].setCapturable(is_capturable);
            if (is_capturable) {
                short[] captured_tile_list = new short[5];
                for (int t = 0; t < 5; t++) {
                    captured_tile_list[t] = din.nextShort();
                }
                tile_list[index].setCapturedTileList(captured_tile_list);
            }
            boolean is_destroyable = din.nextBoolean();
            tile_list[index].setDestroyable(is_destroyable);
            if (is_destroyable) {
                tile_list[index].setDestroyedTileIndex(din.nextShort());
            }
            boolean is_repairable = din.nextBoolean();
            tile_list[index].setRepairable(is_repairable);
            if (is_repairable) {
                tile_list[index].setRepairedTileIndex(din.nextShort());
            }
            boolean is_animated = din.nextBoolean();
            tile_list[index].setAnimated(is_animated);
            if (is_animated) {
                tile_list[index].setAnimationTileIndex(din.nextShort());
            }
            int mini_map_index = din.nextInt();
            tile_list[index].setMiniMapIndex(mini_map_index);
            boolean is_castle = din.nextBoolean();
            tile_list[index].setCastle(is_castle);
            boolean is_village = din.nextBoolean();
            tile_list[index].setVillage(is_village);
            din.close();
        } catch (java.util.NoSuchElementException ex) {
            throw new AEIIException("bad tile data file!");
        }
    }

    public static Tile getTile(int index) {
        return tile_list[index];
    }

    public static int getTileCount() {
        return tile_list.length;
    }

    public static String getVerificationString() {
        String str = "";
        for (Tile tile : tile_list) {
            str += tile.getVerification();
        }
        return str;
    }

}
