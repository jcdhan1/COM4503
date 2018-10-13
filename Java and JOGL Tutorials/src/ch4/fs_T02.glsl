#version 330 core

in vec3 aColor;
in vec2 aTexCoord;

out vec4 fragColor;

uniform sampler2D first_texture;
uniform sampler2D second_texture;
uniform sampler2D third_texture;//ch 4.2 Exercise 3
uniform float mixf;

void main() {
	/*ch 4.2 Exercise 1
	fragColor = vec4(aColor, 1.0f);//solid color
	fragColor = vec4(texture(first_texture, aTexCoord).rgb, 1.0f);//texture
	fragColor = vec4(texture(first_texture, aTexCoord).rgb * aColor, 1.0f);//texture with red, blue and green at corners of triangle
	*/

    //ch 4.2 Exercise 2:  multiply the float literals by mixf
    //ch 4.2 Exercise 3: third texture
	fragColor = vec4(
		mix(
    		mix(
    			texture(first_texture, aTexCoord),
    			texture(second_texture, aTexCoord),
    			mixf),
    		texture(third_texture, aTexCoord),
    		0.5*mixf).rgb* aColor,
		1.0f
	);
}
