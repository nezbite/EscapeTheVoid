package cga.exercise.components.map

import cga.exercise.components.geometry.Renderable
import org.joml.Vector3f
import kotlin.random.Random

/**
 * Class for managing the infinitely generated map
 */
class MapManager {
    val MAP_SIZE = 40
    val SEGMENT_SIZE = 3*6
    val FAR_SEGMENT_SIZE = 3*6*2

    var segments: Array<MapSegment> = Array(MAP_SIZE) { MapSegment(Renderable(mutableListOf())) }
    var farSegments: Array<MapSegment> = Array(MAP_SIZE) { MapSegment(Renderable(mutableListOf())) }
    var obstacles: Array<MapObstacle> = Array(MAP_SIZE) { MapObstacle(Renderable(mutableListOf())) }

    var segmentIds = mutableListOf<Int>()
    var farSegmentIds = mutableListOf<Int>()
    var obstacleIds = mutableListOf<Int>()
    var obstaclePositions = mutableListOf<Int>()

    lateinit var random: Random

    var currentSegment = 0 // Current segment the player is on
    var mapSegment = 0 // Current starting segment of the map

    var roadModels = mutableListOf<Renderable>()
    var farModels = mutableListOf<Renderable>()
    var obstacleModels = mutableListOf<Renderable>()

    fun init(seed: Int = 0) {
        random = Random(seed)
        segments = Array(MAP_SIZE) { MapSegment(Renderable(mutableListOf())) }
        farSegments = Array(MAP_SIZE) { MapSegment(Renderable(mutableListOf())) }
        obstacles = Array(MAP_SIZE) { MapObstacle(Renderable(mutableListOf())) }

        segmentIds = mutableListOf()
        segmentIds.add(3)
        for (i in 1 .. segments.size) {
            segmentIds.add(getNextMapSegment(segmentIds.last()))
        }

        farSegmentIds = mutableListOf()
        farSegmentIds.add(0)
        for (i in 1 .. farSegments.size) {
            farSegmentIds.add(random.nextInt(farModels.size))
        }

        obstacleIds = MutableList(10) { -1 }
        obstaclePositions = MutableList(10) { -1 }
        for (i in 10 .. obstacles.size) {
            val nextSegment = random.nextInt(-1, obstacleModels.size)
            if (nextSegment == -1) {
                obstacleIds.add(-1)
                obstaclePositions.add(-1)
            } else {
                obstacleIds.add(nextSegment)
                obstaclePositions.add(random.nextInt(0, 4))
            }
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
            if (farSegmentIds.size < currentSegment/2 + MAP_SIZE) {
                for (i in farSegmentIds.size .. currentSegment/2 + MAP_SIZE-1) {
                    farSegmentIds.add(random.nextInt(farModels.size))
                }
            }
            if (obstacleIds.size < currentSegment + MAP_SIZE) {
                val nextSegment = random.nextInt(-1, obstacleModels.size)
                if (nextSegment == -1) {
                    obstacleIds.add(-1)
                    obstaclePositions.add(-1)
                } else {
                    obstacleIds.add(nextSegment)
                    obstaclePositions.add(random.nextInt(0, 4))
                }
            }
            setSegments()
        }
    }

    fun setSegments() {
        if (currentSegment < 0) {
            currentSegment = 0
        }

        segments = Array(segments.size) { MapSegment(Renderable(mutableListOf())) }
        for (i in 0 .. segments.size-1) {
            val segment = MapSegment(roadModels[segmentIds[currentSegment+i]].copy())
            segment.position = currentSegment+i-2
            segment.renderable.setPosition(Vector3f(0f, 0f, segment.position*SEGMENT_SIZE.toFloat()))
            segments[i] = segment
        }
        farSegments = Array(farSegments.size) { MapSegment(Renderable(mutableListOf())) }
        for (i in 0 .. farSegments.size-1) {
            val segment = MapSegment(farModels[farSegmentIds[currentSegment/2+i]].copy())
            segment.position = currentSegment/2+i-2
            segment.renderable.setPosition(Vector3f(0f, 0f, segment.position*FAR_SEGMENT_SIZE.toFloat()))
            farSegments[i] = segment
        }
        obstacles = Array(obstacles.size) { MapObstacle(Renderable(mutableListOf())) }
        for (i in 0 .. obstacles.size-1) {
            val obstacleId = obstacleIds[currentSegment+i]
            if (obstacleId == -1) {
                continue
            }
            val obstacle = MapObstacle(obstacleModels[obstacleId].copy())
            obstacle.position = currentSegment+i-2
            obstacle.lane = obstaclePositions[currentSegment+i]
            obstacle.renderable.setPosition(Vector3f((obstacle.lane*3f)-4.5f, 0f, obstacle.position*SEGMENT_SIZE.toFloat()-10f))
            obstacles[i] = obstacle
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

    val obstacleWidth = 1f
    val obstacleLength = 1.5f

    fun getObstacleHitbox(): Hitbox? {
        val carPos = currentSegment+8
        val obstacleLane = obstaclePositions[carPos]
        if (obstacleLane == -1) {
            return null
        }
        return Hitbox(
            (obstacleLane*3f)-4.5f-obstacleWidth,
            (obstacleLane*3f)-4.5f+obstacleWidth,
            (carPos-3)*(SEGMENT_SIZE)-4f-obstacleLength,
            (carPos-3)*(SEGMENT_SIZE)-4f+obstacleLength
        )
    }
}

class Hitbox(val minX: Float, val maxX: Float, val minZ: Float, val maxZ: Float)

class MapSegmentInfo(val id: Int, val probability: Int, val possibleFollowers: MutableList<Int>)