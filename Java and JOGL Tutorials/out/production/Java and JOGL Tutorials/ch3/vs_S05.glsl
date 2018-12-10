#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 color;
out vec3 aColor;
//ch 3.3 Exercise 2
uniform float offset;

void main() {
  //gl_Position = vec4(position.x, position.y, position.z, 1.0);
  //ch 3.3 Exercise 1,2
  gl_Position = vec4(position.x+offset, -position.y, position.z, 1.0);

  aColor = color;
}
