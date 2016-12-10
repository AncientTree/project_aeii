package net.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Status;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.gui.MapCanvas;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 7/8/2016.
 */
public class CanvasRenderer {

    private static TextureRegion move_alpha;
    private static TextureRegion attack_alpha;

    private static float state_time = 0f;

    private static MapCanvas canvas;

    private CanvasRenderer() {
    }

    public static int ts() {
        if (canvas == null) {
            return AER.ts;
        } else {
            return canvas.ts();
        }
    }

    public static void setCanvas(MapCanvas canvas) {
        CanvasRenderer.canvas = canvas;
    }

    public static MapCanvas getCanvas() {
        return canvas;
    }

    public static void initialize() {
        Texture alpha_texture = AER.resources.getAlphaTexture();
        int size = alpha_texture.getHeight();
        move_alpha = new TextureRegion(alpha_texture, size, 0, size, size);
        attack_alpha = new TextureRegion(alpha_texture, 0, 0, size, size);
    }

    public static void drawUnit_(Batch batch, Unit unit, float screen_x, float screen_y, int frame, int ts) {
        TextureRegion unit_texture;
        if (unit.isStandby()) {
            batch.setShader(AER.resources.getGrayscaleShader(0f));
            unit_texture = AER.resources.getUnitTexture(unit.getTeam(), unit.getIndex(), 0);
        } else {
            unit_texture = AER.resources.getUnitTexture(unit.getTeam(), unit.getIndex(), frame);
        }
        batch.draw(unit_texture, screen_x, screen_y, ts, ts);
        if (unit.isCommander()) {
            drawHead_(batch, unit.getHead(), screen_x, screen_y, unit.isStandby() ? 0 : frame, ts);
        }
        batch.setShader(null);
        batch.flush();
    }

    public static void drawHead_(Batch batch, int head_index, float screen_x, float screen_y, int frame, int ts) {
        batch.draw(AER.resources.getHeadTexture(head_index),
                screen_x + ts * 7 / 24, screen_y + ts / 2 - frame * ts / 24, ts * 13 / 24, ts * 12 / 24);
    }

    public static void drawUnit(Batch batch, Unit unit, int map_x, int map_y) {
        drawUnit(batch, unit, map_x, map_y, 0f, 0f);
    }

    public static void drawUnit(Batch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        int screen_x = getCanvas().getXOnScreen(map_x);
        int screen_y = getCanvas().getYOnScreen(map_y);
        drawUnit_(batch, unit, screen_x + offset_x, screen_y + offset_y, getCurrentFrame(), ts());
    }

    public static void drawUnitWithInformation(Batch batch, Unit unit, int map_x, int map_y) {
        drawUnitWithInformation(batch, unit, map_x, map_y, 0, 0);
    }

    public static void drawUnitWithInformation(Batch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        drawUnit(batch, unit, map_x, map_y, offset_x, offset_y);

        int ts = ts();
        int screen_x = getCanvas().getXOnScreen(map_x);
        int screen_y = getCanvas().getYOnScreen(map_y);
        //draw health points
        if (unit.getCurrentHP() != unit.getMaxHP()) {
            AER.font.drawSNumber(batch, unit.getCurrentHP(), (int) (screen_x + offset_x), (int) (screen_y + offset_y));
        }
        //draw status
        int sw = ts * AER.resources.getStatusTexture(0).getRegionWidth() / 24;
        int sh = ts * AER.resources.getStatusTexture(0).getRegionHeight() / 24;
        if (unit.getStatus() != null) {
            switch (unit.getStatus().getType()) {
                case Status.POISONED:
                    batch.draw(AER.resources.getStatusTexture(0), screen_x + offset_x, screen_y + ts - sh + offset_y, sw, sh);
                    break;
                case Status.SLOWED:
                    batch.draw(AER.resources.getStatusTexture(2), screen_x + offset_x, screen_y + ts - sh + offset_y, sw, sh);
                    break;
                case Status.INSPIRED:
                    batch.draw(AER.resources.getStatusTexture(1), screen_x + offset_x, screen_y + ts - sh + offset_y, sw, sh);
                    break;
                case Status.BLINDED:
                    batch.draw(AER.resources.getStatusTexture(3), screen_x + offset_x, screen_y + ts - sh + offset_y, sw, sh);
                    break;
                default:
                    //do nothing
            }
        }
        if (unit.getLevel() > 0) {
            batch.draw(AER.resources.getLevelTexture(unit.getLevel() - 1), screen_x + ts - sw + offset_x, screen_y + ts - sh + offset_y, sw, sh);
        }
        batch.flush();
    }

