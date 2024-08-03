package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import org.joml.Vector2f

class Material(var diff: Texture2D,
               var emit: Texture2D,
               var specular: Texture2D,
               var shininess: Float = 50.0f,
               var tcMultiplier : Vector2f = Vector2f(1.0f)) {

    fun bind(shaderProgram: ShaderProgram) {
        // Binde die Diffuse-Textur an Textur-Einheit 0 und setze den uniform im Shader
        shaderProgram.use()
        diff.bind(0)
        shaderProgram.setUniform("material_diffuse", 0)

        // Binde die Emission-Textur an Textur-Einheit 1 und setze den uniform im Shader
        emit.bind(1)
        shaderProgram.setUniform("material_emission", 1)

        // Binde die Specular-Textur an Textur-Einheit 2 und setze den uniform im Shader
        specular.bind(2)
        shaderProgram.setUniform("material_specular", 2)

        // Setze den Shininess-Wert im Shader
        shaderProgram.setUniform("material_shininess", shininess)

        // Setze den Textur-Koordinaten-Multiplier im Shader
        shaderProgram.setUniform("tcMultiplier", tcMultiplier)
    }
}