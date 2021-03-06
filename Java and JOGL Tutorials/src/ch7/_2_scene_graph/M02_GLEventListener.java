package ch7._2_scene_graph;

import ch7._2_scene_graph.gmaths.*;

import java.nio.*;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

public class M02_GLEventListener implements GLEventListener {

	private static final boolean DISPLAY_SHADERS = false;

	public M02_GLEventListener(Camera camera) {
		this.camera = camera;
		this.camera.setPosition(new Vec3(4f, 8f, 18f));
		this.camera.setTarget(new Vec3(0f, 2f, 0f));
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
		cube.dispose(gl);
	}

	// ***************************************************
	/* THE SCENE
	 * Now define all the methods to handle the scene.
	 * This will be added to in later examples.
	 */

	private Camera camera;
	private Mat4 perspective;
	private Model floor, cube;
	private Light light;
	private SGNode twoBranchRoot;

	private void initialise(GL3 gl) {
		String user_dir = System.getProperty("user.dir");
		System.out.println("Working Directory = " + user_dir);
		createRandomNumbers();
		int[] textureId0 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\chequerboard.jpg");
		int[] textureId1 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\container2.jpg");
		int[] textureId2 = TextureLibrary.loadTexture(gl,
				user_dir + "\\src\\ch7\\_2_scene_graph\\textures\\container2_specular.jpg");

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

		//***CUBE***
		Vec3 transVec = new Vec3(0, 0.5f, 0);//ch 7.2 Exercise 1
		mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
		shader = new Shader(gl, user_dir + "\\src\\ch7\\_2_scene_graph\\vs_cube_04.glsl",
				user_dir + "\\src\\ch7\\_2_scene_graph\\fs_cube_04.glsl");
		material = new Material(new Vec3(1.0f, 0.5f, 0.31f),
				new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
		modelMatrix = Mat4.multiply(Mat4Transform.scale(4, 4, 4),
				Mat4Transform.translate(transVec));
		/* Only one cube is needed. The same cube can be used to draw each part of the two-branch structure,
			but with different model transformations used in each case, as we shall see.
			Note: the modelMatrix set up when initialising the cube is not used when drawing an object as part of a
			scene graph. Thus, in this example the modelMatrix could be set to the identity matrix, new Mat4(1).
		 */
		cube = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId1, textureId2);


		twoBranchRoot = new NameNode("two-branch structure");
		NameNode lowerBranch = new NameNode("lower branch");
		float lowerBranchHeight = 4.0f;//ch 7.2 Exercise 1
		Mat4 m = Mat4Transform.scale(2, lowerBranchHeight, 2);
		m = Mat4.multiply(m, Mat4Transform.translate(transVec));
		TransformNode lowerBranchTransform = new TransformNode("scale(2," + lowerBranchHeight + ",2);" + transVec.toString(), m);
		ModelNode lowerBranchShape = new ModelNode("Cube(0)", cube);

		TransformNode translateToTop = new TransformNode("translate(0," + lowerBranchHeight + ",0)",
				Mat4Transform.translate(0, lowerBranchHeight, 0));
		NameNode upperBranch = new NameNode("upper branch");
		float upperBranchHeight = 3.9f;//ch 7.2 Exercise 2
		m = Mat4Transform.scale(1.4f, upperBranchHeight, 1.4f);
		m = Mat4.multiply(m, Mat4Transform.translate(transVec));
		TransformNode upperBranchTransform = new TransformNode("scale(1.4f," + upperBranchHeight + ",1.4f);" + transVec.toString(), m);
		ModelNode upperBranchShape = new ModelNode("Cube(1)", cube);
		//ch 7.2 Exercise 2
		TransformNode translateHigher = new TransformNode("translate(0," + (upperBranchHeight) + ",0)",
				Mat4Transform.translate(0, upperBranchHeight, 0));
		NameNode higherBranch = new NameNode("upper branch");
		m = Mat4Transform.scale(1, 2, 1);
		m = Mat4.multiply(m, Mat4Transform.translate(transVec));
		TransformNode higherBranchTransform = new TransformNode("scale(1,2,1);" + transVec.toString(), m);
		ModelNode higherBranchShape = new ModelNode("Cube(2)", cube);

		twoBranchRoot.addChild(lowerBranch);
			lowerBranch.addChild(lowerBranchTransform);
				lowerBranchTransform.addChild(lowerBranchShape);
			lowerBranch.addChild(translateToTop);
				translateToTop.addChild(upperBranch);
					upperBranch.addChild(upperBranchTransform);
						upperBranchTransform.addChild(upperBranchShape);
					upperBranch.addChild(translateHigher);
						translateHigher.addChild(higherBranch);
							higherBranch.addChild(higherBranchTransform);
								higherBranchTransform.addChild(higherBranchShape);
		twoBranchRoot.update();  // IMPORTANT – must be done every time any part of the scene graph changes
		// Following two lines can be used to check scene graph construction is correct
		//twoBranchRoot.print(0, false);
		//System.exit(0);
	}

	private void render(GL3 gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		light.setPosition(getLightPosition());  // changing light position each frame
		light.render(gl);
		floor.render(gl);
		twoBranchRoot.draw(gl);
	}

	// The light's position is continually being changed, so needs to be calculated for each frame.
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
