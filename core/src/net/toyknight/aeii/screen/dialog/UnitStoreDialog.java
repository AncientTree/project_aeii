package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.screen.StageScreen;
import net.toyknight.aeii.screen.widgets.SmallCircleLabel;
import net.toyknight.aeii.screen.widgets.UnitListListener;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.screen.GameScreen;
import net.toyknight.aeii.screen.widgets.AvailableUnitList;
import net.toyknight.aeii.utils.Language;
import net.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 4/20/2015.
 */
public class UnitStoreDialog extends BasicDialog implements UnitListListener {

    private Label label_price;
    private Label label_occupancy;
    private Label label_attack_range;

    private SmallCircleLabel label_attack;
    private SmallCircleLabel label_move;
    private SmallCircleLabel label_physical_defence;
    private SmallCircleLabel label_magic_defence;

    private Label label_description;

    private TextButton btn_recruit;
    private AvailableUnitList unit_list;
    private ScrollPane sp_unit_list;

    private int castle_x;
    private int castle_y;

    private Unit selected_unit;

    public UnitStoreDialog(StageScreen owner) {
        super(owner);
        int UNIT_STORE_WIDTH = 11 * ts;
        int UNIT_STORE_HEIGHT = ts + ts * 3 / 2 * 5;
        this.setBounds(
                (getOwner().getViewportWidth() - UNIT_STORE_WIDTH) / 2,
                (getOwner().getViewportHeight() - UNIT_STORE_HEIGHT) / 2 + ts,
                UNIT_STORE_WIDTH, UNIT_STORE_HEIGHT);
        this.initComponents(getContext().getSkin());
    }

    private void initComponents(Skin skin) {
        this.unit_list = new AvailableUnitList(getContext());
        this.unit_list.setUnitListListener(this);
        sp_unit_list = new ScrollPane(unit_list, skin) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        getResources().getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_unit_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(getResources().getListBackground()));
        sp_unit_list.setScrollBarPositions(false, true);
        add(sp_unit_list).size(ts * 5, ts * 3 / 2 * 5);

        Table information_pane = new Table();

        Table hud_pane = new Table();

        int hs = ts * 11 / 24;

        Image image_price = new Image(ResourceManager.createDrawable(getResources().getStatusHudIcon(1), hs, hs));
        hud_pane.add(image_price);
        label_price = new Label("", getContext().getSkin());
        hud_pane.add(label_price).width(ts).height(hs);

        Image image_attack_range = new Image(ResourceManager.createDrawable(getResources().getStatusHudIcon(2), hs, hs));
        hud_pane.add(image_attack_range);
        label_attack_range = new Label("", getContext().getSkin());
        hud_pane.add(label_attack_range).width(ts).height(hs);

        Image image_occupancy = new Image(ResourceManager.createDrawable(getResources().getStatusHudIcon(0), hs, hs));
        hud_pane.add(image_occupancy);
        label_occupancy = new Label("", getContext().getSkin());
        hud_pane.add(label_occupancy).width(ts).height(hs);

        information_pane.add(hud_pane).width(ts * 4 + ts / 2).padBottom(ts / 8).row();

        Table data_pane = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };

        label_attack = new SmallCircleLabel(getContext(), getResources().getBattleHudIcon(0));
        data_pane.add(label_attack).width(ts * 2 + ts / 8).padTop(ts / 8);
        label_move = new SmallCircleLabel(getContext(), getResources().getActionIcon(4));
        label_move.setTextColor(Color.WHITE);
        data_pane.add(label_move).width(ts * 2 + ts / 8).padLeft(ts / 4).padTop(ts / 8).row();
        label_physical_defence = new SmallCircleLabel(getContext(), getResources().getBattleHudIcon(1));
        label_physical_defence.setTextColor(Color.WHITE);
        data_pane.add(label_physical_defence).width(ts * 2 + ts / 8).padTop(ts / 4).padBottom(ts / 8);
        label_magic_defence = new SmallCircleLabel(getContext(), getResources().getBattleHudIcon(2));
        label_magic_defence.setTextColor(Color.WHITE);
        data_pane.add(label_magic_defence).width(ts * 2 + ts / 8).padLeft(ts / 4).padTop(ts / 4).padBottom(ts / 8);

        information_pane.add(data_pane).width(ts * 4 + ts / 2).row();

        label_description = new Label("", getContext().getSkin());
        label_description.setWrap(true);
        float description_height = ts * 3 / 2 * 5 - hud_pane.getPrefHeight() - data_pane.getPrefHeight() - ts - ts / 4;
        ScrollPane sp_description = new ScrollPane(label_description);
        information_pane.add(sp_description).size(ts * 4 + ts / 2, description_height).padTop(ts / 9).padBottom(ts / 8).row();

        Table button_pane = new Table();

        this.btn_recruit = new TextButton(Language.getText("LB_RECRUIT"), skin);
        this.btn_recruit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().doBuyUnit(selected_unit.getIndex(), castle_x, castle_y);
                getOwner().closeDialog("store");
            }
        });
        button_pane.add(btn_recruit).size(ts * 2, ts);

        TextButton btn_close = new TextButton(Language.getText("LB_CLOSE"), skin);
        btn_close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("store");
            }
        });
        button_pane.add(btn_close).size(ts * 2, ts).padLeft(ts / 2);

        information_pane.add(button_pane).size(ts * 4 + ts / 2, ts);

        add(information_pane).size(ts * 5 - ts / 2, ts * 3 / 2 * 5).padLeft(ts / 2);
    }

    public GameScreen getOwner() {
        return (GameScreen) super.getOwner();
    }

    private GameCore getGame() {
        return getOwner().getGame();
    }

    private GameManager getManager() {
        return getOwner().getContext().getGameManager();
    }

    @Override
    public void display() {
        this.castle_x = getOwner().getCursorMapX();
        this.castle_y = getOwner().getCursorMapY();
        this.unit_list.setGame(getGame());
        Array<Integer> available_units = getManager().getGame().getRule().getAvailableUnits();
        unit_list.setAvailableUnits(available_units);
        sp_unit_list.layout();
    }

    @Override
    public void onUnitSelected(int index) {
        selected_unit = UnitFactory.getSample(index);
        if (selected_unit.isCommander()) {
            selected_unit = getGame().getCommander(selected_unit.getTeam());
        }
        update();
    }

    private void update() {
        int current_team = getGame().getCurrentTeam();
        if (selected_unit != null) {
            btn_recruit.setVisible(getManager().canBuy(selected_unit.getIndex(), current_team, castle_x, castle_y));
            int price = getGame().getUnitPrice(selected_unit.getIndex(), current_team);
            label_price.setText(price > 0 ? Integer.toString(price) : "-");
            label_occupancy.setText(Integer.toString(selected_unit.getOccupancy()));
            label_attack_range.setText(selected_unit.getMinAttackRange() + "-" + selected_unit.getMaxAttackRange());
            label_attack.setText(Integer.toString(selected_unit.getAttack()));
            label_attack.setTextColor(selected_unit.getAttackType() == Unit.ATTACK_PHYSICAL ?
                    getResources().getPhysicalAttackColor() : getResources().getMagicalAttackColor());
            label_move.setText(Integer.toString(selected_unit.getMovementPoint()));
            label_physical_defence.setText(Integer.toString(selected_unit.getPhysicalDefence()));
            label_magic_defence.setText(Integer.toString(selected_unit.getMagicDefence()));
            label_description.setText(Language.getUnitDescription(selected_unit.getIndex()));
        } else {
            btn_recruit.setVisible(false);
        }
    }

}
