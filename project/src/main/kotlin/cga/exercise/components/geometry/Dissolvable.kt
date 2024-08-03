package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.glfwGetTime
import kotlin.system.measureTimeMillis

class Dissolvable(
    var noiseTexture: Texture2D = Texture2D("project/assets/textures/noiseMap.png",true),
    var colorOverlay: Vector3f = Vector3f(0.0f)
) {
    val currentTime = glfwGetTime().toFloat()
    fun bind(shaderProgram: ShaderProgram) {

        // Binde die Noise-Textur an Textur-Einheit 3 und setze den uniform im Shader
        noiseTexture.bind(0)
        shaderProgram.setUniform("noiseTexture", 0)

        // Setze das Farb-Overlay im Shader
        shaderProgram.setUniform("colorOverlay", colorOverlay)

    }
}