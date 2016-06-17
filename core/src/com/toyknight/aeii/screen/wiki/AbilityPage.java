package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.Status;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/16/2016.
 */
public class AbilityPage extends Table {

    private final int ts;

    private final Wiki wiki;

    private final Label label_description;

    private final Table unit_references;

    private final Table content_references;

    private final Label label_none;

    public AbilityPage(Wiki wiki) {
        super();
        this.wiki = wiki;
        this.ts = getWiki().getContext().getTileSize();

        label_description = new Label("", getWiki().getContext().getSkin());
        label_description.setWrap(true);
        add(label_description).width(ts * 7).padBottom(ts / 8).row();

        Label label_uha = new Label(Language.getText("LB_UHA"), getWiki().getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_uha.setAlignment(Align.center);
        add(label_uha).size(ts * 7, label_uha.getPrefHeight() + ts / 4).row();

        unit_references = new Table();
        add(unit_references).width(ts * 7).pad(ts / 8).row();

        Label label_references = new Label(Language.getText("LB_REFERENCES"), getWiki().getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_references.setAlignment(Align.center);
        add(label_references).size(ts * 7, label_references.getPrefHeight() + ts / 4).row();

        content_references = new Table();
        add(content_references).width(ts * 7).pad(ts / 8);

        label_none = new Label(Language.getText("LB_NONE"), getWiki().getContext().getSkin());
    }

    public Wiki getWiki() {
        return wiki;
    }

    public void setAbility(int ability) {
        label_description.setText(Language.getAbilityDescription(ability));

        int count = 0;
        unit_references.clearChildren();
        for (int index = 0; index < UnitFactory.getUnitCount(); index++) {
            if (UnitFactory.getSample(index).hasAbility(ability)) {
                ReferenceLabel reference_unit =
                        new ReferenceLabel(ReferenceLabel.TYPE_UNIT, index, getWiki().getContext().getSkin());
                reference_unit.addListener(unit_reference_click_listener);
                unit_references.add(reference_unit).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                count++;
            }
        }
        if (count == 0) {
            unit_references.add(label_none).width(ts * 7 - ts / 4);
        }
        content_references.clearChildren();
        switch (ability) {
            case Ability.AIR_FORCE:
            case Ability.BLOODTHIRSTY:
            case Ability.CHARGER:
            case Ability.COMMANDER:
            case Ability.CONQUEROR:
            case Ability.COUNTER_MADNESS:
            case Ability.CRAWLER:
            case Ability.DESTROYER:
            case Ability.FIGHTER_OF_THE_FOREST:
            case Ability.FIGHTER_OF_THE_MOUNTAIN:
            case Ability.FIGHTER_OF_THE_SEA:
            case Ability.GUARDIAN:
            case Ability.HEALER:
            case Ability.HEAVY_MACHINE:
            case Ability.LORD_OF_TERROR:
            case Ability.REFRESH_AURA:
            case Ability.REPAIRER:
            case Ability.SON_OF_THE_FOREST:
            case Ability.SON_OF_THE_MOUNTAIN:
            case Ability.SON_OF_THE_SEA:
                content_references.add(new Label(Language.getText("LB_NONE"), getWiki().getContext().getSkin())).
                        width(ts * 7 - ts / 4);
                break;
            case Ability.AMBUSH:
                ReferenceLabel reference_ability = new ReferenceLabel(
                        ReferenceLabel.TYPE_ABILITY, Ability.COUNTER_MADNESS, getWiki().getContext().getSkin());
                reference_ability.addListener(content_reference_click_listener);
                content_references.add(reference_ability).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.ATTACK_AURA:
                ReferenceLabel reference_status = new ReferenceLabel(
                        ReferenceLabel.TYPE_STATUS, Status.INSPIRED, getWiki().getContext().getSkin());
                reference_status.addListener(content_reference_click_listener);
                content_references.add(reference_status).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.BLINDER:
                reference_status = new ReferenceLabel(
                        ReferenceLabel.TYPE_STATUS, Status.BLINDED, getWiki().getContext().getSkin());
                reference_status.addListener(content_reference_click_listener);
                content_references.add(reference_status).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.MARKSMAN:
                reference_ability = new ReferenceLabel(
                        ReferenceLabel.TYPE_ABILITY, Ability.AIR_FORCE, getWiki().getContext().getSkin());
                reference_ability.addListener(content_reference_click_listener);
                content_references.add(reference_ability).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.NECROMANCER:
            case Ability.POISONER:
            case Ability.REHABILITATION:
            case Ability.UNDEAD:
                reference_status = new ReferenceLabel(
                        ReferenceLabel.TYPE_STATUS, Status.POISONED, getWiki().getContext().getSkin());
                reference_status.addListener(content_reference_click_listener);
                content_references.add(reference_status).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.SLOWING_AURA:
                reference_status = new ReferenceLabel(
                        ReferenceLabel.TYPE_STATUS, Status.SLOWED, getWiki().getContext().getSkin());
                reference_status.addListener(content_reference_click_listener);
                content_references.add(reference_status).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
        }
    }

    private ClickListener unit_reference_click_listener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            Actor actor = event.getTarget();
            if (actor instanceof ReferenceLabel) {
                int value = ((ReferenceLabel) actor).getValue();
                getWiki().gotoUnitPage(value);
            }
        }
    };

    private ClickListener content_reference_click_listener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            Actor actor = event.getTarget();
            if (actor instanceof ReferenceLabel) {
                int value = ((ReferenceLabel) actor).getValue();
                if (((ReferenceLabel) actor).getType() == ReferenceLabel.TYPE_STATUS) {
                    getWiki().gotoStatusPage(value);
                }
                if (((ReferenceLabel) actor).getType() == ReferenceLabel.TYPE_ABILITY) {
                    getWiki().gotoAbilityPage(value);
                }
            }
        }
    };

}
