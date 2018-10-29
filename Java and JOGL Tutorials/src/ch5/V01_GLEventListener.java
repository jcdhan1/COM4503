package ch5;

import ch5.gmaths.*;

import java.nio.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.texture.spi.JPEGImage;

public class V01_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;
	private Shader shader;
	private float aspect;

	/* The constructor is not used to initialise anything */
	public V01_GLEventListener() {
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
		//gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
		//gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
		//gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
		initialise(gl);
		startTime = getSeconds();
	}

	/* Called to indicate the drawing surface has been moved and/or resized  */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glViewport(x, y, width, height);
		aspect = (float) width / (float) height;
	}

	/* Draw */
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		render(gl);
	}

	/* Clean up memory, if necessary */
	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glDeleteBuffers(1, vertexBufferId, 0);
		gl.glDeleteVertexArrays(1, vertexArrayId, 0);
		gl.glDeleteBuffers(1, elementBufferId, 0);
		gl.glDeleteBuffers(1, textureId1, 0);
	}

	// ***************************************************
	/* THE SCENE
	 * Now define all the methods to handle the scene.
	 * This will be added to in later examples.
	 */

	// texture id
	private int[] textureId1 = new int[1];

	public void initialise(GL3 gl) {
		//shader = new Shader(gl, "vs_V01.glsl", "fs_V01.glsl");
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);
		shader = new Shader(gl, user_dir + "\\src\\ch5\\vs_V01.glsl", user_dir + "\\src\\ch5\\fs_V01.glsl");




		fillBuffers(gl);
		textureId1 = TextureLibrary.loadTexture(gl, user_dir + "\\src\\ch4\\wattBook.jpg");
	}

	public void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		double elapsedTime = getSeconds() - startTime;

		Mat4 projectionMatrix = Mat4Transform.perspective(45, aspect);

		//float zposition = 2f;
		float zposition = 2f+(float)(Math.sin(Math.toRadians(elapsedTime*50)));//ch 5.2.1 Exercise 1: This line oscillates z-coordinate of all objects in the scene
		Vec3 position = new Vec3(0, 0, zposition);
		Mat4 viewMatrix = Mat4Transform.lookAt(position, new Vec3(0, 0, 0), new Vec3(0, 1, 0));

		//float angle = -55f;
		float angle = (float)(-115*Math.sin(Math.toRadians(elapsedTime*50)));//ch 5.2.1 Exercise 1: This line rotates all objects in the scene around the x-axis.
		Mat4 modelMatrix = Mat4Transform.rotateAroundX(angle);

		//ch 5.2.1 Exercise 2
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundY(angle), modelMatrix);
		modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundZ(angle), modelMatrix);

		Mat4 mvpMatrix = Mat4.multiply(viewMatrix, modelMatrix);
		mvpMatrix = Mat4.multiply(projectionMatrix, mvpMatrix);

		shader.use(gl);
		shader.setFloatArray(gl, "model", modelMatrix.toFloatArrayForGLSL());
		shader.setFloatArray(gl, "view", viewMatrix.toFloatArrayForGLSL());
		shader.setFloatArray(gl, "projection", projectionMatrix.toFloatArrayForGLSL());
		shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureId1[0]);

		gl.glBindVertexArray(vertexArrayId[0]);
		gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
		gl.glBindVertexArray(0);
	}

	// ***************************************************
	/* TIME
	 */

	private double startTime;

	private double getSeconds() {
		return System.currentTimeMillis() / 1000.0;
	}

	// ***************************************************
	/* THE DATA
	 */
	// anticlockwise/counterclockwise ordering
	private float[] vertices = {      // position, colour, tex coords
			-0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // top left
			-0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,  // bottom left
			0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,  // bottom right
			0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f   // top right
	};

	private int vertexStride = 8;
	private int vertexXYZFloats = 3;
	private int vertexColourFloats = 3;
	private int vertexTexFloats = 2;

	private int[] indices = {         // Note that we start from 0
			0, 1, 2,
			0, 2, 3
	};

	// ***************************************************
	/* THE BUFFERS
	 */

	private int[] vertexBufferId = new int[1];
	private int[] vertexArrayId = new int[1];
	private int[] elementBufferId = new int[1];

	private void fillBuffers(GL3 gl) {
		gl.glGenVertexArrays(1, vertexArrayId, 0);
		gl.glBindVertexArray(vertexArrayId[0]);
		gl.glGenBuffers(1, vertexBufferId, 0);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);
		FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);

		gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);

		int stride = vertexStride;
		int numXYZFloats = vertexXYZFloats;
		int offset = 0;
		gl.glVertexAttribPointer(0, numXYZFloats, GL.GL_FLOAT, false, stride * Float.BYTES, offset);
		gl.glEnableVertexAttribArray(0);

		int numColorFloats = vertexColourFloats; // red, green and blue values for each colour
		offset = numXYZFloats * Float.BYTES;  // the colour values are three floats after the three x,y,z values
		// so change the offset value
		gl.glVertexAttribPointer(1, numColorFloats, GL.GL_FLOAT, false, stride * Float.BYTES, offset);
		// the vertex shader uses location 1 (sometimes called index 1)
		// for the colour information
		// location, size, type, normalize, stride, offset
		// offset is relative to the start of the array of data
		gl.glEnableVertexAttribArray(1);// Enable the vertex attribute array at location 1

		// now do the texture coordinates  in vertex attribute 2
		int numTexFloats = vertexTexFloats;
		offset = (numXYZFloats + numColorFloats) * Float.BYTES;
		gl.glVertexAttribPointer(2, numTexFloats, GL.GL_FLOAT, false, stride * Float.BYTES, offset);
		gl.glEnableVertexAttribArray(2);

		gl.glGenBuffers(1, elementBufferId, 0);
		IntBuffer ib = Buffers.newDirectIntBuffer(indices);
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);
		gl.glBindVertexArray(0);
	}

}