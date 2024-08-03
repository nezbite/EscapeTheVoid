import cga.exercise.components.skybox.Skybox
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack.stackPush
import org.joml.Matrix4f
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray

class SkyboxRenderer(private val skybox: Skybox, private val shaderProgram: Int) {

    fun render(projection: Matrix4f, view: Matrix4f) {
        glUseProgram(shaderProgram)

        glBindVertexArray(skybox.vao)
        glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.textureId)

        // Setze die uniforms für die Shader
        val projectionLocation = glGetUniformLocation(shaderProgram, "projection")
        stackPush().use { stack ->
            val projectionBuffer = stack.mallocFloat(16)
            projection.get(projectionBuffer)
            glUniformMatrix4fv(projectionLocation, false, projectionBuffer)
        }

        // Entferne die Translation von der View-Matrix für die Skybox
        val viewWithoutTranslation = Matrix4f(view).setTranslation(0f, 0f, 0f)
        val viewLocation = glGetUniformLocation(shaderProgram, "view")
        stackPush().use { stack ->
            val viewBuffer = stack.mallocFloat(16)
            viewWithoutTranslation.get(viewBuffer)
            glUniformMatrix4fv(viewLocation, false, viewBuffer)
        }

        // Zeichne die Skybox
        glDrawArrays(GL_TRIANGLES, 0, 36)

        glBindVertexArray(0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0)
    }
}

