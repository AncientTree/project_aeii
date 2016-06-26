package net.toyknight.aeii.campaign;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * @author toyknight 6/24/2016.
 */
public abstract class CampaignController {

    private final ObjectMap<String, Integer> attributes;

    private final Array<StageController> stages;

    private int current_stage;

    public CampaignController() {
        this.attributes = new ObjectMap<String, Integer>();
        this.stages = new Array<StageController>();
        this.current_stage = 0;
    }

    public final void resetAttributes() {
        attributes.clear();
    }

    public final void setAttributes(ObjectMap<String, Integer> attributes) {
        resetAttributes();
        attributes.putAll(attributes);
    }

    public final ObjectMap<String, Integer> getAttributes() {
        return attributes;
    }

    public final void setAttribute(String name, Integer value) {
        getAttributes().put(name, value);
    }

    public final Integer getAttribute(String name) {
        return getAttributes().get(name);
    }

    public final void addStage(StageController stage) {
        stages.add(stage);
    }

    public final StageController getStage(int index) {
        return stages.get(index);
    }

    public final StageController getCurrentStage() {
        return getStage(current_stage);
    }

    public final Array<StageController> getStages() {
        return stages;
    }

    public final boolean setCurrentStage(int current_stage) {
        if (0 <= current_stage && current_stage < stages.size) {
            this.current_stage = current_stage;
            return true;
        } else {
            return false;
        }
    }

    public final boolean nextStage() {
        return setCurrentStage(current_stage + 1);
    }

    abstract public String getCode();

    abstract public void initialize();

    abstract public String getCampaignName();

    abstract public int getDifficulty();

    public final Snapshot createSnapshot() {
        return new Snapshot(getCode(), getCampaignName(), getDifficulty());
    }

    public class Snapshot {

        public final String code;
        public final String name;
        public final int difficulty;

        public Snapshot(String code, String name, int difficulty) {
            this.code = code;
            this.name = name;
            this.difficulty = difficulty;
        }

        @Override
        public String toString() {
            return name;
        }

    }

}
