#version 330 core

in vec3 aColor;
in vec2 aTexCoord;

out vec4 fragColor;

uniform sampler2D first_texture;
uniform sampler2D second_texture;
uniform float mixf;

void main() {
	//ch 4.2 Exercise 1
	//fragColor = vec4(aColor, 1.0f);
	//fragColor = vec4(texture(first_texture, aTexCoord).rgb, 1.0f);
	//fragColor = vec4(texture(first_texture, aTexCoord).rgb * aColor, 1.0f);

    //ch 4.2 Exercise 2:  multiply the float literals by mixf
	fragColor = vec4(texture(second_texture, aTexCoord).rgb * aColor, mixf);
	fragColor = vec4(mix(texture(first_texture, aTexCoord), texture(second_texture, aTexCoord), 0.3*mixf).rgb* aColor, mixf);
}
