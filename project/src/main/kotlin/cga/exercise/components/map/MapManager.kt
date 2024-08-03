package cga.exercise.components.map

import cga.exercise.components.geometry.Renderable
import org.joml.Vector3f
import kotlin.random.Random

/**
 * Class for managing the infinitely generated map
 */
class MapManager {
    val MAP_SIZE = 100
    val SEGMENT_SIZE = 3*6
    var segments: Array<MapSegment> = Array(MAP_SIZE) { MapSegment(Renderable(mutableListOf())) }
    var segmentIds = mutableListOf<Int>()
    var random = Random(1)

    var currentSegment = 0 // Current segment the player is on
    var mapSegment = 0 // Current starting segment of the map

    var roadModels = mutableListOf<Renderable>()

    fun init() {
        segmentIds.add(3)
        for (i in 1 .. segments.size) {
            segmentIds.add(getNextMapSegment(segmentIds.last()))
        }
        setSegments()
    }


    fun update() {
        if (mapSegment != currentSegment) {
            mapSegment = currentSegment
            if (segmentIds.size < currentSegment + MAP_SIZE) {
                for (i in segmentIds.size .. currentSegment + MAP_SIZE-1) {
                    segmentIds.add(getNextMapSegment(segmentIds.last()))
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
            segment.renderable.setPosition(Vector3f(0f, 0f, segment.position*SEGMENT_SIZE.toFloat()))
            segments[i] = segment
        }
    }

    val M_TUNNEL_ENTRY = 0
    val M_TUNNEL = 1
    val M_ROAD1 = 2
    val M_ROAD2 = 3
    val M_ROAD3 = 4

    val segmentInfos: Map<Int, MapSegmentInfo> = mapOf(
        M_TUNNEL_ENTRY to MapSegmentInfo(M_TUNNEL_ENTRY, 1, mutableListOf(M_TUNNEL)),
        M_TUNNEL to MapSegmentInfo(M_TUNNEL, 100, mutableListOf(M_TUNNEL, M_ROAD1)),
        M_ROAD1 to MapSegmentInfo(M_ROAD1, 10, mutableListOf(M_ROAD1, M_ROAD2)),
        M_ROAD2 to MapSegmentInfo(M_ROAD2, 10, mutableListOf(M_ROAD1, M_ROAD2, M_ROAD3)),
        M_ROAD3 to MapSegmentInfo(M_ROAD3, 5, mutableListOf(M_ROAD3, M_TUNNEL_ENTRY))
    )

    fun getNextMapSegment(last: Int): Int {
        val info = segmentInfos[last]!!
        val followers = mutableListOf<MapSegmentInfo>()
        for (follower in info.possibleFollowers) {
            for (i in 0..segmentInfos[follower]!!.probability) {
                followers.add(segmentInfos[follower]!!)
            }
        }
        return followers[random.nextInt(followers.size)].id
    }
}

class MapSegmentInfo(val id: Int, val probability: Int, val possibleFollowers: MutableList<Int>)