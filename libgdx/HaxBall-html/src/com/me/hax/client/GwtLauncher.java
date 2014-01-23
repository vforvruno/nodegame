package com.me.hax.client;

import com.me.hax.HaxBall;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(1200, 800);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new HaxBall();
	}
}