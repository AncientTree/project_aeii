package net.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.screen.widgets.PreviewFrame;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/17/2016.
 */
public class StatusPage extends Table {

    private final int ts;

    private final Wiki wiki;

    private final PreviewFrame status_preview;

    private final Label label_description;

    public StatusPage(Wiki wiki) {
        super();
        this.wiki = wiki;
        this.ts = getWiki().getContext().getTileSize();

        status_preview = new PreviewFrame(ts);
        status_preview.setTileIndex(18);
        status_preview.setUnitIndex(0);
        status_preview.setTeam(0);
        add(status_preview).size(ts, ts).padBottom(ts / 8).row();

        label_description = new Label("", getWiki().getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(ResourceManager.getWhiteColor(), this.getX(), this.getY() + this.getHeight() + ts / 8, this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_description.setWrap(true);
        add(label_description).width(ts * 7).pad(ts / 8);
    }

    public Wiki getWiki() {
        return wiki;
    }

    public void setStatus(int status) {
        status_preview.setStatus(status);
        label_description.setText(Language.getStatusDescription(status));
    }

}
