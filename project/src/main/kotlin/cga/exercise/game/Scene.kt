package cga.exercise.game

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.*
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.map.MapManager
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import cga.framework.OBJLoader.loadOBJ
import org.joml.Math.*
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.ARBVertexArrayObject.*
import org.lwjgl.opengl.GL20.*

/**
 * Created by Fabian on 16.09.2017.
 */
class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")

    private val simpleMesh: Mesh

    private val groundMesh: Mesh
    private val sphereMesh: Mesh

//    private lateinit var pointLight: PointLight
//    private lateinit var spotLight: SpotLight

    private var environment = mutableListOf<Renderable>()


    private var dx: Double = 0.0
    private var dy: Double = 0.0
    private var yRotation: Double = 0.0
    private var oldXpos: Double = 0.0
    private var oldYpos: Double = 0.0

//    private var groundProjection: Matrix4f
//    private var sphereProjection: Matrix4f

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
//        val vertices = floatArrayOf(
//            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
//            0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,
//            0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
//            0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
//            -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f
//        )

        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)

        val vertices = floatArrayOf( // Initialien DS
            -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f,    // 0
            -0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,    // 1
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f,   // 2
            0.2f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,     // 3
            0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,     // 4
            0.2f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,     // 5
            0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,     // 6
            0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f,     // 7
            0.2f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f     // 8
        )

        val indices = intArrayOf(
            0, 1, 2,
            3, 7, 4,
            5, 6, 8
        )


        val attribute:Array<VertexAttribute> = arrayOf(
            VertexAttribute(3, GL_FLOAT, 24, 0),
            VertexAttribute(3, GL_FLOAT, 24, 12)
            )
        simpleMesh = Mesh(vertices, indices, attribute)

        var objRes = loadOBJ("assets/models/ground.obj")
        var obj = objRes.objects[0].meshes

        var lightCycleModel = ModelLoader.loadModel("assets/Light Cycle/Light Cycle/HQ_Movie cycle.obj", Math.toRadians(-90.0).toFloat(), Math.toRadians(90.0).toFloat(), 0f)

        val objAttributes:Array<VertexAttribute> = arrayOf(
            VertexAttribute(3, GL_FLOAT, 32, 0),
            VertexAttribute(2, GL_FLOAT, 32, 12),
            VertexAttribute(3, GL_FLOAT, 32, 20)
        )

        groundMesh = Mesh(obj[0].vertexData, obj[0].indexData, objAttributes)


        var groundTextureEmit = Texture2D("assets/textures/ground_emit.png", true)
        var groundTextureDiff = Texture2D("assets/textures/ground_diff.png", true)
        var groundTextureSpec = Texture2D("assets/textures/ground_spec.png", true)



        var groundMaterial = Material(groundTextureDiff, groundTextureEmit, groundTextureSpec, 60.0f, Vector2f(64.0f, 64.0f))


        objRes = loadOBJ("assets/models/sphere.obj")
        obj = objRes.objects[0].meshes

        sphereMesh = Mesh(obj[0].vertexData, obj[0].indexData, objAttributes)




        // Camera init
        // rotate -20° around x-axis
        // translate by 0, 0, 4
        tronCamera.rotate(Math.toRadians(-35.0).toFloat(), 0.0f, 0.0f)
        tronCamera.translate(Vector3f(0.0f, 0.0f, 4.0f))


        // Ground Transformation
        // Rotate 90° around x-axis
        // Scale by 0.03
//        groundProjection = Matrix4f()
//        groundProjection.rotate(Math.toRadians(-90.0).toFloat(), 1.0f, 0.0f, 0.0f)
//        groundProjection.scale(0.03f)

        var groundRenderable = Renderable(
            listOf(groundMesh).toMutableList()
        , Vector3f(0.0f, 1.0f, 0.0f))

        groundRenderable.setMaterial(groundMaterial)



