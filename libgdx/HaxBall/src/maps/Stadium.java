package maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.ArrayMap;
import com.me.hax.entities.Ball;
import com.me.hax.entities.Player;

public class Stadium implements Screen {

	private World world;
	private Box2DDebugRenderer debugRender;
	private OrthographicCamera camera;
	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8;
	private final int POSITIONITERATIONS = 3;
	private float speed = 5000;
	private Body body;
	private Vector2 movement = new Vector2();
	private Player player1;
	private SpriteBatch batch;

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		player1.movePlayer();

		world.step(Gdx.graphics.getDeltaTime(), VELOCITYITERATIONS, POSITIONITERATIONS);
		debugRender.render(world, camera.combined);
		
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportHeight = height / 100;
		camera.viewportWidth = width / 100;
		camera.update();
	}

	@Override
	public void show() {

		camera = new OrthographicCamera(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		
		world = new World(new Vector2(0, 0), true);
		
	
		
		
		Ball ball = new Ball(new Vector2(0,0), .2f, new CircleShape(), world);
		player1 = new Player(2, new Vector2(-2, 0), .3f, new CircleShape(),	world, camera.combined);

		world.setContactListener(player1); 
		
		Gdx.input.setInputProcessor(new InputMultiplexer(

		new com.me.hax.InputController() {
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
				case Keys.ESCAPE:
					// Game g = (Game) Gdx.app.getApplicationListener();
					// g.setScreen(new SelectGame());
					System.exit(0);
					break;
				}
				return false;
			}
		}, player1));

		debugRender = new Box2DDebugRenderer();


		BodyDef bodyDef = new BodyDef();

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1;
		fixtureDef.friction = 3f;
		fixtureDef.restitution = 0f;

		// Input

		// Piso.
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		// Shape
		ChainShape groundShape = new ChainShape();
		groundShape.createChain(
				new Vector2[] { 
						new Vector2(-6, -.8f),
						new Vector2(-6, -3.5f), new Vector2(6, -3.5f),
						new Vector2(6, -.8f), new Vector2(6.1f, -.8f),
						new Vector2(6.1f, -3.6f), new Vector2(-6.1f, -3.6f),
						new Vector2(-6.1f, -.8f), new Vector2(-6f, -.8f),
						}
				);

		// Fixture
		fixtureDef.shape = groundShape;
		fixtureDef.friction = 1.5f;
		fixtureDef.restitution = 0;

		
		Body ground = world.createBody(bodyDef);
		ground.createFixture(fixtureDef);
		
		
		//------First---------
		BodyDef robeBody = new BodyDef();
		robeBody.type = BodyType.StaticBody;
		robeBody.position.set(new Vector2(-6.05f,-.8f));
		//
		CircleShape robeShape = new CircleShape();
		robeShape.setRadius(.12f);
		//
		FixtureDef robeFixture = new FixtureDef();
		robeFixture.density = 1;
		robeFixture.friction = .1f;
		robeFixture.restitution = 1f;
		robeFixture.shape = robeShape;

		
		//-------Second_1---------
		robeBody.position.set(new Vector2(7f,.8f));
		Body Brobe2a_1 = world.createBody(robeBody);
		Brobe2a_1.createFixture(robeFixture);
		
		//-------Third---------
		robeBody.position.set(new Vector2(6.05f,.8f));
		Body Brobe1b = world.createBody(robeBody);
		Brobe1b.createFixture(robeFixture);
		
		
		//red del arco
		ArrayMap<Integer, Body> net = new ArrayMap<Integer, Body>();
		
		float startPoint 	= Brobe1b.getPosition().x + (robeShape.getRadius() + .1f );
		float endPoint  	= Brobe2a_1.getPosition().x -  (robeShape.getRadius() + .1f );
		
		
		float distance_1 = endPoint - startPoint;	
		int aux = 1;
		
		for(float f = .0f; f <= distance_1; f += 0.12f ){
			 
			robeBody.position.set(new Vector2(startPoint+f, Brobe2a_1.getPosition().y));
			
			robeShape.setRadius(.03f);
			robeBody.type = BodyType.DynamicBody;
			Body auxNet_A = world.createBody(robeBody);
			auxNet_A.createFixture(robeFixture);
			net.put(aux, auxNet_A);
			aux++;
		}
		

		for(int x = 1; x <= net.size; x++){
			
			if( x + 1 < net.size ){ 
				DistanceJointDef n = new DistanceJointDef();
				Body bodyA = net.get( x );
				Body bodyB = net.get( x + 1 );
				n.bodyA = bodyA;
				n.bodyB = bodyB;
				
				
				n.length = .12f;
				n.collideConnected = false;
				world.createJoint(n);
			}
		}


			
		body = world.createBody(bodyDef);
		groundShape.dispose();

		// //

		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		// Shape
		ChainShape groundShape1 = new ChainShape();
		groundShape1.createChain(
				new Vector2[] {
						new Vector2(6, .8f),
						new Vector2(6, 3.5f), new Vector2(-6, 3.5f),
						new Vector2(-6, .8f), new Vector2(-6.1f, .8f),
						new Vector2(-6.1f, 3.6f), new Vector2(6.1f, 3.6f),
						new Vector2(6.1f, .8f), new Vector2(6f, .8f), 
						}
				);

		// Fixture
		fixtureDef.shape = groundShape1;
		fixtureDef.friction = 1.5f;
		fixtureDef.restitution = 0;

		Body ground1 = world.createBody(bodyDef);
		ground1.createFixture(fixtureDef);
		
		

		body = world.createBody(bodyDef);
		groundShape.dispose();

	
		
		
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
