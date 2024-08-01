package cga.exercise.components.map

import cga.exercise.components.geometry.Renderable
import org.joml.Vector3f
import kotlin.random.Random

/**
 * Class for managing the infinitely generated map
 */
class MapManager {
    var segments: Array<MapSegment> = Array(40) { MapSegment(Renderable(mutableListOf())) }
    var segmentIds = mutableListOf<Int>()
    var random = Random(1)

    var currentSegment = 0 // Current segment the player is on
    var mapSegment = 0 // Current starting segment of the map

    var roadModels = mutableListOf<Renderable>()

    fun init() {
        for (i in 0 .. segments.size) {
            segmentIds.add(random.nextInt(roadModels.size))
        }
        setSegments()
    }


    fun update() {
        if (mapSegment != currentSegment) {
            mapSegment = currentSegment
            if (segmentIds.size < currentSegment + 40) {
                for (i in segmentIds.size .. currentSegment + 39) {
                    segmentIds.add(random.nextInt(roadModels.size))
                }
            }
            setSegments()
        }
    }

    fun setSegments() {
        segments = Array(segments.size) { MapSegment(Renderable(mutableListOf())) }
        for (i in 0 .. segments.size-1) {
            if (currentSegment < 0) {
                currentSegment = 0
            }
            val segment = MapSegment(roadModels[segmentIds[currentSegment+i]].copy())
            segment.position = currentSegment+i-2
            segment.renderable.setPosition(Vector3f(0f, 0f, segment.position*segment.width.toFloat()))
            segments[i] = segment
        }
    }
}