//        groundRenderable.rotate(Math.toRadians(-90.0).toFloat(), 0.0f, 0.0f)
//        groundRenderable.scale(Vector3f(0.03f, 0.03f, 0.03f))



        //renderables.add(groundRenderable)

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


        val ROAD_SEGMENTS = 10

        val roadModel1 = ModelLoader.loadModel("assets/Environment/Road1.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val roadModel2 = ModelLoader.loadModel("assets/Environment/Road2.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        val roadModel3 = ModelLoader.loadModel("assets/Environment/Road3.obj", 0f, Math.toRadians(180.0).toFloat(), 0f)
        mapManager.roadModels.add(roadModel1!!)
        mapManager.roadModels.add(roadModel2!!)
        mapManager.roadModels.add(roadModel3!!)

        mapManager.init()

//        if (roadModel1 != null && roadModel2 != null && roadModel3 != null) {
//            for (i in 0 until ROAD_SEGMENTS) {
//                var roadSegment: Renderable
//                val model = (Math.random() * 3).toInt()
//                roadSegment = when (model) {
//                    1 -> roadModel1.copy()
//                    2 -> roadModel2.copy()
//                    else -> roadModel3.copy()
//                }
//                roadSegment.scale(Vector3f(1.5f))
//                roadSegment.translate(Vector3f(0.0f, 0.0f, -i*3f))
//                environment.add(roadSegment)
//            }
//        }




        // Sphere Transformation
        // Scale by 0.5
//        sphereProjection = Matrix4f()
//        sphereProjection.scale(0.5f)

//        var sphereRenderable = Renderable(
//            listOf(sphereMesh).toMutableList()
//        )

//        sphereRenderable.scale(Vector3f(0.5f, 0.5f, 0.5f))

//        renderables.add(sphereRenderable)

        //tronCamera.parent = player



        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        //initial opengl state
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()


    }

    fun render(dt: Float, t: Float) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        staticShader.use()

        // color overlay of bike over time (with sin and t)
//        val colorOverlay = Vector3f(0.5f + 0.5f * Math.sin(t + 0.0).toFloat(), 0.5f + 0.5f * Math.sin(t + 2.0).toFloat(), 0.5f + 0.5f * Math.sin(t + 4.0).toFloat())
//        player.colorOverlay = colorOverlay
//        pointLight.color = colorOverlay

        tronCamera.bind(staticShader)

        for (renderable in renderables) {
            renderable.render(staticShader)
        }

        for (renderable in environment) {
            renderable.render(staticShader)
        }

        for (segment in mapManager.segments) {
            segment.renderable.render(staticShader)
        }


//        pointLight.bind(staticShader)
//        spotLight.bind(staticShader, tronCamera.getCalculateViewMatrix())

        // set matrices with setUniform()
