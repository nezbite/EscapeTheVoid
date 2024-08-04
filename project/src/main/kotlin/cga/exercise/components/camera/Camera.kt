package cga.exercise.components.camera

import cga.exercise.components.geometry.Transformable

class Camera(
    var fov : Float = 90f,
    var aspectRatio : Float = 16f/9f,
    var nearPlane : Float = 0.1f,
    var farPlane : Float = 500f
) : Transformable(), ICamera {

    override fun getCalculateViewMatrix(): org.joml.Matrix4f {
        return org.joml.Matrix4f().lookAt(getWorldPosition(), getWorldPosition().sub(getWorldZAxis()), getWorldYAxis())
    }

    override fun getCalculateProjectionMatrix(): org.joml.Matrix4f {
        return org.joml.Matrix4f().perspective(Math.toRadians(fov.toDouble()).toFloat(), aspectRatio, nearPlane, farPlane)
    }

    override fun bind(shaderProgram: cga.exercise.components.shader.ShaderProgram) {
        shaderProgram.setUniform("view_matrix", getCalculateViewMatrix(), false)
        shaderProgram.setUniform("proj_matrix", getCalculateProjectionMatrix(), false)
    }
}