#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 color;
layout (location = 2) in vec2 texCoord;

out vec3 aColor;
out vec2 aTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 mvpMatrix;

void main() {
	//mat4 mvpMatrix2 = projection * view * model;
	//gl_Position = mvpMatrix2 * vec4(position, 1.0);

	gl_Position = mvpMatrix * vec4(position, 1.0);

	aColor = color;
	aTexCoord = texCoord;
}
