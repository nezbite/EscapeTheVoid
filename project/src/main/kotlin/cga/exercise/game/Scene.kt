package cga.exercise.game

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.*
import cga.exercise.components.map.MapManager
import cga.exercise.components.shader.ShaderProgram
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL20.*

class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")

//    private lateinit var pointLight: PointLight
//    private lateinit var spotLight: SpotLight


    private var dx: Double = 0.0
    private var dy: Double = 0.0
    private var yRotation: Double = Math.toRadians(-90.0)
    private var oldXpos: Double = 0.0
    private var oldYpos: Double = 0.0

    private var renderables = mutableListOf<Renderable>()

    private var tronCamera : TronCamera = TronCamera()


    private lateinit var player: Transformable
    private lateinit var backWheels: Transformable
    private lateinit var frontLeftWheelTurning: Transformable
    private lateinit var frontRightWheelTurning: Transformable
    private lateinit var frontLeftWheel: Transformable
    private lateinit var frontRightWheel: Transformable

    private var mapManager = MapManager()


    //scene setup
    init {
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        tronCamera.translate(Vector3f(0.0f, 0.0f, 4.0f))


        // Setting up Car and Components

        val carModel = ModelLoader.loadModel("assets/Car/Car.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val backWheelsModel = ModelLoader.loadModel("assets/Car/BackWheels.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontLeftWheelModel = ModelLoader.loadModel("assets/Car/FLWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontRightWheelModel = ModelLoader.loadModel("assets/Car/FRWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        if (carModel != null && backWheelsModel != null && frontLeftWheelModel != null && frontRightWheelModel != null) {
            carModel.scale(Vector3f(0.8f))
            backWheelsModel.parent = carModel
            var flWheelObject = Transformable()
            var frWheelObject = Transformable()
            flWheelObject.parent = carModel
            frWheelObject.parent = carModel
            frontLeftWheelModel.parent = flWheelObject
            frontRightWheelModel.parent = frWheelObject

            renderables.add(backWheelsModel)
            renderables.add(frontLeftWheelModel)
            renderables.add(frontRightWheelModel)
            renderables.add(carModel)

            player = carModel
            backWheels = backWheelsModel
            frontLeftWheelTurning = flWheelObject
            frontRightWheelTurning = frWheelObject
            frontLeftWheel = frontLeftWheelModel
            frontRightWheel = frontRightWheelModel

            player.rotate(0.0f, Math.toRadians(180.0).toFloat(), 0.0f)
        }


        // Setting up Map Manager

        val roadModel1 = ModelLoader.loadModel("assets/Environment/Road1.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val roadModel2 = ModelLoader.loadModel("assets/Environment/Road2.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val roadModel3 = ModelLoader.loadModel("assets/Environment/Road3.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        mapManager.roadModels.add(roadModel1!!)
        mapManager.roadModels.add(roadModel2!!)
        mapManager.roadModels.add(roadModel3!!)

        mapManager.init()


        // Background
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
    }

    fun render(dt: Float, t: Float) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        staticShader.use()

        // Bind camera
        tronCamera.bind(staticShader)

        // Render Renderables
        for (renderable in renderables) {
            renderable.render(staticShader)
        }

        // Render Map Segments
        for (segment in mapManager.segments) {
            segment.renderable.render(staticShader)
        }
    }

    var velocity = 0.0f
    var friction = 1.0f
    var maxSpeed = 50f
    var targetRotation = 0.0f


    val rotateMul = 0.5f*Math.PI.toFloat()
    var acceleratorState = 0.0f; // rollend

    fun update(dt: Float, t: Float) {

        accelerationInput(dt)
        steeringInput()

        // Velocity calculation
        velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)

        // Steering calculation
        val roll = targetRotation*dt * Math.min(velocity/5, 1f)
        player.rotate(0.0f, roll, 0.0f)
        player.translate(Vector3f(0.0f, 0.0f, -velocity*dt))

        // Effects
        tronCamera.fov = 80f + 20f * Math.min(velocity/maxSpeed, 1f)

        // Camera Orbit
        updateCameraOrbit()

        // Car Components
        updateWheelSpin(dt)


        // Map Manager
        mapManager.currentSegment = (player.getWorldPosition().z/3).toInt()
        mapManager.update()
    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

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
            rotateMul * (1-velocity/maxSpeed)
        } else if (window.getKeyState(GLFW_KEY_D)) {
            -rotateMul * (1-velocity/maxSpeed)
        } else {
            targetRotation * 0.95f
        }
    }

    private fun updateCameraOrbit() {
        // Set Camera Rotation
        tronCamera.setRotation(0f, (-yRotation + 1.5).toFloat(),0f)

        if (dx > 1) {
            dx = 0.0
        }

        // Calculate Camera Position
        yRotation += dx
        val cameraPosition = player.getWorldPosition().add(getOrbitVector(5.0f, yRotation))
        tronCamera.setPosition(cameraPosition)

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