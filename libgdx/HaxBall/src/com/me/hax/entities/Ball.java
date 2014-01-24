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

	
	private Body playerBody;
	private BodyDef playerBodyDef;
	private FixtureDef playerFixtureDef;
	private Shape playerShape;


	public Ball(Vector2 position, float size, Shape shape,
			World world) {

		if (shape == null) {
			playerShape = new CircleShape();

		} else {
			playerShape = shape;
		}

		playerBodyDef = new BodyDef();
		playerFixtureDef = new FixtureDef();

		playerBodyDef.type = BodyType.DynamicBody;
		playerBodyDef.position.set(position.x, position.y);

		((CircleShape) playerShape).setRadius(size);

		playerFixtureDef.shape = playerShape;

		playerFixtureDef.restitution = .5f;
		playerFixtureDef.friction = .5f;
		playerFixtureDef.density = 5f;
		playerFixtureDef.isSensor = false;
		

	
		
		playerBody = world.createBody(playerBodyDef);

		playerBody.createFixture(playerFixtureDef);
		
		playerBody.setUserData("ball");
	}


	
}
