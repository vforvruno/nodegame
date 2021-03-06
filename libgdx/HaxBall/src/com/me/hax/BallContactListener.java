package com.me.hax;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.me.hax.entities.Ball;
import com.me.hax.entities.Player;
import com.me.hax.entities.Player.Controls;

public class BallContactListener implements ContactListener {

	@Override
	public void beginContact(Contact contact) {

		Body ball;
		Body player;
		if (contact.getFixtureA().getBody().getUserData()!=null && contact.getFixtureA().getBody().getUserData().equals("ball") && contact.getFixtureB().getBody().getUserData() != null)			
		{
			ball = contact.getFixtureA().getBody();
			player = contact.getFixtureB().getBody();
		}
		else
		if (contact.getFixtureB().getBody().getUserData()!=null && contact.getFixtureB().getBody().getUserData().equals("ball") && contact.getFixtureA().getBody().getUserData() != null)			
		{
			ball = contact.getFixtureB().getBody();
			player = contact.getFixtureA().getBody();			
		}
		else
		{
			return;
		}

		Player p = (Player) player.getUserData();

		if (p.getAction(Controls.S)) {

			float x = ball.getPosition().x - player.getPosition().x;
			float y = ball.getPosition().y - player.getPosition().y;

			float absx = Math.abs(x);
			float absy = Math.abs(y);
			float sigx = Math.signum(x);
			float sigy = Math.signum(y);

			if (absx > absy) {
				float newy = (absy * Ball.maxSpeed) / absx;
				ball.setLinearVelocity(Ball.maxSpeed * sigx, newy * sigy);
			} else {
				float newx = (absx * Ball.maxSpeed) / absy;
				ball.setLinearVelocity(newx * sigx, Ball.maxSpeed * sigy);
			}
		}
	}

	@Override
	public void endContact(Contact contact) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub

	}

}
