package aca15jch;

import aca15jch.gmaths.Mat4;
import aca15jch.gmaths.Mat4Transform;
import aca15jch.gmaths.Vec3;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;

public class Anilamp_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;

	public Anilamp_GLEventListener(Camera camera) {
		this.camera = camera;
		this.camera.setPosition(new Vec3(4f, 12f, 18f));
	}

	// ***************************************************
	/*
	 * METHODS DEFINED BY GLEventListener
	 */

	/* Initialisation */
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LESS);
		gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
		gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
		gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
		initialise(gl);
		startTime = getSeconds();
	}

	/* Called to indicate the drawing surface has been moved and/or resized  */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glViewport(x, y, width, height);
		float aspect = (float) width / (float) height;
		camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
	}

	/* Draw */
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		render(gl);
	}

	/* Clean up memory, if necessary */
	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		skybox.dispose(gl);
		for (Model wall : this.walls) {
			wall.dispose(gl);
		}
		tabletop.dispose(gl);
		for (Model leg : this.legs) {
			leg.dispose(gl);
		}
		for (Model panel : this.deskTidy) {
			panel.dispose(gl);
		}
		for (Model pen : this.pens) {
			pen.dispose(gl);
		}
		paper.dispose(gl);
		paperweight.dispose(gl);
		smartphone.dispose(gl);
		light.dispose(gl);
	}


	// ***************************************************
	/* INTERACTION
	 *
	 *
	 */

	public void jump() {
		xPosition += 0.5f;
		if (xPosition > 5f) xPosition = 5f;
		updateX();
	}

	public void resetPosition() {
		xPosition =-3;
		updateX();
	}

	private void updateX() {
		translateX.setTransform(Mat4Transform.translate(xPosition, 0, 0));
		translateX.update(); // IMPORTANT – the scene graph has changed
	}


	// ***************************************************
	/* THE SCENE
	 * Now define all the methods to handle the scene.
	 * This will be added to in later examples.
	 */

	private Camera camera;
	private Mat4 perspective;
	private Model skybox, floor, tabletop, paper, paperweight, smartphone;
	private List<Model> walls	 = new ArrayList<Model>(),
						legs 	 = new ArrayList<Model>(),
						deskTidy = new ArrayList<Model>(),
						pens	 = new ArrayList<Model>();
	private Light light;
	private SGNode lampRoot;

	private TransformNode translateX, rotateBicep, rotateForearm, rotateHead;
	private float a4Length  = 210f,
				  xPosition = 3,
			      rotateBicepAngleStart =  30, rotateBicepAngle		= rotateBicepAngleStart,
			 	  rotateForearmAngleStart   = -60, rotateForearmAngle   = rotateForearmAngleStart,
				  rotateHeadAngleStart = 30, rotateHeadAngle = rotateHeadAngleStart;


	private Vec3 lampBaseDim	  = new Vec3(1,0.125f,1),
				 lampBicepDim	  = new Vec3(lampBaseDim.x/4,(float) (lampBaseDim.x*Math.sqrt(3)/2),lampBaseDim.x/4),
				 lampElbowDim	  = new Vec3(lampBicepDim.x, lampBicepDim.x, lampBicepDim.x),
				 lampForearmDim	  = new Vec3(lampBicepDim.x, (float) (lampBaseDim.x*Math.sqrt(3)/2), lampBicepDim.x),
				 lampHeadDim	  = new Vec3(lampBaseDim.x,lampBaseDim.x/2,lampBaseDim.x/2),
				 tabletopDim	  = new Vec3(8,0.5f,4),
				 legDim			  = new Vec3(0.5f,3,0.5f),
				 deskTidyPanelDim = new Vec3 (0.5f,0.5f,0.125f),
				 //Huawei P10 to scale with A4 paper
				 smartphoneDim = new Vec3((float) (69.3/a4Length), (float) (7/a4Length), 145.9f/a4Length);

	private void initialise(GL3 gl) {
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);
		createRandomNumbers();
		int[] sky = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\cloud.jpg");
		int[] chequerboard = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\chequerboard.jpg");
		int[] wallpaper = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\wallpaper.jpg");
		int[] wood = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\wood.jpg");
		int[] textureId1 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\jade.jpg");
		int[] textureId2 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\jade_specular.jpg");
		int[] globe = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\ear0xuu2.jpg");
		int[] globe_specular = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\ear0xuu2_specular.jpg");
		int[] metal = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\metal.jpg");
		int[] metal_specular = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\metal_specular.jpg");
		int[] smartphone_net = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\smartphone_net.jpg");
		int[] smartphone_net_specular = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\smartphone_net_specular.jpg");

		//Instantiate a Light object that represents the bulb of the lamp.
		light = new Light(gl);
		light.setCamera(camera);

		//***SKYBOX***
		Mesh mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		Shader shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_textured_blinn_phong.glsl",
				user_dir + "\\src\\aca15jch\\fs_textured_blinn_phong.glsl");
		Material material = new Material(new Vec3(1, 1, 1), new Vec3(1, 1, 1),
				new Vec3(0, 0, 0), 20f);
		Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(64, 64, 64),
				Mat4Transform.translate(0, 0.25f, 0));
		skybox = new Model(gl, camera, light, shader, material, modelMatrix, mesh, sky, sky);

		//***FLOOR and WALL***
		mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_textured_blinn_phong.glsl",
				user_dir + "\\src\\aca15jch\\fs_textured_blinn_phong.glsl");
		material = new Material(new Vec3(0.81f, 0.81f, 0),
				new Vec3(0.81f, 0.81f, 0), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
		modelMatrix = Mat4Transform.scale(16, 0, 16);
		this.floor = new Model(gl, camera, light, shader, material, modelMatrix, mesh, chequerboard);

		this.walls.add(new Model(gl, camera, light, shader, material, vertWallMatrix(false), mesh, wallpaper));
		this.walls.add(new Model(gl, camera, light, shader, material, vertWallMatrix(true), mesh, wallpaper));
		this.walls.add(new Model(gl, camera, light, shader, material, horizWallMatrix(false), mesh, wallpaper));
		this.walls.add(new Model(gl, camera, light, shader, material, horizWallMatrix(true), mesh, wallpaper));

		float minHeight = 0.5f;
		//***TABLE***
		Mat4 tableTransMat = Mat4Transform.translate(new Vec3(0,this.legDim.y,-(this.tabletopDim.z*1.5f)));
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_textured_blinn_phong.glsl",
				user_dir + "\\src\\aca15jch\\fs_textured_blinn_phong.glsl");
		material = new Material(new Vec3(1, 1, 1),
				new Vec3(0.75f, 0.75f, 0.75f
				), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
		modelMatrix = Mat4.multiply(tableTransMat, Mat4Transform.scale(tabletopDim));
		mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		this.tabletop = new Model(gl, camera, light, shader, material, modelMatrix, mesh, wood);
		//Table legs
		for (int i=0; i<4; i++) {
			modelMatrix = Mat4.multiply(tableTransMat,Mat4.multiply(attachLeg(i), Mat4Transform.scale(legDim)));
			this.legs.add(new Model(gl, camera, light, shader, material, modelMatrix, mesh, wood));
		}

		//***PAPER***
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_solid.glsl",
				user_dir + "\\src\\aca15jch\\fs_solid.glsl");
		mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
		modelMatrix = Mat4.multiply(tableTransMat,
						Mat4.multiply(Mat4Transform.translate(-3f,tabletopDim.y/1.9f,-1f),
						Mat4Transform.scale(1,1,(float)Math.sqrt(2))));//ISO 216 aspect ratio like A4
		this.paper = new Model(gl, camera, light, shader, new Material(new Vec3(1, 1, 1),
				new Vec3(1, 1, 1), new Vec3(0, 0, 0), 1), modelMatrix, mesh, null, null);

		//***PAPERWEIGHT***
		mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_textured_blinn_phong.glsl",
				user_dir + "\\src\\aca15jch\\fs_textured_blinn_phong.glsl");
		modelMatrix = Mat4.multiply(tableTransMat, Mat4Transform.translate(-3,0.75f,-1));
		paperweight = new Model(gl, camera, light, shader, material, modelMatrix, mesh, globe, globe_specular);

		//***DESKTIDY***
		Vec3 deskTidyTransVec = new Vec3(3,(deskTidyPanelDim.y+tabletopDim.y)/2,-1);
		material = new Material(new Vec3(0.5f, 0.5f, 1),
				new Vec3(0.5f, 0.5f, 1
				), new Vec3(0.5f, 0.5f, 1), 64.0f);
		mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		float tx, tz, spacing = -(deskTidyPanelDim.x + deskTidyPanelDim.z)/2;
		for (int i=0; i<4; i++) {
			switch(i) {
				case 3:
					tx=-spacing;
					tz=0;
					break;
				case 2:
					tx=0;
					tz=-spacing;
					break;
				case 1:
					tx=spacing;
					tz=0;
					break;
				default:
					tx=0;
					tz=spacing;
			}
			modelMatrix = Mat4.multiply(tableTransMat,
							Mat4.multiply(Mat4Transform.translate(deskTidyTransVec),
								Mat4.multiply(Mat4Transform.translate(tx,0,tz),
									Mat4.multiply(Mat4Transform.rotateAroundY(90 * i),
									Mat4Transform.scale(deskTidyPanelDim)))));
			this.deskTidy.add(new Model(gl, camera, light, shader, material, modelMatrix, mesh, metal, metal_specular));
		}
		//Base of desk tidy
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_solid.glsl",
				user_dir + "\\src\\aca15jch\\fs_solid.glsl");
		mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
		modelMatrix = Mat4.multiply(tableTransMat,
				Mat4.multiply(Mat4Transform.translate(deskTidyTransVec.x,tabletopDim.y/1.9f,deskTidyTransVec.z),
						Mat4Transform.scale(deskTidyPanelDim.x,0,deskTidyPanelDim.y)));
		this.deskTidy.add(new Model(gl, camera, light, shader, new Material(new Vec3(1, 1, 1),
				new Vec3(1, 1, 1), new Vec3(0, 0, 0), 0), modelMatrix, mesh, null, null));

		//***PENS***
		mesh = new Mesh(gl, Cylinder.vertices.clone(), Cylinder.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_solid.glsl",
				user_dir + "\\src\\aca15jch\\fs_solid.glsl");
		for (int i=0; i < 3; i++) {
			Vec3 color;
			float xtrans;
			switch (i) {
				case 1: {
					color = new Vec3(0,1,0);
					xtrans = 0;
					break;
				}
				case 2: {
					color = new Vec3(0,0,1);
					xtrans = deskTidyPanelDim.z;
					break;
				}
				default:
					color = new Vec3(1, 0, 0);
					xtrans = -deskTidyPanelDim.z;
			}

			modelMatrix = Mat4.multiply(tableTransMat,
					Mat4.multiply(Mat4Transform.translate(deskTidyTransVec.x+xtrans,
														  (deskTidyPanelDim.y*1.5f+tabletopDim.y)/2,
															  deskTidyTransVec.z),
							Mat4Transform.scale(deskTidyPanelDim.z, deskTidyPanelDim.y*1.5f, deskTidyPanelDim.z)));
			this.pens.add(new Model(gl, camera, light, shader, new Material(color,
					color, color, 8), modelMatrix, mesh));
		}
		//***SMARTPHONE***
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_textured_blinn_phong.glsl",
				user_dir + "\\src\\aca15jch\\fs_textured_blinn_phong.glsl");
		mesh = new Mesh(gl, CubeNet.vertices.clone(), CubeNet.indices.clone());
		modelMatrix = Mat4.multiply(tableTransMat,
				Mat4.multiply(Mat4Transform.translate(0,(smartphoneDim.y+tabletopDim.y)/2,0),
						Mat4Transform.scale(smartphoneDim)));
		this.smartphone = new Model(gl, camera, light, shader, new Material(new Vec3(0.1f, 0.1f, 0.1f),
				new Vec3(0.1f, 0.1f, 0.1f), new Vec3(0.5f, 0.5f, 0.5f), 0.1f),
				modelMatrix, mesh, smartphone_net, smartphone_net_specular);

		//***LAMP***
		lampRoot = new NameNode("n-branch structure");
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_textured_blinn_phong.glsl",
				user_dir + "\\src\\aca15jch\\fs_textured_blinn_phong.glsl");
		material = new Material(new Vec3(0.1f, 0.1f, 0.1f),
				   new Vec3(0.1f, 0.1f, 0.1f), new Vec3(0.5f, 0.5f, 0.5f), 30);
		//Base
		Vec3 randomPosition = new Vec3(xPosition, 0, 0);
		translateX = new TransformNode("translate(" + xPosition + ",0,0)",
									   Mat4Transform.translate(randomPosition));
		mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		Model lampBase = new Model(gl, camera, light, shader, material,
				modelMatrix, mesh, metal, metal_specular);
		NameNode branch0 = new NameNode("Branch 0");
		TransformNode makeBranch0 = new TransformNode("scale("+lampBaseDim+")", Mat4Transform.scale(lampBaseDim));
		ModelNode lampBaseNode = new ModelNode("Lamp Base", lampBase);
		//Bicep
		rotateBicep = new TransformNode("rotateAroundZ(" + rotateBicepAngle + ")",
				Mat4Transform.rotateAroundZ(rotateBicepAngle));
		mesh = new Mesh(gl, Cylinder.vertices.clone(), Cylinder.indices.clone());
		Model lampBicep = new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal);
		NameNode branch1 = new NameNode("Branch 1");
		Vec3 transVec0 = new Vec3 (0,lampBicepDim.y/2,0);
		Mat4 m = Mat4.multiply(Mat4Transform.translate(transVec0),Mat4Transform.scale(lampBicepDim));
		TransformNode makeBranch1 = new TransformNode("translate("+transVec0+");scale("+lampBicepDim+")", m);
		ModelNode lampBicepNode = new ModelNode("Lamp Bicep", lampBicep);
		//Elbow
		mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		Model lampElbow = new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal);
		NameNode branch2 = new NameNode("Branch 2");
		Vec3 transVec1 = new Vec3 (0,lampBicepDim.y,0);
		m = Mat4.multiply(Mat4Transform.translate(transVec1),
						  Mat4.multiply(Mat4Transform.rotateAroundX(90),Mat4Transform.scale(lampElbowDim)));
		TransformNode makeBranch2 = new TransformNode("translate("+transVec1+");rotateAroundX(90);scale("
													  +lampElbowDim+")", m);
		ModelNode lampElbowNode = new ModelNode("Lamp Elbow", lampElbow);
		//Forearm
		TransformNode attachToElbow = new TransformNode("translate(0,"+lampBicepDim.y+",0)",
				Mat4Transform.translate(0, lampBicepDim.y, 0));
		rotateForearm = new TransformNode("rotateAroundZ(" + rotateForearmAngle + ")",
				Mat4Transform.rotateAroundZ(rotateForearmAngle));
		mesh = new Mesh(gl, Cylinder.vertices.clone(), Cylinder.indices.clone());
		Model lampForearm = new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal);
		NameNode branch3 = new NameNode("Branch 3");
		m = Mat4Transform.scale(lampForearmDim);
		Vec3 transVec2 = new Vec3 (0,lampForearmDim.y/2,0);
		m = Mat4.multiply(Mat4Transform.translate(transVec2),m);
		TransformNode makeBranch3 = new TransformNode("scale"+lampForearmDim+";translate"+transVec2, m);
		ModelNode lampForearmNode = new ModelNode("Lamp Forearm", lampForearm);
		//Head
		TransformNode attachToForearm = new TransformNode("translate(0,"+lampBaseDim.y+",0)",
				Mat4Transform.translate(0, lampForearmDim.y, 0));
		rotateHead = new TransformNode("rotateAroundZ(" + rotateHeadAngle + ")",
				Mat4Transform.rotateAroundZ(rotateHeadAngle));
		mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		NameNode branch4 = new NameNode("Branch 4");
		m = Mat4Transform.scale(lampHeadDim);
		TransformNode makeHighestBranch = new TransformNode("scale("+lampHeadDim+")", m);
		ModelNode cube2Node = new ModelNode("Cube(2)",
				new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal_specular));
		lampRoot.addChild(translateX);
			translateX.addChild(branch0);
				branch0.addChild(makeBranch0);
					makeBranch0.addChild(lampBaseNode);
				branch0.addChild(rotateBicep);
					rotateBicep.addChild(branch1);
						branch1.addChild(makeBranch1);
							makeBranch1.addChild(lampBicepNode);
						branch1.addChild(branch2);
							branch2.addChild(makeBranch2);
								makeBranch2.addChild(lampElbowNode);
							branch2.addChild(attachToElbow);
								attachToElbow.addChild(rotateForearm);
									rotateForearm.addChild(branch3);
										branch3.addChild(makeBranch3);
											makeBranch3.addChild(lampForearmNode);
										branch3.addChild(attachToForearm);
											attachToForearm.addChild(rotateHead);
												rotateHead.addChild(branch4);
													branch4.addChild(makeHighestBranch);
														makeHighestBranch.addChild(cube2Node);

		lampRoot.update();  // IMPORTANT – must be done every time any part of the scene graph changes
		//lampRoot.print(0, false);
		//System.exit(0);
	}


	private Mat4 attachLeg(int n) {
		float x,z;
		switch (n) {
			case 3:
				x=3.5f;
				z=1.5f;
				break;
			case 2:
				x=-3.5f;
				z=1.5f;
				break;
			case 1:
				x=3.5f;
				z=-1.5f;
				break;
			default:
				x=-3.5f;
				z=-1.5f;
				break;
		}
		return Mat4Transform.translate(x,-legDim.y/2,z);
	}


	private void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		Vec3 bulb = new Vec3(xPosition+lampHeadDim.x/2,1+lampHeadDim.y,0);

		light.setPosition(bulb);
		light.setDirection(4+xPosition, 0, 0);
		light.render(gl);
		floor.render(gl);
		for (Model wall : this.walls) {
			wall.render(gl);
		}
		tabletop.render(gl);
		for (Model leg : this.legs) {
			leg.render(gl);
		}
		for (Model panel : this.deskTidy) {
			panel.render(gl);
		}
		for (Model pen : this.pens) {
			gl.glFrontFace(gl.GL_CCW);
			pen.render(gl);
		}
		paper.render(gl);
		paperweight.render(gl);
		smartphone.render(gl);
		gl.glFrontFace(gl.GL_CW);
		skybox.render(gl);
		gl.glFrontFace(gl.GL_CCW);
		//updateBranches();
		lampRoot.draw(gl);
	}

	private void updateBranches() {
		double elapsedTime = getSeconds() - startTime;
		rotateBicepAngle = rotateBicepAngleStart * (float) Math.sin(elapsedTime* Math.PI*0.5);
		rotateForearmAngle = rotateForearmAngleStart * (float) Math.sin(elapsedTime * Math.PI*0.5);
		rotateHeadAngle = rotateHeadAngleStart * (float) Math.sin(elapsedTime *  Math.PI*0.5);
		rotateBicep.setTransform(Mat4Transform.rotateAroundZ(rotateBicepAngle));
		rotateForearm.setTransform(Mat4Transform.rotateAroundZ(rotateForearmAngle));
		rotateHead.setTransform(Mat4Transform.rotateAroundZ(rotateHeadAngle));
		lampRoot.update(); // IMPORTANT – the scene graph has changed
	}

	private Mat4 vertWallMatrix(boolean top) {
		float size=16/3f;
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.translate(0, (top ? size*5 : size) * 0.5f, -16f * 0.5f), modelMatrix);
		return modelMatrix;
	}

	private Mat4 horizWallMatrix(boolean left) {
		float size=16/3f;
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1, 16), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.translate((left ? -1 : 1)*size, 8, -16f * 0.5f), modelMatrix);
		return modelMatrix;
	}

	// ***************************************************
	/* TIME
	 */

	private double startTime;

	private double getSeconds() {
		return System.currentTimeMillis() / 1000.0;
	}

	// ***************************************************
	/* An array of random numbers
	 */

	private int NUM_RANDOMS = 1000;
	private float[] randoms;

	private void createRandomNumbers() {
		randoms = new float[NUM_RANDOMS];
		for (int i = 0; i < NUM_RANDOMS; ++i) {
			randoms[i] = (float) Math.random();
		}
	}

}

