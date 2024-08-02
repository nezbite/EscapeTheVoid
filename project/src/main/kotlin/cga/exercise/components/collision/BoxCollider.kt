package cga.exercise.components.collision

import cga.exercise.components.geometry.Renderable
import org.joml.Vector3f
import kotlin.math.roundToInt

class BoxCollider(var width: Float, var depth: Float) {
    var bounds: Bounds = Bounds(Vector3f(), Vector3f(), Vector3f(), Vector3f())
    var center: Vector3f = Vector3f()
    var renderables = mutableListOf<Renderable>()

    fun updateBounds(position: Vector3f, rotation: Vector3f) {
        val halfWidth = width / 2
        val halfDepth = depth / 2

        val a = Vector3f(-halfWidth, 0f, halfDepth)
        val b = Vector3f(halfWidth, 0f, halfDepth)
        val c = Vector3f(-halfWidth, 0f, -halfDepth)
        val d = Vector3f(halfWidth, 0f, -halfDepth)

        val yRot = -rotation.y

//        a.rotateX(rotation.x)
        a.rotateY(yRot)
//        a.rotateZ(rotation.z)


//        b.rotateX(rotation.x)
        b.rotateY(yRot)
//        b.rotateZ(rotation.z)

//        c.rotateX(rotation.x)
        c.rotateY(yRot)
//        c.rotateZ(rotation.z)

//        d.rotateX(rotation.x)
        d.rotateY(yRot)
//        d.rotateZ(rotation.z)

        a.add(position)
        b.add(position)
        c.add(position)
        d.add(position)

        bounds = Bounds(a, b, c, d)
        center = Vector3f((a.x + b.x + c.x + d.x) / 4, (a.y + b.y + c.y + d.y) / 4, (a.z + b.z + c.z + d.z) / 4)

        for (renderable in renderables) {
            renderable.setRotation(rotation.x, rotation.y, rotation.z)
            renderable.scale(Vector3f(.1f, 1f, .1f))
        }

        renderables[0].setPosition(a)
        renderables[1].setPosition(b)
        renderables[2].setPosition(c)
        renderables[3].setPosition(d)


    }

    fun checkZAxisCollision(collisionPosition: Float): Boolean {
        val acol = bounds.a.x > collisionPosition
        val bcol = bounds.b.x > collisionPosition
        val ccol = bounds.c.x > collisionPosition
        val dcol = bounds.d.x > collisionPosition
        return acol || bcol || ccol || dcol
    }

    fun Double.round(decimals: Int = 2): String = "%.${decimals}f".format(this)
}


/**
 * When viewing the object from the top:
 * a - b
 * |   |
 * c - d
 */
class Bounds(var a: Vector3f, var b: Vector3f, var c: Vector3f, var d: Vector3f)