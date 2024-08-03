package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f
import org.lwjgl.opengl.GL30.*

/**
 * Creates a Mesh object from vertexdata, intexdata and a given set of vertex attributes
 *
 * @param vertexdata plain float array of vertex data
 * @param indexdata  index data
 * @param attributes vertex attributes contained in vertex data
 * @throws Exception If the creation of the required OpenGL objects fails, an exception is thrown
 *
 * Created by Fabian on 16.09.2017.
 */
class Mesh(vertexdata: FloatArray, indexdata: IntArray, attributes: Array<VertexAttribute>, var material: Material? = null, var dissolvable: Dissolvable? = null) {
    //private data
    private var vaoId = 0
    private var vboId = 0
    private var iboId = 0
    private var indexcount = 0

    init {
        // ToDo
        // Aufgabe 1.2.2
        // shovel geometry data to GPU and tell OpenGL how to interpret it

        vaoId = glGenVertexArrays()
        glBindVertexArray(vaoId)

        vboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferData(GL_ARRAY_BUFFER, vertexdata, GL_STATIC_DRAW)

        indexcount = indexdata.size

        for (i in 0 until attributes.size) {
            glEnableVertexAttribArray(i)
            glVertexAttribPointer(i, attributes[i].n, attributes[i].type, false, attributes[i].stride, attributes[i].offset)
        }

        iboId = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexdata, GL_STATIC_DRAW)

        glBindVertexArray(0)
        println("mesh creation completed")
    }

    //Only send the geometry to the gpu
    /**
     * renders the mesh
     */
    fun render() {
       // println("renderfunction")
        glBindVertexArray(vaoId)

        glDrawElements(GL_TRIANGLES, indexcount, GL_UNSIGNED_INT, 0)

        glBindVertexArray(0)
    }

    fun render(shaderProgram: ShaderProgram, color: Vector3f = Vector3f(0f, 0f, 0f)) {
        // Shader-Programm verwenden
        shaderProgram.use()

        shaderProgram.setUniform("colorOverlay", color)

        // Material binden, falls vorhanden
        material?.bind(shaderProgram)

        //Dissolvability binden,falls vorhanden
        dissolvable?.bind(shaderProgram)

        // VAO binden und Geometrie rendern
        glBindVertexArray(vaoId)
        glDrawElements(GL_TRIANGLES, indexcount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }

    /**
     * Deletes the previously allocated OpenGL objects for this mesh
     */
    fun cleanup() {
        if (vboId != 0) glDeleteBuffers(vboId)
        if (iboId != 0) glDeleteBuffers(iboId)
        if (vaoId != 0) glDeleteVertexArrays(vaoId)
    }
}