fun main() {
    println("=== Симуляция движения людей ===\n")

    val humans = arrayOf(
        Human("Иванов Иван Иванович", 25, 1.5),
        Human("Петров Петр Петрович", 30, 2.0),
        Human("Сидорова Анна Сергеевна", 28, 1.2),
        Human("Козлов Алексей Владимирович", 35, 1.8),
        Human("Николаева Мария Дмитриевна", 22, 1.0),
        Human("Иванов Иван Иванович", 25, 1.5),
        Human("Петров Петр Петрович", 30, 2.0),
        Human("Сидорова Анна Сергеевна", 28, 1.2),
        Human("Козлов Алексей Владимирович", 35, 1.8),
        Human("Николаева Мария Дмитриевна", 22, 1.0)
    )


    val simulationTime = 10

    println("Начальные позиции:")
    humans.forEach { println(it) }
    println()


    for (second in 1..simulationTime) {
        println("--- Секунда $second ---")


        humans.forEach { human ->
            human.move()
            println("${human.getFullName()} переместился в позицию ${human.getPosition()}")
        }
        println()
    }

    println("Финальные позиции:")
    humans.forEach { println(it) }

    println("\n=== Статистика ===")
    var totalDistance = 0.0
    humans.forEach { human ->

        val distance = Math.sqrt(human.getX() * human.getX() + human.getY() * human.getY())
        totalDistance += distance
        println("${human.getFullName()} прошел примерно ${String.format("%.2f", distance)} единиц")
    }
    println("Среднее расстояние: ${String.format("%.2f", totalDistance / humans.size)} единиц")
}