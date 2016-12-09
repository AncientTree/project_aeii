package net.toyknight.aeii.system;

import net.toyknight.aeii.GameException;
import net.toyknight.aeii.entity.Tile;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author toyknight 12/9/2016.
 */
public class Tiles {

    private static Tile[] tiles;

    protected Tiles() {
    }

    public void initialize() throws GameException {
        //read configuration
        Scanner din = new Scanner(Tiles.class.getResourceAsStream("/data/tiles/tile_config.dat"));
        int tile_count = din.nextInt();
        din.close();

        //read tile data
        tiles = new Tile[tile_count];
        for (int index = 0; index < tile_count; index++) {
            InputStream tile_input =
                    Tiles.class.getResourceAsStream("/data/tiles/tile_" + index + ".dat");
            readTileData(tile_input, index);
        }
    }

    private void readTileData(InputStream is, int index) throws GameException {
        try {
            Scanner din = new Scanner(is);
            int defence_bonus = din.nextInt();
            int step_cost = din.nextInt();
            int hp_recovery = din.nextInt();
            int type = din.nextInt();
            int top_tile_index = din.nextInt();
            int team = din.nextInt();
            tiles[index] = new Tile(defence_bonus, step_cost, type);
            tiles[index].setTopTileIndex(top_tile_index);
            tiles[index].setHpRecovery(hp_recovery);
            tiles[index].setTeam(team);
            int access_tile_count = din.nextInt();
            if (access_tile_count > 0) {
                int[] access_tile_list = new int[access_tile_count];
                for (int n = 0; n < access_tile_count; n++) {
                    access_tile_list[n] = din.nextInt();
                }
                tiles[index].setAccessTileList(access_tile_list);
            }
            boolean is_capturable = din.nextBoolean();
            tiles[index].setCapturable(is_capturable);
            if (is_capturable) {
                short[] captured_tile_list = new short[5];
                for (int t = 0; t < 5; t++) {
                    captured_tile_list[t] = din.nextShort();
                }
                tiles[index].setCapturedTileList(captured_tile_list);
            }
            boolean is_destroyable = din.nextBoolean();
            tiles[index].setDestroyable(is_destroyable);
            if (is_destroyable) {
                tiles[index].setDestroyedTileIndex(din.nextShort());
            }
            boolean is_repairable = din.nextBoolean();
            tiles[index].setRepairable(is_repairable);
            if (is_repairable) {
                tiles[index].setRepairedTileIndex(din.nextShort());
            }
            boolean is_animated = din.nextBoolean();
            tiles[index].setAnimated(is_animated);
            if (is_animated) {
                tiles[index].setAnimationTileIndex(din.nextShort());
            }
            int mini_map_index = din.nextInt();
            tiles[index].setMiniMapIndex(mini_map_index);
            boolean is_castle = din.nextBoolean();
            tiles[index].setCastle(is_castle);
            boolean is_village = din.nextBoolean();
            tiles[index].setVillage(is_village);
            if (din.hasNextBoolean()) {
                boolean is_temple = din.nextBoolean();
                tiles[index].setTemple(is_temple);
            }
            din.close();
        } catch (java.util.NoSuchElementException ex) {
            throw new GameException("bad tile data file!");
        }
    }

    public Tile getTile(int index) {
        return tiles[index];
    }

    public int getTileCount() {
        return tiles.length;
    }

    public String getVerificationString() {
        String str = "";
        for (Tile tile : tiles) {
            str += tile.getVerification();
        }
        return str;
    }

}
