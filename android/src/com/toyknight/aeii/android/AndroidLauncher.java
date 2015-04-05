package com.toyknight.aeii.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.Platform;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new AEIIApplication(Platform.Android, 48), config);
	}
}
