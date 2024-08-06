package cga.exercise.components.blur

import org.lwjgl.opengl.ARBVertexArrayObject.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer

class FullScreenQuad {
    private var vaoID: Int = 0
    private var vboID: Int = 0

    init {
        // Fullscreen quad vertices
        val vertices = floatArrayOf(
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 1.0f
        )

        // Generate VAO and VBO
        vaoID = glGenVertexArrays()
        vboID = glGenBuffers()

        // Bind VAO
        glBindVertexArray(vaoID)

        // Bind and set VBO
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        // Vertex attributes
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0)

        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4)

        // Unbind VAO and VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun render() {
        // Bind VAO and draw quad
        glBindVertexArray(vaoID)
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        glBindVertexArray(0)
    }

    fun cleanup() {
        glDeleteBuffers(vboID)
        glDeleteVertexArrays(vaoID)
    }
}
