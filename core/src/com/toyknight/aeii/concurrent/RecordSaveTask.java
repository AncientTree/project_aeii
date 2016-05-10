package com.toyknight.aeii.concurrent;

import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.io.Output;
import com.toyknight.aeii.record.GameRecord;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.GameToolkit;

/**
 * @author toyknight 5/9/2016.
 */
public class RecordSaveTask extends AsyncTask<Void> {

    private final GameRecord record;

    public RecordSaveTask(GameRecord record) {
        this.record = record;
    }

    @Override
    public Void doTask() throws Exception {
        String filename = GameToolkit.createFilename(GameToolkit.RECORD);
        FileHandle record_file = FileProvider.getUserFile("save/" + filename);
        Output output = new Output(record_file.write(false));
        String content = record.toJson().toString();
        output.writeInt(GameToolkit.RECORD);
        output.writeString(content);
        output.flush();
        output.close();
        return null;
    }

    @Override
    public void onFinish(Void result) {
    }

    @Override
    public void onFail(String message) {
        System.err.println("Error saving game record: " + message);
    }

}
