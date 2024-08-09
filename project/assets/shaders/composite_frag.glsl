#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D sceneTexture;
uniform sampler2D edgeTexture;
uniform float edgeDraw;

void main()
{
    vec4 sceneColor = texture(sceneTexture, TexCoord);
    vec4 edgeColor = texture(edgeTexture, TexCoord);

    float amount = edgeColor.a * edgeDraw;

    vec4 finalColor = mix(sceneColor, sceneColor * edgeColor, amount);

    FragColor = finalColor;
}
