import kotlin.random.Random

class Human(
    private var fullName: String,
    private var age: Int,
    private var currentSpeed: Double
) {
    private var x: Double = 0.0
    private var y: Double = 0.0

    fun getFullName(): String = fullName
    fun getAge(): Int = age
    fun getCurrentSpeed(): Double = currentSpeed
    fun getX(): Double = x
    fun getY(): Double = y

    fun setFullName(newName: String) {
        fullName = newName
    }

    fun setAge(newAge: Int) {
        if (newAge >= 0) {
            age = newAge
        }
    }

    fun setCurrentSpeed(newSpeed: Double) {
        if (newSpeed >= 0) {
            currentSpeed = newSpeed
        }
    }

    fun move() {
        val direction = Random.nextDouble(0.0, 2 * Math.PI)

        val stepLength = currentSpeed * Random.nextDouble(0.5, 1.5)

        x += stepLength * Math.cos(direction)
        y += stepLength * Math.sin(direction)

        currentSpeed *= Random.nextDouble(0.8, 1.2)

        if (currentSpeed < 0.1) currentSpeed = 0.1
    }

    fun getPosition(): String {
        return "(${String.format("%.2f", x)}, ${String.format("%.2f", y)})"
    }

    override fun toString(): String {
        return "$fullName (возраст: $age, скорость: ${String.format("%.2f", currentSpeed)}, позиция: ${getPosition()})"
    }
}
