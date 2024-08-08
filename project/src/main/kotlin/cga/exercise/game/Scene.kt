package cga.exercise.game

import cga.exercise.components.blur.BlurEffect
import cga.exercise.components.blur.Framebuffer
import cga.exercise.components.blur.FullScreenQuad
import cga.exercise.components.camera.Camera
import cga.exercise.components.collision.BoxCollider
import cga.exercise.components.geometry.*
import cga.exercise.components.light.DirectionalLight
import cga.exercise.components.light.LightManager
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.map.MapManager
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.skybox.Skybox
import cga.exercise.components.skybox.SkyboxRenderer
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import org.joml.Math.clamp
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL20.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram = ShaderProgram("assets/shaders/main_vert.glsl", "assets/shaders/main_frag.glsl")
    private val uiShader: ShaderProgram = ShaderProgram("assets/shaders/ui_vert.glsl", "assets/shaders/ui_frag.glsl")
    private val ui3dShader: ShaderProgram = ShaderProgram("assets/shaders/ui3d_vert.glsl", "assets/shaders/ui3d_frag.glsl")
    private val edgeDetectionShader: ShaderProgram = ShaderProgram("assets/shaders/outline_vert.glsl", "assets/shaders/outline_frag.glsl")
    private val compositeShader: ShaderProgram = ShaderProgram("assets/shaders/composite_vert.glsl", "assets/shaders/composite_frag.glsl")

    val lightManager = LightManager()

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
    private var carRenderables = mutableListOf<Renderable>()

    private var camera: Camera = Camera()
    private var cameraHolder: Transformable = Transformable()


    private lateinit var player: Renderable
    private lateinit var backWheels: Renderable
    private lateinit var frontLeftWheelTurning: Transformable
    private lateinit var frontRightWheelTurning: Transformable
    private lateinit var frontLeftWheel: Renderable
    private lateinit var frontRightWheel: Renderable
    private var leftHeadlight: SpotLight
    private var rightHeadlight: SpotLight

    private var mapManager = MapManager()

    private var carCollider = BoxCollider(1.35f, 3.6f)

    private var shouldDissolve = false
    private var dissolveFactor = 0.0f

    val HIGHWAY_DIVIDER = 5.8f
    val FIELD_DIVIDER = -6f
    val FIELD_TRANSITION = 1f
    val RIGHT_LIMIT = 8f

    private var void: Renderable
    private var voidSpeed = 3f
    private var voidDistance = 100f


    // UI
    val CAMERA_HOLDER_START_POS = Vector3f(2.4f, 2f, -.2f)
    val CAMERA_HOLDER_START_ROT = Vector3f(0f, Math.toRadians(90.0).toFloat(), 0f)
    val CAMERA_HOLDER_END_POS = Vector3f(0f, 4f, -5f)
    val CAMERA_START_ROT = Vector3f(-.5f, 0f, 0f)

    val UI_SCORE_START = Vector3f(-.25f, 1.2f, 0f)
    val UI_SCORE_PLAY = Vector3f(-.25f, .83f, 0f)

    val UI_CREDITS_START = Vector3f(1.74f, -.82f, 0f)
    val UI_CREDITS_PLAY = Vector3f(2.5f, -.82f, 0f)

    val UI_CONTROLS_START = Vector3f(-1.62f, -.82f, 0f)
    val UI_CONTROLS_PLAY = Vector3f(-2.5f, -.82f, 0f)

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

    private var uiControls: Renderable
    private var uiCredits: Renderable

    // Debug
    private var renderCollisions = false
    private var debugColliders = mutableListOf<Renderable>()
    private var collisionDebugCubes = mutableListOf<Renderable>()
    private var tunnelCollisionDebugCubes = mutableListOf<Renderable>()

    // Skybox
    private var skybox: Skybox
    private var skyboxRenderer: SkyboxRenderer
    private var skyboxShaderProgram: ShaderProgram

    // Blur
    private val framebuffer: Framebuffer
    private val horizontalFramebuffer: Framebuffer
    private val verticalFramebuffer: Framebuffer
    private val blurEffectH: BlurEffect
    private val blurEffectV: BlurEffect
    private var horizontalBlurShader: ShaderProgram
    private var verticalBlurShader: ShaderProgram
    private var textureShader: ShaderProgram
    private var fullScreenQuad: FullScreenQuad

    // Edge Detection
    private val edgeDetectionFramebuffer: Framebuffer


    //scene setup
    init {

        // Add lights
        lightManager.addDirectionalLight(DirectionalLight(Vector3f(1.0f, 1.0f, -1.0f), Vector3f(1.5f, 3.0f, 4.0f), 0.1f))
        lightManager.addPointLight(PointLight(Vector3f(3.0f),Vector3f(3.0f,1.0f,1.0f)))

        // Add Skybox
        skyboxShaderProgram = ShaderProgram("assets/shaders/skybox_vert.glsl","assets/shaders/skybox_frag.glsl")
        skybox = Skybox.createSkybox()
        skyboxRenderer = SkyboxRenderer(skyboxShaderProgram)

        // Add Blur
        horizontalBlurShader = ShaderProgram("assets/shaders/blur_vert.glsl","assets/shaders/horizontal_blur_frag.glsl")
        verticalBlurShader = ShaderProgram("assets/shaders/blur_vert.glsl","assets/shaders/vertical_blur_frag.glsl")
        framebuffer = Framebuffer(window.windowWidth,window.windowHeight)
        horizontalFramebuffer = Framebuffer(window.windowWidth,window.windowHeight)
        verticalFramebuffer = Framebuffer(window.windowWidth,window.windowHeight)
        textureShader = ShaderProgram("assets/shaders/simple_texture_vert.glsl","assets/shaders/simple_texture_frag.glsl")
        fullScreenQuad = FullScreenQuad()
        blurEffectH = BlurEffect(horizontalBlurShader,fullScreenQuad)
        blurEffectV = BlurEffect(verticalBlurShader,fullScreenQuad)

        setBlur(0f)

        // Add Edge Detection
        edgeDetectionFramebuffer = Framebuffer(window.windowWidth,window.windowHeight)

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

        ui0 = ModelLoader.loadModel("assets/UI/0.obj", 0f, 0f, 0f)!!
        ui1 = ModelLoader.loadModel("assets/UI/1.obj", 0f, 0f, 0f)!!
        ui2 = ModelLoader.loadModel("assets/UI/2.obj", 0f, 0f, 0f)!!
        ui3 = ModelLoader.loadModel("assets/UI/3.obj", 0f, 0f, 0f)!!
        ui4 = ModelLoader.loadModel("assets/UI/4.obj", 0f, 0f, 0f)!!
        ui5 = ModelLoader.loadModel("assets/UI/5.obj", 0f, 0f, 0f)!!
        ui6 = ModelLoader.loadModel("assets/UI/6.obj", 0f, 0f, 0f)!!
        ui7 = ModelLoader.loadModel("assets/UI/7.obj", 0f, 0f, 0f)!!
        ui8 = ModelLoader.loadModel("assets/UI/8.obj", 0f, 0f, 0f)!!
        ui9 = ModelLoader.loadModel("assets/UI/9.obj", 0f, 0f, 0f)!!

        uiScore = Transformable()
        uiDigit1 = ui0.copy()
        uiDigit2 = ui0.copy()
        uiDigit3 = ui0.copy()
        uiDigit4 = ui0.copy()

        uiDigit1.parent = uiScore
        uiDigit2.parent = uiScore
        uiDigit3.parent = uiScore
        uiDigit4.parent = uiScore
        uiDigit2.translate(Vector3f(.5f, 0f, 0f))
        uiDigit3.translate(Vector3f(1f, 0f, 0f))
        uiDigit4.translate(Vector3f(1.5f, 0f, 0f))

        uiScore.setPosition(UI_SCORE_START)
        uiScore.scale(Vector3f(.25f))

        uiControls = ModelLoader.loadModel("assets/UI/Controls.obj", 0f, 0f, 0f)!!
        uiControls.setPosition(UI_CONTROLS_START)
        uiControls.scale(Vector3f(.065f))

        uiCredits = ModelLoader.loadModel("assets/UI/Credits.obj", 0f, 0f, 0f)!!
        uiCredits.setPosition(UI_CREDITS_START)
        uiCredits.scale(Vector3f(.065f))


        uiGameOver = ModelLoader.loadModel("assets/UI/GameOver.obj", 0f, 0f, 0f)!!
        uiGameOver.scale(Vector3f(.4f))
        uiGameOver.translate(Vector3f(-2.2f, .8f, 0f))


        // Setting up the Void
        val voidModel = ModelLoader.loadModel("assets/Environment/Void.obj", 0f, 0f, 0f)

        void = voidModel!!
        void.translate(Vector3f(0f, 0f, -200f))
        void.scale(Vector3f(10f, .8f, 10f))

        // Setting up Car and Components
        val carModel =
            ModelLoader.loadModel("assets/Car/Car.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val backWheelsModel =
            ModelLoader.loadModel("assets/Car/BackWheels.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontLeftWheelModel =
            ModelLoader.loadModel("assets/Car/FLWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val frontRightWheelModel =
            ModelLoader.loadModel("assets/Car/FRWheel.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)

        val testCube = ModelLoader.loadModel("assets/Environment/cube.obj", 0f, 0f, 0f)
        val obstacleCube = ModelLoader.loadModel("assets/Environment/cube.obj", 0f, 0f, 0f)

        collisionDebugCubes.add(obstacleCube!!.copy())
        collisionDebugCubes.add(obstacleCube.copy())
        collisionDebugCubes.add(obstacleCube.copy())
        collisionDebugCubes.add(obstacleCube.copy())
        for (cube in collisionDebugCubes) {
            cube.scale(Vector3f(.2f, 2f, .2f))
        }

        tunnelCollisionDebugCubes.add(testCube!!.copy())
        tunnelCollisionDebugCubes.add(testCube.copy())
        tunnelCollisionDebugCubes.add(testCube.copy())
        tunnelCollisionDebugCubes.add(testCube.copy())
        for (cube in tunnelCollisionDebugCubes) {
            cube.scale(Vector3f(.2f, 5f, .2f))
        }
        
        val cubeModel = ModelLoader.loadModel("assets/Environment/cube.obj", 0f, 0f, 0f)
        if (carModel != null && backWheelsModel != null && frontLeftWheelModel != null && frontRightWheelModel != null) {
            carModel.scale(Vector3f(0.8f))
            backWheelsModel.parent = carModel
            val flWheelObject = Transformable()
            val frWheelObject = Transformable()
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
            for (light in lightManager.spotLights) light.rotate(0.0f,Math.toRadians(180.0).toFloat(),0.0f)

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
        }

        // Add Headlights
        leftHeadlight = SpotLight(Vector3f(-.6f, 0.7f, -1.2f), Vector3f(0f, .2f, 0f), Vector3f(4f), 1f, 25f)
        rightHeadlight = SpotLight(Vector3f(.6f, 0.7f, -1.2f), Vector3f(0f, .2f, 0f), Vector3f(4f), 1f, 25f)

        lightManager.addSpotLight(leftHeadlight)
        lightManager.addSpotLight(rightHeadlight)


        leftHeadlight.parent = player
        rightHeadlight.parent = player


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

        val map_obstacle_1 = ModelLoader.loadModel("assets/Environment/Obstacle1.obj", 0f, 0f, 0f)
        val map_obstacle_2 = ModelLoader.loadModel("assets/Environment/Obstacle2.obj", 0f, 0f, 0f)
        val map_obstacle_3 = ModelLoader.loadModel("assets/Environment/Obstacle3.obj", 0f, 0f, 0f)

        mapManager.obstacleModels.add(map_obstacle_1!!)
        mapManager.obstacleModels.add(map_obstacle_2!!)
        mapManager.obstacleModels.add(map_obstacle_3!!)

        mapManager.init(Random.nextInt())


        // Background
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()

    }

    fun render(dt: Float, t: Float) {
        // Render Scene to the framebuffer
        framebuffer.bind()
        glViewport(0, 0, window.windowWidth, window.windowHeight)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Skybox
        glDepthFunc(GL_LEQUAL)
        glDepthMask(false)
        camera.bind(skyboxShaderProgram)
        skyboxRenderer.render(skybox, camera)
        glDepthMask(true)
        glDepthFunc(GL_LESS)

        // Draw using Toon Shader
        staticShader.use()
        lightManager.bindPointLights(staticShader)
        lightManager.bindSpotLights(staticShader)
        camera.bind(staticShader)
        lightManager.bindDirectionalLights(staticShader)

        for (renderable in renderables) {
            renderable.render(staticShader)
        }

        for (segment in mapManager.segments) {
            segment.renderable.render(staticShader)
        }

        for (segment in mapManager.farSegments) {
            segment.renderable.render(staticShader)
        }

        for (obstacle in mapManager.obstacles) {
            obstacle.renderable.render(staticShader)
        }

        if (renderCollisions) {
            for (collider in debugColliders) {
                collider.render(staticShader)
            }
            for (collider in carCollider.renderables) {
                collider.render(staticShader)
            }
            for (cube in collisionDebugCubes) {
                cube.render(staticShader)
            }
            for (cube in tunnelCollisionDebugCubes) {
                cube.render(staticShader)
            }
        }

        for (renderable in carRenderables) {
            renderable.render(staticShader)
        }

        framebuffer.unbind()

        // Apply horizontal blur
        horizontalFramebuffer.bind()
        glViewport(0, 0, window.windowWidth, window.windowHeight)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        horizontalBlurShader.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, framebuffer.textureID)
        horizontalBlurShader.setUniform("screenTexture", 0)
        blurEffectH.bind()
        fullScreenQuad.render()

        horizontalFramebuffer.unbind()

        // Apply vertical blur
        verticalFramebuffer.bind()
        glViewport(0, 0, window.windowWidth, window.windowHeight)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        verticalBlurShader.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, horizontalFramebuffer.textureID)
        verticalBlurShader.setUniform("screenTexture", 0)
        blurEffectV.bind()
        fullScreenQuad.render()

        verticalFramebuffer.unbind()

        // Apply edge detection
        edgeDetectionFramebuffer.bind()
        glViewport(0, 0, window.windowWidth, window.windowHeight)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        edgeDetectionShader.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, verticalFramebuffer.textureID)
        edgeDetectionShader.setUniform("screenTexture", 0)
        edgeDetectionShader.setUniform("texSize", Vector2f(window.windowWidth.toFloat(), window.windowHeight.toFloat()))
        fullScreenQuad.render()

        edgeDetectionFramebuffer.unbind()

        // Composite edges with the scene
        glViewport(0, 0, window.windowWidth, window.windowHeight)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Use a shader that combines the edge detection with the blurred scene
        compositeShader.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, verticalFramebuffer.textureID) // The blurred scene
        compositeShader.setUniform("sceneTexture", 0)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, edgeDetectionFramebuffer.textureID) // The edge detection result
        compositeShader.setUniform("edgeTexture", 1)
        fullScreenQuad.render()

        // Render UI
        glViewport(0, 0, window.windowWidth, window.windowHeight)
        glClear(GL_DEPTH_BUFFER_BIT)

        ui3dShader.use()
        camera.bind(ui3dShader)

        void.render(ui3dShader)

        glClear(GL_DEPTH_BUFFER_BIT)
        if (gameState == GS_MENU || gameState == GS_STARTING) {
            uiTitle.render(ui3dShader)
        }

        renderUI()
    }


    private fun renderUI() {
        uiShader.use()
        camera.bind(uiShader)

        uiDigit1.render(uiShader)
        uiDigit2.render(uiShader)
        uiDigit3.render(uiShader)
        uiDigit4.render(uiShader)
        if (gameState == GS_GAMEOVER) {
            uiGameOver.render(uiShader)
        }
        uiControls.render(uiShader)
        uiCredits.render(uiShader)
    }


    // Car Data
    private var velocity = 0.0f
    private var friction = 1.0f
    private var maxSpeed = 50f
    private var MAX_SPEED = 50f
    private var MAX_SPEED_GRASS = .1f
    private var targetRotation = 0.0f


    private val rotateMul = 0.5f * Math.PI.toFloat()
    private var acceleratorState = 0.0f; // rollend

    private var menuAnimTime = 0f
    private var voidAnimTime = 0f

    fun update(dt: Float, t: Float) {
        updateDebug()

        updateDissolve(dt)

        val fieldDistance = player.getWorldPosition().x - FIELD_DIVIDER
        val baseMaxSpeed = MAX_SPEED + 60f * getScore()/1000f
        if (fieldDistance > FIELD_TRANSITION) {
            maxSpeed = baseMaxSpeed
        } else {
            maxSpeed = lerp(MAX_SPEED_GRASS, baseMaxSpeed, clamp(0f, 1f,-fieldDistance / RIGHT_LIMIT))
        }

        if (player.getWorldPosition().x < -10f) {
            velocity = lerp(0f, velocity, clamp(0f, 1f, -fieldDistance / 800f))
        }

        println(velocity)

        when (gameState) {
            // Main Menu
            GS_MENU -> {
                steeringInput()
                updateWheelSpin(dt)
                return
            }

            // Transition between Menu and Gameplay
            GS_STARTING -> {
                if (menuAnimTime > 1f) {
                    gameState = GS_GAME
                    uiScore.setPosition(UI_SCORE_PLAY)
                    uiCredits.setPosition(UI_CREDITS_PLAY)
                    uiControls.setPosition(UI_CONTROLS_PLAY)
                } else {
                    val hpos = Vector3f(CAMERA_HOLDER_START_POS).lerp(
                        player.getWorldPosition().add(CAMERA_HOLDER_END_POS),
                        menuAnimTime
                    )
                    val fov = 60f + 28f * Math.min(menuAnimTime, 1f)

                    camera.fov = fov

                    cameraHolder.rotate(0f, Math.toRadians(90.0 * dt).toFloat(), 0f)
                    cameraHolder.setPosition(hpos)

                    uiScore.setPosition(lerp(UI_SCORE_PLAY, UI_SCORE_START, menuAnimTime))
                    uiCredits.setPosition(lerp(UI_CREDITS_PLAY, UI_CREDITS_START, menuAnimTime))
                    uiControls.setPosition(lerp(UI_CONTROLS_PLAY, UI_CONTROLS_START, menuAnimTime))

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

            // Game Over Screen
            GS_GAMEOVER -> {
                acceleratorState = 0f
                val roll = targetRotation * dt * Math.min(velocity / 5, 1f)
                player.rotate(0.0f, roll, 0.0f)

                velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)
                player.translate(Vector3f(0.0f, 0.0f, -velocity * dt))
                updateWheelSpin(dt)

                if (voidAnimTime < 1f) {
                    voidAnimTime += dt
                    void.translate(Vector3f(0f, 0f, voidSpeed * dt*1.3f * (1f-voidAnimTime)))
                }
                return
            }
        }

        // Velocity calculation
        velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)

        // Steering calculation
        val roll = targetRotation * dt * Math.min(velocity / 5, 1f)
        player.rotate(0.0f, roll, 0.0f)
        player.translate(Vector3f(0.0f, 0.0f, -velocity * dt))

        // Effects
        camera.fov = 80f + 20f * Math.min(abs(velocity) / MAX_SPEED, 1f) + 20f * clamp(0f, 1f, 1-(voidDistance/50))

        // Show Score
        updateScore()

        // Camera Orbit
        //updateCameraOrbit()
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

    fun updateDebug() {
        if (renderCollisions) {
            val hitbox = mapManager.getObstacleHitbox()
            if (hitbox == null) {
                return
            }
            collisionDebugCubes[0].setPosition(Vector3f(hitbox.minX, 0f, hitbox.minZ))
            collisionDebugCubes[1].setPosition(Vector3f(hitbox.maxX, 0f, hitbox.minZ))
            collisionDebugCubes[2].setPosition(Vector3f(hitbox.minX, 0f, hitbox.maxZ))
            collisionDebugCubes[3].setPosition(Vector3f(hitbox.maxX, 0f, hitbox.maxZ))

            val tunnelHitbox = mapManager.getTunnelHitbox()
            if (tunnelHitbox == null) {
                return
            }
            tunnelCollisionDebugCubes[0].setPosition(Vector3f(tunnelHitbox.minX, 5f, tunnelHitbox.minZ))
            tunnelCollisionDebugCubes[1].setPosition(Vector3f(tunnelHitbox.maxX, 5f, tunnelHitbox.minZ))
            tunnelCollisionDebugCubes[2].setPosition(Vector3f(tunnelHitbox.minX, 5f, tunnelHitbox.maxZ))
            tunnelCollisionDebugCubes[3].setPosition(Vector3f(tunnelHitbox.maxX, 5f, tunnelHitbox.maxZ))
        }
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
        setBlur(0f)
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
        voidAnimTime = 0f
        gameState = GS_MENU

        shouldDissolve = false
        dissolveFactor = 0f
        player.dissolveFactor = 0f
        backWheels.dissolveFactor = 0f
        frontLeftWheel.dissolveFactor = 0f
        frontRightWheel.dissolveFactor = 0f

        cameraOffset = Vector3f(0f, 4f, -5f)
        cameraRotOffset = Vector3f(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)

        void.setPosition(Vector3f(0f, 0f, -200f))

        uiScore.setPosition(UI_SCORE_START)
        uiCredits.setPosition(UI_CREDITS_START)
        uiControls.setPosition(UI_CONTROLS_START)

        mapManager.currentSegment = 0
        mapManager.init(Random.nextInt())

        updateScore()
    }


    // Update functions
    private fun updateDissolve(dt: Float) {
        if (dissolveFactor < 1f && shouldDissolve) {
            player.dissolveFactor = dissolveFactor
            backWheels.dissolveFactor = dissolveFactor
            frontLeftWheel.dissolveFactor = dissolveFactor
            frontRightWheel.dissolveFactor = dissolveFactor

            setBlur(dissolveFactor*2)

            dissolveFactor += dt/2
        } else {
            accelerationInput()
            steeringInput()
        }
    }

    private fun accelerationInput() {
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
            rotateMul * (1 - velocity*.5f / MAX_SPEED)
        } else if (window.getKeyState(GLFW_KEY_D)) {
            -rotateMul * (1 - velocity*.5f / MAX_SPEED)
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
            if (player.getWorldRotation().y < .3f && player.getWorldRotation().y > -.3f) {
                velocity *= .9f
                targetRotation = -3f
            } else {
                velocity = -velocity * .8f
                camera.startScreenShake(0.5f,0.5f)
                gameOver()
            }
        }

        // Obstacle collision
        val hitbox = mapManager.getObstacleHitbox()
        if (hitbox != null) {
            if (carCollider.checkHitboxCollision(hitbox)) {
                velocity = -velocity * .8f
                camera.startScreenShake(0.5f,0.5f)
                gameOver()
            }
        }

        // Tunnel collision
        val tunnelHitbox = mapManager.getTunnelHitbox()
        if (tunnelHitbox != null) {
            if (carCollider.checkHitboxCollision(tunnelHitbox)) {
                val field = FIELD_DIVIDER-.2f
                if (player.getWorldPosition().x > field) {
                    if (carCollider.checkZAxisCollision(field)) {
                        val minDistance = field+.75f+abs(player.getWorldRotation().y)
                        if (player.getWorldPosition().x < minDistance) {
                            player.setPosition(Vector3f(minDistance, player.getWorldPosition().y, player.getWorldPosition().z))
                        }
                        if (player.getWorldRotation().y < 0 || targetRotation > 0) {
                            targetRotation = 1f
                            player.translate(Vector3f(-0.1f, 0.0f, 0.0f))
                            return
                        }
                        if (player.getWorldRotation().y < .3f && player.getWorldRotation().y > -.3f) {
                            velocity *= .9f
                            targetRotation = -3f
                        } else {
                            velocity = -velocity * .8f
                            camera.startScreenShake(0.5f,0.5f)
                            gameOver()
                        }
                    }
                } else {
                    velocity = -velocity * .8f
                    camera.startScreenShake(0.5f,0.5f)
                    gameOver()
                }
            }
        }
    }

    private var cameraAngle = 0.0f

    private var cameraOffset = Vector3f(0f, 4f, -5f)
    private var cameraRotOffset = Vector3f(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)

    private fun updateCamera(dt: Float) {
        if (cameraHolder.getWorldRotation() != Vector3f(0f)) {
            cameraHolder.setRotation(0f, 0f, 0f)
        }
        val voidDistanceView = clamp(0f, 1f, 1-(voidDistance/50))
        val cameraPos = if (window.getKeyState(GLFW_KEY_E)) {
            Vector3f(0f, 4f, 5f)
        } else {
            Vector3f(0f, 4f, -5f).add(Vector3f(0f, voidDistanceView*1.8f, voidDistanceView*-2))
        }
        val cameraRot = if (window.getKeyState(GLFW_KEY_E)) {
            Vector3f(-.9f, 0f, 0f)
        } else {
            Vector3f(.5f, Math.toRadians(180.0).toFloat(), cameraAngle).add(Vector3f(voidDistanceView*.55f, 0f, 0f))
        }
        cameraOffset = cameraOffset.lerp(cameraPos, dt*4)
        cameraRotOffset = cameraRotOffset.lerp(cameraRot, dt*8)
        cameraAngle = lerp(targetRotation*.1f, cameraAngle, dt)
        camera.setRotation(cameraRotOffset.x, cameraRotOffset.y, cameraRotOffset.z)
        camera.setPosition(player.getWorldPosition().add(cameraOffset))
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
        if (window.getKeyState(GLFW_KEY_E)) {
            camera.setRotation(.5f, Math.toRadians(180.0).toFloat(), cameraAngle)
            camera.setPosition(player.getWorldPosition().add(Vector3f(0f, 4f, -5f)))
        }
        targetRotation = 0f
        uiScore.setPosition(Vector3f(-.25f, -.1f, 0f))
    }

    private fun getScore(): Int {
        if (gameState != GS_GAME) {
            return 0
        }
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
        voidSpeed = 3f + 3*getScore()/1000f
        void.translate(Vector3f(0f, 0f, dt*voidSpeed))
        voidDistance = player.getWorldPosition().z - void.getWorldPosition().z - 100

        if (voidDistance < 0) {
            gameOver()
        }
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

    private fun setBlur(blurAmount: Float) {
        blurEffectH.setBlurAmount(blurAmount)
        blurEffectV.setBlurAmount(blurAmount)
    }


    // Helper Functions
    private fun getOrbitVector(orbitRadius: Float, angle: Double): Vector3f {
        return Vector3f(orbitRadius * cos(angle).toFloat(), 1.5f, orbitRadius * sin(angle).toFloat())
    }

    private fun lerp(target: Float, input: Float, speed: Float): Float {
        return input + ((target - input) * speed)
    }

    private fun lerp(target: Vector3f, input: Vector3f, speed: Float): Vector3f {
        var newVal = Vector3f(input.x, input.y, input.z)
        newVal.x = lerp(target.x, input.x, speed)
        newVal.y = lerp(target.y, input.y, speed)
        newVal.z = lerp(target.z, input.z, speed)
        return newVal
    }
}