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
import com.toyknight.aeii.renderer.FontRenderer;
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

    private TextButton btn_buy;
    private AvailableUnitList unit_list;

    private int castle_x;
    private int castle_y;

    private Unit selected_unit;
    private int price;

    public UnitStoreDialog(GameScreen screen) {
        super(screen);
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
        //sp_unit_list.setFadeScrollBars(false);
        sp_unit_list.setBounds(ts / 2, ts / 2, ts * 5, ts * 3 / 2 * 5);
        this.addActor(sp_unit_list);

        this.btn_buy = new TextButton(Language.getText("LB_BUY"), skin);
        this.btn_buy.setBounds(ts * 6, ts / 2, ts * 2, ts);
        this.btn_buy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getManager().doBuyUnit(selected_unit.getIndex(), castle_x, castle_y);
                getOwner().closeDialog("store");
            }
        });
        this.addActor(btn_buy);
        TextButton btn_close = new TextButton(Language.getText("LB_CLOSE"), skin);
        btn_close.setBounds(ts * 8 + ts / 2, ts / 2, ts * 2, ts);
        btn_close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("store");
            }
        });
        this.addActor(btn_close);
    }

    public GameScreen getOwner() {
        return (GameScreen) super.getOwner();
    }

    private GameCore getGame() {
        return getOwner().getGame();
    }

    private GameManager getManager() {
        return getOwner().getManager();
    }

    @Override
    public void display() {
        this.castle_x = getOwner().getCursorMapX();
        this.castle_y = getOwner().getCursorMapY();
        this.unit_list.setGame(getGame());
        GameManager manager = getManager();
        Array<Integer> available_units = manager.getGame().getRule().getAvailableUnitList();
        unit_list.setAvailableUnits(available_units);
    }

    @Override
    public void onUnitSelected(int index) {
        selected_unit = UnitFactory.getSample(index);
        if (selected_unit.isCommander()) {
            selected_unit = getGame().getCommander(selected_unit.getTeam());
        }
        updateButton();
    }

    private void updateButton() {
        int current_team = getGame().getCurrentTeam();
        if (selected_unit != null) {
            price = getGame().getUnitPrice(selected_unit.getIndex(), current_team);
            btn_buy.setVisible(canBuy(selected_unit.getIndex(), price));
        } else {
            btn_buy.setVisible(false);
        }
    }

    private boolean canBuy(int unit_index, int unit_price) {
        Unit sample_unit = UnitFactory.getSample(unit_index);
        int movement_point = sample_unit.getMovementPoint();
        sample_unit.setCurrentMovementPoint(movement_point);
        sample_unit.setX(castle_x);
        sample_unit.setY(castle_y);
        return unit_price >= 0
                && getGame().getCurrentPlayer().getGold() >= unit_price
                && getManager().createMovablePositions(sample_unit).size > 0
                && (getGame().getCurrentPlayer().getPopulation() < getGame().getRule().getMaxPopulation() || sample_unit.isCommander());
    }

    @Override
    protected void drawCustom(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), height = getHeight();
        int interval = ts * 13 / 24;
        int lw = FontRenderer.getLNumberWidth(0, false);
        int lh = FontRenderer.getLCharHeight();
        //price
        batch.draw(ResourceManager.getStatusHudIcon(1),
                x + ts * 10 + ts / 2 - lw * 4 - 11 * ts / 24,
                y + height - ts / 2 - lh,
                11 * ts / 24,
                11 * ts / 24);
        if (price >= 0) {
            FontRenderer.drawLNumber(batch,
                    price,
                    x + ts * 10 + ts / 2 - lw * 4,
                    y + height - (ts / 2 + (interval - lh) / 2 + lh));
        } else {
            batch.draw(FontRenderer.getLMinus(),
                    x + ts * 10 + ts / 2 - lw * 2 - lw / 2,
                    y + height - (ts / 2 + (interval - lh) / 2 + lh),
                    lw, lh);
        }
        //split line
        batch.draw(ResourceManager.getWhiteColor(),
                x + ts * 6,
                y + height - (ts / 2 + interval),
                ts * 4 + ts / 2,
                1);
        int scw = ts * 20 / 24;
        int sch = ts * 21 / 24;
        int acs = ts * 16 / 24;
        int hw = ts * 13 / 24;
        int hh = ts * 16 / 24;
        int item_h = sch + ts / 6;
        int tfh = sch - ts / 4;
        float lbh = FontRenderer.getTextFont().getCapHeight();
        //attack
        batch.draw(ResourceManager.getTextBackground(),
                x + ts * 6 + scw / 2,
                y + height - (ts / 2 + interval + (item_h - tfh) / 2 + tfh),
                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
        if (selected_unit.getAttackType() == Unit.ATTACK_PHYSICAL) {
            FontRenderer.setTextColor(ResourceManager.getPhysicalAttackColor());
        } else {
            FontRenderer.setTextColor(ResourceManager.getMagicalAttackColor());
        }
        FontRenderer.drawText(batch,
                Integer.toString(selected_unit.getAttack()),
                x + ts * 6 + scw + ts / 12,
                y + height - (ts / 2 + interval + (item_h - lbh) / 2));
        batch.draw(ResourceManager.getSmallCircleTexture(0),
                x + ts * 6,
                y + height - (ts / 2 + interval + (item_h - sch) / 2 + sch),
                20 * ts / 24,
                21 * ts / 24);
        batch.draw(ResourceManager.getBattleHudIcon(0),
                x + ts * 6 + (scw - hw) / 2,
                y + height - (ts / 2 + interval + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
                hw, hh);
        FontRenderer.setTextColor(Color.WHITE);
        //movement point
        batch.draw(ResourceManager.getTextBackground(),
                x + ts * 8 + ts / 4 + scw / 2,
                y + height - (ts / 2 + interval + (item_h - tfh) / 2 + tfh),
                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
        FontRenderer.drawText(batch,
                Integer.toString(selected_unit.getMovementPoint()),
                x + ts * 8 + ts / 4 + scw + ts / 12,
                y + height - (ts / 2 + interval + (item_h - lbh) / 2));
        batch.draw(ResourceManager.getSmallCircleTexture(0),
                x + ts * 8 + ts / 4,
                y + height - (ts / 2 + interval + (item_h - sch) / 2 + sch),
                20 * ts / 24,
                21 * ts / 24);
        batch.draw(ResourceManager.getActionIcon(4),
                x + ts * 8 + ts / 4 + (scw - acs) / 2,
                y + height - (ts / 2 + interval + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
                acs, acs);
        //physical defence
        batch.draw(ResourceManager.getTextBackground(),
                x + ts * 6 + scw / 2,
                y + height - (ts / 2 + interval + item_h + (item_h - tfh) / 2 + tfh),
                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
        FontRenderer.drawText(batch,
                Integer.toString(selected_unit.getPhysicalDefence()),
                x + ts * 6 + scw + ts / 12,
                y + height - (ts / 2 + interval + item_h + (item_h - lbh) / 2));
        batch.draw(ResourceManager.getSmallCircleTexture(0),
                x + ts * 6,
                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + sch),
                20 * ts / 24,
                21 * ts / 24);
        batch.draw(ResourceManager.getBattleHudIcon(1),
                x + ts * 6 + (scw - hw) / 2,
                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
                hw, hh);
        //magical defence
        batch.draw(ResourceManager.getTextBackground(),
                x + ts * 8 + ts / 4 + scw / 2,
                y + height - (ts / 2 + interval + item_h + (item_h - tfh) / 2 + tfh),
                ts * 2 + ts / 4 - scw / 2 - ts / 12, tfh);
        FontRenderer.drawText(batch,
                Integer.toString(selected_unit.getMagicDefence()),
                x + ts * 8 + ts / 4 + scw + ts / 12,
                y + height - (ts / 2 + interval + item_h + (item_h - lbh) / 2));
        batch.draw(ResourceManager.getSmallCircleTexture(0),
                x + ts * 8 + ts / 4,
                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + sch),
                20 * ts / 24,
                21 * ts / 24);
        batch.draw(ResourceManager.getBattleHudIcon(2),
                x + ts * 8 + ts / 4 + (scw - hw) / 2,
                y + height - (ts / 2 + interval + item_h + (item_h - sch) / 2 + (sch - hh) / 2 + hh),
                hw, hh);
        //split line
        batch.draw(ResourceManager.getWhiteColor(),
                x + ts * 6,
                y + height - (ts / 2 + interval + item_h * 2),
                ts * 4 + ts / 2,
                1);
    }

}
