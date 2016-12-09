package net.toyknight.aeii.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.gui.dialog.GameLoadDialog;
import net.toyknight.aeii.gui.dialog.MainMenu;
import net.toyknight.aeii.gui.dialog.ServerListDialog;
import net.toyknight.aeii.gui.dialog.SettingDialog;
import net.toyknight.aeii.gui.widgets.CircleButton;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 4/3/2015.
 */
public class MainMenuScreen extends StageScreen {

    private final MainMenu menu;
    private final SettingDialog setting_dialog;

    private final CircleButton btn_setting;
    private final Label lb_version;

    public MainMenuScreen(GameContext context) {
        super(context);

        this.menu = new MainMenu(this);
        this.addActor(menu);

        ServerListDialog server_list = new ServerListDialog(this);
        this.addDialog("server", server_list);

        GameLoadDialog load_dialog = new GameLoadDialog(this);
        this.addDialog("load", load_dialog);

        this.setting_dialog = new SettingDialog(this);
        this.addDialog("setting", setting_dialog);

        this.btn_setting = new CircleButton(getContext(), CircleButton.LARGE, getResources().getMenuIcon(4));
        this.btn_setting.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (setting_dialog.isVisible()) {
                    showDialog("menu");
                } else {
                    showDialog("setting");
                }
            }
        });
        this.btn_setting.setPosition(0, 0);
        this.addActor(btn_setting);

        this.lb_version = new Label(GameContext.EXTERNAL_VERSION, getContext().getSkin());
        this.lb_version.setPosition(Gdx.graphics.getWidth() - lb_version.getPrefWidth(), 0);
        this.addActor(lb_version);
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(getResources().getMainMenuBackgroundTexture(),
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getFontRenderer().drawTitleCenter(batch, AER.lang.getText("_TITLE"),
                0, Gdx.graphics.getHeight() - 85, Gdx.graphics.getWidth(), 85);
        batch.end();
        super.draw();
    }

    @Override
    public void show() {
        super.show();
        closeAllDialogs();
    }

    @Override
    public void onDisconnect() {
        //do nothing
    }

}
