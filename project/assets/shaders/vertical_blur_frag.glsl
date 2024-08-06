#version 330 core
out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D screenTexture;
uniform float weights[5];
uniform float offsets[5];
uniform float blurAmount; // Add this uniform

void main() {
    vec2 tex_offset = 1.0 / textureSize(screenTexture, 0); // Gets size of a single texel
    // Clamp blurAmount between 0.0 and 1.0
    float clampedBlurAmount = clamp(blurAmount, 0.0, 3.0);
    vec3 result = texture(screenTexture, TexCoords).rgb * weights[0]; // Current fragment's contribution

    // Adjust offsets based on clampedBlurAmount
    float adjustedOffsets[5];
    for (int i = 0; i < 5; ++i) {
        adjustedOffsets[i] = offsets[i] * clampedBlurAmount; // Scale offsets by clampedBlurAmount
    }

    for (int i = 1; i < 5; ++i) {
        result += texture(screenTexture, TexCoords + vec2(0.0, adjustedOffsets[i]) * tex_offset).rgb * weights[i];
        result += texture(screenTexture, TexCoords - vec2(0.0, adjustedOffsets[i]) * tex_offset).rgb * weights[i];
    }

    FragColor = vec4(result, 1.0);
}
