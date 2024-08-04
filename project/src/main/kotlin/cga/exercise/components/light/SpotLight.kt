package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f

class SpotLight(worldPos: Vector3f, var direction:Vector3f,color: Vector3f, var innerConeAngle: Float, var outerConeAngle: Float) : PointLight(worldPos, color) {

    fun bind(shaderProgram: ShaderProgram, index: Int, viewMatrix: Matrix4f) {

        // Transform the position and direction to view space
        val viewSpacePos = viewMatrix.transformPosition(worldPos)
        val viewSpaceDir = viewMatrix.transformDirection(direction)

        shaderProgram.setUniform("spotLightColors[$index]", color)
        shaderProgram.setUniform("spotLightPositions[$index]", viewSpacePos)
        shaderProgram.setUniform("spotLightDirections[$index]", viewSpaceDir)
        shaderProgram.setUniform("spotLightInnerCutOffs[$index]", Math.toRadians(innerConeAngle.toDouble()).toFloat())
        shaderProgram.setUniform("spotLightOuterCutOffs[$index]", Math.toRadians(outerConeAngle.toDouble()).toFloat())
    }

    fun getSpotLightViewMatrix(): Matrix4f {
        val target = worldPos.add(direction.mul(1.0f)) // Adjust the scaling as needed
        return Matrix4f().lookAt(worldPos, target, Vector3f(0f, 1f, 0f))
    }

}