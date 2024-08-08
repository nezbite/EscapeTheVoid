#version 330 core

const int MAX_LIGHTS = 4;

// Eingang vom Vertex-Shader
in struct VertexData {
    vec3 color;
    vec3 normal;
    vec2 texCoord; // Texturkoordinaten
    vec3 fragPosition; // Fragmentsposition im Viewspace
    vec3 toCameraVector; // Vektor zum Kameraposition im Viewspace
    vec3 toPointLightVector[MAX_LIGHTS]; // Vektor zum Point-Light im Viewspace
    vec3 toSpotLightVector[MAX_LIGHTS]; // Vektor zum Spot-Light im Viewspace
    vec3 spotLightDirections[MAX_LIGHTS];
} vertexData;

uniform sampler2D material_diffuse;

out vec4 FragColor;

void main() {
    // Texturfarbe abrufen und in lineare Werte umwandeln
    vec4 textureColor = texture(material_diffuse, vertexData.texCoord);

    // Umwandlung des finalen Farbwerts in den sRGB Farbraum
    FragColor = vec4(textureColor.rgb, 1);
}