//        staticShader.setUniform("model_matrix", groundProjection, true)
//        groundMesh.render()
//
//        staticShader.setUniform("model_matrix", sphereProjection, true)
//        sphereMesh.render()

        // simpleMesh.render()

        // ToDo

        //glBindVertexArray(vaoID)

        //glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
        //glBindVertexArray(0)
    }

    var velocity = 0.0f
    var friction = 1.0f
    var maxSpeed = 50f
    var targetRotation = 0.0f

    fun update(dt: Float, t: Float) {
        val translateMul = 5.0f;
        val rotateMul = 0.5f*Math.PI.toFloat()
        var acceleratorState = 0.0f; // rollend


        if (window.getKeyState(GLFW_KEY_W)) {
            acceleratorState = 0.8f
        }
        if (window.getKeyState(GLFW_KEY_S)) {
            acceleratorState = -0.25f
        }
        velocity = velocity * (1 - dt * friction) + (acceleratorState * maxSpeed) * (dt * friction)


        if (window.getKeyState(GLFW_KEY_A)) {
            targetRotation = rotateMul * (1-velocity/maxSpeed)
        } else if (window.getKeyState(GLFW_KEY_D)) {
            targetRotation = -rotateMul * (1-velocity/maxSpeed)
        } else {
            targetRotation *= 0.95f
        }



        val roll = targetRotation*dt * Math.min(velocity/5, 1f)
        player.rotate(0.0f, roll, 0.0f)
        //player.rotateAroundPoint(0.0f, 0.0f, roll*0.1f, Vector3f(0.0f, 0.0f, 0.0f))
        player.translate(Vector3f(0.0f, 0.0f, -velocity*dt))

        tronCamera.fov = 80f + 20f * Math.min(velocity/maxSpeed, 1f)

        // Update the camera
        // Orbit the camera around the sphere based on dx

        // Calculate Camera LookAt
        val cameraLookAt = player.getWorldPosition()
        tronCamera.setRotation(0f, (-yRotation + 1.5).toFloat(),0f)

        // Calculate Camera Position
        yRotation += dx
        val cameraPosition = player.getWorldPosition().add(GetOrbitVector(5.0f, yRotation))
        tronCamera.setPosition(cameraPosition)

        dx=0.0
        dy=0.0

        if (targetRotation < 0.01f && targetRotation > -0.01f) {
            targetRotation = 0.0f
        }

        val fwrot = targetRotation*.5f

        // Update front wheel angle
        frontLeftWheel.rotateAroundPoint(-velocity*dt*5, 0.0f, 0.0f, Vector3f(0.0f, 0.35f, -1.32f))
        frontLeftWheelTurning.setRotationAroundPoint(0.0f, lerp(fwrot, frontLeftWheelTurning.getRotation().y, dt*5), 0.0f, Vector3f(-0.8f, 0.35f, -1.32f))

        frontRightWheel.rotateAroundPoint(-velocity*dt*5, 0.0f, 0.0f, Vector3f(0.8f, 0.35f, -1.32f))
        frontRightWheelTurning.setRotationAroundPoint(0.0f, lerp(fwrot, frontRightWheelTurning.getRotation().y, dt*5), 0.0f, Vector3f(0.8f, 0.35f, -1.32f))

        // Update wheel spin
        backWheels.rotateAroundPoint(-velocity*dt*5, 0.0f, 0.0f, Vector3f(0.0f, 0.35f, 1.32f))



        // Map Manager
        mapManager.currentSegment = (player.getWorldPosition().z/3).toInt()
        mapManager.update()

    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    fun onMouseMove(xpos: Double, ypos: Double) {
        // Calculate the difference between the old and new mouse positions
        dx = (xpos - oldXpos) * 0.002
        dy = (ypos - oldYpos) * 0.002

        // Rotate the camera based on the mouse movement
        //tronCamera.rotateAroundPoint(0.0f, dx.toFloat(), 0.0f, player.getWorldPosition())


        // Update the old mouse positions
        oldXpos = xpos
        oldYpos = ypos
    }

    fun cleanup() {}


    fun GetOrbitVector(orbitRadius: Float, angle: Double): Vector3f {
        return Vector3f(orbitRadius * Math.cos(angle.toDouble()).toFloat(), 1.5f, orbitRadius * Math.sin(angle.toDouble()).toFloat())
    }

    // Returns the pitch, yaw and roll angles of the camera
    fun Vector3f.LookAt(target: Vector3f): Quaternionf {
        // Calculate the direction vector
        val dir = target.sub(this, Vector3f()).normalize()

        // Calculate the pitch (x-rotation) and yaw (y-rotation)
        val pitch = Math.asin(-dir.y.toDouble()).toFloat()
        val yaw = Math.atan2(dir.x.toDouble(), dir.z.toDouble()).toFloat()

        // Create a Quaternion from the pitch and yaw
        val rotation = Quaternionf().rotateYXZ(yaw, pitch, 0f)

        return rotation
    }

    fun lerp(target: Float, input: Float, speed: Float): Float {
        return input + ((target - input) * speed)
    }
}