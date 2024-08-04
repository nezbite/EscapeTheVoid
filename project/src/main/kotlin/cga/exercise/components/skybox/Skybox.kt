package cga.exercise.components.skybox

import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer

class Skybox(
    val vaoID: Int,
    val vertexCount: Int,
    val textureId: Int
) {
    companion object {
        fun createSkybox(): Skybox {
            // Create VAO
            val vaoID = glGenVertexArrays()
            glBindVertexArray(vaoID)

            // Create VBO
            val vertices = floatArrayOf(
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,

                -1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,

                -1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f
            )
            val vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.size)
            vertexBuffer.put(vertices).flip()

            val vboID = glGenBuffers()
            glBindBuffer(GL_ARRAY_BUFFER, vboID)
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

            glEnableVertexAttribArray(0)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)

            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            // Load cubemap textures
            val textureId = loadCubemap(
                arrayOf(
                    "project/assets/Environment/posx.jpg",
                    "project/assets/Environment/negx.jpg",
                    "project/assets/Environment/posy.jpg",
                    "project/assets/Environment/negy.jpg",
                    "project/assets/Environment/posz.jpg",
                    "project/assets/Environment/negz.jpg"
                )
            )

            return Skybox(vaoID, vertices.size / 3, textureId)
        }

        private fun loadCubemap(texturePaths: Array<String>): Int {
            val textureID = glGenTextures()
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureID)

            MemoryStack.stackPush().use { stack ->
                val width: IntBuffer = stack.mallocInt(1)
                val height: IntBuffer = stack.mallocInt(1)
                val nrChannels: IntBuffer = stack.mallocInt(1)

                for (i in texturePaths.indices) {
                    println("Loading texture: ${texturePaths[i]}")
                    val data: ByteBuffer? = stbi_load(texturePaths[i], width, height, nrChannels, 3)
                    if (data != null) {
                        println("Loaded texture: ${texturePaths[i]} (width: ${width.get(0)}, height: ${height.get(0)})")
                        glTexImage2D(
                            GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                            0,
                            GL_RGB,
                            width.get(0),
                            height.get(0),
                            0,
                            GL_RGB,
                            GL_UNSIGNED_BYTE,
                            data
                        )
                        stbi_image_free(data)
                    } else {
                        throw RuntimeException("Failed to load cubemap texture at ${texturePaths[i]}: ${stbi_failure_reason()}")
                    }
                }
            }

            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

            return textureID
        }
    }
}
