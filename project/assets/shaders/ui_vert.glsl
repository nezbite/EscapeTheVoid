#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 color;

uniform mat4 model_matrix;
uniform float aspect_ratio = 16.0/9.0;

out vec3 col;

void main(){
    mat4 proj_matrix = mat4(
        vec4(1/aspect_ratio, 0.0, 0.0, 0.0),
        vec4(0.0, 1.0, 0.0, 0.0),
        vec4(0.0, 0.0, 1.0, 0.0),
        vec4(0.0, 0.0, 0.0, 1.0)
    );
    gl_Position = proj_matrix * model_matrix * vec4(position, 1.0);
    col = color;
}