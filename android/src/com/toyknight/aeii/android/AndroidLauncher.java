package com.toyknight.aeii.android;

import android.os.Bundle;
import android.util.DisplayMetrics;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.utils.Platform;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        int tileSize = this.getTileSize();
        initialize(new AEIIApplication(Platform.Android, tileSize), config);


    }

    /**
     * Get tile size for different resolution of device.
     *
     * @return the tile size
     */
    private int getTileSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        if (height <= 800) {
            return 48;
        } else if (height >= 1920) {
            return 96;
        } else {
            return 72;
        }
    }
}
