package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.Vector3f

class Dissolvable(
    var noiseTexture: Texture2D = Texture2D("assets/textures/noiseMap.png",true),
    var colorOverlay: Vector3f = Vector3f(0.0f)
) {
    fun bind(shaderProgram: ShaderProgram) {

        // Binde die Noise-Textur an Textur-Einheit 3 und setze den uniform im Shader
        noiseTexture.bind(0)
        shaderProgram.setUniform("noiseTexture", 0)

        // Setze das Farb-Overlay im Shader
        shaderProgram.setUniform("colorOverlay", colorOverlay)

    }
}