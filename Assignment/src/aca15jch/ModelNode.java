package aca15jch;

import com.jogamp.opengl.*;
/**
 * Code reused from ModelNode from Chapter 7
 */
public class ModelNode extends SGNode {

	protected Model model;

	public ModelNode(String name, Model m) {
		super(name);
		model = m;
	}

	public void draw(GL3 gl) {
		model.render(gl, worldTransform);
		for (int i = 0; i < children.size(); i++) {
			children.get(i).draw(gl);
		}
	}

}
