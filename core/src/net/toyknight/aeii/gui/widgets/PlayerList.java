package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.Player;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.manager.RoomManager;
import net.toyknight.aeii.network.entity.PlayerSnapshot;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 10/2/2015.
 */
public class PlayerList extends StringList<PlayerSnapshot> {

    private final RoomManager room_manager;

    private final Unit[] commanders;

    private final int big_circle_width;
    private final int big_circle_height;
    private final int bc_offset;
    private final int unit_offset;

    public PlayerList(GameContext context, int item_height, int ts) {
        super(item_height);
        this.room_manager = context.getRoomManager();

        this.commanders = new Unit[4];
        for (int team = 0; team < 4; team++) {
            commanders[team] = AER.units.createCommander(team);
        }
        this.big_circle_width = ts * 32 / 24;
        this.big_circle_height = ts * 33 / 24;
        this.bc_offset = (item_height - big_circle_height) / 2;
        this.unit_offset = (big_circle_height - ts) / 2;
    }

    private RoomManager getRoomManager() {
        return room_manager;
    }

    private boolean hasTeamAccess(int id, int team) {
        return getRoomManager().getAllocation(team) == id;
    }

    private int getPlayerType(int team) {
        return getRoomManager().getGame().getPlayer(team).getType();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int ts = AER.ts;
        float x = getX(), y = getY(), width = getWidth();
        float itemY = getHeight();
        for (int index = 0; index < items.size; index++) {
            PlayerSnapshot player = items.get(index);
            if (selection.contains(player)) {
                batch.draw(AER.resources.getListSelectedBackground(), x, y + itemY - item_height, width, item_height);
            }
            AER.font.setTextColor(Color.WHITE);
            String user_string = player.toString();
            AER.font.drawText(batch, user_string,
                    x + ts / 4, y + itemY - item_height + text_offset + AER.resources.getTextFont().getCapHeight());

            float s_width = AER.font.getTextLayout(user_string).width;
            int ti = 0;
            for (int team = 0; team < 4; team++) {
                if (hasTeamAccess(player.id, team) && getPlayerType(team) != Player.ROBOT) {
                    batch.draw(AER.resources.getBigCircleTexture(0),
                            x + ts / 4 + s_width + bc_offset + ti * (bc_offset + big_circle_width),
                            y + itemY - item_height + bc_offset,
                            big_circle_width, big_circle_height);
                    CanvasRenderer.drawUnit_(batch, commanders[team],
                            x + ts / 4 + s_width + bc_offset + ti * (bc_offset + big_circle_width) + unit_offset,
                            y + itemY - item_height + bc_offset + unit_offset,
                            0, ts);
                    ti++;
                }
            }
            itemY -= item_height;
            batch.flush();
        }
    }

}
