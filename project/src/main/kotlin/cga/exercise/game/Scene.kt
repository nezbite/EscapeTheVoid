package cga.exercise.game

import cga.exercise.components.camera.Camera
import cga.exercise.components.collision.BoxCollider
import cga.exercise.components.geometry.*
import cga.exercise.components.light.PointLight
import cga.exercise.components.map.MapManager
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.skybox.Skybox
import cga.exercise.components.skybox.SkyboxRenderer
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL20.*
import kotlin.math.abs
import kotlin.random.Random

class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram = ShaderProgram("assets/shaders/main_vert.glsl", "assets/shaders/main_frag.glsl")
//    private val dissolveShader: ShaderProgram =
//        ShaderProgram("assets/shaders/car_vert_d.glsl", "assets/shaders/car_frag_d.glsl")

//    private lateinit var pointLight: PointLight
//    private lateinit var spotLight: SpotLight

    val GS_MENU = 0
    val GS_STARTING = 1
    val GS_GAME = 2
    val GS_GAMEOVER = 3

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

    private var backView = false


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

    private lateinit var void: Renderable
    private var voidSpeed = 30f


    // UI
    val CAMERA_HOLDER_START_POS = Vector3f(2.4f, 2f, -.2f)
    val CAMERA_HOLDER_START_ROT = Vector3f(0f, Math.toRadians(90.0).toFloat(), 0f)
    val CAMERA_HOLDER_END_POS = Vector3f(0f, 4f, -5f)
    val CAMERA_START_ROT = Vector3f(-.5f, 0f, 0f)

    private var uiTitle: Renderable

    private var uiScore: Transformable
    private var uiDigit1: Renderable
    private var uiDigit2: Renderable
    private var uiDigit3: Renderable
    private var uiDigit4: Renderable

    private var ui0: Renderable
    private var ui1: Renderable
    private var ui2: Renderable
    private var ui3: Renderable
    private var ui4: Renderable
    private var ui5: Renderable
    private var ui6: Renderable
    private var ui7: Renderable
    private var ui8: Renderable
    private var ui9: Renderable

    private var uiGameOver: Renderable

    // Debug
    private var renderCollisions = false
    private var debugColliders = mutableListOf<Renderable>()

    private var skybox: Skybox
    private var skyboxRenderer: SkyboxRenderer
    private var skyboxShaderProgram: ShaderProgram

    //scene setup
    init {

        skyboxShaderProgram = ShaderProgram("assets/shaders/skybox_vert.glsl","assets/shaders/skybox_frag.glsl")
        skybox = Skybox.createSkybox()
        skyboxRenderer = SkyboxRenderer(skyboxShaderProgram)

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

        val uiNumberPitch = -.5f
        val uiNumberYaw = Math.toRadians(180.0).toFloat()
        val uiNumberRoll = 0f
        ui0 = ModelLoader.loadModel("assets/UI/0.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui1 = ModelLoader.loadModel("assets/UI/1.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui2 = ModelLoader.loadModel("assets/UI/2.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui3 = ModelLoader.loadModel("assets/UI/3.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui4 = ModelLoader.loadModel("assets/UI/4.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui5 = ModelLoader.loadModel("assets/UI/5.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui6 = ModelLoader.loadModel("assets/UI/6.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui7 = ModelLoader.loadModel("assets/UI/7.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui8 = ModelLoader.loadModel("assets/UI/8.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        ui9 = ModelLoader.loadModel("assets/UI/9.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!

        uiScore = Transformable()
        uiDigit1 = ui0.copy()
        uiDigit2 = ui0.copy()
        uiDigit3 = ui0.copy()
        uiDigit4 = ui0.copy()

        uiDigit1.parent = uiScore
        uiDigit2.parent = uiScore
        uiDigit3.parent = uiScore
        uiDigit4.parent = uiScore
        uiDigit2.translate(Vector3f(-.5f, 0f, 0f))
        uiDigit3.translate(Vector3f(-1f, 0f, 0f))
        uiDigit4.translate(Vector3f(-1.5f, 0f, 0f))

        uiScore.scale(Vector3f(.1f))


        uiGameOver = ModelLoader.loadModel("assets/UI/GameOver.obj", uiNumberPitch, uiNumberYaw, uiNumberRoll)!!
        uiGameOver.scale(Vector3f(.2f))




        // Setting up Car and Components


        val carModel = ModelLoader.loadModel("assets/Car/Car.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val backWheelsModel =
            ModelLoader.loadModel("assets/Car/BackWheels.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontLeftWheelModel =
            ModelLoader.loadModel("assets/Car/FLWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontRightWheelModel =
            ModelLoader.loadModel("assets/Car/FRWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)

        val voidModel = ModelLoader.loadModel("assets/Environment/Void.obj", 0f, 0f, 0f)

        void = voidModel!!
        void.translate(Vector3f(0f, 0f, -100f))
        void.scale(Vector3f(10f, 1f, 1f))
        renderables.add(void)

        val testCube = ModelLoader.loadModel("assets/Environment/cube.obj", 0f, 0f, 0f)
        
        val cubeModel = ModelLoader.loadModel("assets/Environment/cube.obj", 0f, 0f, 0f)
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

        val map_far1 = ModelLoader.loadModel("assets/Environment/Far1.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val map_far2 = ModelLoader.loadModel("assets/Environment/Far2.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)

        mapManager.farModels.add(map_far1!!)
        mapManager.farModels.add(map_far2!!)

        mapManager.init(Random.nextInt())


        // Background
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
    }

    fun render(dt: Float, t: Float) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glDepthFunc(GL_LEQUAL)
        glDepthMask(false)
        camera.bind(skyboxShaderProgram)
        skyboxRenderer.render(skybox, camera)
        glDepthMask(true)
        glDepthFunc(GL_LESS)
        
        staticShader.use()

        // Bind camera
        camera.bind(staticShader)

        // Render Renderables
        for (renderable in renderables) {
            renderable.render(staticShader)
        }

        for (light in pointLights) {
            light.bind(staticShader)
        }

        if (gameState == GS_MENU || gameState == GS_STARTING) {
            uiTitle.render(staticShader)
            uiDigit1.render(staticShader)
            uiDigit2.render(staticShader)
            uiDigit3.render(staticShader)
            uiDigit4.render(staticShader)
        } else if (gameState == GS_GAMEOVER) {
            uiGameOver.render(staticShader)
            uiDigit1.render(staticShader)
            uiDigit2.render(staticShader)
            uiDigit3.render(staticShader)
            uiDigit4.render(staticShader)
        } else {
            uiDigit1.render(staticShader)
            uiDigit2.render(staticShader)
            uiDigit3.render(staticShader)
            uiDigit4.render(staticShader)
        }

        // Render Map Segments
        for (segment in mapManager.segments) {
            segment.renderable.render(staticShader)
        }

        for (segment in mapManager.farSegments) {
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

        /*staticShader.cleanup()
        dissolveShader.cleanup()
        skyboxShaderProgram.cleanup()*/
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

        if (gameState == GS_GAMEOVER) {
            acceleratorState = 0f
            val roll = targetRotation * dt * Math.min(velocity / 5, 1f)
            player.rotate(0.0f, roll, 0.0f)

            velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)
            player.translate(Vector3f(0.0f, 0.0f, -velocity * dt))
            updateWheelSpin(dt)
            return
        }

        // Velocity calculation
        velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)

        // Steering calculation
        val roll = targetRotation * dt * Math.min(velocity / 5, 1f)
        player.rotate(0.0f, roll, 0.0f)
        player.translate(Vector3f(0.0f, 0.0f, -velocity * dt))

        // Effects
        camera.fov = 80f + 20f * Math.min(abs(velocity) / maxSpeed, 1f)

        // Show Score
        updateScore()

        // Camera Orbit
//        updateCameraOrbit()
        updateCamera(dt)

        // Car Components
        updateWheelSpin(dt)


        // Collisions
        updateCollisions()

        // Void
        moveVoid(dt)


        // Map Manager
        mapManager.currentSegment = (player.getWorldPosition().z/mapManager.SEGMENT_SIZE).toInt()-4
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
        if (key == GLFW_KEY_ENTER && action == GLFW_PRESS && gameState == GS_GAMEOVER) {
            resetScene()
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

    private fun resetScene() {
        player.setPosition(Vector3f(0f, 0f, 0f))
        player.setRotation(0f, Math.toRadians(180.0).toFloat(), 0f)
        player.scale(Vector3f(0.8f))
        velocity = 0f
        targetRotation = 0f
        cameraHolder.setRotation(CAMERA_HOLDER_START_ROT.x, CAMERA_HOLDER_START_ROT.y, CAMERA_HOLDER_START_ROT.z)
        cameraHolder.setPosition(CAMERA_HOLDER_START_POS)
        camera.setRotation(CAMERA_START_ROT.x, CAMERA_START_ROT.y, CAMERA_START_ROT.z)
        camera.fov = 60f
        menuAnimTime = 0f
        gameState = GS_MENU

        shouldDissolve = false
        dissolveFactor = 0f
        player.dissolveFactor = 0f
        backWheels.dissolveFactor = 0f
        frontLeftWheel.dissolveFactor = 0f
        frontRightWheel.dissolveFactor = 0f

        cameraOffset = Vector3f(0f, 4f, -5f)
        cameraRotOffset = Vector3f(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)


        mapManager.currentSegment = 0
        mapManager.init(Random.nextInt())
    }


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
                gameOver()
            }
        }

    }

    var cameraAngle = 0.0f

    var cameraOffset = Vector3f(0f, 4f, -5f)
    var cameraRotOffset = Vector3f(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)

    private fun updateCamera(dt: Float) {
        val cameraPos = if (window.getKeyState(GLFW_KEY_E)) {
            Vector3f(0f, 4f, 5f)
        } else {
            Vector3f(0f, 4f, -5f)
        }
        val cameraRot = if (window.getKeyState(GLFW_KEY_E)) {
            Vector3f(-.9f, 0f, 0f)
        } else {
            Vector3f(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)
        }
        cameraOffset = cameraOffset.lerp(cameraPos, dt*4)
        cameraRotOffset = cameraRotOffset.lerp(cameraRot, dt*8)
        cameraAngle = lerp(targetRotation*.1f, cameraAngle, dt)
        camera.setRotation(cameraRotOffset.x, cameraRotOffset.y, cameraRotOffset.z)
        camera.setPosition(player.getWorldPosition().add(cameraOffset))
        uiScore.setRotation(0f, 0f, -cameraAngle)
        uiScore.scale(Vector3f(.1f*camera.fov/60f))
        val lowFovPos = Vector3f(.15f, 4.13f, -4f)
        val highFovPos = Vector3f(.15f, 4.3f, -4f)
        val desiredPos = if (gameState == GS_GAMEOVER) {
            lowFovPos.lerp(highFovPos, (camera.fov-80f)/20).add(Vector3f(0f, -1f, 0f))
        } else {
            lowFovPos.lerp(highFovPos, (camera.fov-80f)/20)
        }
        uiScore.setPosition(player.getWorldPosition().add(desiredPos))

        uiGameOver.setRotation(0f, 0f, -cameraAngle)
        uiGameOver.scale(Vector3f(.2f))
        uiGameOver.setPosition(player.getWorldPosition().add(Vector3f(.45f, 3.8f, -4.5f)))
    }

    private fun updateScore() {
        val score = getScore()
        val digit1: Int = score / 1000
        val digit2: Int = (score % 1000) / 100
        val digit3: Int = (score % 100) / 10
        val digit4: Int = score % 10


        uiDigit1.meshes = getNumberModel(digit1).meshes
        uiDigit2.meshes = getNumberModel(digit2).meshes
        uiDigit3.meshes = getNumberModel(digit3).meshes
        uiDigit4.meshes = getNumberModel(digit4).meshes
    }

    private fun gameOver() {
        shouldDissolve = true
        gameState = GS_GAMEOVER
        camera.setRotation(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)
        camera.setPosition(player.getWorldPosition().add(Vector3f(0f, 4f, -5f)))
    }

    private fun getScore(): Int {
        return player.getWorldPosition().z.toInt()/10
    }

    private fun getNumberModel(number: Int): Renderable {
        return when (number) {
            0 -> ui0
            1 -> ui1
            2 -> ui2
            3 -> ui3
            4 -> ui4
            5 -> ui5
            6 -> ui6
            7 -> ui7
            8 -> ui8
            9 -> ui9
            else -> ui0
        }
    }

    private fun moveVoid(dt: Float) {
        void.translate(Vector3f(0f, 0f, dt*voidSpeed))
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