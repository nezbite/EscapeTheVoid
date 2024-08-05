package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram

interface IDirectionalLight {
    fun bind(shaderProgram: ShaderProgram, lightIndex: Int)
}
