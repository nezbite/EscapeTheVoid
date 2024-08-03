#version 330 core

out vec4 FragColor;

in vec3 texCoords;

uniform samplerCube skybox; // Cube Map-Textur

void main() {
    FragColor = texture(skybox, texCoords); // Textur aus der Cube Map abrufen
}

