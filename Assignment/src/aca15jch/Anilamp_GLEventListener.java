package aca15jch;

import aca15jch.gmaths.*;
import com.jogamp.opengl.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Event listener class. Mainly based code on M04_GLEventListener from Chapter 7 scene graph.
 *
 * @author aca15jch
 */
public class Anilamp_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;

	/**
	 * Constructor
	 * @param camera
	 */
	public Anilamp_GLEventListener(Camera camera) {
		this.camera = camera;
		this.camera.setPosition(new Vec3(4, -1, 5));
	}

	/**
	 *
	 * @param drawable
	 */
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
		this.startTime = getSeconds();
	}

	/**
	 * Called to indicate the drawing surface has been moved and/or resized
	 * @param drawable
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glViewport(x, y, width, height);
		float aspect = (float) width / (float) height;
		camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
	}

	/**
	 *
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		render(gl);
	}

	/**
	 * Clean up memory, if necessary
	 * @param drawable
	 */
	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		this.skybox.dispose(gl);
		for (Model wall : this.walls) {
			wall.dispose(gl);
		}
		this.tabletop.dispose(gl);
		for (Model leg : this.legs) {
			leg.dispose(gl);
		}
		for (Model panel : this.deskTidy) {
			panel.dispose(gl);
		}
		for (Model pen : this.pens) {
			pen.dispose(gl);
		}
		this.paper.dispose(gl);
		this.paperweight.dispose(gl);
		this.smartphone.dispose(gl);
		this.light.dispose(gl);
	}


	// ***************************************************
	/* INTERACTION
	 *
	 *
	 */

	private boolean animation = false, reset = false, posing = false;
	private double savedTime = 0, limit = 2;

	public void startAnimation(boolean reset) {
		this.animation = true;
		this.reset = reset;
		this.posing = false;
		this.startTime = getSeconds() - this.savedTime;
	}

	public void startAnimation(double limit) {
		this.animation = true;
		this.reset = false;
		this.posing = true;
		this.limit = limit;
		this.startTime = getSeconds() - this.savedTime;
	}

	/**
	 * Random jump
	 */
	public void jump() {
		boolean valid = false;
		while (!valid) {
			zPosition = 1 - 2 * (float) Math.random();
			xPosition = 1 + (float) (Math.random() * tabletopDim.x);
			boolean avoidsPaperweight = !(zPosition < 0) || (xPosition > 2);
			boolean avoidsSmartphoneDeskTidy = xPosition < (tabletopDim.x - (lampBaseDim.x + 1));
			valid = avoidsPaperweight && avoidsSmartphoneDeskTidy;
		}

		retransform();
	}

	/**
	 * Reset
	 */
	public void resetPosition() {
		xPosition = 1.55f;
		zPosition = 1.55f;
		reset = false;
		retransform();
	}

	/**
	 * Transformation after each jump
	 */
	private void retransform() {
		double yaw = Math.atan2(xPosition, zPosition) * 180 / Math.PI;
		Mat4 yawMat = Mat4Transform.rotateAroundY((float) yaw - 90);
		translateXZ.setTransform(Mat4.multiply(Mat4Transform.translate(xPosition,
				(lampBaseDim.y - tabletopDim.y) / 2 - 1.33f, zPosition), yawMat));
		translateXZ.update();
	}

	/*FIELDS FOR THE SCENE*/
	private Camera camera;
	private Model skybox, floor, tabletop, paper, paperweight, smartphone;
	private List<Model> walls	 = new ArrayList<Model>(),
						legs 	 = new ArrayList<Model>(),
						deskTidy = new ArrayList<Model>(),
						pens	 = new ArrayList<Model>();
	private Light light;
	private SGNode lampRoot;

	private TransformNode translateXZ, rotateBicep, rotateForearm, rotateHead;
	private float a4Length  = 210f,
				  xPosition = 1.55f, zPosition = 1.55f,
			      rotateBicepAngleStart =  30, rotateBicepAngle		= rotateBicepAngleStart,
			 	  rotateForearmAngleStart   = -60, rotateForearmAngle   = rotateForearmAngleStart,
				  rotateHeadAngleStart = 30, rotateHeadAngle = rotateHeadAngleStart;

	/*DIMENSIONS*/
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

	/**
	 *
	 * @param gl
	 */
	private void initialise(GL3 gl) {
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);
		if (user_dir.endsWith("src")){
			user_dir = user_dir.substring(0,user_dir.length()-4);
		}
		int[] sky = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\cloud.jpg");
		int[] chequerboard = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\chequerboard.jpg");
		int[] wallpaper = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\wallpaper.jpg");
		int[] wood = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\aca15jch\\textures\\wood.jpg");
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
		material = new Material(new Vec3(0.81f, 0.81f, 0),
				new Vec3(0.81f, 0.81f, 0), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
		Mat4 transWallMat = Mat4Transform.translate(
				tabletopDim.x/2, -(legDim.y+tabletopDim.y+1.33f),tabletopDim.z*3/2);
		modelMatrix = Mat4.multiply(transWallMat,Mat4Transform.scale(16, 0, 16));
		this.floor = new Model(gl, camera, light, shader, material, modelMatrix, mesh, chequerboard);

		this.walls.add(new Model(gl, camera, light, shader, material,
				Mat4.multiply(transWallMat, vertWallMatrix(false)), mesh, wallpaper));
		this.walls.add(new Model(gl, camera, light, shader, material,
				Mat4.multiply(transWallMat, vertWallMatrix(true)), mesh, wallpaper));
		this.walls.add(new Model(gl, camera, light, shader, material,
				Mat4.multiply(transWallMat, horizWallMatrix(false)), mesh, wallpaper));
		this.walls.add(new Model(gl, camera, light, shader, material,
				Mat4.multiply(transWallMat, horizWallMatrix(true)), mesh, wallpaper));

		//***TABLE***
		Mat4 tableTransMat = Mat4Transform.translate(new Vec3(tabletopDim.x/2,-(tabletopDim.y+1.33f),0));
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
				new Vec3(1, 1, 1), new Vec3(0, 0, 0), 1), modelMatrix, mesh);

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
				new Vec3(1, 1, 1), new Vec3(0, 0, 0), 0), modelMatrix, mesh));

		//***PENS***
		mesh = new Mesh(gl, Cylinder.vertices.clone(), Cylinder.indices.clone());
		for (int i = 0; i < 3; i++) {
			Vec3 color;
			float xtrans;
			switch (i) {
				case 1: {
					color = new Vec3(0, 1, 0);
					xtrans = 0;
					break;
				}
				case 2: {
					color = new Vec3(0, 0, 1);
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
				Mat4.multiply(Mat4Transform.translate(deskTidyTransVec.x,(smartphoneDim.y+tabletopDim.y)/2,
						-deskTidyTransVec.z), Mat4Transform.scale(smartphoneDim)));
		this.smartphone = new Model(gl, camera, light, shader, new Material(new Vec3(0.1f, 0.1f, 0.1f),
				new Vec3(0.1f, 0.1f, 0.1f), new Vec3(0.5f, 0.5f, 0.5f), 0.1f),
				modelMatrix, mesh, smartphone_net, smartphone_net_specular);

		//***LAMP***
		lampRoot = new NameNode("n-branch structure");
		material = new Material(new Vec3(0.1f, 0.1f, 0.1f),
				   new Vec3(0.1f, 0.1f, 0.1f), new Vec3(0.5f, 0.5f, 0.5f), 30);
		//Base
		Vec3 initPosition = new Vec3(xPosition, (lampBaseDim.y-tabletopDim.y)/2-1.33f, zPosition);
		translateXZ = new TransformNode("translate(" + initPosition + ")",
									   Mat4Transform.translate(initPosition));
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
		Model lampBicep = new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal_specular);
		NameNode branch1 = new NameNode("Branch 1");
		Vec3 transVec0 = new Vec3 (0,lampBicepDim.y/2,0);
		Mat4 m = Mat4.multiply(Mat4Transform.translate(transVec0),Mat4Transform.scale(lampBicepDim));
		TransformNode makeBranch1 = new TransformNode("translate("+transVec0+");scale("+lampBicepDim+")", m);
		ModelNode lampBicepNode = new ModelNode("Lamp Bicep", lampBicep);
		//Elbow
		mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		Model lampElbow = new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal_specular);
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
		Model lampForearm = new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal_specular);
		NameNode branch3 = new NameNode("Branch 3");
		m = Mat4Transform.scale(lampForearmDim);
		Vec3 transVec2 = new Vec3 (0,lampForearmDim.y/2,0);
		m = Mat4.multiply(Mat4Transform.translate(transVec2),m);
		TransformNode makeBranch3 = new TransformNode("translate("+transVec2+");scale("+lampForearmDim+")", m);
		ModelNode lampForearmNode = new ModelNode("Lamp Forearm", lampForearm);
		//Head
		TransformNode attachToForearm = new TransformNode("translate(0,"+lampForearmDim.y+",0)",
				Mat4Transform.translate(0, lampForearmDim.y, 0));
		rotateHead = new TransformNode("rotateAroundZ(" + rotateHeadAngle + ")",
				Mat4Transform.rotateAroundZ(rotateHeadAngle));
		mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		NameNode branch4 = new NameNode("Branch 4");
		m = Mat4Transform.scale(lampHeadDim);
		TransformNode makeBranch4 = new TransformNode("scale("+lampHeadDim+")", m);
		ModelNode lampHeadNode = new ModelNode("Lamp Head",
				new Model(gl, camera, light, shader, material, new Mat4(1), mesh, metal, metal_specular));
		//Head Decoration
		TransformNode decorateHeadL = new TransformNode("translate(0,0,"+(-lampHeadDim.z/2)+")",
				Mat4Transform.translate(0, 0, -lampHeadDim.z/2));
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_solid.glsl",
				user_dir + "\\src\\aca15jch\\fs_solid.glsl");
		material =  new Material(new Vec3(0, 0, 1),
				new Vec3(0, 0, 1), new Vec3(0, 0, 1), 10);
		NameNode branch4_5 = new NameNode("Branch 4.5");
		m = Mat4Transform.scale(Vec3.multiply(lampHeadDim,0.5f));
		TransformNode makeBranch4_5 = new TransformNode("scale("+Vec3.multiply(lampHeadDim,0.5f)+")", m);
		ModelNode lampLeft = new ModelNode("Lamp Left",
				new Model(gl, camera, light, shader, material, new Mat4(1),mesh));
		TransformNode decorateHeadR = new TransformNode("translate(0,0,"+(lampHeadDim.z/2)+")",
				Mat4Transform.translate(0, 0,lampHeadDim.z/2));
		NameNode branch4_75 = new NameNode("Branch 4.75");
		TransformNode makeBranch4_75 = new TransformNode("scale("+Vec3.multiply(lampHeadDim,0.5f)+")", m);
		ModelNode lampRight = new ModelNode("Lamp Right",
				new Model(gl, camera, light, shader, material, new Mat4(1),mesh));
		//Bulb
		TransformNode attachToHead = new TransformNode("translate("+lampHeadDim.x/2+"0,0)",
				Mat4Transform.translate(lampHeadDim.x/2, 0, 0));
		mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		NameNode branch5 = new NameNode("Branch 5");
		float diameter=lampHeadDim.x/2;
		m = Mat4Transform.scale(diameter,diameter,diameter);
		TransformNode makeBranch5 = new TransformNode("scale("+diameter+","+diameter+","+diameter+")", m);
		material =  new Material(new Vec3(1, 1, 1),
				new Vec3(1, 1, 1), new Vec3(1, 1, 1), 10);
		ModelNode lampBulbNode = new ModelNode("Lamp Bulb",
				new Model(gl, camera, light, shader, material, new Mat4(1), mesh));
		lampRoot.addChild(translateXZ);
			translateXZ.addChild(branch0);
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
													branch4.addChild(makeBranch4);
														makeBranch4.addChild(lampHeadNode);
													branch4.addChild(decorateHeadL);
														decorateHeadL.addChild(branch4_5);
															branch4_5.addChild(makeBranch4_5);
																makeBranch4_5.addChild(lampLeft);
													branch4.addChild(decorateHeadR);
														decorateHeadR.addChild(branch4_75);
															branch4_75.addChild(makeBranch4_75);
																makeBranch4_75.addChild(lampRight);
													branch4.addChild(attachToHead);
														attachToHead.addChild(branch5);
															branch5.addChild(makeBranch5);
																makeBranch5.addChild(lampBulbNode);
		lampRoot.update();
		resetPosition();
	}

	/**
	 * Translations to attach legs to table.
	 *
	 * @param n
	 * @return Translation matrix according to the leg number it is.
	 */
	private Mat4 attachLeg(int n) {
		float x, z;
		switch (n) {
			case 3:
				x = 3.5f;
				z = 1.5f;
				break;
			case 2:
				x = -3.5f;
				z = 1.5f;
				break;
			case 1:
				x = 3.5f;
				z = -1.5f;
				break;
			default:
				x = -3.5f;
				z = -1.5f;
				break;
		}
		return Mat4Transform.translate(x, -legDim.y / 2, z);
	}

	/**
	 * Translations to put up top and bottom wall panels.
	 *
	 * @param top
	 * @return translation matrix
	 */
	private Mat4 vertWallMatrix(boolean top) {
		float size = 16 / 3f;
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.translate(0, (top ? size * 5 : size) * 0.5f, -16f * 0.5f), modelMatrix);
		return modelMatrix;
	}

	/**
	 * Translations to put up left and right wall panels.
	 *
	 * @param left
	 * @return translation matrix
	 */
	private Mat4 horizWallMatrix(boolean left) {
		float size = 16 / 3f;
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1, 16), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.translate((left ? -1 : 1) * size, 8, -16f * 0.5f), modelMatrix);
		return modelMatrix;
	}

	/**
	 * @param gl
	 */
	private void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		light.setDirection(xPosition, 0, zPosition);
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
		if (animation) {
			jumping();
		}
		lampRoot.draw(gl);
	}

	private void jumping() {
		double elapsedTime = getSeconds() - startTime, period = 2,
				multiplicand = Math.abs(Math.cos(elapsedTime * Math.PI / period));
		rotateBicepAngle = rotateBicepAngleStart * (float) multiplicand;
		rotateForearmAngle = rotateForearmAngleStart * (float) multiplicand;
		rotateHeadAngle = rotateHeadAngleStart * (float) multiplicand;
		light.setDirection(xPosition, (float) Math.sin(elapsedTime * Math.PI / period) / 12, zPosition);
		rotateBicep.setTransform(Mat4Transform.rotateAroundZ(rotateBicepAngle));
		rotateForearm.setTransform(Mat4Transform.rotateAroundZ(rotateForearmAngle));
		rotateHead.setTransform(Mat4Transform.rotateAroundZ(rotateHeadAngle));
		if (elapsedTime > (posing ? this.limit : period)) {
			animation = false;
			startTime = getSeconds() - savedTime;
			if (!posing) {
				if (reset) {
					resetPosition();
				} else {
					jump();
				}
			}
		}
		lampRoot.update();
	}

	private double startTime;

	private double getSeconds() {
		return System.currentTimeMillis() / 1000.0;
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
			case KeyEvent.VK_LEFT:
				m = Camera.Movement.LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				m = Camera.Movement.RIGHT;
				break;
			case KeyEvent.VK_UP:
				m = Camera.Movement.UP;
				break;
			case KeyEvent.VK_DOWN:
				m = Camera.Movement.DOWN;
				break;
			case KeyEvent.VK_A:
				m = Camera.Movement.FORWARD;
				break;
			case KeyEvent.VK_Z:
				m = Camera.Movement.BACK;
				break;
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
	 * @param e instance of MouseEvent
	 */
	public void mouseDragged(MouseEvent e) {
		Point ms = e.getPoint();
		float sensitivity = 0.001f;
		float dx = (float) (ms.x - lastpoint.x) * sensitivity;
		float dy = (float) (ms.y - lastpoint.y) * sensitivity;
		//System.out.println("dy,dy: "+dx+","+dy);
		if (e.getModifiers() == MouseEvent.BUTTON1_MASK)
			camera.updateYawPitch(dx, -dy);
		lastpoint = ms;
	}

	/**
	 * mouse is used to control camera position
	 *
	 * @param e instance of MouseEvent
	 */
	public void mouseMoved(MouseEvent e) {
		lastpoint = e.getPoint();
	}
}
