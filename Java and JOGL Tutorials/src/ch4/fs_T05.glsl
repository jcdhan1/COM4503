#version 330 core

in vec3 aColor;
in vec2 aTexCoord;

out vec4 fragColor;

uniform sampler2D first_texture;

void main() {
	// fragColor = vec4(aColor, 1.0f);
	fragColor = vec4(texture(first_texture, aTexCoord).rgb, 1.0f);
	// fragColor = vec4(texture(first_texture, aTexCoord).rgb * aColor, 1.0f);
}
