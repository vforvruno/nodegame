package com.me.hax.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

public class Ball{

	
	private Body ballBody;
	private BodyDef ballBodyDef;
	private FixtureDef ballFixtureDef;
	private Shape ballShape;
	private Vector2 position;
	public static float maxSpeed = 5f;
	public static float maxKickVel =3f;

	public Ball(Vector2 position, float size, Shape shape,
			World world) {

		if (shape == null) {
			ballShape = new CircleShape();

		} else {
			ballShape = shape;
		}
		
		this.position = position;
		ballBodyDef = new BodyDef();
		ballFixtureDef = new FixtureDef();

		ballBodyDef.type = BodyType.DynamicBody;
		ballBodyDef.position.set(position.x, position.y);

		((CircleShape) ballShape).setRadius(size);

		ballFixtureDef.shape = ballShape;

		ballFixtureDef.restitution = .5f;
		ballFixtureDef.friction = 0f;
		ballFixtureDef.density = 5f;
		ballFixtureDef.isSensor = false;
		

	
		
		ballBody = world.createBody(ballBodyDef);

		ballBody.createFixture(ballFixtureDef);
		
		ballBody.setUserData("ball");
	}


	public float getMaxSpeed() {
		return maxSpeed;
	}


	public void setMaxSpeed(float maxSpeed) {
		Ball.maxSpeed = maxSpeed;
	}


	public float getMaxKickVel() {
		return maxKickVel;
	}


	public void setMaxKickVel(float maxKickVel) {
		Ball.maxKickVel = maxKickVel;
	}


	public Vector2 getPosition() {
		return position;
	}


	public void setPosition(Vector2 position) {
		this.position = position;
	}


	public Body getBallBody() {
		return ballBody;
	}


	public void setBallBody(Body ballBody) {
		this.ballBody = ballBody;
	}


	
}
