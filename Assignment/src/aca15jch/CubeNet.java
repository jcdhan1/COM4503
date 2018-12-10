package aca15jch;

/**
 * Subclass of Cube where each face is a different. Code resued from chapter 5
 */
public class CubeNet extends Cube {
	public static final float[] vertices = new float[]{  // x,y,z, colour, s,t
			-0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.67f,  // 0
			-0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.33f, 0.67f,  // 1
			-0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // 2
			-0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.33f, 1.0f,  // 3
			0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.33f,  // 4
			0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.67f, 0.33f,  // 5
			0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.67f,  // 6
			0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.67f, 0.67f,  // 7

			-0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.67f, 0.33f,  // 8+
			-0.5f, -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.33f, 0.67f,  // 9-
			-0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.67f, 0.67f,  // 10+
			-0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.33f, 1.0f,  // 11-
			0.5f, -0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.33f, 0.33f,  // 12+
			0.5f, -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.67f, 0.67f,  // 13-
			0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.33f, 0.67f,  // 14+
			0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.67f, 1.0f,  // 15-

			-0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.67f, 0.67f,  // 16-
			-0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.67f, 1.0f,  // 17-
			-0.5f, 0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.67f,  // 18+
			-0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.33f,  // 19+
			0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.67f,  // 20-
			0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,  // 21-
			0.5f, 0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.33f, 0.67f,  // 22+
			0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.33f, 0.33f   // 23+
	};

	public static int[]  indices = new int[]{
			0, 1, 3, // x -ve
			3, 2, 0, // x -ve
			4, 6, 7, // x +ve
			7, 5, 4, // x +ve
			9, 13, 15, // z +ve
			15, 11, 9, // z +ve
			8, 10, 14, // z -ve
			14, 12, 8, // z -ve
			16, 20, 21, // y -ve
			21, 17, 16, // y -ve
			23, 22, 18, // y +ve
			18, 19, 23  // y +ve
	};
}
