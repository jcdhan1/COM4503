#version 330 core

in vec3 aPos;
in vec3 aNormal;

out vec4 fragColor;

uniform vec3 objectColor;
uniform vec3 lightColor;
uniform vec3 lightPos;
uniform vec3 viewPos;

//ch 6.1.4
uniform float ambientStrength;
uniform float diffuseStrength;
uniform float specularStrength;


void main() {
  // ambient
  //float ambientStrength = 0.1;
  vec3 ambient = ambientStrength * lightColor;
  
  // diffuse
  //float diffuseStrength = 0.9;
  vec3 norm = normalize(aNormal);
  vec3 lightDir = normalize(lightPos - aPos);  
  float diff = max(dot(norm, lightDir), 0.0);
  vec3 diffuse = diffuseStrength * diff * lightColor;
  
  // specular
  //float specularStrength = 0.5;
  vec3 viewDir = normalize(viewPos - aPos);
  vec3 reflectDir = reflect(-lightDir, norm);  
  float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
  vec3 specular = specularStrength * spec * lightColor;  

  vec3 result = (ambient + diffuse + specular) * objectColor;
  fragColor = vec4(result, 1.0);
}