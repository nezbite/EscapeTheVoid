#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D screenTexture;
uniform vec2 texSize;

// Sobel edge detection parameters
const float edgeThreshold = 0.4;

void main()
{
    // Calculate the texture coordinates for the neighboring pixels
    vec2 texOffset = 0.8 / texSize;

    // Sample neighboring pixels
    float topLeft    = texture(screenTexture, TexCoord + vec2(-texOffset.x,  texOffset.y)).r;
    float topCenter  = texture(screenTexture, TexCoord + vec2(0.0,  texOffset.y)).r;
    float topRight   = texture(screenTexture, TexCoord + vec2( texOffset.x,  texOffset.y)).r;
    float middleLeft = texture(screenTexture, TexCoord + vec2(-texOffset.x, 0.0)).r;
    float middle     = texture(screenTexture, TexCoord).r;
    float middleRight= texture(screenTexture, TexCoord + vec2( texOffset.x, 0.0)).r;
    float bottomLeft = texture(screenTexture, TexCoord + vec2(-texOffset.x, -texOffset.y)).r;
    float bottomCenter = texture(screenTexture, TexCoord + vec2(0.0, -texOffset.y)).r;
    float bottomRight = texture(screenTexture, TexCoord + vec2( texOffset.x, -texOffset.y)).r;

    // Sobel operator to find edges
    vec2 Gx = vec2(-1.0, 1.0) * (topLeft + 2.0 * middleLeft + bottomLeft - topRight - 2.0 * middleRight - bottomRight);
    vec2 Gy = vec2(-1.0, 1.0) * (topLeft + 2.0 * topCenter + topRight - bottomLeft - 2.0 * bottomCenter - bottomRight);

    // Compute gradient magnitude
    float edgeStrength = length(Gx) + length(Gy) /2;

    // Set color: black for edges, transparent for background
    if (edgeStrength > edgeThreshold)
    {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0); // Lines
    }
    else
    {
        FragColor = vec4(1.0, 1.0, 1.0, 0.0); // Background
    }
}
