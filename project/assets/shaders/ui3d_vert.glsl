#version 330 core

// Eingabe-Attribute
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord; // Texturkoordinaten
layout(location = 2) in vec3 normal;

// Uniforms
uniform mat4 model_matrix;
uniform mat4 view_matrix;
uniform mat4 proj_matrix;
uniform vec2 tcMultiplier; // Texturkoordinaten-Multiplikator

const int MAX_LIGHTS = 4;

// Ausgabe-Datenstruktur
out struct VertexData
{
    vec3 color;
    vec3 normal;
    vec2 texCoord; // Texturkoordinaten
    vec3 fragPosition; // Fragmentsposition im Viewspace
    vec3 toCameraVector; // Vektor zum Kameraposition im Viewspace
    vec3 toPointLightVector[MAX_LIGHTS]; // Vektoren zu Point Lights im Viewspace
    vec3 toSpotLightVector[MAX_LIGHTS]; // Vektoren zu Spot Lights im Viewspace
    vec3 spotLightDirections[MAX_LIGHTS];
} vertexData;


void main() {
    // Umwandlung in homogene Koordinaten
    vec4 objectSpacePos = vec4(position, 1.0);
    // Berechnung der Weltkoordinaten durch Anwendung der Modellmatrix
    vec4 worldSpacePos = model_matrix * objectSpacePos;
    // Berechnung der Fragmentsposition im Viewspace
    vec4 viewSpacePos = view_matrix * worldSpacePos;
    vertexData.fragPosition = viewSpacePos.xyz / viewSpacePos.w;

    // Berechnung der Texturkoordinaten mit Multiplikator
    vertexData.texCoord = texCoord * tcMultiplier;

    // Berechnung der normalisierten Normalen im Viewspace
    mat4 modelview = view_matrix * model_matrix;
    vertexData.normal = (transpose(inverse(modelview)) * vec4(normal, 0.0)).xyz;

    // Vektor zum Kameraposition im Viewspace berechnen
    vertexData.toCameraVector = -viewSpacePos.xyz;

    // Setzen der Position für den Vertex
    gl_Position = proj_matrix * viewSpacePos;
}
