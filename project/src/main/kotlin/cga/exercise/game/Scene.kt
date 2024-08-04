package cga.exercise.game

import SkyboxRenderer
import cga.exercise.components.camera.Camera
import cga.exercise.components.collision.BoxCollider
import cga.exercise.components.geometry.*
import cga.exercise.components.light.PointLight
import cga.exercise.components.map.MapManager
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.skybox.Skybox
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL20.*
import kotlin.math.abs

class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram = ShaderProgram("project/assets/shaders/tron_vert.glsl", "project/assets/shaders/tron_frag.glsl")
//    private val dissolveShader: ShaderProgram =
//        ShaderProgram("assets/shaders/car_vert_d.glsl", "assets/shaders/car_frag_d.glsl")

//    private lateinit var pointLight: PointLight
//    private lateinit var spotLight: SpotLight

    val GS_MENU = 0
    val GS_STARTING = 1
    val GS_GAME = 2

    private var gameState = GS_MENU


    private var dx: Double = 0.0
    private var dy: Double = 0.0
    private var yRotation: Double = Math.toRadians(-90.0)
    private var oldXpos: Double = 0.0
    private var oldYpos: Double = 0.0

    private var renderables = mutableListOf<Renderable>()
    private var pointLights = mutableListOf<PointLight>()
    private var carRenderables = mutableListOf<Renderable>()

    private var camera: Camera = Camera()
    private var cameraHolder: Transformable = Transformable()


    private lateinit var player: Renderable
    private lateinit var backWheels: Renderable
    private lateinit var frontLeftWheelTurning: Transformable
    private lateinit var frontRightWheelTurning: Transformable
    private lateinit var frontLeftWheel: Renderable
    private lateinit var frontRightWheel: Renderable

    private var mapManager = MapManager()

    private var carCollider = BoxCollider(1.35f, 3.6f)

    private var startTime = System.currentTimeMillis()

    private var shouldDissolve = false
    private var dissolveFactor = 0.0f

    val HIGHWAY_DIVIDER = 5.8f


    // UI
