package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram

class LightManager {
    val directionalLights = mutableListOf<DirectionalLight>()
    val spotLights = mutableListOf<SpotLight>()
    val pointLights = mutableListOf<PointLight>()

    fun addDirectionalLight(light: DirectionalLight) {
        directionalLights.add(light)
    }

    fun addSpotLight(light: SpotLight) {
        spotLights.add(light)
    }

    fun addPointLight(light: PointLight) {
        pointLights.add(light)
    }

    fun bindDirectionalLights(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("numDirLights", directionalLights.size)

        for ((index, light) in directionalLights.withIndex()) {
            light.bind(shaderProgram, index)
        }
    }

    fun bindPointLights(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("numPointLights", pointLights.size)

        for ((index, light) in pointLights.withIndex()) {
            light.bind(shaderProgram, index)
        }
    }

    fun bindSpotLights(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("numSpotLights", spotLights.size)

        for ((index, light) in spotLights.withIndex()) {
            light.bind(shaderProgram,index)
        }
    }


}
