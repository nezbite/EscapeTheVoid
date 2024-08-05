package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f

class SpotLight(worldPos: Vector3f, var direction:Vector3f,color: Vector3f, var innerConeAngle: Float, var outerConeAngle: Float) : PointLight(worldPos, color) {

    init {
        this.direction.normalize()
        this.translate(worldPos)
    }

    fun bind(shaderProgram: ShaderProgram, index: Int, viewMatrix: Matrix4f) {

        // Transform the position and direction to view space
        val viewSpacePos = viewMatrix.transformPosition(worldPos)
        val viewSpaceDir = viewMatrix.transformDirection(direction).normalize()

        println("color: $color")
        println("viewSpaceDir: $viewSpaceDir")
        println("viewSpacePos: $viewSpacePos")
        println("spotLightInnerCutOffs: ${Math.toRadians(innerConeAngle.toDouble()).toFloat()}")
        println("spotLightOuterCutOffs: ${Math.toRadians(outerConeAngle.toDouble()).toFloat()}")
        shaderProgram.setUniform("spotLightColors[$index]", color)
        shaderProgram.setUniform("spotLightPositions[$index]", viewSpacePos)
        shaderProgram.setUniform("spotLightDirections[$index]", viewSpaceDir)
        shaderProgram.setUniform("spotLightInnerCutOffs[$index]", Math.toRadians(innerConeAngle.toDouble()).toFloat())
        shaderProgram.setUniform("spotLightOuterCutOffs[$index]", Math.toRadians(outerConeAngle.toDouble()).toFloat())
    }

    fun getSpotLightViewMatrix(): Matrix4f {
        // Überprüfen, ob der direction Vektor nicht null ist
        if (direction.length() == 0f) {
            println("Direction is zero vector, cannot calculate view matrix")
            return Matrix4f() // Gibt eine Identitätsmatrix zurück oder eine andere geeignete Lösung
        }

        val normalizedDirection = direction.normalize()

        // Berechnung des Zielvektors, der einige Einheiten in Richtung der Richtung verschoben wird
        val target = Vector3f(worldPos).add(normalizedDirection.mul(1.0f)) // Verwendung von Vector3f(worldPos)

        // Überprüfen und Anpassen des "up"-Vektors
        val up = if (normalizedDirection.equals(Vector3f(0f, 1f, 0f)) || normalizedDirection.equals(Vector3f(0f, -1f, 0f))) {
            Vector3f(1f, 0f, 0f) // Alternativer "up"-Vektor
        } else {
            Vector3f(0f, 1f, 0f)
        }

        // Debug-Ausgaben
        /*println("Target: $target")
        println("WorldPos: $worldPos")
        println("Direction: $direction")
        println("Normalized Direction: $normalizedDirection")
        println("Normalized Direction Length: ${normalizedDirection.length()}")
        println("World Position Length: ${worldPos.length()}")
        println("Target Position Length: ${target.length()}")*/

        return Matrix4f().lookAt(worldPos, target, up)
    }

}