package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f

class SpotLight(worldPos: Vector3f, color: Vector3f, var innerConeAngle: Float, var outerConeAngle: Float) : PointLight(worldPos, color), ISpotLight {
    init {
        this.translate(worldPos)
    }

    override fun bind(shaderProgram: ShaderProgram, viewMatrix: Matrix4f) {
        shaderProgram.setUniform("spotLightColor", color)
        shaderProgram.setUniform("spotLightPosition", getWorldPosition())
        shaderProgram.setUniform("spotLightDirection", Vector3f(getWorldZAxis().negate().mul(Matrix3f(viewMatrix))))
        shaderProgram.setUniform("spotLightInnerCutOff", Math.toRadians(innerConeAngle.toDouble()).toFloat())
        shaderProgram.setUniform("spotLightOuterCutOff", Math.toRadians(outerConeAngle.toDouble()).toFloat())
    }
}