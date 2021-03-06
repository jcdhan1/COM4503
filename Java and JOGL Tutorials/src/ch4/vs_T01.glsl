#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 color;
layout (location = 2) in vec2 texCoord;

out vec3 aColor;
out vec2 aTexCoord;

void main() {
  gl_Position = vec4(position.x, position.y, position.z, 1.0);
  aColor = color;
  aTexCoord = texCoord;
  aTexCoord = texCoord + vec2(0.5f, -0.3f); //ch 4.1.3 Exercise 4
}