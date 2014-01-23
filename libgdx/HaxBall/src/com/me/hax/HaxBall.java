package com.me.hax;

import maps.Stadium;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;


public class HaxBall extends Game {

	@Override
	public void create() {
		//TODO cambiar por la pantallla principal.
		setScreen(new Stadium());
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
	}

	@Override
	public Screen getScreen() {
		return super.getScreen();
	}

}
