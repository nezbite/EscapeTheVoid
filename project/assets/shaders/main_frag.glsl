#version 330 core

// Eingang vom Vertex-Shader
in struct VertexData {
    vec3 color;
    vec3 normal;
    vec2 texCoord; // Texturkoordinaten
    vec3 fragPosition; // Fragmentsposition im Viewspace
    vec3 toCameraVector; // Vektor zum Kameraposition im Viewspace
    vec3 toPointLightVector[10]; // Vektor zum Point-Light im Viewspace
    vec3 toSpotLightVector[10]; // Vektor zum Spot-Light im Viewspace
} vertexData;

// Uniforms für Texturen
uniform sampler2D material_diffuse;
uniform sampler2D material_emission;
uniform sampler2D material_specular;
uniform float material_shininess;

// Uniforms für Point Light
uniform int numPointLights;
uniform vec3 pointLightPositions[10];
uniform vec3 pointLightColors[10];

// Uniforms für Spot Light
uniform int numSpotLights;
uniform vec3 spotLightPositions[10];
uniform vec3 spotLightDirections[10];
uniform vec3 spotLightColors[10];
uniform float spotLightInnerCutOffs[10];
uniform float spotLightOuterCutOffs[10];

// Uniforms für Directional Lights
uniform int numDirLights;
uniform vec3 dirLightDirections[10];
uniform vec3 dirLightColors[10];
uniform float dirLightIntensities[10];

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

vec3 calculateBlinnPhongPointLight(vec3 fragColor, vec3 normal, vec3 toCamera, vec3 toLight, vec3 lightColor) {
    float distance = length(toLight);
    float attenuation = 1.0 / (distance * distance + 0.01);

    vec3 ambient = 0.01 * lightColor; // Ambient lighting

    // Diffuse lighting
    vec3 lightDir = normalize(toLight);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * lightColor * fragColor * attenuation;

    // Specular lighting
    vec3 viewDir = normalize(toCamera);
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material_shininess);
    vec3 specular = spec * lightColor * attenuation;

    return ambient + diffuse + specular;
}

vec3 calculateBlinnPhongSpotLight(
    vec3 fragColor,
    vec3 normal,
    vec3 toCamera,
    vec3 toLight,
    vec3 lightDir,
    vec3 lightColor,
    float innerCutOff,
    float outerCutOff,
    float material_shininess
) {
    float distance = length(toLight);
    // Normalize the vectors
    vec3 lightDirection = normalize(toLight);
    vec3 normalizedLightDir = normalize(lightDir);
    vec3 normalizedNormal = normalize(normal);

    // Calculate the angle between the light direction and the light's normalized direction
    float theta = dot(lightDirection, normalizedLightDir);

    // Check if the fragment is within the spotlight cone
    if (theta < cos(innerCutOff)) {
        return vec3(0.0); // Outside of inner cone, no light
    } else if (theta > cos(outerCutOff)) {
        return lightColor * fragColor; // Full intensity within inner cone
    }

    // Calculate spotlight intensity using smoothstep for a softer transition
    float epsilon = outerCutOff - innerCutOff;
    float intensity = smoothstep(cos(outerCutOff), cos(innerCutOff), theta);

    // Calculate attenuation based on distance
    float attenuation = 1.0 / (distance * distance + 0.01); // Adjusted to avoid division by zero

    // Ambient component
    vec3 ambient = 0.1 * fragColor; // Or lightColor if you prefer

    // Diffuse component
    float diff = max(dot(normalizedNormal, lightDirection), 0.0);
    vec3 diffuse = diff * lightColor * fragColor * attenuation * intensity;

    // Specular component
    vec3 viewDir = normalize(toCamera);
    vec3 halfDir = normalize(lightDirection + viewDir);
    float spec = pow(max(dot(normalizedNormal, halfDir), 0.0), material_shininess);
    vec3 specular = spec * lightColor * attenuation * intensity;

    // Combine the components
    return ambient + diffuse + specular;
}


vec3 calculateBlinnPhongDirectionalLight(vec3 normal, vec3 viewDir, vec3 lightDir, vec3 lightColor, float intensity) {
    vec3 ambient = 0.1 * lightColor * intensity; // Ambient lighting

    // Diffuse lighting
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * lightColor * intensity;

    // Specular lighting
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material_shininess);
    vec3 specular = spec * lightColor * intensity;

    return ambient + diffuse + specular;
}

void main() {
    // Dissolve-Effekt anwenden
    float noiseValue = texture(noiseTexture, vertexData.texCoord).r;
    if (noiseValue < dissolveFactor) {
        discard;
    }

    vec3 normalizedNormal = normalize(vertexData.normal);

    // Texturfarbe abrufen und in lineare Werte umwandeln
    vec4 textureColor = texture(material_diffuse, vertexData.texCoord);
    vec3 fragColor = toLinear(textureColor.rgb);

    // Berechnung der Spot Light Beleuchtung
    vec3 spotLighting = vec3(0.0);
    for (int j = 0; j < numSpotLights; j++) {
        spotLighting += calculateBlinnPhongSpotLight(fragColor, normalizedNormal, vertexData.toCameraVector, vertexData.toSpotLightVector[j], spotLightDirections[j], spotLightColors[j], spotLightInnerCutOffs[j], spotLightOuterCutOffs[j],material_shininess);
    }

    // Berechnung der Point Light Beleuchtung
    vec3 pointLighting = vec3(0.0);
    for (int i = 0; i < numPointLights; i++) {
        pointLighting += calculateBlinnPhongPointLight(fragColor, normalizedNormal, vertexData.toCameraVector, vertexData.toPointLightVector[i], pointLightColors[i]);
    }


    // Berechnung der Directional Light Beleuchtung
    vec3 dirLighting = vec3(0.0);
    for (int k = 0; k < numDirLights; k++) {
        vec3 dirLightDirectionNormalized = normalize(dirLightDirections[k]);
        dirLighting += calculateBlinnPhongDirectionalLight(normalizedNormal, vertexData.toCameraVector, dirLightDirectionNormalized, dirLightColors[k], dirLightIntensities[k]);
    }

    // Emissive Textur abfragen und in lineare Werte umwandeln
    vec3 emissionColor = toLinear(texture(material_emission, vertexData.texCoord).rgb) * 0.4;

    // Finalen Farbwert setzen basierend auf der Phong-Beleuchtung, kombiniert mit Spot-Light- und Directional-Light-Beleuchtung und emissiven Textur
    vec3 finalColor = clamp(spotLighting + pointLighting + dirLighting + emissionColor,0.0,1.0);

    // Farb-Overlay nur anwenden, wenn es gesetzt ist
    if (colorOverlay != vec3(0.0)) {
        finalColor *= colorOverlay;
    }

    // Umwandlung des finalen Farbwerts in den sRGB Farbraum
    FragColor = vec4(toGamma(finalColor), 1.0);
}
