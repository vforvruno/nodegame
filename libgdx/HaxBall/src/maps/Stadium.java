package maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.me.hax.BallContactListener;
import com.me.hax.entities.Ball;
import com.me.hax.entities.Player;

public class Stadium implements Screen {

	private World world;
	private Box2DDebugRenderer debugRender;
	private OrthographicCamera camera;
	private final int VELOCITYITERATIONS = 8;
	private final int POSITIONITERATIONS = 3;
	private Player player1, player2;
	private ShapeRenderer renderer;
	private TextureRegion region;
	private Ball ball;
	SpriteBatch batch = new SpriteBatch();
	private Texture texture;
	private Array<Body> bodyForPainting = new Array<Body>();
	
	@Override
	public void render(float delta) {
		
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		

		world.step(Gdx.graphics.getDeltaTime(), VELOCITYITERATIONS, POSITIONITERATIONS);
		
		debugRender.render(world, camera.combined);
	
		//soccer image field
		batch.begin();
		batch.draw(region, 90, 35, 1450, 730);
		batch.end();
		for(Body b: bodyForPainting){
			renderer.begin(ShapeType.Filled);
			renderer.setColor(Color.WHITE);
			renderer.circle(b.getPosition().x, b.getPosition().y, b.getFixtureList().get(0).getShape().getRadius(), 10);
			renderer.end();
		}
		
		//Paint soccer field
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.WHITE);
		renderer.rect(-6.01f , 3.5f, 12,.02f);
		renderer.rect(6f , -3.53f, -12,.02f);
		renderer.rect(0,-3.5f,.02f,7f);
		renderer.rect(6f,-3.5f,.02f,7f);
		renderer.rect(-6.03f,-3.5f,.02f,7f);
		
		renderer.rect(-6.03f,-1,1,.02f);
		renderer.rect(-6.03f,1,1,.02f);
		renderer.rect(-5.05f,-1f,.02f,2f);
		
		renderer.rect(5.03f,1,1,.02f);
		renderer.rect(5.03f,-1,1,.02f);
		renderer.rect(5.03f,-1f,.02f,2f);
		
