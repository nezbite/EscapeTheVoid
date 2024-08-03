package cga.exercise.components.skybox

import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO

data class Skybox(
    val vertices: FloatArray,
    val textureId: Int, // Ein int für die Cube Map
    var vao: Int = 0,
    var vbo: Int = 0
) {

    companion object {
        fun createSkybox(): Skybox {
            val vertices = floatArrayOf(
                // Positionen für die Vertices der Skybox
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f
            )

            // Skybox erstellen
            val textureId = loadSkyboxTextures()

            // VAO und VBO erstellen
            val skybox = Skybox(vertices, textureId)
            skybox.vao = glGenVertexArrays()
            skybox.vbo = glGenBuffers()

            glBindVertexArray(skybox.vao)

            glBindBuffer(GL_ARRAY_BUFFER, skybox.vbo)
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
            glEnableVertexAttribArray(0)

            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)

            return skybox
        }

        fun loadSkyboxTextures(): Int {
            val textureID = glGenTextures()
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureID)

            val faces = arrayOf(
                "project/assets/Environment/posx.jpg",  // +X
                "project/assets/Environment/negx.jpg",   // -X
                "project/assets/Environment/posy.jpg",    // +Y
                "project/assets/Environment/negy.jpg", // -Y
                "project/assets/Environment/posz.jpg",  // +Z
                "project/assets/Environment/negz.jpg" // -Z
            )

            for (i in faces.indices) {
                val textureData = loadTexture(faces[i])

                // Erstelle ByteBuffer aus ByteArray
                val buffer = ByteBuffer.allocateDirect(textureData.data.size)
                buffer.put(textureData.data)
                buffer.flip() // Setze den Buffer bereit für das Lesen

                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, textureData.width, textureData.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)
            }

            glBindTexture(GL_TEXTURE_CUBE_MAP, 0)

            return textureID
        }

        fun loadTexture(path: String): TextureData {
            val image = ImageIO.read(File(path))
            val width = image.width
            val height = image.height
            val data = ByteArray(width * height * 4)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = image.getRGB(x, y)
                    data[(x + y * width) * 4 + 0] = ((pixel shr 16) and 0xFF).toByte() // R
                    data[(x + y * width) * 4 + 1] = ((pixel shr 8) and 0xFF).toByte()  // G
                    data[(x + y * width) * 4 + 2] = (pixel and 0xFF).toByte()           // B
                    data[(x + y * width) * 4 + 3] = ((pixel shr 24) and 0xFF).toByte() // A
                }
            }

            return TextureData(data, width, height)
        }

        data class TextureData(val data: ByteArray, val width: Int, val height: Int)
    }

    fun render() {
        glBindVertexArray(vao)
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId)
        glDrawArrays(GL_TRIANGLES, 0, vertices.size / 3)
        glBindVertexArray(0)
    }
}
