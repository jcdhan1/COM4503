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
uniform bool blinn;


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
  float spec = 0.0;
  if(blinn) {
      vec3 halfwayDir = normalize(lightDir + viewDir);
      spec = pow(max(dot(norm, halfwayDir), 0.0), 16.0);
  } else {
      vec3 reflectDir = reflect(-lightDir, norm);
      spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0);
  }
  vec3 specular = specularStrength * spec * lightColor;

  vec3 result = (ambient + diffuse + specular) * objectColor;
  fragColor = vec4(result, 1.0);
}