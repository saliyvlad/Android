package interfaces

interface Movable {
    var x: Double
    var y: Double
    var currentSpeed: Double
    fun move()
    fun getPosition(): String
}