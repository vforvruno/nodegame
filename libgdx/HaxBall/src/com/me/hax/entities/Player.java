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

public class Player extends InputAdapter{

	private Matrix4 camera;
	private Body playerBody;
	private BodyDef playerBodyDef;
	private FixtureDef playerFixtureDef;
	private Shape playerShape;
	private float maxSpeed = 2f, speed = .1f;
	private ShapeRenderer renderer;
	private float size;
	private boolean contactFlag = false;
	private Vector2 position;
	private final int[] controls;
	private Color color;
	
	public enum Controls {
		U, D, L, R, S 
	}

	public Map<Controls, Boolean> keys = new HashMap<Controls, Boolean>();

//	static {
//		keys.put(Controls.U, false);
//		keys.put(Controls.D, false);
//		keys.put(Controls.L, false);
//		keys.put(Controls.R, false);
//		keys.put(Controls.S, false);
//	}

	public Player(float speed, Vector2 position, float size, Shape shape,
			World world, Matrix4 camera, int[] controls, Color c) {

		if (shape == null) {
			playerShape = new CircleShape();

		} else {
			playerShape = shape;
		}
		this.controls = controls;
		this.size = size;
		this.color = c;
		playerBodyDef = new BodyDef();
		playerFixtureDef = new FixtureDef();

		playerBodyDef.type = BodyType.DynamicBody;
		playerBodyDef.position.set(position.x, position.y);
		this.position = position;
		((CircleShape) playerShape).setRadius(size);

		playerFixtureDef.shape = playerShape;

		playerFixtureDef.restitution = .5f;
		playerFixtureDef.friction = 1f;
		playerFixtureDef.density = 3f;
		playerFixtureDef.isSensor = false;
		
		
		
		
		playerBody = world.createBody(playerBodyDef);

		playerBody.createFixture(playerFixtureDef);


		playerBody.setUserData(this);
		
		
		
		renderer = new ShapeRenderer();
		this.camera = camera;
		
		keys.put(Controls.U, false);
		keys.put(Controls.D, false);
		keys.put(Controls.L, false);
		keys.put(Controls.R, false);
		keys.put(Controls.S, false);
		
		
	}
	
	public boolean getAction(Controls s){
		return keys.get(s);
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
		
		int u = controls[0];
		int d = controls[1];
		int l = controls[2];
		int r = controls[3];
		int s = controls[4];
		
		if(keycode == u)
		{
			setPressKey(Controls.U);
			return true;
		}
		else if (keycode == d)
		{
			setPressKey(Controls.D);
			return true;
		}
		else if (keycode == l)
		{
			setPressKey(Controls.L);
			return true;
		}
		else if (keycode == r)
		{
			setPressKey(Controls.R);
			return true;
		}
		else if (keycode == s)
		{
			setPressKey(Controls.S);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		int u = controls[0];
		int d = controls[1];
		int l = controls[2];
		int r = controls[3];
		int s = controls[4];
		
		if(keycode == u)
		{
			setPressRelease(Controls.U);
			return true;
		}
		else if (keycode == d)
		{
			setPressRelease(Controls.D);
			return true;
		}
		else if (keycode == l)
		{
			setPressRelease(Controls.L);
			return true;
		}
		else if (keycode == r)
		{
			setPressRelease(Controls.R);
			return true;
		}
		else if (keycode == s)
		{
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
			this.paintPlayer(this.color);
			boolean colorFlag = false;
			//Kick option.
			if(key == key.S && value){
				//draw super poderes!
				if(!colorFlag){
					renderer.setProjectionMatrix(camera);
					renderer.begin(ShapeType.Filled);
					renderer.setColor(Color.LIGHT_GRAY);
					renderer.circle(playerBody.getPosition().x ,playerBody.getPosition().y, playerBody.getFixtureList().get(0).getShape().getRadius() - .05f, 20);
					renderer.end();
					colorFlag = true;
				}
				//playerBody.getFixtureList().get(0).getShape().setRadius(size + .051f);
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
	
	private void paintPlayer(Color c){
		renderer.setProjectionMatrix(camera);
		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.BLACK);
		renderer.circle(playerBody.getPosition().x ,playerBody.getPosition().y, playerBody.getFixtureList().get(0).getShape().getRadius(), 50);
		renderer.setColor(c);
		renderer.circle(playerBody.getPosition().x ,playerBody.getPosition().y, playerBody.getFixtureList().get(0).getShape().getRadius()-.55f, 50);
		renderer.end();
	}

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		playerBodyDef.position.set(position);
	}

	public Body getPlayerBody() {
		return playerBody;
	}

	public void setPlayerBody(Body playerBody) {
		this.playerBody = playerBody;
	
	}

	public BodyDef getPlayerBodyDef() {
		return playerBodyDef;
	}

	public void setPlayerBodyDef(BodyDef playerBodyDef) {
		this.playerBodyDef = playerBodyDef;
	}
	

}
