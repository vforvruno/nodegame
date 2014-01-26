package com.me.hax;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "HaxBall";
		cfg.useGL20 = false;
		cfg.width = 1600;
		cfg.height = 800;
		
		new LwjglApplication(new HaxBall(), cfg);
	}
}
