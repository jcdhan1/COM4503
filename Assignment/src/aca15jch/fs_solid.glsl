#version 330 core
//For shapes that are one solid color. Supports directional lighting.
in vec3 aPos;
in vec3 aNormal;

out vec4 fragColor;

uniform vec3 viewPos;

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
    float cutOff = cos(radians(12.5f));
	// ambient
	vec3 ambient = material.ambient;

	// diffuse
	vec3 norm = normalize(aNormal);
	vec3 diffuse = material.diffuse;

	// specular
	vec3 viewDir = normalize(viewPos - aPos);
	vec3 reflectDir = reflect(-lightDir, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
	vec3 specular = (spec * material.specular);

	float theta = dot(lightDir, normalize(-light.direction));
	if(theta > cutOff) {
		float diff = max(dot(norm, lightDir), 0.0);
    	diffuse = light.diffuse * diff * diffuse;
    	ambient = light.ambient * ambient;
    	specular = light.specular * specular;
    } else {
    	diffuse = vec3(0.375) * diffuse;
		ambient = vec3(0.375) * ambient;
		specular = vec3(0.375) * specular;
    }

	vec3 result = ambient + diffuse + specular;
	fragColor = vec4(result, 1.0);
}
