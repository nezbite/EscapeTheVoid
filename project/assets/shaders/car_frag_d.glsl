#version 330 core

// Eingang vom Vertex-Shader
in struct VertexData
{
    vec3 color;
    vec3 normal;
    vec2 texCoord; // Texturkoordinaten
    vec3 fragPosition; // Fragmentsposition im Viewspace
    vec3 toCameraVector; // Vektor zum Kameraposition im Viewspace
    vec3 toPointLightVector; // Vektor zum Point-Light im Viewspace
    vec3 toSpotLightVector; // Vektor zum Spot-Light im Viewspace
} vertexData;

// Uniforms für Texturen
uniform sampler2D material_diffuse;
uniform sampler2D material_emission;
uniform sampler2D material_specular;
uniform float material_shininess;

// Uniforms für Point Light
uniform vec3 pointLightPosition;
uniform vec3 pointLightColor;

// Uniforms für Spot Light
uniform vec3 spotLightPosition;
uniform vec3 spotLightDirection;
uniform float spotLightInnerCutOff;
uniform float spotLightOuterCutOff;
uniform vec3 spotLightColor;
uniform vec4 viewMatrix;

// Gamma-Korrektur Parameter
uniform float gamma = 2.2;

// Uniform für Farb-Overlay
uniform vec3 colorOverlay;

// Uniforms für Dissolve-Effekt
uniform sampler2D noiseTexture;
uniform float dissolveFactor;
uniform float time;

// Ausgabe des Fragment-Shaders
out vec4 FragColor;

// Funktionen zur Gammakorrektur
vec3 toLinear(vec3 color) {
    return pow(color, vec3(gamma));
}

vec3 toGamma(vec3 color) {
    return pow(color, vec3(1.0 / gamma));
}

vec3 calculateBlinnPhongLighting(vec3 fragColor, vec3 normal, vec3 fragPosition, vec3 toCamera, vec3 toLight) {
    float distance = length(toLight);
    float attenuation = 1.0 / (distance * distance);

    vec3 ambient = 0.1 * fragColor; // Ambient lighting

    // Diffuse lighting
    vec3 lightDir = normalize(toLight);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * pointLightColor * fragColor * attenuation;

    // Specular lighting
    vec3 viewDir = normalize(toCamera);
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material_shininess);
    vec3 specular = spec * pointLightColor * attenuation;

    return ambient + diffuse + specular;
}

vec3 calculateBlinnPhongSpotLight(vec3 fragPosition, vec3 normal, vec3 viewDir)
{
    vec3 lightDir = normalize(spotLightPosition - fragPosition);
    float distance = length(spotLightPosition - fragPosition);
    float attenuation = 1.0 / (distance * distance);

    vec3 spotDir = normalize(spotLightDirection);
    float theta = dot(lightDir, -spotDir);
    float epsilon = spotLightOuterCutOff - spotLightInnerCutOff;
    float intensity = clamp((theta - spotLightInnerCutOff) / epsilon, 0.0, 1.0);

    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * spotLightColor * intensity * attenuation;

    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material_shininess);
    vec3 specular = material_shininess * spec * spotLightColor * intensity * attenuation;

    return diffuse + specular;
}

void main() {

    // Dissolve-Effekt anwenden
    float noiseValue = texture(noiseTexture, vertexData.texCoord).r;
    if (noiseValue < dissolveFactor)
    {
        discard;
    }
    vec3 normalizedNormal = normalize(vertexData.normal);

    // Texturfarbe abrufen und in lineare Werte umwandeln
    vec4 textureColor = texture(material_diffuse, vertexData.texCoord);
    vec3 fragColor = toLinear(textureColor.rgb);

    // Berechnung der Beleuchtung
    vec3 lighting = calculateBlinnPhongLighting(fragColor, normalizedNormal, vertexData.fragPosition, vertexData.toCameraVector, vertexData.toPointLightVector);

    // Calculate the vector to the spotlight in view space
    vec3 spotLighting = calculateBlinnPhongSpotLight(vertexData.fragPosition, normalizedNormal, vertexData.toSpotLightVector);

    // Emissive Textur abfragen und in lineare Werte umwandeln
    vec3 emissionColor = toLinear(texture(material_emission, vertexData.texCoord).rgb);

    // Finalen Farbwert setzen basierend auf der Phong-Beleuchtung und emissiven Textur, kombiniert mit Spot-Light-Beleuchtung
    vec3 finalColor = lighting + spotLighting + emissionColor;





    // Farb-Overlay nur anwenden, wenn es gesetzt ist
    if (colorOverlay != vec3(0.0)) {
        finalColor *= colorOverlay;
    }

    // Punktlichtfarbe mit der Farb-Variation beeinflussen
    vec3 finalPointLightColor = pointLightColor;

    // Umwandlung des finalen Farbwerts in den sRGB Farbraum
    FragColor = vec4(toGamma(finalColor), 1.0);
}

