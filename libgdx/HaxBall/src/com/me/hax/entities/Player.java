package com.me.hax.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import sun.misc.JavaLangAccess;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends InputAdapter implements ContactListener{

	private Matrix4 camera;
	private Body playerBody;
	private BodyDef playerBodyDef;
	private FixtureDef playerFixtureDef;
	private Shape playerShape;
	private float maxSpeed = 5f, speed = .1f, maxSpeedBall = 8f;
	private ShapeRenderer renderer;
	private float size;
	private boolean contactFlag = false;
	private Vector2 position;
	
	enum Controls {
		U, D, L, R, S 
	}

	static Map<Controls, Boolean> keys = new HashMap<Controls, Boolean>();

	static {
		keys.put(Controls.U, false);
		keys.put(Controls.D, false);
		keys.put(Controls.L, false);
		keys.put(Controls.R, false);
		keys.put(Controls.S, false);
	}

	public Player(float speed, Vector2 position, float size, Shape shape,
			World world, Matrix4 camera) {

		if (shape == null) {
			playerShape = new CircleShape();

		} else {
			playerShape = shape;
		}

		this.size = size;
		
		playerBodyDef = new BodyDef();
		playerFixtureDef = new FixtureDef();

		playerBodyDef.type = BodyType.DynamicBody;
		playerBodyDef.position.set(position.x, position.y);
		this.position = position;
		((CircleShape) playerShape).setRadius(size);

		playerFixtureDef.shape = playerShape;

		playerFixtureDef.restitution = .5f;
		playerFixtureDef.friction = 0f;
		playerFixtureDef.density = 5f;
		playerFixtureDef.isSensor = false;
		
		
		
		
		playerBody = world.createBody(playerBodyDef);

		playerBody.createFixture(playerFixtureDef);

		
		renderer = new ShapeRenderer();
		this.camera = camera;
		
		
		
		
	}

	public Body getPlayer() {
		return playerBody;
	}

	private void setPressKey(Controls k){
		keys.put(k, true);
	}
	
	private void setPressRelease(Controls k){
		keys.put(k, false);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.UP:
			setPressKey(Controls.U);
			return true;
		case Keys.DOWN:
			setPressKey(Controls.D);
			return true;
		case Keys.LEFT:
			setPressKey(Controls.L);
			return true;
		case Keys.RIGHT:
			setPressKey(Controls.R);
			return true;
		case Keys.SPACE:
			setPressKey(Controls.S);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.UP:
			setPressRelease(Controls.U);
			return true;
		case Keys.DOWN:
			setPressRelease(Controls.D);
			return true;
		case Keys.LEFT:
			setPressRelease(Controls.L);
			return true;
		case Keys.RIGHT:
			setPressRelease(Controls.R);
			return true;
		case Keys.SPACE:
			setPressRelease(Controls.S);
			return true;
		}
		return false;
	}
	
	public void movePlayer(){
		for(Entry<Controls, Boolean> entry : keys.entrySet()) {
		    Controls key = entry.getKey();
		    Boolean value = entry.getValue();
		    
		    if (key == key.U && value) {

				if (playerBody.getLinearVelocity().y < maxSpeed
						|| playerBody.getLinearVelocity().y + speed > maxSpeed) {
					playerBody.applyForceToCenter(0, maxSpeed, true);
				} else if (playerBody.getLinearVelocity().y + speed < maxSpeed) {
					playerBody.applyForceToCenter(0,
							playerBody.getLinearVelocity().y + speed, true);
				}

			}

			if (key == key.D && value) {

				if (playerBody.getLinearVelocity().y < -maxSpeed
						|| -playerBody.getLinearVelocity().y + -speed > -maxSpeed) {
					playerBody.applyForceToCenter(0, -maxSpeed, true);
				} else if (-playerBody.getLinearVelocity().y + -speed < -maxSpeed) {
					playerBody.applyForceToCenter(0,
							-playerBody.getLinearVelocity().y + -speed, true);
				}

			}

			if (key == key.L && value) {

				if (playerBody.getLinearVelocity().x < -maxSpeed
						|| -playerBody.getLinearVelocity().x + -speed > -maxSpeed) {
					playerBody.applyForceToCenter(-maxSpeed, 0, true);
				} else if (-playerBody.getLinearVelocity().x + -speed < -maxSpeed) {
					playerBody.applyForceToCenter(-playerBody.getLinearVelocity().x
							+ -speed, 0, true);
				}

			}

			if (key == key.R && value) {

				if (playerBody.getLinearVelocity().x < maxSpeed
						|| playerBody.getLinearVelocity().x + speed > maxSpeed) {
					playerBody.applyForceToCenter(maxSpeed, 0, true);
				} else if (-playerBody.getLinearVelocity().x + speed < maxSpeed) {
					playerBody.applyForceToCenter(playerBody.getLinearVelocity().x
							+ speed, 0, true);
				}

			}
			          
			boolean colorFlag = false;
			//Kick option.
			if(key == key.S && value){
				//draw super poderes!
				if(!colorFlag){
					renderer.setProjectionMatrix(camera);
					renderer.begin(ShapeType.Filled);
					renderer.setColor(Color.ORANGE);
					renderer.circle(playerBody.getPosition().x ,playerBody.getPosition().y, playerBody.getFixtureList().get(0).getShape().getRadius() - .05f, 20);;
					renderer.end();
					colorFlag = true;
				}
				playerBody.getFixtureList().get(0).getShape().setRadius(size + .051f);
				//playerBody.getFixtureList().get(0).setDensity(100);
				//playerBody.getFixtureList().get(0).setRestitution(1.5f);
				contactFlag = true;
				
			}else if(key == key.S && !value){
				playerBody.getFixtureList().get(0).getShape().setRadius(size);
				//playerBody.getFixtureList().get(0).setDensity(1.5f);
				//playerBody.getFixtureList().get(0).setRestitution(.5f);
				colorFlag = false;
				contactFlag = false;
			}
			
			
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}
	
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
		
	}
	
	@Override
	public void endContact(Contact contact) {
	
	}
	
	@Override
	public void beginContact(Contact contact) {
		if(contactFlag){
			Body ball =  contact.getFixtureA().getBody();
			Body player = contact.getFixtureB().getBody();
			
			if((ball == playerBody || player == playerBody) && ( (player.getUserData() != null && player.getUserData().equals( "ball")) || (ball.getUserData() != null && ball.getUserData().equals( "ball" )))) {
				/*
				 * body1 = pelota
				 * body2 = jugador.
				 */
				if(ball != playerBody){
					ball =  contact.getFixtureA().getBody();	
					player = contact.getFixtureB().getBody();	
				}
				
				float x = ball.getPosition().x - player.getPosition().x; 
				float y = ball.getPosition().y - player.getPosition().y;
				
				
				float absx = Math.abs(x);
				float absy = Math.abs(y);
				float sigx = Math.signum(x);
				float sigy = Math.signum(y);
				
				if ( absx > absy )
				{
					float newy = (absy*maxSpeedBall) / absx;
					ball.setLinearVelocity( maxSpeedBall*sigx, newy*sigy);	
				}
				else
				{
					float newx = (absx*maxSpeedBall) / absy;
					ball.setLinearVelocity( newx*sigx, maxSpeedBall*sigy);	
				}		
			}
		}	
	}

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		playerBodyDef.position.set(position);
	}
	
	
}
