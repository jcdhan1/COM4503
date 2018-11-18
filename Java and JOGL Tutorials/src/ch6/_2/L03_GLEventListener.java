package ch6._2;

import ch6._2.gmaths.*;

import java.nio.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

public class L03_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;
	private Camera camera;

	/* The constructor is not used to initialise anything */
	public L03_GLEventListener(Camera camera) {
		this.camera = camera;
		this.camera.setPosition(new Vec3(6f, 9f, 17f));
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
		cube.dispose(gl);
		tt1.dispose(gl);
		light.dispose(gl);
	}

	// ***************************************************
	/* THE SCENE
	 * Now define all the methods to handle the scene.
	 * This will be added to in later examples.
	 */

	private Model cube, tt1;
	private Light light;

	public void initialise(GL3 gl) {
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);

		light = new Light(gl);
		light.setCamera(camera);
		//ch 6.2 Exercise 1
		//Big cube
		Mesh m = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
		Shader shader = new Shader(gl, user_dir + "\\src\\ch6\\_2\\vs_tt_03.glsl", user_dir + "\\src\\ch6\\_2\\fs_tt_03.glsl");
		Material material = new Material(new Vec3(1f, 1f, 1f), new Vec3(0.5f, 0.5f, 0.5f), new Vec3(1f, 1f, 1f), 1.0f);

		tt1 = new Model(gl, camera, light, shader, material, new Mat4(1), m);

		//Small cube
		m = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\ch6\\_2\\vs_cube_03.glsl", user_dir + "\\src\\ch6\\_2\\fs_cube_03.glsl");
		//material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
		cube = new Model(gl, camera, light, shader, material, new Mat4(1), m);
	}

	// Transforms may be altered each frame for objects so they are set in the render method.
	// If the transforms do not change each frame, then the model matrix could be set in initialise() and then only retrieved here,
	// although if the same object is being used in multiple positions, then
	// the transforms would need updating for each use of the object.
	// For more efficiency, if the object is static, its vertices could be defined once in the correct world positions.

	public void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		updateLightColour(); //ch 6.2 Exercise 2 The light changes hue as it moves around
		light.setPosition(getLightPosition());  // changing light position each frame
		light.render(gl);

		cube.setModelMatrix(getMforCube());     // change transform
		cube.render(gl);
		tt1.setModelMatrix(getMforTT1());       // change transform
		tt1.render(gl);
		tt1.setModelMatrix(getMforTT2());       // change transform
		tt1.render(gl);
		tt1.setModelMatrix(getMforTT3());       // change transform
		tt1.render(gl);
	}

	// Method to alter light colour over time

	private void updateLightColour() {
		double elapsedTime = getSeconds() - startTime;
		Vec3 lightColour = new Vec3();
		lightColour.x = (float) Math.sin(elapsedTime * 2.0f);
		lightColour.y = (float) Math.sin(elapsedTime * 0.7f);
		lightColour.z = (float) Math.sin(elapsedTime * 1.3f);
		Material m = light.getMaterial();
		m.setDiffuse(Vec3.multiply(lightColour, 0.5f));
		m.setAmbient(Vec3.multiply(m.getDiffuse(), 0.2f));
		light.setMaterial(m);
	}

	// The light's postion is continually being changed, so needs to be calculated for each frame.
	private Vec3 getLightPosition() {
		double elapsedTime = getSeconds() - startTime;
		float x = 3.0f * (float) (Math.sin(Math.toRadians(elapsedTime * 50)));
		float y = 2.4f;
		float z = 3.0f * (float) (Math.cos(Math.toRadians(elapsedTime * 50)));
		return new Vec3(x, y, z);
	}

	// As the transforms do not change over time for this object, they could be stored once rather than continually being calculated
	private Mat4 getMforCube() {
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.translate(0f, 0.5f, 0f), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(2f, 2f, 2f), modelMatrix);
		return modelMatrix;
	}

	// As the transforms do not change over time for this object, they could be stored once rather than continually being calculated
	private Mat4 getMforTT1() {
		float size = 16f;
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
		return modelMatrix;
	}

	// As the transforms do not change over time for this object, they could be stored once rather than continually being calculated
	private Mat4 getMforTT2() {
		float size = 16f;
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.translate(0, size * 0.5f, -size * 0.5f), modelMatrix);
		return modelMatrix;
	}

	// As the transforms do not change over time for this object, they could be stored once rather than continually being calculated
	private Mat4 getMforTT3() {
		float size = 16f;
		Mat4 modelMatrix = new Mat4(1);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(size, 1f, size), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(90), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(-90), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.translate(-size * 0.5f, size * 0.5f, 0), modelMatrix);
		return modelMatrix;
	}

	// ***************************************************
	/* TIME
	 */

	private double startTime;

	private double getSeconds() {
		return System.currentTimeMillis() / 1000.0;
	}
}
