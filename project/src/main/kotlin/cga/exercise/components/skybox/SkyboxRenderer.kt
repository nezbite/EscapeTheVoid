package cga.exercise.components.skybox

import cga.exercise.components.camera.Camera
import cga.exercise.components.shader.ShaderProgram
import org.lwjgl.opengl.GL30.*

class SkyboxRenderer(private val skyboxShaderProgram: ShaderProgram) {

    fun render(skybox: Skybox, camera: Camera) {
        skyboxShaderProgram.use()

        val viewMatrix = camera.getCalculateViewMatrix().also {
            it.m30(0f) // Set the camera position to 0
            it.m31(0f)
            it.m32(0f)
        }

        skyboxShaderProgram.setUniform("view", viewMatrix)
        skyboxShaderProgram.setUniform("projection", camera.getCalculateProjectionMatrix())

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, skybox.textureId)
        skyboxShaderProgram.setUniform("skybox", 0)

        glBindVertexArray(skybox.vaoID)
        glDrawArrays(GL_TRIANGLES, 0, skybox.vertexCount)
        glBindVertexArray(0)
    }
}
