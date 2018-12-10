#version 330 core

layout (location = 0) in vec3 positionDirection;

uniform mat4 mvpMatrix;

void main() {
	gl_Position = mvpMatrix * vec4(-normalize(positionDirection), 0);
	//gl_Position = mvpMatrix * vec4(positionDirection, 1);
}
