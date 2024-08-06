#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D screenTexture;
uniform float weights[5];
uniform float offsets[5];
uniform float blurAmount; // Add this uniform

void main() {
    vec2 tex_offset = 1.0 / textureSize(screenTexture, 0); // Gets size of a single texel
    vec3 result = texture(screenTexture, TexCoords).rgb * weights[0]; // Current fragment's contribution

    // Adjust offsets based on blurAmount
    float adjustedOffsets[5];
    for (int i = 0; i < 5; ++i) {
        adjustedOffsets[i] = offsets[i] * blurAmount; // Scale offsets by blurAmount
    }

    for (int i = 1; i < 5; ++i) {
        result += texture(screenTexture, TexCoords + vec2(0.0, adjustedOffsets[i]) * tex_offset).rgb * weights[i];
        result += texture(screenTexture, TexCoords - vec2(0.0, adjustedOffsets[i]) * tex_offset).rgb * weights[i];
    }


    FragColor = vec4(result, 1.0);
}
