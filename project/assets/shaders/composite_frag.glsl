#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D sceneTexture;
uniform sampler2D edgeTexture;

void main()
{
    vec4 sceneColor = texture(sceneTexture, TexCoord);
    vec4 edgeColor = texture(edgeTexture, TexCoord);

    vec4 finalColor = mix(sceneColor, sceneColor * edgeColor, edgeColor.a);

    FragColor = finalColor;
}
