#version 330 core
//Directional lighting as opposed to positional lighting.
layout (location = 0) in vec3 position;

uniform mat4 mvpMatrix;

void main() {
	gl_Position = mvpMatrix * vec4(position, 0); //For directional lights, the w component is 0.
}
