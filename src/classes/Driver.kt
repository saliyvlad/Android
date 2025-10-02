import kotlin.random.Random

class Driver(
    fullName: String,
    age: Int,
    currentSpeed: Double,
    private var vehicleType: String,
    private var direction: Double = Math.PI / 4
) : Human(fullName, age, currentSpeed) {

    override fun move() {
        val speedVariation = Random.nextDouble(0.8, 1.2)
        val actualSpeed = getCurrentSpeed() * speedVariation

        val directionVariation = Random.nextDouble(-0.17, 0.17)
        direction += directionVariation

        if (direction < 0) direction += 2 * Math.PI
        if (direction >= 2 * Math.PI) direction -= 2 * Math.PI

        val newX = getX() + actualSpeed * Math.cos(direction)
        val newY = getY() + actualSpeed * Math.sin(direction)

        setPosition(newX, newY)
        setCurrentSpeed(actualSpeed.coerceAtLeast(0.1))
    }

    fun getVehicleType(): String = vehicleType
    fun setVehicleType(type: String) { vehicleType = type }

    fun getDirection(): Double = direction
    fun setDirection(newDirection: Double) { direction = newDirection % (2 * Math.PI) }

    override fun toString(): String {
        return "${getFullName()} (возраст: ${getAge()}, водитель $vehicleType, " +
                "скорость: ${String.format("%.2f", getCurrentSpeed())}, " +
                "позиция: ${getPosition()}, направление: ${String.format("%.1f", Math.toDegrees(direction))}°)"
    }
}