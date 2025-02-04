package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f

class Renderable(var meshes: MutableList<Mesh>, var colorOverlay: Vector3f = Vector3f(0f, 0f, 0f), var dissolveFactor: Float = 0.0f) : Transformable(), IRenderable {
    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("model_matrix", this.getWorldModelMatrix(), false)
        for (mesh in meshes) {
            mesh.render(shaderProgram, colorOverlay, dissolveFactor)
        }
    }

    fun setMaterial(material: Material) {
        for (mesh in meshes) {
            mesh.material = material
        }
    }

    fun copy(): Renderable {
        val copy = Renderable(meshes, colorOverlay)
        return copy
    }
}