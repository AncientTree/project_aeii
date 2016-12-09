package net.toyknight.aeii.gui.wiki;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.entity.Ability;
import net.toyknight.aeii.entity.Status;
import net.toyknight.aeii.gui.widgets.AEIITable;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/16/2016.
 */
public class AbilityPage extends AEIITable {

    private final Wiki wiki;

    private final Label label_description;

    private final Table unit_references;

    private final Table content_references;

    private final Label label_none;

    public AbilityPage(Wiki wiki) {
        super(wiki.getContext());
        this.wiki = wiki;

        label_description = new Label("", getWiki().getContext().getSkin());
        label_description.setWrap(true);
        add(label_description).width(ts * 7).padBottom(ts / 8).row();

        Label label_uha = new Label(AER.lang.getText("LB_UHA"), getWiki().getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_uha.setAlignment(Align.center);
        add(label_uha).size(ts * 7, label_uha.getPrefHeight() + ts / 4).row();

        unit_references = new Table();
        add(unit_references).width(ts * 7).pad(ts / 8).row();

        Label label_references = new Label(AER.lang.getText("LB_REFERENCES"), getWiki().getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_references.setAlignment(Align.center);
        add(label_references).size(ts * 7, label_references.getPrefHeight() + ts / 4).row();

        content_references = new Table();
        add(content_references).width(ts * 7).pad(ts / 8);

        label_none = new Label(AER.lang.getText("LB_NONE"), getWiki().getContext().getSkin());
    }

    public Wiki getWiki() {
        return wiki;
    }

    public void setAbility(int ability) {
        label_description.setText(AER.lang.getAbilityDescription(ability));

        int count = 0;
        unit_references.clearChildren();
        for (int index = 0; index < AER.units.getUnitCount(); index++) {
            if (AER.units.getSample(index).hasAbility(ability)) {
                ReferenceLabel reference_unit =
                        new ReferenceLabel(getContext(), ReferenceLabel.TYPE_UNIT, index);
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
            case Ability.HARD_SKIN:
                content_references.add(new Label(AER.lang.getText("LB_NONE"), getWiki().getContext().getSkin())).
                        width(ts * 7 - ts / 4);
                break;
            case Ability.ATTACK_AURA:
                ReferenceLabel reference_status = new ReferenceLabel(
                        getContext(), ReferenceLabel.TYPE_STATUS, Status.INSPIRED);
                reference_status.addListener(content_reference_click_listener);
                content_references.add(reference_status).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.BLINDER:
                reference_status = new ReferenceLabel(
                        getContext(), ReferenceLabel.TYPE_STATUS, Status.BLINDED);
                reference_status.addListener(content_reference_click_listener);
                content_references.add(reference_status).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.MARKSMAN:
                ReferenceLabel reference_ability = new ReferenceLabel(
                        getContext(), ReferenceLabel.TYPE_ABILITY, Ability.AIR_FORCE);
                reference_ability.addListener(content_reference_click_listener);
                content_references.add(reference_ability).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.NECROMANCER:
            case Ability.POISONER:
            case Ability.REHABILITATION:
            case Ability.UNDEAD:
                reference_status = new ReferenceLabel(
                        getContext(), ReferenceLabel.TYPE_STATUS, Status.POISONED);
                reference_status.addListener(content_reference_click_listener);
                content_references.add(reference_status).width(ts * 7 - ts / 4).padBottom(ts / 8).row();
                break;
            case Ability.SLOWING_AURA:
                reference_status = new ReferenceLabel(
                        getContext(), ReferenceLabel.TYPE_STATUS, Status.SLOWED);
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