class MyKeyboardInput extends KeyAdapter {
	private Camera camera;

	public MyKeyboardInput(Camera camera) {
		this.camera = camera;
	}

	public void keyPressed(KeyEvent e) {
		Camera.Movement m = Camera.Movement.NO_MOVEMENT;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:  m = Camera.Movement.LEFT;  break;
			case KeyEvent.VK_RIGHT: m = Camera.Movement.RIGHT; break;
			case KeyEvent.VK_UP:    m = Camera.Movement.UP;    break;
			case KeyEvent.VK_DOWN:  m = Camera.Movement.DOWN;  break;
			case KeyEvent.VK_A:  m = Camera.Movement.FORWARD;  break;
			case KeyEvent.VK_Z:  m = Camera.Movement.BACK;  break;
		}
		camera.keyboardInput(m);
	}
}

class MyMouseInput extends MouseMotionAdapter {
	private Point lastpoint;
	private Camera camera;

	public MyMouseInput(Camera camera) {
		this.camera = camera;
	}

	/**
	 * mouse is used to control camera position
	 *
	 * @param e  instance of MouseEvent
	 */
	public void mouseDragged(MouseEvent e) {
		Point ms = e.getPoint();
		float sensitivity = 0.001f;
		float dx=(float) (ms.x-lastpoint.x)*sensitivity;
		float dy=(float) (ms.y-lastpoint.y)*sensitivity;
		//System.out.println("dy,dy: "+dx+","+dy);
		if (e.getModifiers()==MouseEvent.BUTTON1_MASK)
			camera.updateYawPitch(dx, -dy);
		lastpoint = ms;
	}

	/**
	 * mouse is used to control camera position
	 *
	 * @param e  instance of MouseEvent
	 */
	public void mouseMoved(MouseEvent e) {
		lastpoint = e.getPoint();
	}
}
