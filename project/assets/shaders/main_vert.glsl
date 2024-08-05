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

// Ausgabe-Datenstruktur
out struct VertexData
{
    vec3 color;
    vec3 normal;
    vec2 texCoord; // Texturkoordinaten
    vec3 fragPosition; // Fragmentsposition im Viewspace
    vec3 toCameraVector; // Vektor zum Kameraposition im Viewspace
    vec3 toPointLightVector[10]; // Vektoren zu Point Lights im Viewspace
    vec3 toSpotLightVector[10]; // Vektoren zu Spot Lights im Viewspace
} vertexData;

// Anzahl der Point Lights und Spot Lights
uniform int numPointLights;
uniform int numSpotLights;

// Uniforms für Point Lights
uniform vec3 pointLightPositions[10]; // Positionen der Point Lights
uniform vec3 pointLightColors[10]; // Farben der Point Lights

// Uniforms für Spot Lights
uniform vec3 spotLightPositions[10]; // Positionen der Spot Lights
uniform vec3 spotLightDirections[10]; // Richtungen der Spot Lights

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

    // Vektor zum Point Light im Viewspace berechnen
    for (int i = 0; i < numPointLights; i++) {
        vertexData.toPointLightVector[i] = (view_matrix * vec4(pointLightPositions[i], 1.0)).xyz - vertexData.fragPosition;
    }

    // Vektor zum Spot Light im Viewspace berechnen
    for (int j = 0; j < numSpotLights; j++) {
        vertexData.toSpotLightVector[j] = (view_matrix * vec4(spotLightPositions[j], 1.0)).xyz - vertexData.fragPosition;
    }

    // Vektor zum Kameraposition im Viewspace berechnen
    vertexData.toCameraVector = -viewSpacePos.xyz;

    // Setzen der Position für den Vertex
    gl_Position = proj_matrix * viewSpacePos;
}
