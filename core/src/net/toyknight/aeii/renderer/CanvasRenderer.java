package net.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Status;
import net.toyknight.aeii.entity.Tile;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.screen.MapCanvas;
import net.toyknight.aeii.utils.TileFactory;

/**
 * @author toyknight 7/8/2016.
 */
public class CanvasRenderer {

    private final GameContext context;

    private final TextureRegion move_alpha;
    private final TextureRegion attack_alpha;

    private MapCanvas canvas;

    private float state_time = 0f;

    public CanvasRenderer(GameContext context) {
        this.context = context;

        Texture alpha_texture = getResources().getAlphaTexture();
        int size = alpha_texture.getHeight();
        move_alpha = new TextureRegion(alpha_texture, size, 0, size, size);
        attack_alpha = new TextureRegion(alpha_texture, 0, 0, size, size);
    }

    public int ts() {
        if (canvas == null) {
            return getContext().getTileSize();
        } else {
            return canvas.ts();
        }
    }

    public void setCanvas(MapCanvas canvas) {
        this.canvas = canvas;
    }

    public MapCanvas getCanvas() {
        return canvas;
    }

    public GameContext getContext() {
        return context;
    }

    public ResourceManager getResources() {
        return getContext().getResources();
    }

    public void drawUnit_(Batch batch, Unit unit, float screen_x, float screen_y, int frame, int ts) {
        TextureRegion unit_texture;
        if (unit.isStandby()) {
            batch.setShader(getResources().getGrayscaleShader(0f));
            unit_texture = getResources().getUnitTexture(unit.getTeam(), unit.getIndex(), 0);
        } else {
            unit_texture = getResources().getUnitTexture(unit.getTeam(), unit.getIndex(), frame);
        }
        batch.draw(unit_texture, screen_x, screen_y, ts, ts);
        if (unit.isCommander()) {
            drawHead(batch, unit.getHead(), screen_x, screen_y, frame, ts);
        }
        batch.setShader(null);
        batch.flush();
    }

    public void drawHead(Batch batch, int head_index, float screen_x, float screen_y, int frame, int ts) {
        batch.draw(getResources().getHeadTexture(head_index),
                screen_x + ts / 24 * 7, screen_y + ts / 2 - frame * ts / 24, ts * 13 / 24, ts * 12 / 24);
    }

    public void drawUnit(Batch batch, Unit unit, int map_x, int map_y) {
        drawUnit(batch, unit, map_x, map_y, 0f, 0f);
    }

    public void drawUnit(Batch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        int screen_x = canvas.getXOnScreen(map_x);
        int screen_y = canvas.getYOnScreen(map_y);
        drawUnit_(batch, unit, screen_x + offset_x, screen_y + offset_y, getCurrentFrame(), ts());
    }

    public void drawUnitWithInformation(Batch batch, Unit unit, int map_x, int map_y) {
        drawUnitWithInformation(batch, unit, map_x, map_y, 0, 0);
    }

    public void drawUnitWithInformation(Batch batch, Unit unit, int map_x, int map_y, float offset_x, float offset_y) {
        drawUnit(batch, unit, map_x, map_y, offset_x, offset_y);
        int screen_x = canvas.getXOnScreen(map_x);
        int screen_y = canvas.getYOnScreen(map_y);
        //draw health points
        if (unit.getCurrentHp() != unit.getMaxHp()) {
            getContext().getFontRenderer().drawSNumber(
                    batch, unit.getCurrentHp(), (int) (screen_x + offset_x), (int) (screen_y + offset_y));
        }
        //draw status
        int sw = ts() * getResources().getStatusTexture(0).getRegionWidth() / 24;
        int sh = ts() * getResources().getStatusTexture(0).getRegionHeight() / 24;
        if (unit.getStatus() != null) {
            switch (unit.getStatus().getType()) {
                case Status.POISONED:
                    batch.draw(getResources().getStatusTexture(0), screen_x + offset_x, screen_y + ts() - sh + offset_y, sw, sh);
                    break;
                case Status.SLOWED:
                    batch.draw(getResources().getStatusTexture(2), screen_x + offset_x, screen_y + ts() - sh + offset_y, sw, sh);
                    break;
                case Status.INSPIRED:
                    batch.draw(getResources().getStatusTexture(1), screen_x + offset_x, screen_y + ts() - sh + offset_y, sw, sh);
                    break;
                case Status.BLINDED:
                    batch.draw(getResources().getStatusTexture(3), screen_x + offset_x, screen_y + ts() - sh + offset_y, sw, sh);
                    break;
                default:
                    //do nothing
            }
        }
        batch.flush();
    }

