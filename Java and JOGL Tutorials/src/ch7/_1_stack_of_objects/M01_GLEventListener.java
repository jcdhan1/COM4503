package ch7._1_stack_of_objects;

import ch7._1_stack_of_objects.gmaths.*;

import com.jogamp.opengl.*;

public class M01_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;

	public M01_GLEventListener(Camera camera) {
		this.camera = camera;
		this.camera.setPosition(new Vec3(4f, 6f, 15f));
		this.camera.setTarget(new Vec3(0f, 5f, 0f));
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
		disposeModels(gl);
	}

	// ***************************************************
	/* THE SCENE
	 * Now define all the methods to handle the scene.
	 * This will be added to in later examples.
	 */

	private Camera camera;
	private Model tt1, skybox, sphere, sphere2;//ch 7.1 Exercise 1
	private Light light;

	private void disposeModels(GL3 gl) {
		tt1.dispose(gl);
		skybox.dispose(gl);
		sphere.dispose(gl);
		sphere2.dispose(gl);//ch 7.1 Exercise 1
		light.dispose(gl);
	}

	public void initialise(GL3 gl) {
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);
		createRandomNumbers();
		int[] textureId0 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\textures\\chequerboard.jpg");
		int[] textureId1 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\textures\\container2.jpg");
		int[] textureId2 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\textures\\container2_specular.jpg");
		int[] textureId3 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\textures\\jade.jpg");
		int[] textureId4 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\textures\\jade_specular.jpg");
		int[] textureId5 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\textures\\ear0xuu2.jpg");
		int[] textureId6 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\textures\\ear0xuu2_specular.jpg");

		light = new Light(gl);
		light.setCamera(camera);

		Mesh m = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
		Shader shader = new Shader(gl, user_dir + "\\src\\ch7\\_1_stack_of_objects\\vs_tt_05.glsl",
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\fs_tt_05.glsl");
		Material material = new Material(new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.0f, 0.5f, 0.81f),
				new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
		Mat4 modelMatrix = Mat4Transform.scale(16, 1f, 16);
		tt1 = new Model(gl, camera, light, shader, material, modelMatrix, m, textureId0);

		m = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\ch7\\_1_stack_of_objects\\vs_cube_04.glsl",
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\fs_cube_04.glsl");
		material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f),
				new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(4, 4, 4),
				Mat4Transform.translate(0, 0.5f, 0));
		skybox = new Model(gl, camera, light, shader, material, modelMatrix, m, textureId1, textureId2);

		m = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\ch7\\_1_stack_of_objects\\vs_sphere_04.glsl",
				user_dir + "\\src\\ch7\\_1_stack_of_objects\\fs_sphere_04.glsl");

		// no texture version
		// shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04_notex.txt");

		material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f),
				new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(3, 3, 3),
				Mat4Transform.translate(0, 0.5f, 0));
		modelMatrix = Mat4.multiply(Mat4Transform.translate(0, 4, 0), modelMatrix);

		sphere = new Model(gl, camera, light, shader, material, modelMatrix, m, textureId3, textureId4);

		//ch 7.1 Exercise 1, change Sphere to Cube for Exercise 2
		sphere2 = new Model(gl, camera, light, new Shader(gl,
			user_dir + "\\src\\ch7\\_1_stack_of_objects\\vs_sphere_04.glsl",
			user_dir + "\\src\\ch7\\_1_stack_of_objects\\fs_sphere_04.glsl"), material,
			Mat4.multiply(Mat4Transform.translate(0, 3, 0), modelMatrix),
			new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone()),
			textureId5, textureId6); //ch 7.1 Exercise 3;

		// no texture version
		// sphere = new Model(gl, camera, light, shader, material, modelMatrix, m);
	}

	private void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		light.setPosition(getLightPosition());  // changing light position each frame
		light.render(gl);

		tt1.render(gl);
		skybox.render(gl);
		sphere.render(gl);
		sphere2.render(gl);
	}

	// The light's postion is continually being changed, so needs to be calculated for each frame.
	private Vec3 getLightPosition() {
		double elapsedTime = getSeconds() - startTime;
		float x = 5.0f * (float) (Math.sin(Math.toRadians(elapsedTime * 50)));
		float y = 2.7f;
		float z = 5.0f * (float) (Math.cos(Math.toRadians(elapsedTime * 50)));
		return new Vec3(x, y, z);

		//return new Vec3(5f,3.4f,5f);  // use to set in a specific position for testing
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