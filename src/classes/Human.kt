package classes

import interfaces.Movable
import kotlin.random.Random

open class Human(
    protected var fullName: String,
    var age: Int,
    override var currentSpeed: Double
) : Movable {

    override var x: Double = 0.0
    override var y: Double = 0.0

    fun getName(): String = fullName

    fun setName(newName: String) {
        fullName = newName
    }

    override fun move() {
        val direction = Random.nextDouble(0.0, 2 * Math.PI)

        val stepLength = currentSpeed * Random.nextDouble(0.5, 1.5)

        x += stepLength * Math.cos(direction)
        y += stepLength * Math.sin(direction)

        currentSpeed *= Random.nextDouble(0.8, 1.2)

        if (currentSpeed < 0.1) currentSpeed = 0.1

        println("Пешеход $fullName $age лет движется со скоростью ${"%.1f".format(currentSpeed)}, текущая позиция ${getPosition()}")
    }

    override fun getPosition(): String {
        return "(${"%.1f".format(x)}, ${"%.1f".format(y)})"
    }

    override fun toString(): String {
        return "Пешеход $fullName (возраст: $age, скорость: ${"%.1f".format(currentSpeed)})"
    }
}