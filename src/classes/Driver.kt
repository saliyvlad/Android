package classes

import interfaces.Movable
import kotlin.random.Random

class Driver(
    fullName: String,
    age: Int,
    currentSpeed: Double,
    var category: String,
    var car: String,
) : Human(fullName, age, currentSpeed) {

    private var direction: Double = Random.nextDouble(0.0, 2 * Math.PI)

    override fun move() {
        val speedVariation = Random.nextDouble(0.9, 1.1)
        currentSpeed = (currentSpeed * speedVariation).coerceIn(0.1, 200.0)

        val directionVariation = Random.nextDouble(-0.1, 0.1)
        direction = (direction + directionVariation) % (2 * Math.PI)

        x += currentSpeed * Math.cos(direction)
        y += currentSpeed * Math.sin(direction)

        println("Водитель ${getName()} $age лет едет на $car со скоростью ${"%.1f".format(currentSpeed)} км/ч " +
                "(категория $category), текущая позиция ${getPosition()}")
    }

    fun getDirectionInDegrees(): Double {
        return Math.toDegrees(direction)
    }

    override fun toString(): String {
        return "Водитель ${getName()} (возраст: $age, автомобиль: $car, категория: $category)"
    }
}