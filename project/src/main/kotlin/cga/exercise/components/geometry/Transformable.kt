package cga.exercise.components.geometry

import org.joml.Matrix4f
import org.joml.Vector3f

open class Transformable(private var modelMatrix: Matrix4f = Matrix4f(), var parent: Transformable? = null) {

    /**
     * Returns copy of object model matrix
     * @return modelMatrix
     */
    fun getModelMatrix(): Matrix4f {
        return Matrix4f(modelMatrix)
    }

    /**
     * Returns multiplication of world and object model matrices.
     * Multiplication has to be recursive for all parents.
     * Hint: scene graph
     * @return world modelMatrix
     */
    fun getWorldModelMatrix(): Matrix4f {
        /*if (parent == null) {
            return Matrix4f(modelMatrix)
        } else {
            return parent!!.getWorldModelMatrix().mul(modelMatrix)
        }*/
        return parent?.getWorldModelMatrix()?.mul(getModelMatrix()) ?: getModelMatrix();
    }

    /**
     * Rotates object around its own origin.
     * @param pitch radiant angle around x-axis ccw
     * @param yaw radiant angle around y-axis ccw
     * @param roll radiant angle around z-axis ccw
     */
    fun rotate(pitch: Float, yaw: Float, roll: Float) {
        modelMatrix.rotateXYZ(pitch, yaw, roll)
    }

    fun setRotation(pitch: Float, yaw: Float, roll: Float) {
        modelMatrix.identity()
        modelMatrix.rotateXYZ(pitch, yaw, roll)
    }

    /**
     * Rotates object around given rotation center.
     * @param pitch radiant angle around x-axis ccw
     * @param yaw radiant angle around y-axis ccw
     * @param roll radiant angle around z-axis ccw
     * @param altMidpoint rotation center
     */
    fun rotateAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {
        // Erstelle Matrizen für die Verschiebung, Rotation und Rückverschiebung
        val translationMatrix = Matrix4f().translate(-altMidpoint.x, -altMidpoint.y, -altMidpoint.z)
        val invTranslationMatrix = Matrix4f().translate(altMidpoint.x, altMidpoint.y, altMidpoint.z)
        val rotationMatrix = Matrix4f().rotateXYZ(pitch, yaw, roll);

        // Rotationsmatrix um X-Achse (pitch)
        //rotationMatrix.rotateX(pitch)
        // Rotationsmatrix um Y-Achse (yaw)
        //rotationMatrix.rotateY(yaw)
        // Rotationsmatrix um Z-Achse (roll)
        //rotationMatrix.rotateZ(roll)
        /*val tmp = Matrix4f();
        tmp.translate(altMidpoint);
        tmp.rotateXYZ(pitch, yaw, roll);
        tmp.translate(Vector3f(altMidpoint).negate());
        modelMatrix = tmp.mul(modelMatrix);*/
        // Transformationen kombinieren
        modelMatrix = invTranslationMatrix.mul(rotationMatrix.mul(translationMatrix.mul(modelMatrix, Matrix4f()), Matrix4f()), Matrix4f())

    }

    fun setRotationAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {
        modelMatrix.identity()
        modelMatrix.translate(altMidpoint)
        modelMatrix.rotateXYZ(pitch, yaw, roll)
        modelMatrix.translate(Vector3f(altMidpoint).negate())
    }

    /**
     * Translates object based on its own coordinate system.
     * @param deltaPos delta positions
     */
    fun translate(deltaPos: Vector3f) {
        modelMatrix.translate(deltaPos)
    }

    fun setPosition(pos: Vector3f) {
        modelMatrix.m30(pos.x)
        modelMatrix.m31(pos.y)
        modelMatrix.m32(pos.z)
    }

    /**
     * Translates object based on its parent coordinate system.
     * Hint: this operation has to be left-multiplied
     * @param deltaPos delta positions (x, y, z)
     */
    fun preTranslate(deltaPos: Vector3f) {
        // Erstelle eine Übersetzungsmatrix basierend auf deltaPos
        val translationMatrix = Matrix4f().translate(deltaPos)
        // Multipliziere die Übersetzungsmatrix von links mit der aktuellen Modellmatrix
        modelMatrix = translationMatrix.mul(modelMatrix)
    }

    /**
     * Scales object related to its own origin
     * @param scale scale factor (x, y, z)
     */
    fun scale(scale: Vector3f) {
        modelMatrix.scale(scale)
    }

    /**
     * Returns position based on aggregated translations.
     * Hint: last column of model matrix
     * @return position
     */
    fun getPosition(): Vector3f {
        return Vector3f(modelMatrix.m30(), modelMatrix.m31(), modelMatrix.m32())
    }

    fun getRotation(): Vector3f {
        return Vector3f().set(modelMatrix.getEulerAnglesXYZ(Vector3f()))
    }

    fun getWorldRotation(): Vector3f {
        return Vector3f().set(getWorldModelMatrix().getEulerAnglesXYZ(Vector3f()))
    }

    /**
     * Returns position based on aggregated translations incl. parents.
     * Hint: last column of world model matrix
     * @return position
     */
    fun getWorldPosition(): Vector3f {
        return getWorldModelMatrix().getTranslation(Vector3f())
    }

    /**
     * Returns x-axis of object coordinate system
     * Hint: first normalized column of model matrix
     * @return x-axis
     */
    fun getXAxis(): Vector3f {
        return Vector3f(modelMatrix.m00(), modelMatrix.m01(), modelMatrix.m02()).normalize()
    }

    /**
     * Returns y-axis of object coordinate system
     * Hint: second normalized column of model matrix
     * @return y-axis
     */
    fun getYAxis(): Vector3f {
        return Vector3f(modelMatrix.m10(), modelMatrix.m11(), modelMatrix.m12()).normalize()
    }

    /**
     * Returns z-axis of object coordinate system
     * Hint: third normalized column of model matrix
     * @return z-axis
     */
    fun getZAxis(): Vector3f {
        return Vector3f(modelMatrix.m20(), modelMatrix.m21(), modelMatrix.m22()).normalize()
    }

    /**
     * Returns x-axis of world coordinate system
     * Hint: first normalized column of world model matrix
     * @return x-axis
     */
    fun getWorldXAxis(): Vector3f {
        var wmm = getWorldModelMatrix()
        return Vector3f(wmm.m00(), wmm.m01(), wmm.m02()).normalize()
    }

    /**
     * Returns y-axis of world coordinate system
     * Hint: second normalized column of world model matrix
     * @return y-axis
     */
    fun getWorldYAxis(): Vector3f {
        var wmm = getWorldModelMatrix()
        return Vector3f(wmm.m10(), wmm.m11(), wmm.m12()).normalize()
    }

    /**
     * Returns z-axis of world coordinate system
     * Hint: third normalized column of world model matrix
     * @return z-axis
     */
    fun getWorldZAxis(): Vector3f {
        var wmm = getWorldModelMatrix()
        return Vector3f(wmm.m20(), wmm.m21(), wmm.m22()).normalize()
    }
}