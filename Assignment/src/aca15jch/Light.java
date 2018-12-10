package aca15jch;

import aca15jch.gmaths.*;

import java.nio.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

/**
 * @author aca15jch
 * Reused code from Dr. Maddock's Light class. New features I've added include directional lighting.
 */
public class Light {

	private Material material;
	private Vec3 position, direction;
	private Mat4 model;
	private Shader shader;
	private Camera camera;

	/**
	 * Constructor
	 * @param gl
	 */
	public Light(GL3 gl) {
		material = new Material();
		material.setAmbient(1, 1, 1);
		material.setDiffuse(1, 1, 1);
		material.setSpecular(1, 1, 1);
		position = new Vec3(0, 0, 0);
		direction = new Vec3(1,0,0);
		model = new Mat4(1);
		String user_dir = System.getProperty("user.dir");
		if (user_dir.endsWith("src")){
			user_dir = user_dir.substring(0,user_dir.length()-4);
		}
		System.out.println("Working Directory = " + user_dir);
		shader = new Shader(gl, user_dir + "\\src\\aca15jch\\vs_light_01.glsl",
								user_dir + "\\src\\aca15jch\\fs_light_01.glsl");
		fillBuffers(gl);
	}

	/**
	 * Set the position vector.
	 * @param v
	 */
	public void setPosition(Vec3 v) {
		position.x = v.x;
		position.y = v.y;
		position.z = v.z;
	}

	/**
	 * Set components of position vector.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}

	/**
	 * Get the position vector.
	 */
	public Vec3 getPosition() {
		return position;
	}

	/**
	 * Set the direction vector.
	 * @param v
	 */
	public void setDirection(Vec3 v) {
		position.x = v.x;
		position.y = v.y;
		position.z = v.z;
	}

	/**
	 * Set components of direction vector.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setDirection(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}

	/**
	 * Get the direction vector.
	 */
	public Vec3 getDirection() {
		return position;
	}

	/**
	 * Set the material.
	 * @param m
	 */
	public void setMaterial(Material m) {
		material = m;
	}

	/**
	 * Get the material.
	 */
	public Material getMaterial() {
		return material;
	}

	/**
	 * Set the camera.
	 * @param camera
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * Render
	 * @param gl
	 */
	public void render(GL3 gl) {
		Mat4 model = new Mat4(1);
		model = Mat4.multiply(Mat4Transform.scale(0.3f, 0.3f, 0.3f), model);
		model = Mat4.multiply(Mat4Transform.translate(position), model);

		Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), model));

		shader.use(gl);
		shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

		gl.glBindVertexArray(vertexArrayId[0]);
		gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
		gl.glBindVertexArray(0);
	}

	/**
	 * Dispose
	 * @param gl
	 */
	public void dispose(GL3 gl) {
		gl.glDeleteBuffers(1, vertexBufferId, 0);
		gl.glDeleteVertexArrays(1, vertexArrayId, 0);
		gl.glDeleteBuffers(1, elementBufferId, 0);
	}

	// ***************************************************
	/* THE DATA
	 */
	// anticlockwise/counterclockwise ordering

	private float[] vertices = new float[]{  // x,y,z
			-0.5f, -0.5f, -0.5f,  // 0
			-0.5f, -0.5f, 0.5f,  // 1
			-0.5f, 0.5f, -0.5f,  // 2
			-0.5f, 0.5f, 0.5f,  // 3
			0.5f, -0.5f, -0.5f,  // 4
			0.5f, -0.5f, 0.5f,  // 5
			0.5f, 0.5f, -0.5f,  // 6
			0.5f, 0.5f, 0.5f   // 7
	};

	private int[] indices = new int[]{
			0, 1, 3, // x -ve
			3, 2, 0, // x -ve
			4, 6, 7, // x +ve
			7, 5, 4, // x +ve
			1, 5, 7, // z +ve
			7, 3, 1, // z +ve
			6, 4, 0, // z -ve
			0, 2, 6, // z -ve
			0, 4, 5, // y -ve
			5, 1, 0, // y -ve
			2, 3, 7, // y +ve
			7, 6, 2  // y +ve
	};

	private int vertexStride = 3;
	private int vertexXYZFloats = 3;

	// ***************************************************
	/* THE LIGHT BUFFERS
	 */

	private int[] vertexBufferId = new int[1];
	private int[] vertexArrayId = new int[1];
	private int[] elementBufferId = new int[1];

	/**
	 * Fill buffers
	 * @param gl
	 */
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

		gl.glGenBuffers(1, elementBufferId, 0);
		IntBuffer ib = Buffers.newDirectIntBuffer(indices);
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);
		gl.glBindVertexArray(0);
	}
}
