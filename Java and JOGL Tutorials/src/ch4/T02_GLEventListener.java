package ch4;

import java.nio.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.texture.spi.JPEGImage;

public class T02_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;
	private Shader shader;

	/* The constructor is not used to initialise anything */
	public T02_GLEventListener() {
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
		initialise(gl);
		startTime = getSeconds();
	}

	/* Called to indicate the drawing surface has been moved and/or resized  */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glViewport(x, y, width, height);
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
		gl.glDeleteBuffers(1, textureId2, 0);
	}

	// ***************************************************
	/* TIME
	 */

	private double startTime;

	private double getSeconds() {
		return System.currentTimeMillis() / 1000.0;
	}

	// ***************************************************
	/* THE SCENE
	 * Now define all the methods to handle the scene.
	 * This will be added to in later examples.
	 */

	// Now for some textures
	private int[] textureId1 = new int[1];
	private int[] textureId2 = new int[1];
	private int[] textureId3 = new int[1];//ch 4.2 Exercise 3

	public void initialise(GL3 gl) {
		//shader = new Shader(gl, "vs_T02.txt", "fs_T02.txt");
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);
		shader = new Shader(gl, user_dir + "\\src\\ch4\\vs_T02.glsl", user_dir + "\\src\\ch4\\fs_T02.glsl");

		fillBuffers(gl);
		textureId1 = TextureLibrary.loadTexture(gl, user_dir + "\\src\\ch4\\wattBook.jpg");
		textureId2 = TextureLibrary.loadTexture(gl, user_dir + "\\src\\ch4\\chequerboard.jpg");
		textureId3 = TextureLibrary.loadTexture(gl, user_dir + "\\src\\ch4\\cloud.jpg");//ch 4.2 Exercise 3
	}

	/**
	 * ch 4.2 Exercise 2
	 * oscillate given time, frequency and amplitude
	 * @param elapsedTime time parameter
	 * @param f frequency
	 * @param a amplitude (keep below 1 for color)
	 * @return
	 */
	public float oscillate(double elapsedTime, double f, double a) {
		return (float) (a*Math.sin(2*Math.PI*f*elapsedTime));
	}

	public void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		//ch 4.2 Exercise 2
		double elapsedTime = getSeconds() - startTime;

		int mixfLocation = gl.glGetUniformLocation(shader.getID(), "mixf");
		float mixf = oscillate(elapsedTime, 1,1);
		gl.glUniform1f(mixfLocation, mixf);

		gl.glBindVertexArray(vertexArrayId[0]);
		gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
		gl.glBindVertexArray(0);


		shader.use(gl);
		shader.setInt(gl, "first_texture", 0);
		shader.setInt(gl, "second_texture", 1);
		shader.setInt(gl, "third_texture", 2);//ch 4.2 Exercise 3
		// the following two lines are before the Shader.setInt() method was implemented
		gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "first_texture"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "second_texture"), 1);
		gl.glUniform1i(gl.glGetUniformLocation(shader.getID(), "third_texture"), 2);//ch 4.2 Exercise 3

		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureId1[0]);
		gl.glActiveTexture(GL.GL_TEXTURE1);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureId2[0]);
		//ch 4.2 Exercise 3
		gl.glActiveTexture(GL.GL_TEXTURE2);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureId3[0]);

		gl.glBindVertexArray(vertexArrayId[0]);
		gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
		gl.glBindVertexArray(0);

		// always good practice to set everything back to defaults once configured.
		gl.glActiveTexture(GL.GL_TEXTURE0);
	}

	// ***************************************************
	/* THE DATA
	 */

	private float[] vertices = {      // position, colour, tex coords
			0.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, 1.0f,
			0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
			-0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f
	};

	private int vertexStride = 8;
	private int vertexXYZFloats = 3;
	private int vertexColourFloats = 3;
	private int vertexTexFloats = 2;

	private int[] indices = {         // Note that we start from 0
			0, 1, 2
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

		// now do the texture coordinates
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
