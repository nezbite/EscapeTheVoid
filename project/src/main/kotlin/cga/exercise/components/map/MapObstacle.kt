package cga.exercise.components.map

import cga.exercise.components.geometry.Renderable

class MapObstacle(var renderable: Renderable) {
    var lane = -1
    var position = 0
    var length = 4f
}