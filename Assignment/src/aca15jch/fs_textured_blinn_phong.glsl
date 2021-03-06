#version 330 core
//For textured shapes. Supports directional lighting.
in vec3 aPos;
in vec3 aNormal;
in vec2 aTexCoord;

out vec4 fragColor;
 
uniform vec3 viewPos;
uniform sampler2D first_texture;
uniform sampler2D second_texture;

struct Light {
	vec3 position;
	vec3 ambient;
	vec3 diffuse;
	vec3 specular;
	vec3 direction;
};

uniform Light light;  

struct Material {
	vec3 ambient;
	vec3 diffuse;
	vec3 specular;
	float shininess;
}; 
  
uniform Material material;

void main() {
	vec3 lightDir = normalize(light.position - aPos);
    float cutOff =  cos(radians(12.5f));
	// ambient
	vec3 ambient = vec3(texture(first_texture, aTexCoord));

	// diffuse
	vec3 norm = normalize(aNormal);
	vec3 diffuse = material.diffuse * vec3(texture(first_texture, aTexCoord));

	// specular
	vec3 viewDir = normalize(viewPos - aPos);
	vec3 reflectDir = reflect(-lightDir, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
	vec3 specular = spec * vec3(texture(second_texture, aTexCoord));

	float theta = dot(lightDir, normalize(-light.direction));
	if(theta > cutOff) {
		float diff = max(dot(norm, lightDir), 0.0);
    	diffuse = light.diffuse * diff * diffuse;
    	ambient = light.ambient * ambient;
    	specular = light.specular * specular;
    } else {
    	diffuse = vec3(0.4) * diffuse;
		ambient = vec3(0.4) * ambient;
		specular = vec3(0.4) * specular;
    }

	vec3 result = ambient + diffuse + specular;
	fragColor = vec4(result, 1.0);
}
