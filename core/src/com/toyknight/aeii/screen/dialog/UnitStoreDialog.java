package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.widgets.SmallCircleLabel;
import com.toyknight.aeii.screen.widgets.UnitListListener;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.screen.widgets.AvailableUnitList;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.UnitFactory;

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
        this.unit_list = new AvailableUnitList(ts);
        this.unit_list.setUnitListListener(this);
        ScrollPane sp_unit_list = new ScrollPane(unit_list, skin) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_unit_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_unit_list.setScrollBarPositions(false, true);
        add(sp_unit_list).size(ts * 5, ts * 3 / 2 * 5);

        Table information_pane = new Table();

        Table hud_pane = new Table();

        int hs = ts * 11 / 24;

        Image image_price = new Image(ResourceManager.createDrawable(ResourceManager.getStatusHudIcon(1), hs, hs));
        hud_pane.add(image_price);
        label_price = new Label("", getContext().getSkin());
        hud_pane.add(label_price).width(ts);

        Image image_attack_range = new Image(ResourceManager.createDrawable(ResourceManager.getStatusHudIcon(2), hs, hs));
        hud_pane.add(image_attack_range);
        label_attack_range = new Label("", getContext().getSkin());
        hud_pane.add(label_attack_range).width(ts);

        Image image_occupancy = new Image(ResourceManager.createDrawable(ResourceManager.getStatusHudIcon(0), hs, hs));
        hud_pane.add(image_occupancy);
        label_occupancy = new Label("", getContext().getSkin());
        hud_pane.add(label_occupancy).width(ts);

        information_pane.add(hud_pane).width(ts * 4 + ts / 2).padBottom(ts / 8).row();

        Table data_pane = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };

        label_attack = new SmallCircleLabel(ResourceManager.getBattleHudIcon(0), ts);
        data_pane.add(label_attack).width(ts * 2 + ts / 8).padTop(ts / 8);
        label_move = new SmallCircleLabel(ResourceManager.getActionIcon(4), ts);
        label_move.setTextColor(Color.WHITE);
        data_pane.add(label_move).width(ts * 2 + ts / 8).padLeft(ts / 4).padTop(ts / 8).row();
        label_physical_defence = new SmallCircleLabel(ResourceManager.getBattleHudIcon(1), ts);
        label_physical_defence.setTextColor(Color.WHITE);
        data_pane.add(label_physical_defence).width(ts * 2 + ts / 8).padTop(ts / 4).padBottom(ts / 8);
        label_magic_defence = new SmallCircleLabel(ResourceManager.getBattleHudIcon(2), ts);
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
        GameManager manager = getManager();
        Array<Integer> available_units = manager.getGame().getRule().getAvailableUnits();
        unit_list.setAvailableUnits(available_units);
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
                    ResourceManager.getPhysicalAttackColor() : ResourceManager.getMagicalAttackColor());
            label_move.setText(Integer.toString(selected_unit.getMovementPoint()));
            label_physical_defence.setText(Integer.toString(selected_unit.getPhysicalDefence()));
            label_magic_defence.setText(Integer.toString(selected_unit.getMagicDefence()));
            label_description.setText(Language.getUnitDescription(selected_unit.getIndex()));
        } else {
            btn_recruit.setVisible(false);
        }
    }

    @Override
    protected void drawCustom(Batch batch, float parentAlpha) {
//        float x = getX(), y = getY(), height = getHeight();
//        int interval = ts * 13 / 24;
//        int lw = FontRenderer.getLNumberWidth(0, false);
//        int lh = FontRenderer.getLCharHeight();
//        int current_team = getGame().getCurrentTeam();
//        int price = getGame().getUnitPrice(selected_unit.getIndex(), current_team);
//        //price
//        batch.draw(ResourceManager.getStatusHudIcon(1),
//                x + ts * 10 + ts / 2 - lw * 4 - 11 * ts / 24,
//                y + height - ts / 2 - lh,
//                11 * ts / 24,
//                11 * ts / 24);
//
//        if (price >= 0) {
//            FontRenderer.drawLNumber(batch,
//                    price,
//                    x + ts * 10 + ts / 2 - lw * 4,
//                    y + height - (ts / 2 + (interval - lh) / 2 + lh) + ts / 24);
//        } else {
//            batch.draw(FontRenderer.getLMinus(),
//                    x + ts * 10 + ts / 2 - lw * 2 - lw / 2,
//                    y + height - (ts / 2 + (interval - lh) / 2 + lh + ts / 24),
//                    lw, lh);
//        }
//        //occupancy
//        batch.draw(ResourceManager.getStatusHudIcon(0),
//                x + ts * 10 + ts / 2 - lw * 7 - 11 * ts / 24,
//                y + height - ts / 2 - lh,
//                11 * ts / 24,
//                11 * ts / 24);
//        FontRenderer.drawLNumber(batch,
//                selected_unit.getOccupancy(),
//                x + ts * 10 + ts / 2 - lw * 7,
//                y + height - (ts / 2 + (interval - lh) / 2 + lh) + ts / 24);
//        //split line
//        batch.draw(ResourceManager.getWhiteColor(),
//                x + ts * 6,
//                y + height - (ts / 2 + interval),
//                ts * 4 + ts / 2,
//                1);
//        int scw = ts * 20 / 24;
//        int sch = ts * 21 / 24;
//        int acs = ts * 16 / 24;
//        int hw = ts * 13 / 24;
//        int hh = ts * 16 / 24;
//        int item_h = sch + ts / 6;
//        int tfh = sch - ts / 4;
//        float lbh = ResourceManager.getTextFont().getCapHeight();
//        //attack
//        batch.draw(ResourceManager.getTextBackground(),
//                x + ts * 6 + scw / 2,
//                y + height - (ts / 2 + interval + (item_h - tfh) / 2 + tfh),
//                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
//        if (selected_unit.getAttackType() == Unit.ATTACK_PHYSICAL) {
//            FontRenderer.setTextColor(ResourceManager.getPhysicalAttackColor());
//        } else {
//            FontRenderer.setTextColor(ResourceManager.getMagicalAttackColor());
//        }
//        FontRenderer.drawText(batch,
//                Integer.toString(selected_unit.getAttack()),
//                x + ts * 6 + scw + ts / 12,
//                y + height - (ts / 2 + interval + (item_h - lbh) / 2));
//        batch.draw(ResourceManager.getSmallCircleTexture(0),
//                x + ts * 6,
//                y + height - (ts / 2 + interval + (item_h - sch) / 2 + sch),
//                20 * ts / 24,
//                21 * ts / 24);
//        batch.draw(ResourceManager.getBattleHudIcon(0),
//                x + ts * 6 + (scw - hw) / 2,
//                y + height - (ts / 2 + interval + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
//                hw, hh);
//        FontRenderer.setTextColor(Color.WHITE);
//        //movement point
//        batch.draw(ResourceManager.getTextBackground(),
//                x + ts * 8 + ts / 4 + scw / 2,
//                y + height - (ts / 2 + interval + (item_h - tfh) / 2 + tfh),
//                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
//        FontRenderer.drawText(batch,
//                Integer.toString(selected_unit.getMovementPoint()),
//                x + ts * 8 + ts / 4 + scw + ts / 12,
//                y + height - (ts / 2 + interval + (item_h - lbh) / 2));
//        batch.draw(ResourceManager.getSmallCircleTexture(0),
//                x + ts * 8 + ts / 4,
//                y + height - (ts / 2 + interval + (item_h - sch) / 2 + sch),
//                20 * ts / 24,
//                21 * ts / 24);
//        batch.draw(ResourceManager.getActionIcon(4),
//                x + ts * 8 + ts / 4 + (scw - acs) / 2,
//                y + height - (ts / 2 + interval + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
//                acs, acs);
//        //physical defence
//        batch.draw(ResourceManager.getTextBackground(),
//                x + ts * 6 + scw / 2,
//                y + height - (ts / 2 + interval + item_h + (item_h - tfh) / 2 + tfh),
//                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
//        FontRenderer.drawText(batch,
//                Integer.toString(selected_unit.getPhysicalDefence()),
//                x + ts * 6 + scw + ts / 12,
//                y + height - (ts / 2 + interval + item_h + (item_h - lbh) / 2));
//        batch.draw(ResourceManager.getSmallCircleTexture(0),
//                x + ts * 6,
//                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + sch),
//                20 * ts / 24,
//                21 * ts / 24);
//        batch.draw(ResourceManager.getBattleHudIcon(1),
//                x + ts * 6 + (scw - hw) / 2,
//                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
//                hw, hh);
//        //magical defence
//        batch.draw(ResourceManager.getTextBackground(),
//                x + ts * 8 + ts / 4 + scw / 2,
//                y + height - (ts / 2 + interval + item_h + (item_h - tfh) / 2 + tfh),
//                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
//        FontRenderer.drawText(batch,
//                Integer.toString(selected_unit.getMagicDefence()),
//                x + ts * 8 + ts / 4 + scw + ts / 12,
//                y + height - (ts / 2 + interval + item_h + (item_h - lbh) / 2));
//        batch.draw(ResourceManager.getSmallCircleTexture(0),
//                x + ts * 8 + ts / 4,
//                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + sch),
//                20 * ts / 24,
//                21 * ts / 24);
//        batch.draw(ResourceManager.getBattleHudIcon(2),
//                x + ts * 8 + ts / 4 + (scw - hw) / 2,
//                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
//                hw, hh);
//        //split line
//        batch.draw(ResourceManager.getWhiteColor(),
//                x + ts * 6,
//                y + height - (ts / 2 + interval + item_h * 2),
//                ts * 4 + ts / 2,
//                1);
    }

}
