package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.FontRenderer;
import com.toyknight.aeii.serializable.PlayerSnapshot;
import com.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 10/2/2015.
 */
public class PlayerList extends StringList<PlayerSnapshot> {

    private final int ts;

    private final int big_circle_width;
    private final int big_circle_height;
    private final int bc_offset;
    private final int unit_offset;

    private String[] allocation;

    public PlayerList(int item_height, int ts) {
        super(item_height);
        this.ts = ts;
        this.big_circle_width = ts * 32 / 24;
        this.big_circle_height = ts * 33 / 24;
        this.bc_offset = (item_height - big_circle_height) / 2;
        this.unit_offset = (big_circle_height - ts) / 2;
    }

    public void setItems(Array<PlayerSnapshot> items, String[] allocation) {
        setItems(items);
        this.allocation = allocation;
    }

    public boolean hasTeamAccess(String service_name, int team) {
        return allocation[team].equals(service_name);
    }

    public void removePlayer(String service_name) {
        int index = -1;
        for (int i = 0; i < items.size; i++) {
            if (items.get(i).service_name.equals(service_name)) {
                index = i;
            }
        }
        if (index >= 0) {
            items.removeIndex(index);
            for (int team = 0; team < 4; team++) {
                if (allocation[team].equals(service_name)) {
                    allocation[team] = "NONE";
                }
            }
            updateList();
        }
    }

    public void addPlayer(String service_name, String username) {
        PlayerSnapshot player = new PlayerSnapshot();
        player.service_name = service_name;
        player.username = username;
        player.is_host = false;
        items.add(player);
        updateList();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth();
        float itemY = getHeight();
        for (int index = 0; index < items.size; index++) {
            PlayerSnapshot item = items.get(index);
            if (selection.contains(item)) {
                batch.draw(ResourceManager.getListSelectedBackground(), x, y + itemY - item_height, width, item_height);
            }
            FontRenderer.setTextColor(Color.WHITE);
            String user_string = item.toString();
            FontRenderer.drawText(batch, user_string, x + ts / 4, y + itemY - item_height + text_offset + FontRenderer.getTextFont().getCapHeight());

            float s_width = FontRenderer.getTextLayout(user_string).width;
            int ti = 0;
            for (int team = 0; team < 4; team++) {
                if (hasTeamAccess(item.service_name, team)) {
                    batch.draw(ResourceManager.getBigCircleTexture(0),
                            x + ts / 4 + s_width + bc_offset + ti * (bc_offset + big_circle_width),
                            y + itemY - item_height + bc_offset,
                            big_circle_width, big_circle_height);
                    batch.draw(ResourceManager.getUnitTexture(team, UnitFactory.getCommanderIndex(), 0, 0),
                            x + ts / 4 + s_width + bc_offset + ti * (bc_offset + big_circle_width) + unit_offset,
                            y + itemY - item_height + bc_offset + unit_offset,
                            ts, ts);
                    ti++;
                }
            }
            itemY -= item_height;
            batch.flush();
        }
    }

}
