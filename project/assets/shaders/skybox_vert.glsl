#version 330 core

layout(location = 0) in vec3 aPos;

uniform mat4 projection;
uniform mat4 view;

out vec3 TexCoords;

void main()
{
    TexCoords = aPos;  // Using the vertex position for texture coordinates

    // Remove translation from view matrix
    mat4 viewNoTranslation = mat4(mat3(view)); // This effectively removes the translation from the view matrix

    // Scale the position
    float scale = 100.0; // Adjust this value to scale the skybox to the desired size
    gl_Position = projection * viewNoTranslation * vec4(aPos * scale, 1.0);
}
