import kotlin.concurrent.thread

fun main() {
    println("=== Параллельная симуляция движения людей и водителей ===\n")

    val humans = listOf(
        Human("Иванов Иван Иванович", 25, 1.5),
        Human("Петров Петр Петрович", 30, 2.0),
        Human("Сидорова Анна Сергеевна", 28, 1.2),
        Human("Козлов Алексей Владимирович", 35, 1.8),
        Human("Иванов Иван Иванович", 25, 1.5),
        Human("Петров Петр Петрович", 30, 2.0),
        Human("Сидорова Анна Сергеевна", 28, 1.2),
        Human("Козлов Алексей Владимирович", 35, 1.8),
        Human("Иванов Иван Иванович", 25, 1.5),
        Human("Петров Петр Петрович", 30, 2.0),
    )

    val driver = Driver(
        "Николаева Мария Дмитриевна",
        32,
        3.5,
        "автомобиля",
        Math.PI / 2
    )

    println("Начальные позиции:")
    humans.forEach { println("Пешеход: $it") }
    println("Водитель: $driver")
    println()

    val simulationTime = 8

    val threads = mutableListOf<Thread>()

    humans.forEachIndexed { index, human ->
        val thread = thread {
            println("Поток пешехода ${index + 1} запущен")
            for (second in 1..simulationTime) {
                human.move()
                Thread.sleep(100)
            }
            println("Поток пешехода ${index + 1} завершен")
        }
        threads.add(thread)
    }

    val driverThread = thread {
        println("Поток водителя запущен")
        for (second in 1..simulationTime) {
            driver.move()
            Thread.sleep(100)
        }
        println("Поток водителя завершен")
    }
    threads.add(driverThread)

    for (second in 1..simulationTime) {
        Thread.sleep(1000) // Ждем 1 секунду

        println("\n--- Секунда $second ---")
        humans.forEachIndexed { index, human ->
            println("Пешеход ${index + 1}: ${human.getFullName()} -> ${human.getPosition()}")
        }
        println("Водитель: ${driver.getFullName()} -> ${driver.getPosition()}")
    }

    threads.forEach { it.join() }

    println("\n=== Финальные позиции ===")
    humans.forEach { println("Пешеход: $it") }
    println("Водитель: $driver")

    println("\n=== Статистика движения ===")
    humans.forEach { human ->
        val distance = Math.sqrt(human.getX() * human.getX() + human.getY() * human.getY())
        println("${human.getFullName()} прошел ${String.format("%.2f", distance)} единиц")
    }
    val driverDistance = Math.sqrt(driver.getX() * driver.getX() + driver.getY() * driver.getY())
    println("${driver.getFullName()} проехал ${String.format("%.2f", driverDistance)} единиц")

    println("\n=== Анализ траекторий ===")
    println("Пешеходы: случайное блуждание (Random Walk)")
    println("Водитель: прямолинейное движение с небольшими отклонениями")
}