//    val CAMERA_HOLDER_START_POS = Vector3f(2.5f, 5f, -.2f)
//    val CAMERA_HOLDER_START_ROT = Vector3f(Math.toRadians(-90.0).toFloat(), 0.0f, 0.0f)
    val CAMERA_HOLDER_START_POS = Vector3f(2.4f, 2f, -.2f)
    val CAMERA_HOLDER_START_ROT = Vector3f(0f, Math.toRadians(90.0).toFloat(), 0f)
    val CAMERA_HOLDER_END_POS = Vector3f(0f, 4f, -5f)
    val CAMERA_HOLDER_END_ROT = Vector3f(0f, Math.toRadians(180.0).toFloat(), 0f)
    val CAMERA_START_ROT = Vector3f(-.5f, 0f, 0f)
    val CAMERA_END_ROT = Vector3f(0.0f, 0.0f, Math.toRadians(180.0).toFloat())

    private lateinit var uiTitle: Renderable

    // Debug
    private var renderCollisions = false
    private var debugColliders = mutableListOf<Renderable>()

    private lateinit var skybox: Skybox
    private lateinit var skyboxRenderer: SkyboxRenderer
    private var skyboxShaderProgram: ShaderProgram


    //scene setup
    init {

        skyboxShaderProgram = ShaderProgram("project/assets/shaders/skybox_vert.glsl","project/assets/shaders/skybox_frag.glsl")
        skybox = Skybox.createSkybox()

        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        camera.parent = cameraHolder
        cameraHolder.translate(CAMERA_HOLDER_START_POS)
        cameraHolder.rotate(CAMERA_HOLDER_START_ROT.x, CAMERA_HOLDER_START_ROT.y, CAMERA_HOLDER_START_ROT.z)
        camera.rotate(CAMERA_START_ROT.x, CAMERA_START_ROT.y, CAMERA_START_ROT.z)
        camera.fov = 60f

        // Setting up UI
        uiTitle = ModelLoader.loadModel("assets/UI/Title.obj", -.5f,  Math.toRadians(90.0).toFloat(), 0f)!!
        uiTitle.scale(Vector3f(.4f))
        uiTitle.translate(Vector3f(0f, 3.7f, 3f))

        // Setting up Car and Components


        val carModel = ModelLoader.loadModel("assets/Car/Car.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val backWheelsModel =
            ModelLoader.loadModel("assets/Car/BackWheels.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontLeftWheelModel =
            ModelLoader.loadModel("assets/Car/FLWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontRightWheelModel =
            ModelLoader.loadModel("assets/Car/FRWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)

        val testCube = ModelLoader.loadModel("assets/Environment/cube.obj", 0f, 0f, 0f)
        
        val cubeModel = ModelLoader.loadModel("project/assets/Environment/cube.obj", 0f, 0f, 0f)
        if (carModel != null && backWheelsModel != null && frontLeftWheelModel != null && frontRightWheelModel != null) {
            carModel.scale(Vector3f(0.8f))
            backWheelsModel.parent = carModel
            var flWheelObject = Transformable()
            var frWheelObject = Transformable()
            flWheelObject.parent = carModel
            frWheelObject.parent = carModel
            frontLeftWheelModel.parent = flWheelObject
            frontRightWheelModel.parent = frWheelObject

            testCube!!.scale(Vector3f(0.1f, 1f, 0.1f))

            carRenderables.add(backWheelsModel)
            carRenderables.add(frontLeftWheelModel)
            carRenderables.add(frontRightWheelModel)
            carRenderables.add(carModel)

            player = carModel
            backWheels = backWheelsModel
            frontLeftWheelTurning = flWheelObject
            frontRightWheelTurning = frWheelObject
            frontLeftWheel = frontLeftWheelModel
            frontRightWheel = frontRightWheelModel

            player.rotate(0.0f, Math.toRadians(180.0).toFloat(), 0.0f)

            // Debug
            cubeModel!!.scale(Vector3f(.1f, 1f, .1f))
            var cubeModelA = cubeModel.copy()
            var cubeModelB = cubeModel.copy()
            var cubeModelC = cubeModel.copy()
            var cubeModelD = cubeModel.copy()
            cubeModelA.scale(Vector3f(.1f, 1f, .1f))
            cubeModelB.scale(Vector3f(.1f, 1f, .1f))
            cubeModelC.scale(Vector3f(.1f, 1f, .1f))
            cubeModelD.scale(Vector3f(.1f, 1f, .1f))

            carCollider.renderables.addAll(mutableListOf(cubeModelA, cubeModelB, cubeModelC, cubeModelD))

            //debugColliders.add(cubeModel)
/*
            val cubeModel2 = cubeModel.copy()
            cubeModel2.scale(Vector3f(.1f, 1f, 100f))
            cubeModel2.setPosition(Vector3f(HIGHWAY_DIVIDER, 0f, 0f))

            debugColliders.add(cubeModel2)*/

        }


        // Setting up Map Manager
        val map_tunnelEntry = ModelLoader.loadModel("assets/Environment/MAP_TunnelEntry.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val map_tunnel = ModelLoader.loadModel("assets/Environment/MAP_Tunnel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val map_road1 = ModelLoader.loadModel("assets/Environment/MAP_Road1.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val map_road2 = ModelLoader.loadModel("assets/Environment/MAP_Road2.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val map_road3 = ModelLoader.loadModel("assets/Environment/MAP_Road3.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        mapManager.roadModels.add(map_tunnelEntry!!)
        mapManager.roadModels.add(map_tunnel!!)
        mapManager.roadModels.add(map_road1!!)
        mapManager.roadModels.add(map_road2!!)
        mapManager.roadModels.add(map_road3!!)

        mapManager.init()


        // Background
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
    }

    fun render(dt: Float, t: Float) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        staticShader.use()

        // Bind camera
        camera.bind(staticShader)


        skyboxShaderProgram.use()
        skybox.render()

        staticShader.use()

        // Render Renderables
        for (renderable in renderables) {
            renderable.render(staticShader)
        }

        for (light in pointLights) {
            light.bind(staticShader)
        }

        if (gameState == GS_MENU || gameState == GS_STARTING) {
            uiTitle.render(staticShader)
        }

        // Render Map Segments
        for (segment in mapManager.segments) {
            segment.renderable.render(staticShader)
        }

        if (renderCollisions) {
            for (collider in debugColliders) {
                collider.render(staticShader)
            }
            for (collider in carCollider.renderables) {
                collider.render(staticShader)
            }
        }

        for (renderable in carRenderables) {
            renderable.render(staticShader)
        }

        staticShader.cleanup()
//        dissolveShader.cleanup()
    }


    // Car Data
    var velocity = 0.0f
    var friction = 1.0f
    var maxSpeed = 50f
    var targetRotation = 0.0f


    val rotateMul = 0.5f * Math.PI.toFloat()
    var acceleratorState = 0.0f; // rollend

    var menuAnimTime = 0f

    fun update(dt: Float, t: Float) {

        if (dissolveFactor < 1f && shouldDissolve) {
            println(dissolveFactor)
            player.dissolveFactor = dissolveFactor
            backWheels.dissolveFactor = dissolveFactor
            frontLeftWheel.dissolveFactor = dissolveFactor
            frontRightWheel.dissolveFactor = dissolveFactor

            dissolveFactor += dt/2
        } else {
            accelerationInput(dt)
            steeringInput()
        }

        if (gameState == GS_MENU) {

            steeringInput()
            updateWheelSpin(dt)
            return
        } else if (gameState == GS_STARTING) {
            if (menuAnimTime > 1f) {
                gameState = GS_GAME
                cameraHolder.setRotation(0f, 0f, 0f)
            } else {
                val hpos = Vector3f(CAMERA_HOLDER_START_POS).lerp(player.getWorldPosition().add(CAMERA_HOLDER_END_POS), menuAnimTime)
                val fov = 60f + 28f * Math.min(menuAnimTime, 1f)

                camera.fov = fov

                cameraHolder.rotate(0f, Math.toRadians(90.0*dt).toFloat(), 0f)
                cameraHolder.setPosition(hpos)

                acceleratorState = menuAnimTime
                menuAnimTime += dt


                steeringInput()

                val roll = targetRotation * dt * Math.min(velocity / 5, 1f)
                player.rotate(0.0f, roll, 0.0f)

                velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)
                player.translate(Vector3f(0.0f, 0.0f, -velocity * dt))
                updateWheelSpin(dt)
            }
            return
        }

        // Velocity calculation
        velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)

        // Steering calculation
        val roll = targetRotation * dt * Math.min(velocity / 5, 1f)
        player.rotate(0.0f, roll, 0.0f)
        player.translate(Vector3f(0.0f, 0.0f, -velocity * dt))

        // Collisions
        updateCollisions()

        // Effects
        camera.fov = 80f + 20f * Math.min(velocity / maxSpeed, 1f)

        // Camera Orbit
//        updateCameraOrbit()
        updateCamera(dt)

        // Car Components
        updateWheelSpin(dt)


        // Map Manager
        mapManager.currentSegment = (player.getWorldPosition().z/mapManager.SEGMENT_SIZE).toInt()
        mapManager.update()

    }



    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {
        if (key == GLFW_KEY_C && action == GLFW_PRESS) {
            renderCollisions = !renderCollisions
        }
        if (key == GLFW_KEY_R && action == GLFW_PRESS) {
           shouldDissolve = !shouldDissolve
        }
        if (key == GLFW_KEY_W && action == GLFW_PRESS && gameState == GS_MENU) {
            gameState = GS_STARTING
        }
    }

    fun onMouseMove(xpos: Double, ypos: Double) {
        // Calculate the difference between the old and new mouse positions
        dx = (xpos - oldXpos) * 0.002
        dy = (ypos - oldYpos) * 0.002

        // Update the old mouse positions
        oldXpos = xpos
        oldYpos = ypos
    }

    fun cleanup() {}


    // Update functions

    private fun accelerationInput(dt: Float) {
        acceleratorState = if (window.getKeyState(GLFW_KEY_W)) {
            0.8f
        } else if (window.getKeyState(GLFW_KEY_S)) {
            -0.25f
        } else {
            0f
        }
    }

    private fun steeringInput() {
        targetRotation = if (window.getKeyState(GLFW_KEY_A)) {
            rotateMul * (1 - velocity*.5f / maxSpeed)
        } else if (window.getKeyState(GLFW_KEY_D)) {
            -rotateMul * (1 - velocity*.5f / maxSpeed)
        } else {
            targetRotation * 0.95f
        }
    }

    private fun updateCollisions() {
        // Calculate Player Collider
        carCollider.updateBounds(player.getWorldPosition(), player.getRotation())

        // Highway divider collision
        if (carCollider.checkZAxisCollision(HIGHWAY_DIVIDER)) {
            val minDistance = HIGHWAY_DIVIDER-.75f-abs(player.getWorldRotation().y)
            if (player.getWorldPosition().x > minDistance) {
                player.setPosition(Vector3f(minDistance, player.getWorldPosition().y, player.getWorldPosition().z))
            }
            if (player.getWorldRotation().y > 0 || targetRotation < 0) {
                targetRotation = -1f
                player.translate(Vector3f(0.1f, 0.0f, 0.0f))
                return
            }
            if (player.getWorldRotation().y < .25f && player.getWorldRotation().y > -.25f) {
                velocity = velocity*.9f
                targetRotation = -3f
            } else {
                velocity = -velocity * .8f
                shouldDissolve = true
            }
        }

    }

    var cameraAngle = 0.0f

    private fun updateCamera(dt: Float) {
        cameraAngle = lerp(targetRotation*.1f, cameraAngle, dt)
        camera.setRotation(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)
        camera.setPosition(player.getWorldPosition().add(Vector3f(0f, 4f, -5f)))
    }

    private fun updateCameraOrbit() {
        // Set Camera Rotation
        camera.setRotation(0f, (-yRotation + 1.5).toFloat(),0f)

        if (dx > 1) {
            dx = 0.0
        }

        // Calculate Camera Position
        yRotation += dx
        val cameraPosition = player.getWorldPosition().add(getOrbitVector(5.0f, yRotation))
        camera.setPosition(cameraPosition)

        dx=0.0
        dy=0.0

        if (targetRotation < 0.01f && targetRotation > -0.01f) {
            targetRotation = 0.0f
        }
    }

    private fun updateWheelSpin(dt: Float) {
        // Calculate steering rotation of front wheels
        val fwRot = targetRotation*.5f

        // Update front wheel angle
        frontLeftWheel.rotateAroundPoint(-velocity*dt*5, 0.0f, 0.0f, Vector3f(0.0f, 0.35f, -1.32f))
        frontLeftWheelTurning.setRotationAroundPoint(0.0f, lerp(fwRot, frontLeftWheelTurning.getRotation().y, dt*5), 0.0f, Vector3f(-0.8f, 0.35f, -1.32f))

        frontRightWheel.rotateAroundPoint(-velocity*dt*5, 0.0f, 0.0f, Vector3f(0.8f, 0.35f, -1.32f))
        frontRightWheelTurning.setRotationAroundPoint(0.0f, lerp(fwRot, frontRightWheelTurning.getRotation().y, dt*5), 0.0f, Vector3f(0.8f, 0.35f, -1.32f))

        // Update wheel spin
        backWheels.rotateAroundPoint(-velocity*dt*5, 0.0f, 0.0f, Vector3f(0.0f, 0.35f, 1.32f))
    }


    // Helper Functions

    private fun getOrbitVector(orbitRadius: Float, angle: Double): Vector3f {
        return Vector3f(orbitRadius * Math.cos(angle).toFloat(), 1.5f, orbitRadius * Math.sin(angle).toFloat())
    }

    private fun lerp(target: Float, input: Float, speed: Float): Float {
        return input + ((target - input) * speed)
    }
}