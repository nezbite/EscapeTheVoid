package cga.exercise.components.camera

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f

class Camera(
    var fov: Float = 90f,
    var aspectRatio: Float = 16f / 9f,
    var nearPlane: Float = 0.1f,
    var farPlane: Float = 500f
) : Transformable(), ICamera {

    private val originalPosition = Vector3f()
    private val shakeEffect = CameraShakeEffect()

    override fun getCalculateViewMatrix(): Matrix4f {
        val shakeOffset = shakeEffect.update(0.0333f) // Assume a frame time of ~16ms (60 FPS)
        val shakenPosition = getWorldPosition().add(Vector3f(shakeOffset, 0.0f))
        return Matrix4f().lookAt(shakenPosition, shakenPosition.sub(getWorldZAxis(), Vector3f()), getWorldYAxis())
    }

    override fun getCalculateProjectionMatrix(): Matrix4f {
        return Matrix4f().perspective(Math.toRadians(fov.toDouble()).toFloat(), aspectRatio, nearPlane, farPlane)
    }

    override fun bind(shader: ShaderProgram) {
        shader.setUniform("view_matrix", getCalculateViewMatrix(), false)
        shader.setUniform("proj_matrix", getCalculateProjectionMatrix(), false)
    }

    fun startScreenShake(duration: Float, intensity: Float) {
        shakeEffect.start(duration, intensity)
    }
}
