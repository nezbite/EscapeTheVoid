package cga.exercise.components.camera

import org.joml.Vector2f

class CameraShakeEffect {
    var duration: Float = 0.5f  // Duration of the shake in seconds
    var intensity: Float = 0.5f  // Intensity of the shake
    private var timeRemaining: Float = 0f  // Time remaining for the shake

    fun start(duration: Float, intensity: Float) {
        this.duration = duration
        this.intensity = intensity
        timeRemaining = duration
    }

    fun update(dt: Float): Vector2f {
        if (timeRemaining > 0) {
            timeRemaining -= dt
            val shakeX = (Math.random() * 2.0 - 1.0).toFloat() * intensity
            val shakeY = (Math.random() * 2.0 - 1.0).toFloat() * intensity
            return Vector2f(shakeX, shakeY)
        }
        return Vector2f(0.0f, 0.0f)  // No shake
    }

    fun isShaking(): Boolean {
        return timeRemaining > 0
    }
}
