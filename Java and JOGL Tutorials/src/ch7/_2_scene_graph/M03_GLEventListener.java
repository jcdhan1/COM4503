package ch7._2_scene_graph;

import ch7._2_scene_graph.gmaths.*;

import java.nio.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

public class M03_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;

	public M03_GLEventListener(Camera camera) {
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
		light.dispose(gl);
		floor.dispose(gl);
		sphere.dispose(gl);
	}


	// ***************************************************
	/* INTERACTION
	 *
	 *
	 */

	public void incXPosition() {
		xPosition += 0.5f;
		if (xPosition > 5f) xPosition = 5f;
		updateX();
	}

	public void decXPosition() {
		xPosition -= 0.5f;
		if (xPosition < -5f) xPosition = -5f;
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
	private Model floor, sphere;
	private Light light;
	private SGNode twoBranchRoot;

	private TransformNode translateX, rotateAll, rotateUpper, rotateHighest;
	private float xPosition = 0;
	private float rotateAllAngleStart = 25, rotateAllAngle = rotateAllAngleStart;
	private float rotateUpperAngleStart = -60, rotateUpperAngle = rotateUpperAngleStart;
	private float rotateHighestAngleStart = -90, rotateHighestAngle = rotateHighestAngleStart;

	private void initialise(GL3 gl) {
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);
		createRandomNumbers();
		int[] textureId0 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\chequerboard.jpg");
		int[] textureId1 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\jade.jpg");
		int[] textureId2 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\jade_specular.jpg");
		int[] textureId3 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\ear0xuu2.jpg");
		int[] textureId4 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\ear0xuu2_specular.jpg");

		light = new Light(gl);
		light.setCamera(camera);

		//***FLOOR***
		Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
		Shader shader = new Shader(gl, user_dir + "\\src\\ch7\\_2_scene_graph\\vs_tt_05.glsl",
				user_dir + "\\src\\ch7\\_2_scene_graph\\fs_tt_05.glsl");
		Material material = new Material(new Vec3(0.0f, 0.5f, 0.81f),
				new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
		Mat4 modelMatrix = Mat4Transform.scale(16, 1f, 16);
		floor = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId0);

		//***SPHERES***
		Vec3 sphereDim0 = new Vec3(4,4,4), transVec = new Vec3(0,0.5f,0);
		mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\ch7\\_2_scene_graph\\vs_cube_04.glsl",
				user_dir + "\\src\\ch7\\_2_scene_graph\\fs_cube_04.glsl");
		material = new Material(new Vec3(1.0f, 0.5f, 0.31f),
				new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(sphereDim0),
				Mat4Transform.translate(transVec));
		sphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId1, textureId2);

		twoBranchRoot = new NameNode("two-branch structure");
		translateX = new TransformNode("translate(" + xPosition + ",0,0)",
				Mat4Transform.translate(xPosition, 0, 0));
		rotateAll = new TransformNode("rotateAroundZ(" + rotateAllAngle + ")",
				Mat4Transform.rotateAroundZ(rotateAllAngle));
		NameNode lowerBranch = new NameNode("lower branch");
		Mat4 m = Mat4Transform.scale(2.5f, sphereDim0.y, 2.5f);
		m = Mat4.multiply(m, Mat4Transform.translate(transVec));
		TransformNode makeLowerBranch = new TransformNode("scale(2.5,"+sphereDim0.y+",2.5); translate"+transVec, m);
		ModelNode cube0Node = new ModelNode("Sphere(0)", sphere);
		TransformNode translateToTop = new TransformNode("translate(0,"+sphereDim0.y+",0)",
				Mat4Transform.translate(0, sphereDim0.y, 0));
		rotateUpper = new TransformNode("rotateAroundZ(" + rotateUpperAngle + ")",
				Mat4Transform.rotateAroundZ(rotateUpperAngle));
		NameNode upperBranch = new NameNode("upper branch");
		Vec3 sphereDim1 = new Vec3(1.4f, 3.1f, 1.4f);
		m = Mat4Transform.scale(sphereDim1);
		m = Mat4.multiply(m, Mat4Transform.translate(transVec));
		TransformNode makeUpperBranch = new TransformNode("scale"+sphereDim1+";translate"+transVec, m);
		ModelNode sphere1Node = new ModelNode("Sphere(1)", sphere);
		//ch 7.3 Exercise 1
		TransformNode translateHigher = new TransformNode("translate(0,"+sphereDim1.y+",0)",
				Mat4Transform.translate(0, sphereDim1.y, 0));
		rotateHighest = new TransformNode("rotateAroundZ(" + rotateHighestAngle + ")",
				Mat4Transform.rotateAroundZ(rotateHighestAngle));
		NameNode highestBranch = new NameNode("highest branch");
		Vec3 sphereDim2 = new Vec3(2, 2, 2);
		m = Mat4Transform.scale(sphereDim2);
		m = Mat4.multiply(m, Mat4Transform.translate(transVec));
		TransformNode makeHighestBranch = new TransformNode("scale"+sphereDim2+";translate"+transVec, m);
		ModelNode sphere2Node = new ModelNode("Sphere(2)",
				new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId3, textureId4));

		twoBranchRoot.addChild(translateX);
			translateX.addChild(rotateAll);
				rotateAll.addChild(lowerBranch);
					lowerBranch.addChild(makeLowerBranch);
						makeLowerBranch.addChild(cube0Node);
					lowerBranch.addChild(translateToTop);
						translateToTop.addChild(rotateUpper);
							rotateUpper.addChild(upperBranch);
								upperBranch.addChild(makeUpperBranch);
									makeUpperBranch.addChild(sphere1Node);
								upperBranch.addChild(translateHigher);
									translateHigher.addChild(highestBranch);
										highestBranch.addChild(makeHighestBranch);
											makeHighestBranch.addChild(sphere2Node);

		twoBranchRoot.update();  // IMPORTANT – must be done every time any part of the scene graph changes
		//twoBranchRoot.print(0, false);
		//System.exit(0);
	}

	private void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		light.setPosition(getLightPosition());  // changing light position each frame
		light.render(gl);
		floor.render(gl);
		updateBranches();
		twoBranchRoot.draw(gl);
	}

	private void updateBranches() {
		double elapsedTime = getSeconds() - startTime;
		rotateAllAngle = rotateAllAngleStart * (float) Math.sin(elapsedTime);
		rotateUpperAngle = rotateUpperAngleStart * (float) Math.sin(elapsedTime * 0.7f);
		rotateAll.setTransform(Mat4Transform.rotateAroundZ(rotateAllAngle));
		rotateUpper.setTransform(Mat4Transform.rotateAroundZ(rotateUpperAngle));
		twoBranchRoot.update(); // IMPORTANT – the scene graph has changed
	}

	// The light's postion is continually being changed, so needs to be calculated for each frame.
	private Vec3 getLightPosition() {
		double elapsedTime = getSeconds() - startTime;
		float x = 5.0f * (float) (Math.sin(Math.toRadians(elapsedTime * 50)));
		float y = 2.7f;
		float z = 5.0f * (float) (Math.cos(Math.toRadians(elapsedTime * 50)));
		return new Vec3(x, y, z);
		//return new Vec3(5f,3.4f,5f);
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
