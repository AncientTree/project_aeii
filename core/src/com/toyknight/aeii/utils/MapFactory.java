package com.toyknight.aeii.utils;

import com.badlogic.gdx.files.FileHandle;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Unit;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Created by toyknight on 4/3/2015.
 */
public class MapFactory {

    private MapFactory() {
    }

    public static Map createMap(FileHandle map_file) throws AEIIException {
        try {
            DataInputStream fis = new DataInputStream(map_file.read());
            String author_name = fis.readUTF();
            boolean[] team_access = new boolean[4];
            for (int team = 0; team < 4; team++) {
                team_access[team] = fis.readBoolean();
            }
            int width = fis.readInt();
            int height = fis.readInt();
            short[][] map_data = new short[width][height];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    map_data[i][j] = fis.readShort();
                }
            }
            Map map = new Map(map_data, team_access, author_name);
            int unit_count = fis.readInt();
            for (int i = 0; i < unit_count; i++) {
                int team = fis.readInt();
                int index = fis.readInt();
                int x = fis.readInt();
                int y = fis.readInt();
                Unit unit = UnitFactory.createUnit(index, team);
                unit.setX(x);
                unit.setY(y);
                map.addUnit(unit);
            }
            fis.close();
            return map;
        } catch(IOException | NoSuchElementException ex) {
            throw new AEIIException("broken map file!");
        }
    }

}
