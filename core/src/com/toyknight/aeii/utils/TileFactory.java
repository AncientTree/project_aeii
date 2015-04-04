package com.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.entity.Tile;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by toyknight on 4/3/2015.
 */
public class TileFactory {

    private static Tile[] tile_list;

    private TileFactory() {
    }

    public static void loadTileData() throws AEIIException {
        String tile_data_dir = "data/tiles/";
        FileHandle tile_config = Gdx.files.internal(tile_data_dir + "tile_config.dat");
        if (tile_config.exists()) {
            try {
                Scanner din = new Scanner(tile_config.read());
                int tile_count = din.nextInt();
                din.close();
                tile_list = new Tile[tile_count];
                for (int index = 0; index < tile_count; index++) {
                    FileHandle tile_data = Gdx.files.internal(tile_data_dir + "tile_" + index + ".dat");
                    if (tile_data.exists()) {
                        loadTileData(tile_data, index);
                    } else {
                        throw new AEIIException("tile_" + index + ".dat not found!");
                    }
                }
            } catch (NoSuchElementException ex) {
                throw new AEIIException("tile_config.dat is broken!");
            }
        } else {
            throw new AEIIException("tile_config.dat not found!");
        }
    }

    private static void loadTileData(FileHandle data, int index) throws AEIIException {
        try {
            Scanner din = new Scanner(data.read());
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

}
