package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.AudioManager;
import net.toyknight.aeii.screen.MainMenuScreen;
import net.toyknight.aeii.screen.widgets.NumberSpinner;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 8/31/2015.
 */
public class SettingDialog extends BasicDialog {

    private TextField input_username;

    private NumberSpinner spinner_music_volume;
    private NumberSpinner spinner_se_volume;

    public SettingDialog(MainMenuScreen screen) {
        super(screen);
        int width = ts * 7 + ts / 2;
        int height = ts * 6;
        this.setBounds(
                (Gdx.graphics.getWidth() - width) / 2,
                (Gdx.graphics.getHeight() - height) / 2,
                width, height);
        this.initComponents();
    }

    private void initComponents() {
        Table setting_bar = new Table();
        add(setting_bar).width(ts * 6 + ts / 2).row();

        Label lb_username = new Label(Language.getText("LB_USERNAME"), getContext().getSkin());
        lb_username.setAlignment(Align.right);
        setting_bar.add(lb_username).width(ts * 3);

        input_username = new TextField("", getContext().getSkin());
        input_username.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '_';
            }
        });
        input_username.setMaxLength(10);
        setting_bar.add(input_username).width(ts * 3).padLeft(ts / 2).row();

        Label lb_se_volume = new Label(Language.getText("LB_SE_VOLUME"), getContext().getSkin());
        lb_se_volume.setAlignment(Align.right);
        setting_bar.add(lb_se_volume).width(ts * 3).padTop(ts / 2);

        spinner_se_volume = new NumberSpinner(getContext(), 0, 100, 10);
        setting_bar.add(spinner_se_volume).size(ts * 3, ts).padLeft(ts / 2).padTop(ts / 2).row();

        Label lb_music_volume = new Label(Language.getText("LB_MUSIC_VOLUME"), getContext().getSkin());
        lb_music_volume.setAlignment(Align.right);
        setting_bar.add(lb_music_volume).width(ts * 3).padTop(ts / 2);

        spinner_music_volume = new NumberSpinner(getContext(), 0, 100, 10);
        setting_bar.add(spinner_music_volume).size(ts * 3, ts).padLeft(ts / 2).padTop(ts / 2);

        Table button_bar = new Table();
        add(button_bar).size(ts * 6 + ts / 2, ts).padTop(ts / 2);

        TextButton btn_save = new TextButton(Language.getText("LB_SAVE"), getContext().getSkin());
        btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                save();
                getOwner().closeDialog("setting");
            }
        });
        button_bar.add(btn_save).size(ts * 3, ts);

        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("setting");
            }
        });
        button_bar.add(btn_cancel).size(ts * 3, ts).padLeft(ts / 2);
    }

    private void save() {
        String username = input_username.getText();
        if (username.length() > 0) {
            getContext().updateConfiguration("username", username);
        } else {
            getContext().updateConfiguration("username", "undefined");
        }
        float se_volume = spinner_se_volume.getSelectedItem() / 100f;
        AudioManager.setSEVolume(se_volume);
        getContext().updateConfiguration("se_volume", Float.toString(se_volume));
        float music_volume = spinner_music_volume.getSelectedItem() / 100f;
        AudioManager.setMusicVolume(music_volume);
        getContext().updateConfiguration("music_volume", Float.toString(music_volume));
        getContext().saveConfiguration();
    }

    @Override
    public MainMenuScreen getOwner() {
        return (MainMenuScreen) super.getOwner();
    }

    @Override
    public void display() {
        input_username.setText(getContext().getUsername());
        int se_volume = (int) (getContext().getSEVolume() * 100);
        spinner_se_volume.select(se_volume);
        int music_volume = (int) (getContext().getMusicVolume() * 100);
        spinner_music_volume.select(music_volume);
        setVisible(true);
    }

}