    public static void drawTile(Batch batch, int index, float x, float y) {
        int ts = ts();
        int current_frame = getCurrentFrame();
        Tile tile = AER.tiles.getTile(index);
        if (tile.isAnimated()) {
            if (current_frame == 0) {
                batch.draw(AER.resources.getTileTexture(index), x, y, ts, ts);
            } else {
                batch.draw(AER.resources.getTileTexture(tile.getAnimationTileIndex()), x, y, ts, ts);
            }
        } else {
            batch.draw(AER.resources.getTileTexture(index), x, y, ts, ts);
        }
        batch.flush();
    }

    public static void drawTopTile(Batch batch, int index, float x, float y) {
        int ts = ts();
        batch.draw(AER.resources.getTopTileTexture(index), x, y, ts, ts);
        batch.flush();
    }

    public static void drawMovePath(Batch batch, Array<Position> move_path) {
        int ts = ts();
        int cursor_size = ts * 26 / 24;
        int offset = ts / 24;
        for (int i = 0; i < move_path.size; i++) {
            if (i < move_path.size - 1) {
                Position p1 = move_path.get(i);
                Position p2 = move_path.get(i + 1);
                if (p1.x == p2.x) {
                    int x = p1.x;
                    int y = p1.y > p2.y ? p1.y : p2.y;
                    int sx = getCanvas().getXOnScreen(x);
                    int sy = getCanvas().getYOnScreen(y);
                    batch.draw(AER.resources.getMovePathColor(), sx + ts / 3, sy + ts / 3, ts / 3, ts / 3 * 4);
                }
                if (p1.y == p2.y) {
                    int x = p1.x < p2.x ? p1.x : p2.x;
                    int y = p1.y;
                    int sx = getCanvas().getXOnScreen(x);
                    int sy = getCanvas().getYOnScreen(y);
                    batch.draw(AER.resources.getMovePathColor(), sx + ts / 3, sy + ts / 3, ts / 3 * 4, ts / 3);
                }
            }
        }
        if (move_path.size > 0) {
            Position dest = move_path.get(move_path.size - 1);
            int sx = canvas.getXOnScreen(dest.x);
            int sy = canvas.getYOnScreen(dest.y);
            batch.draw(AER.resources.getMoveTargetCursorTexture(), sx - offset, sy - offset, cursor_size, cursor_size);
        }
        batch.flush();
    }

    public static void drawMoveAlpha(Batch batch, ObjectSet<Position> movable_positions) {
        int ts = ts();
        for (Position position : movable_positions) {
            int screen_x = getCanvas().getXOnScreen(position.x);
            int screen_y = getCanvas().getYOnScreen(position.y);
            batch.draw(move_alpha, screen_x, screen_y, ts, ts);
        }
        batch.flush();
    }

    public static void drawAttackAlpha(Batch batch, ObjectSet<Position> attackable_positions) {
        int ts = ts();
        for (Position position : attackable_positions) {
            int screen_x = getCanvas().getXOnScreen(position.x);
            int screen_y = getCanvas().getYOnScreen(position.y);
            batch.draw(attack_alpha, screen_x, screen_y, ts, ts);
        }
        batch.flush();
    }

    public static void update(float delta) {
        state_time += delta;
    }

    private static int getCurrentFrame() {
        return ((int) (state_time / 0.3f)) % 2;
    }

}
