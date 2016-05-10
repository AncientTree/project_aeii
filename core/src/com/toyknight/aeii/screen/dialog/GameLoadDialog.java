package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.record.GameRecord;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.screen.StageScreen;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.entity.GameSave;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.GameToolkit;
import com.toyknight.aeii.utils.Language;

import java.io.File;
import java.io.FileFilter;

/**
 * @author toyknight 6/7/2015.
 */
public class GameLoadDialog extends BasicDialog {

    private final SaveFileFilter filter = new SaveFileFilter();

    private TextButton btn_load;
    private TextButton btn_delete;
    private TextButton btn_cancel;

    private StringList<String> save_list;

    public GameLoadDialog(StageScreen owner) {
        super(owner);
        int width = ts * 11;
        int height = Gdx.graphics.getHeight() - ts * 2;
        setBounds((Gdx.graphics.getWidth() - width) / 2, ts, width, height);
        initComponents();
    }

    private void initComponents() {
        save_list = new StringList<String>(ts);
        ScrollPane sp_save_list = new ScrollPane(save_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        ResourceManager.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_save_list.setBounds(ts / 2, ts * 2, getWidth() - ts, getHeight() - ts * 2 - ts / 2);
        sp_save_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_save_list.setScrollBarPositions(false, true);
        this.addActor(sp_save_list);

        btn_load = new TextButton(Language.getText("LB_LOAD"), getContext().getSkin());
        btn_load.setBounds(ts / 2, ts / 2, ts * 3, ts);
        btn_load.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadSelectedSaveFile();
            }
        });
        addActor(btn_load);

        btn_delete = new TextButton(Language.getText("LB_DELETE"), getContext().getSkin());
        btn_delete.setBounds(ts * 4, ts / 2, ts * 3, ts);
        btn_delete.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                deleteSelectedSaveFile();
            }
        });
        addActor(btn_delete);

        btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.setBounds(ts * 6 + ts / 2 * 3, ts / 2, ts * 3, ts);
        addActor(btn_cancel);
    }

    public void setCancelAction(ClickListener listener) {
        btn_cancel.addListener(listener);
    }

    public void loadSelectedSaveFile() {
        String filename = save_list.getSelected();
        if (filename != null) {
            FileHandle save_file = FileProvider.getSaveFile(filename);
            int type = GameToolkit.getType(save_file);
            switch (type) {
                case GameToolkit.SAVE:
                    tryStartGame(save_file);
                    break;
                case GameToolkit.RECORD:
                    tryStartRecord(save_file);
                    break;
                default:
                    getContext().showMessage(Language.getText("MSG_ERR_BSF"), null);
            }
        }
    }

    public void tryStartGame(FileHandle save_file) {
        GameSave game_save = GameToolkit.loadGame(save_file);
        if (game_save == null) {
            getContext().showMessage(Language.getText("MSG_ERR_BSF"), null);
        } else {
            getContext().getGameManager().getGameRecorder().setEnabled(false);
            GameCore game = game_save.getGame();
            for (int team = 0; team < 4; team++) {
                Player player = game.getPlayer(team);
                if (player != null && player.getType() == Player.REMOTE) {
                    player.setType(Player.LOCAL);
                }
            }
            getContext().gotoGameScreen(game_save);
        }
    }

    public void tryStartRecord(FileHandle record_file) {
        GameRecord record = GameToolkit.loadRecord(record_file);
        if (record == null) {
            getContext().showMessage(Language.getText("MSG_ERR_BSF"), null);
        } else {
            if (getContext().getVerificationString().equals(record.getVerificationString())) {
                getContext().getGameManager().getGameRecorder().setEnabled(false);
                for (int team = 0; team < 4; team++) {
                    Player player = record.getGame().getPlayer(team);
                    if (record.getGame().getMap().hasTeamAccess(team)) {
                        player.setType(Player.RECORD);
                    }
                }
                getContext().gotoGameScreen(record);
            } else {
                getContext().showMessage(Language.getText("MSG_ERR_RVM"), null);
            }
        }
    }

    public void deleteSelectedSaveFile() {
        String filename = save_list.getSelected();
        if (filename != null) {
            FileHandle save_file = FileProvider.getSaveFile(filename);
            save_file.delete();
            refresh();
        }
    }

    public void refresh() {
        FileHandle save_dir = FileProvider.getUserDir("save");
        FileHandle[] save_files = save_dir.list(filter);
        Array<String> list = new Array<String>();
        for (FileHandle file : save_files) {
            list.add(file.name());
        }
        save_list.setItems(list);
    }

    @Override
    public void display() {
        refresh();
    }

    private class SaveFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.exists() && !file.isDirectory()) {
                String filename = file.getName();
                return filename.endsWith(".sav") || filename.endsWith(".rec");
            } else {
                return false;
            }

        }
    }

}
