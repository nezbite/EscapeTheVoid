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
    vec3 toPointLightVector; // Vektor zum Point-Light im Viewspace
    vec3 toSpotLightVector; // Vektor zum Spot-Light im Viewspace
} vertexData;

// Uniforms für Point Light
uniform vec3 pointLightPosition;
uniform vec3 pointLightColor;

// Uniforms für Spot Light
uniform vec3 spotLightPosition;
uniform vec3 spotLightDirection;
uniform float spotLightInnerCutOff;
uniform float spotLightOuterCutOff;

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

    vertexData.toPointLightVector = (view_matrix * vec4(pointLightPosition, 1.0)).xyz - viewSpacePos.xyz;

    vertexData.toSpotLightVector = (view_matrix * vec4(spotLightPosition, 1.0)).xyz - viewSpacePos.xyz;

    // Vektor zum Kameraposition im Viewspace berechnen
    vertexData.toCameraVector = -viewSpacePos.xyz;

    //    // Vektor zum Point-Light im Viewspace berechnen
    //    vertexData.toPointLightVector = pointLightPosition - vertexData.fragPosition;
    //
    //    // Vektor zum Spot-Light im Viewspace berechnen
    //    vertexData.toSpotLightVector = spotLightPosition - vertexData.fragPosition;

    // Setzen der Position für den Vertex
    gl_Position = proj_matrix * viewSpacePos;
}