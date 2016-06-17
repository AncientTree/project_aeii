package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.screen.widgets.SmallCircleLabel;
import com.toyknight.aeii.screen.widgets.UnitFrame;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/12/2016.
 */
public class UnitPage extends Table {

    private final int ts;

    private final Wiki wiki;

    private final UnitFrame unit_frame;

    private final SmallCircleLabel label_attack;
    private final SmallCircleLabel label_move;
    private final SmallCircleLabel label_physical_defence;
    private final SmallCircleLabel label_magic_defence;

    private final Label label_price;
    private final Label label_occupancy;
    private final Label label_attack_range;

    private final Label label_description;

    private final Table ability_references;

    private final Label label_none;

    public UnitPage(Wiki wiki) {
        super();
        this.wiki = wiki;
        this.ts = getWiki().getContext().getTileSize();

        Table preview_pane = new Table();

        unit_frame = new UnitFrame(ts);
        preview_pane.add(unit_frame);

        Table data_pane = new Table();
        label_attack = new SmallCircleLabel(ResourceManager.getBattleHudIcon(0), ts);
        data_pane.add(label_attack).width(ts * 2);
        label_move = new SmallCircleLabel(ResourceManager.getActionIcon(4), ts);
        label_move.setTextColor(Color.WHITE);
        data_pane.add(label_move).width(ts * 2).padLeft(ts / 2).row();
        label_physical_defence = new SmallCircleLabel(ResourceManager.getBattleHudIcon(1), ts);
        label_physical_defence.setTextColor(Color.WHITE);
        data_pane.add(label_physical_defence).width(ts * 2).padTop(ts / 4);
        label_magic_defence = new SmallCircleLabel(ResourceManager.getBattleHudIcon(2), ts);
        label_magic_defence.setTextColor(Color.WHITE);
        data_pane.add(label_magic_defence).width(ts * 2).padLeft(ts / 2).padTop(ts / 4);

        preview_pane.add(data_pane).size(ts * 4 + ts / 2, ts * 2).padLeft(ts / 2);

        add(preview_pane).size(ts * 7, ts * 2).padBottom(ts / 8).row();

        Table hud_pane = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };

        int hs = ts * 11 / 24;

        Image image_price = new Image(ResourceManager.createDrawable(ResourceManager.getStatusHudIcon(1), hs, hs));
        hud_pane.add(image_price);
        label_price = new Label("", getWiki().getContext().getSkin());
        hud_pane.add(label_price).width(ts * 2 - hs - ts / 4).padLeft(ts / 4);

        Image image_attack_range = new Image(ResourceManager.createDrawable(ResourceManager.getStatusHudIcon(2), hs, hs));
        hud_pane.add(image_attack_range).padLeft(ts / 2);
        label_attack_range = new Label("", getWiki().getContext().getSkin());
        hud_pane.add(label_attack_range).width(ts * 2 - hs - ts / 4).padLeft(ts / 4);

        Image image_occupancy = new Image(ResourceManager.createDrawable(ResourceManager.getStatusHudIcon(0), hs, hs));
        hud_pane.add(image_occupancy).padLeft(ts / 2);
        label_occupancy = new Label("", getWiki().getContext().getSkin());
        hud_pane.add(label_occupancy).width(ts * 2 - hs - ts / 4).padLeft(ts / 4);

        add(hud_pane).size(ts * 7, ts).padTop(ts / 8).padBottom(ts / 8).row();

        label_description = new Label("", getWiki().getContext().getSkin());
        label_description.setWrap(true);
        add(label_description).width(ts * 7).padBottom(ts / 8).row();

        Label label_abilities = new Label(Language.getText("LB_ABILITIES"), getWiki().getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_abilities.setAlignment(Align.center);
        add(label_abilities).size(ts * 7, label_abilities.getPrefHeight() + ts / 4).row();

        ability_references = new Table();
        add(ability_references).width(ts * 7).pad(ts / 8);

        label_none = new Label(Language.getText("LB_NONE"), getWiki().getContext().getSkin());
    }

    public void setIndex(int index) {
        unit_frame.setIndex(index);
        Unit sample = UnitFactory.getSample(index);
        label_attack.setText(Integer.toString(sample.getAttack()));
        label_attack.setTextColor(sample.getAttackType() == Unit.ATTACK_PHYSICAL ?
                ResourceManager.getPhysicalAttackColor() : ResourceManager.getMagicalAttackColor());
        label_move.setText(Integer.toString(sample.getMovementPoint()));
        label_physical_defence.setText(Integer.toString(sample.getPhysicalDefence()));
        label_magic_defence.setText(Integer.toString(sample.getMagicDefence()));
        label_price.setText(sample.getPrice() > 0 ? Integer.toString(sample.getPrice()) : "-");
        label_occupancy.setText(Integer.toString(sample.getOccupancy()));
        label_attack_range.setText(sample.getMinAttackRange() + "-" + sample.getMaxAttackRange());
        label_description.setText(Language.getUnitGuide(index));
        ability_references.clearChildren();
        if (sample.getAbilities().size > 0) {
            for (int ability : sample.getAbilities()) {
                ReferenceLabel ability_reference =
                        new ReferenceLabel(ReferenceLabel.TYPE_ABILITY, ability, getWiki().getContext().getSkin());
                ability_reference.addListener(ability_reference_click_listener);
                ability_references.add(ability_reference).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
            }
        } else {
            ability_references.add(label_none).width(ts * 7 - ts / 4);
        }
    }

    public Wiki getWiki() {
        return wiki;
    }

    private ClickListener ability_reference_click_listener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            Actor actor = event.getTarget();
            if (actor instanceof ReferenceLabel) {
                int ability = ((ReferenceLabel) actor).getValue();
                getWiki().gotoAbilityPage(ability);
            }
        }
    };

}
