import interfaces.Movable
import classes.Human
import classes.Driver

fun main() {
    println("=== ПАРАЛЛЕЛЬНАЯ СИМУЛЯЦИЯ ДВИЖЕНИЯ ===\n")

    val movables: List<Movable> = listOf(
        Human("Иван Иванов", 25, 2.5),
        Human("Петр Петров", 30, 3.0),
        Human("Анна Сидорова", 22, 1.8),
        Driver("Мария Николаева", 35, 60.0, "B", "Mercedes"),
        Driver("Алексей Козлов", 28, 80.0, "C", "Volvo")
    )

    println("Начало симуляции. Участники:")
    movables.forEach {
        when (it) {
            is Human -> println("${it.getName()} - ${it.age} лет")
            is Driver -> println("${it.getName()} - ${it.age} лет, водитель ${it.car}")
        }
    }
    println("\n=== НАЧАЛО ДВИЖЕНИЯ ===\n")

    val threads = mutableListOf<Thread>()

    movables.forEachIndexed { index, movable ->
        val thread = Thread {
            val name = when (movable) {
                is Human -> "Пешеход ${movable.getName()}"
                is Driver -> "Водитель ${movable.getName()}"
                else -> "Объект $index"
            }

            println("▶Запущен поток: $name")

            repeat(5) { step ->
                movable.move()
                Thread.sleep(1000)
            }

            println("Завершен поток: $name")
        }

        threads.add(thread)
        thread.start()
    }

    threads.forEach { it.join() }

    println("\n=== ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ ===")
    movables.forEach { movable ->

        val distance = Math.sqrt(movable.x * movable.x + movable.y * movable.y)

        when (movable) {
            is Human -> {
                println("${movable.getName()} (${movable.age} лет)")
                println("   Пройдено: ${"%.1f".format(distance)} метров")
            }
            is Driver -> {
                println("${movable.getName()} (${movable.age} лет, ${movable.car})")
                println("   Проехал: ${"%.1f".format(distance)} км")
            }
        }
        println("   Финальная позиция: ${movable.getPosition()}")
        println("   Финальная скорость: ${"%.1f".format(movable.currentSpeed)} ${if (movable is Driver) "км/ч" else "м/с"}")
        println()
    }
}