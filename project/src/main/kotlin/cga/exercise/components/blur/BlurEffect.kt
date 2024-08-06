package cga.exercise.components.blur

import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector2f
import org.lwjgl.opengl.ARBVertexArrayObject.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer

class BlurEffect(private val shader: ShaderProgram) {
    private var blurAmount: Float = 10.0f
    private val weights = floatArrayOf(0.227027f, 0.194594f, 0.121621f, 0.054721f, 0.016216f)// Adjust this based on your desired weights
    private val offsets = floatArrayOf(0.0f,1.0f,2.0f,3.0f,4.0f) // Adjust this based on your desired offsets

    fun bind() {

        shader.setUniform("texture", blurAmount)

        for (i in weights.indices) {
            shader.setUniform("weights[$i]", weights[i]) // Set other weights as needed
            shader.setUniform("offsets[$i]", offsets[i]) // Set other offsets as needed
        }

        shader.setUniform("blurAmount", blurAmount)

    }

    fun setBlurAmount(amount: Float) {
        blurAmount = amount
        shader.setUniform("blurAmount", blurAmount)
    }

    fun setWeightsAndOffsets(newWeights: FloatArray, newOffsets: FloatArray) {
        for (i in weights.indices) {
            weights[i] = newWeights[i]
            offsets[i] = newOffsets[i]
        }
        shader.use()
        for (i in weights.indices) {
            shader.setUniform("weights[$i]", weights[i])
            shader.setUniform("offsets[$i]", offsets[i])
        }
    }

    fun renderFullScreenQuad() {
        // Set up vertex data for a full-screen quad
        val vertices = floatArrayOf(
            // Positions    // Texture Coords
            -1.0f,  1.0f, 0.0f, 1.0f, // Top-left
            1.0f,  1.0f, 1.0f, 1.0f, // Top-right
            1.0f, -1.0f, 1.0f, 0.0f, // Bottom-right
            -1.0f, -1.0f, 0.0f, 0.0f  // Bottom-left
        )

        val indices = intArrayOf(
            0, 1, 2, // First triangle
            0, 2, 3  // Second triangle
        )

        // Create and bind VAO, VBO, and EBO
        val vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        val ebo = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        // Position attribute
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0)
        glEnableVertexAttribArray(0)

        // Texture coordinate attribute
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4)
        glEnableVertexAttribArray(1)

        // Draw the quad
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)

        // Cleanup
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        glDeleteBuffers(vbo)
        glDeleteBuffers(ebo)
        glDeleteVertexArrays(vao)
    }
}
