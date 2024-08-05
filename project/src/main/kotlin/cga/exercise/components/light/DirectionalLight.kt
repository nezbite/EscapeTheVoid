package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f

class DirectionalLight(
    private val direction: Vector3f,
    private val color: Vector3f,
    private val intensity: Float
) : IDirectionalLight {

    override fun bind(shaderProgram: ShaderProgram, lightIndex: Int) {
        shaderProgram.setUniform("dirLightDirections[$lightIndex]", direction)
        shaderProgram.setUniform("dirLightColors[$lightIndex]", color)
        shaderProgram.setUniform("dirLightIntensities[$lightIndex]", intensity)
    }
}