    public void drawTile(SpriteBatch batch, int index, float x, float y) {
        int current_frame = getCurrentFrame();
        Tile tile = TileFactory.getTile(index);
        if (tile.isAnimated()) {
            if (current_frame == 0) {
                batch.draw(getResources().getTileTexture(index), x, y, ts(), ts());
            } else {
                batch.draw(getResources().getTileTexture(tile.getAnimationTileIndex()), x, y, ts(), ts());
            }
        } else {
            batch.draw(getResources().getTileTexture(index), x, y, ts(), ts());
        }
        batch.flush();
    }

    public void drawTopTile(SpriteBatch batch, int index, float x, float y) {
        batch.draw(getResources().getTopTileTexture(index), x, y, ts(), ts());
        batch.flush();
    }

    public void drawMovePath(SpriteBatch batch, Array<Position> move_path) {
        int cursor_size = ts() * 26 / 24;
        int offset = ts() / 24;
        for (int i = 0; i < move_path.size; i++) {
            if (i < move_path.size - 1) {
                Position p1 = move_path.get(i);
                Position p2 = move_path.get(i + 1);
                if (p1.x == p2.x) {
                    int x = p1.x;
                    int y = p1.y > p2.y ? p1.y : p2.y;
                    int sx = getCanvas().getXOnScreen(x);
                    int sy = getCanvas().getYOnScreen(y);
                    batch.draw(getResources().getMovePathColor(), sx + ts() / 3, sy + ts() / 3, ts() / 3, ts() / 3 * 4);
                }
                if (p1.y == p2.y) {
                    int x = p1.x < p2.x ? p1.x : p2.x;
                    int y = p1.y;
                    int sx = getCanvas().getXOnScreen(x);
                    int sy = getCanvas().getYOnScreen(y);
                    batch.draw(getResources().getMovePathColor(), sx + ts() / 3, sy + ts() / 3, ts() / 3 * 4, ts() / 3);
                }
            }
        }
        if (move_path.size > 0) {
            Position dest = move_path.get(move_path.size - 1);
            int sx = getCanvas().getXOnScreen(dest.x);
            int sy = getCanvas().getYOnScreen(dest.y);
            batch.draw(getResources().getMoveTargetCursorTexture(), sx - offset, sy - offset, cursor_size, cursor_size);
        }
        batch.flush();
    }

    public void drawMoveAlpha(SpriteBatch batch, ObjectSet<Position> movable_positions) {
        for (Position position : movable_positions) {
            int screen_x = getCanvas().getXOnScreen(position.x);
            int screen_y = getCanvas().getYOnScreen(position.y);
            batch.draw(move_alpha, screen_x, screen_y, ts(), ts());
        }
        batch.flush();
    }

    public void drawAttackAlpha(SpriteBatch batch, ObjectSet<Position> attackable_positions) {
        for (Position position : attackable_positions) {
            int screen_x = getCanvas().getXOnScreen(position.x);
            int screen_y = getCanvas().getYOnScreen(position.y);
            batch.draw(attack_alpha, screen_x, screen_y, ts(), ts());
        }
        batch.flush();
    }

    private int getCurrentFrame() {
        return ((int) (state_time / 0.3f)) % 2;
    }

    public void update(float delta) {
        state_time += delta;
    }

}