		renderer.circle(0, 0, .1f, 10);
		renderer.end();
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.WHITE);
		renderer.circle(0, 0, .7f, 20);
		renderer.end();
		
		//Paint ball.
		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.BLACK);
		renderer.circle(ball.getBallBody().getPosition().x, ball.getBallBody().getPosition().y, .2f, 50);
		renderer.setColor(.160f,.20f,.20f,.10f);
		renderer.circle(ball.getBallBody().getPosition().x, ball.getBallBody().getPosition().y, .15f, 50);
		renderer.end();
		
	
	
		player1.movePlayer();
		player2.movePlayer();
		ball.stopBall();
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportHeight = height / 100;
		camera.viewportWidth = width / 100;
		camera.update();
	}

	@Override
	public void show() {
		Texture.setEnforcePotImages(false);
		
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
	
		renderer = new ShapeRenderer();

		
		texture = new Texture(Gdx.files.internal("data/soccer.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		region = new TextureRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
		world = new World(new Vector2(0, 0), true);
		
		ball = new Ball(new Vector2(-2,0), .2f, new CircleShape(), world);
		
		//Controls Declaration for player1, must be in order : up,down,left, right.
		int cPlayer1[] = {Keys.UP, Keys.DOWN, Keys.LEFT,Keys.RIGHT, Keys.SPACE};
		player1 = new Player(2, new Vector2(2, 0), .3f, new CircleShape(),	world, camera.combined, cPlayer1, new Color(.50f,.200f,.255f,.10f));
		//Second player
		int cPlayer2[] = {Keys.W, Keys.S, Keys.A,Keys.D, Keys.ALT_LEFT};
		player2 = new Player(2, new Vector2(-2, 0), .3f, new CircleShape(),	world, camera.combined, cPlayer2, new Color(.255f,.200f,.50f,.10f));
		
		world.setContactListener(new BallContactListener()); 
		
		Gdx.input.setInputProcessor(new InputMultiplexer(
		player1, player2,
		
		new com.me.hax.InputController() {
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
				case Keys.ESCAPE:
					//player1.getPlayerBodyDef().position.set(new Vector2(0,0));
					break;
				}
				return false;
			}
		}));

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
						new Vector2(-6, -1f),
						new Vector2(-6, -3.5f),
						new Vector2(6, -3.5f),
						new Vector2(6, -1f), 
						new Vector2(6.02f, -1f),
						new Vector2(6.02f, -3.52f), 
						new Vector2(-6.02f, -3.52f),
						new Vector2(-6.02f, -1f),
						new Vector2(-6f, -1f),
						}
				);

		// Fixture
		fixtureDef.shape = groundShape;
		fixtureDef.friction = 1.5f;
		fixtureDef.restitution = 0;

		Body ground = world.createBody(bodyDef);
		ground.createFixture(fixtureDef);
		
		
	
		


		// //

		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		// Shape
		ChainShape groundShape1 = new ChainShape();
		groundShape1.createChain(
				new Vector2[] { 
						new Vector2(6, 1f),
						new Vector2(6, 3.5f),
						new Vector2(-6, 3.5f),
						new Vector2(-6, 1f), 
						new Vector2(-6.02f, 1f),
						new Vector2(-6.02f, 3.52f), 
						new Vector2(6.02f, 3.52f),
						new Vector2(6.02f,1f),
						new Vector2(6f, 1f),
						}
				);
		
		// Fixture
		fixtureDef.shape = groundShape1;
		fixtureDef.friction = 1.5f;
		fixtureDef.restitution = 0;

		Body ground1 = world.createBody(bodyDef);
		ground1.createFixture(fixtureDef);
		
		


	
		
		//Definicion de palos del arco
		BodyDef robeBody = new BodyDef();
		robeBody.type = BodyType.StaticBody;
		CircleShape robeShape = new CircleShape();
		robeShape.setRadius(.06f);
		FixtureDef robeFixture = new FixtureDef();
		robeFixture.density = 1;
		robeFixture.friction = .1f;
		robeFixture.restitution = 1f;
		robeFixture.shape = robeShape;
		
		//Definicion de arcos a la derecha. (4 en total)
		//-------Palo derecha 1°---------
		robeBody.position.set(new Vector2(6.005f,1f));
		Body palo_derecha1 = world.createBody(robeBody);
		bodyForPainting.add(palo_derecha1); //lo agregamos para poder pintarlo despues.
		palo_derecha1.createFixture(robeFixture);
		
		//-------Palo derecha 2° (Atras del palo 1)----
		robeBody.position.set(new Vector2(7f,.8f));
		Body palo_derecha_atras1 = world.createBody(robeBody);
		bodyForPainting.add(palo_derecha_atras1);
		palo_derecha_atras1.createFixture(robeFixture);
		
		//-------Palo derecha 3°---------
		robeBody.position.set(new Vector2(6.005f,-1f));
		Body palo_derecha2 = world.createBody(robeBody);
		bodyForPainting.add(palo_derecha2);
		palo_derecha2.createFixture(robeFixture);
		
		//-------Palo derecha 4° (Atras del palo 3)----
		robeBody.position.set(new Vector2(7f,-.8f));
		Body palo_derecha_atras2 = world.createBody(robeBody);
		bodyForPainting.add(palo_derecha_atras2);
		palo_derecha_atras2.createFixture(robeFixture);
		
		Body aasd = world.createBody(robeBody);
		aasd.createFixture(robeFixture);
		
		//armamos red del arco de la derecha
		Body palos[] = {palo_derecha1, palo_derecha_atras1, palo_derecha_atras2,palo_derecha2};
		armarRedArco(palos);
		
		////////////////////////////////////////////////////////////////////////////////////////
		
		//Definicion de arcos a la derecha. (4 en total)
		
		//-------Palo derecha 1°---------
		robeBody.position.set(new Vector2(-6.005f,1f));
		Body palo_izq1 = world.createBody(robeBody);
		bodyForPainting.add(palo_izq1);
		palo_izq1.createFixture(robeFixture);
		
		//-------Palo derecha 2° (Atras del palo 1)----
		robeBody.position.set(new Vector2(-7f,.8f));
		Body palo_izq_atras1 = world.createBody(robeBody);
		bodyForPainting.add(palo_izq_atras1);
		palo_izq_atras1.createFixture(robeFixture);
		
		//-------Palo derecha 3°---------
		robeBody.position.set(new Vector2(-6.005f,-1f));
		Body palo_izq2 = world.createBody(robeBody);
		bodyForPainting.add(palo_izq2);
		palo_izq2.createFixture(robeFixture);
		
		//-------Palo derecha 4° (Atras del palo 3)----
		robeBody.position.set(new Vector2(-7f,-.8f));
		Body palo_izq_atras2 = world.createBody(robeBody);
		bodyForPainting.add(palo_izq_atras2);
		palo_izq_atras2.createFixture(robeFixture);
		
		Body aasd1 = world.createBody(robeBody);
		aasd1.createFixture(robeFixture);
		
		//armamos red del arco de la derecha
		Body palosizq[] = {palo_izq1, palo_izq_atras1, palo_izq_atras2,palo_izq2};
		armarRedArco(palosizq);
	}

	public void  armarRedArco(Body[] palos){
		//los palos tienen que estar en orden.
				
		int cantPalos = palos.length+1;		
		
		for(int i = 0; i <= palos.length; i++){
			
			if( i+1 > palos.length-1 ){
				break;
			}
			Body palo_a = palos[i];
			Body palo_b = palos[i+1];
			
			//
			
			float start_x = palo_a.getPosition().x;
			float start_y = palo_a.getPosition().y;
			
			float end_x = palo_b.getPosition().x;
			float end_y = palo_b.getPosition().y;
			
			float diferencia_x = 0;
			float diferencia_y = 0;
			
			
			if(start_x > end_x){
				diferencia_x = (start_x - end_x);
			}else if( start_x < end_x){
				 diferencia_x = (end_x - start_x);
			}
			
			if(start_y > end_y){
				diferencia_y = (start_y - end_y);
			}else if( start_y < end_y){
				diferencia_y = (end_y -start_y );
			}
			
			float cant_x = (diferencia_x / 8);
			float cant_y = (diferencia_y / 8);
			
			ArrayMap<Integer, Body> netLink = new ArrayMap<Integer, Body>();
			netLink.put(0, palo_a);
			for(int u = 1; u < 8; u++){
				float nPosition_x = start_x > end_x ? (start_x - (cant_x * u)) : (start_x + (cant_x * u));
				float nPosition_y = start_y > end_y ? (start_y - (cant_y * u)) : (start_y + (cant_y * u));
				BodyDef b_red = new BodyDef();
				FixtureDef f_red = new FixtureDef();
				CircleShape c_red= new CircleShape();
				b_red.type = BodyType.DynamicBody;
				f_red.shape = c_red;
				//c_red.setPosition(new Vector2(nPosition_x,nPosition_y));
				c_red.setRadius(.02f);
				Body b = world.createBody(b_red);
				bodyForPainting.add(b);
				b.createFixture(f_red);
				netLink.put(u, b);
			}
			netLink.put(netLink.size, palo_b);

			for(int e = 0; e <= netLink.size - 1; e++){
				if(e + 1 <= netLink.size - 1){
					Body b1 = netLink.get(e);
					Body b2 = netLink.get(e+1);
					DistanceJointDef n = new DistanceJointDef();
					n.bodyA = b1;
					n.bodyB = b2;
					n.length = .1f;
					world.createJoint(n);
					
				}
			}
			

			
		}
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
