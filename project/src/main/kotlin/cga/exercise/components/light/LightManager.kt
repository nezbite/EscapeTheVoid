package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram

class LightManager {
    val directionalLights = mutableListOf<DirectionalLight>()
    val spotLights = mutableListOf<SpotLight>()
    val pointLights = mutableListOf<PointLight>()
    val mapPointLights = mutableListOf<PointLight>()

    fun addDirectionalLight(light: DirectionalLight) {
        directionalLights.add(light)
    }

    fun addSpotLight(light: SpotLight) {
        spotLights.add(light)
    }

    fun addPointLight(light: PointLight) {
        pointLights.add(light)
    }

    fun mapPointLights(lights: MutableList<PointLight>) {
        mapPointLights.clear()
        mapPointLights.addAll(lights)
    }

    fun bindDirectionalLights(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("numDirLights", directionalLights.size)

        for ((index, light) in directionalLights.withIndex()) {
            light.bind(shaderProgram, index)
        }
    }

    fun bindPointLights(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("numPointLights", pointLights.size + mapPointLights.size)

        for ((index, light) in pointLights.withIndex()) {
            light.bind(shaderProgram, index)
        }
        for ((index, light) in mapPointLights.withIndex()) {
            light.bind(shaderProgram, index + pointLights.size)
        }
     }

    fun bindSpotLights(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("numSpotLights", spotLights.size)

        for ((index, light) in spotLights.withIndex()) {
            light.bind(shaderProgram,index)
        }
    }


}
