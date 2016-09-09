package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.SnapshotArray;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.screen.GameScreen;
import net.toyknight.aeii.utils.Platform;

import java.util.HashMap;

/**
 * @author toyknight 4/26/2015.
 */
public class ActionButtonBar extends AEIIHorizontalGroup {

    private final GameScreen screen;
    private final int PADDING_LEFT;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;

    private final HashMap<String, CircleButton> buttons;

    private final ShapeRenderer shape_renderer;

    private float margin_left;

    public ActionButtonBar(GameScreen screen) {
        super(screen.getContext());
        this.screen = screen;
        this.PADDING_LEFT = getPlatform().isDesktop() ? ts / 8 : ts / 4;
        this.BUTTON_WIDTH = getPlatform().isDesktop() ? ts / 24 * 20 : ts / 24 * 40;
        this.BUTTON_HEIGHT = getPlatform().isDesktop() ? ts / 24 * 21 : ts / 24 * 42;
        this.buttons = new HashMap<String, CircleButton>();
        this.shape_renderer = new ShapeRenderer();
        this.shape_renderer.setAutoShapeType(true);
        initComponents();
    }

    private void initComponents() {
        CircleButton btn_buy = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(0));
        btn_buy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().setState(GameManager.STATE_SELECT);
                screen.showDialog("store");
            }
        });
        buttons.put("buy", btn_buy);
        CircleButton btn_occupy = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(1));
        btn_occupy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().doOccupy();
                screen.update();
            }
        });
        buttons.put("occupy", btn_occupy);
        CircleButton btn_repair = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(1));
        btn_repair.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().doRepair();
                screen.update();
            }
        });
        buttons.put("repair", btn_repair);
        CircleButton btn_attack = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(2));
        btn_attack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().beginAttackPhase();
                screen.update();
            }
        });
        buttons.put("attack", btn_attack);
        CircleButton btn_summon = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(3));
        btn_summon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().beginSummonPhase();
                screen.update();
            }
        });
        buttons.put("summon", btn_summon);
        CircleButton btn_move = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(4));
        btn_move.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().beginMovePhase();
                screen.update();
            }
        });
        buttons.put("move", btn_move);
        CircleButton btn_standby = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(5));
        btn_standby.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().doStandbySelectedUnit();
                screen.update();
            }
        });
        buttons.put("standby", btn_standby);
        CircleButton btn_heal = new CircleButton(getContext(), CircleButton.SMALL, getResources().getActionIcon(7));
        btn_heal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().beginHealPhase();
                screen.update();
            }
        });
        buttons.put("heal", btn_heal);
    }

    private GameManager getManager() {
        return screen.getContext().getGameManager();
    }

    private Platform getPlatform() {
        return screen.getContext().getPlatform();
    }

    private GameCore getGame() {
        return getManager().getGame();
    }

    public int getButtonHeight() {
        return BUTTON_HEIGHT;
    }

    public boolean isButtonAvailable(String name) {
        return buttons.get(name).isVisible();
    }

    @Override
    public void addActor(Actor actor) {
        actor.setVisible(true);
        super.addActor(actor);
    }

    @Override
    public void clear() {
        for (CircleButton button : buttons.values()) {
            button.setVisible(false);
        }
        super.clear();
    }

    public void updateButtons() {
        this.clear();
        if (screen.canOperate()) {
            Unit selected_unit = getManager().getSelectedUnit();
            switch (getManager().getState()) {
                case GameManager.STATE_ACTION:
                    if (getManager().canSelectedUnitAct()) {
                        if (getManager().hasEnemyWithinRange(selected_unit)) {
                            addActor(buttons.get("attack"));
                        }
                        if (selected_unit.hasAbility(Ability.NECROMANCER)
                                && getManager().hasTombWithinRange(selected_unit)) {
                            addActor(buttons.get("summon"));
                        }
                        if (selected_unit.hasAbility(Ability.HEALER)
                                && getManager().hasAllyCanHealWithinRange(selected_unit)) {
                            addActor(buttons.get("heal"));
                        }
                        if (getManager().getGame().canOccupy(selected_unit, selected_unit.getX(), selected_unit.getY())) {
                            addActor(buttons.get("occupy"));
                        }
                        if (getManager().getGame().canRepair(selected_unit, selected_unit.getX(), selected_unit.getY())) {
                            addActor(buttons.get("repair"));
                        }
                    }
                    addActor(buttons.get("standby"));
                    break;
                case GameManager.STATE_BUY:
                    if (selected_unit.isCommander() && selected_unit.getTeam() == getGame().getCurrentTeam()
                            && getGame().isCastleAccessible(getGame().getMap().getTile(selected_unit.getX(), selected_unit.getY()))) {
                        addActor(buttons.get("buy"));
                        addActor(buttons.get("move"));
                        if (getManager().hasEnemyWithinRange(selected_unit)) {
                            addActor(buttons.get("attack"));
                        }
                    }
                    break;
                default:
                    //do nothing
            }
            this.layout();
        }
    }

    @Override
    public void layout() {
        SnapshotArray<Actor> children = getChildren();
        int btn_count = children.size;
        margin_left = (screen.getViewportWidth() - btn_count * BUTTON_WIDTH - (btn_count + 1) * PADDING_LEFT) / 2;
        for (int i = 0; i < btn_count; i++) {
            children.get(i).setBounds(
                    margin_left + PADDING_LEFT + i * (BUTTON_WIDTH + PADDING_LEFT), 0, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    @Override
    public void draw(Batch batch, float parent_alpha) {
        if (getChildren().size > 0) {
            if (getPlatform().isDesktop()) {
                batch.end();
                shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
                int btn_count = getChildren().size;
                int background_width = btn_count * BUTTON_WIDTH + (btn_count + 1) * PADDING_LEFT;
                int background_height = getPlatform().isDesktop() ? ts / 2 : ts;
                int background_radius = getPlatform().isDesktop() ? ts / 8 : ts / 4;
                getContext().getBorderRenderer().drawRoundedBackground(shape_renderer,
                        getX() + margin_left, getY() - ts / 12, background_width, background_height, background_radius);
                shape_renderer.end();
                batch.begin();
            }
            super.draw(batch, parent_alpha);
        }
    }

}
