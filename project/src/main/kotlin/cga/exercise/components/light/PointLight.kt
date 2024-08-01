package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f

open class PointLight(var worldPos: Vector3f, var color: Vector3f) : Transformable(), IPointLight {
    init {
        this.translate(worldPos);
    }

    override fun bind(shaderProgram: ShaderProgram) {
        // Set the light position
        shaderProgram.setUniform("pointLightPosition", getWorldPosition())
        // Set the light color
        shaderProgram.setUniform("pointLightColor", color)
    }
}