import kotlin.random.Random

class Human(
    private var fullName: String,
    private var age: Int,
    private var currentSpeed: Double
) {
    // Текущие координаты
    private var x: Double = 0.0
    private var y: Double = 0.0

    // Геттеры
    fun getFullName(): String = fullName
    fun getAge(): Int = age
    fun getCurrentSpeed(): Double = currentSpeed
    fun getX(): Double = x
    fun getY(): Double = y

    // Сеттеры
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

    // Метод движения (Random Walk)
    fun move() {
        // Случайное направление в радианах (0 до 2π)
        val direction = Random.nextDouble(0.0, 2 * Math.PI)

        // Случайная длина шага (от 0.5 до 1.5 от текущей скорости)
        val stepLength = currentSpeed * Random.nextDouble(0.5, 1.5)

        // Обновление координат
        x += stepLength * Math.cos(direction)
        y += stepLength * Math.sin(direction)

        // Небольшая случайная корректировка скорости
        currentSpeed *= Random.nextDouble(0.8, 1.2)

        // Ограничение скорости (не может быть отрицательной)
        if (currentSpeed < 0.1) currentSpeed = 0.1
    }

    // Метод для получения текущей позиции
    fun getPosition(): String {
        return "(${String.format("%.2f", x)}, ${String.format("%.2f", y)})"
    }

    // Переопределение toString для удобного вывода
    override fun toString(): String {
        return "$fullName (возраст: $age, скорость: ${String.format("%.2f", currentSpeed)}, позиция: ${getPosition()})"
    }
}