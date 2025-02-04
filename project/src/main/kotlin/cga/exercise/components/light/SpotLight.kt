package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f
import kotlin.math.abs

class SpotLight( worldPos: Vector3f, var direction:Vector3f, var color: Vector3f, var innerConeAngle: Float, var outerConeAngle: Float) : Transformable(), ISpotLight {

    init {
        this.translate(worldPos)
    }

    override fun bind(shaderProgram: ShaderProgram, index: Int) {

       /* println("color: $color")
        println("spotLightInnerCutOffs: ${Math.toRadians(innerConeAngle.toDouble()).toFloat()}")
        println("spotLightOuterCutOffs: ${Math.toRadians(outerConeAngle.toDouble()).toFloat()}")*/

        // Adjust direction by transformable rotation information

        shaderProgram.setUniform("spotLightColors[$index]", color)
        shaderProgram.setUniform("spotLightPositions[$index]", this.getWorldPosition())
        shaderProgram.setUniform("spotLightDirections[$index]", this.getWorldZAxis().add(direction))
        shaderProgram.setUniform("spotLightInnerCutOffs[$index]", Math.toRadians(innerConeAngle.toDouble()).toFloat())
        shaderProgram.setUniform("spotLightOuterCutOffs[$index]", Math.toRadians(outerConeAngle.toDouble()).toFloat())
    }



}