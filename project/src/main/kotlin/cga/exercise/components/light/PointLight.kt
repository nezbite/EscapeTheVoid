package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f

open class PointLight(var worldPos: Vector3f, var color: Vector3f) : Transformable() {
    init {
        this.translate(worldPos)
    }

    fun bind(shaderProgram: ShaderProgram, index: Int) {
        // Set the light position
        shaderProgram.setUniform("pointLightPositions[$index]", this.getWorldPosition())
        // Set the light color
        shaderProgram.setUniform("pointLightColors[$index]", color)
    }
}
