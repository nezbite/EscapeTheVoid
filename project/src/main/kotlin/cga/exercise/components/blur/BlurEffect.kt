package cga.exercise.components.blur

import cga.exercise.components.shader.ShaderProgram

class BlurEffect(private val shader: ShaderProgram, private val fullScreenQuad: FullScreenQuad) {
    private var blurAmount: Float = 0f
    private val weights = floatArrayOf(0.227027f, 0.194594f, 0.121621f, 0.054721f, 0.016216f) // Adjust this based on your desired weights
    private val offsets = floatArrayOf(0.0f, 1.0f, 2.0f, 3.0f, 4.0f) // Adjust this based on your desired offsets

    fun bind() {
        // Bind weights and offsets to the shader
        for (i in weights.indices) {
            shader.setUniform("weights[$i]", weights[i])
            shader.setUniform("offsets[$i]", offsets[i])
        }
        shader.setUniform("blurAmount", blurAmount)
    }

    fun setBlurAmount(amount: Float) {
        blurAmount = amount
    }

    fun setWeightsAndOffsets(newWeights: FloatArray, newOffsets: FloatArray) {
        for (i in weights.indices) {
            weights[i] = newWeights[i]
            offsets[i] = newOffsets[i]
        }
    }

    fun render() {
        shader.use()
        bind()
        fullScreenQuad.render() // Use the FullScreenQuad to render
    }

    fun cleanup() {
        // No need to clean up VAO, VBO, or EBO since we are using FullScreenQuad
        shader.cleanup()
    }
}
