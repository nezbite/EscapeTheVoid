package cga.exercise.components.blur

import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class Framebuffer(width: Int, height: Int) {
    private val fbo: Int = glGenFramebuffers() // Generate framebuffer
    val textureID: Int = glGenTextures() // Generate texture for rendering
    private val rbo: Int = glGenRenderbuffers() // Generate renderbuffer for depth/stencil

    init {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo) // Bind framebuffer

        // Create and configure the texture
        glBindTexture(GL_TEXTURE_2D, textureID)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, null as ByteBuffer?) // Allocate texture
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // Attach the texture to the framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0)

        // Create and attach a renderbuffer for depth and stencil
        glBindRenderbuffer(GL_RENDERBUFFER, rbo)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height) // Allocate renderbuffer
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo)

        // Check if framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer not complete")
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0) // Unbind the framebuffer
    }



    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    }

    fun unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun cleanup() {
        glDeleteFramebuffers(fbo)
        glDeleteTextures(textureID)
        glDeleteRenderbuffers(rbo)
    }
}
