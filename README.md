# Android
# Салий Владислав
# ИКС-432

# Проект "Ходячий" - Симуляция движения

### Movable.kt
**Интерфейс** для всех движущихся объектов:
- `x, y` - координаты
- `currentSpeed` - скорость  
- `move()` - метод движения
- `getPosition()` - получить позицию

### Human.kt 
**Пешеход** - движется случайно (Random Walk):
```kotlin
// Случайное направление и длина шага
x += скорость × cos(случайный_угол)
y += скорость × sin(случайный_угол)
```

### Driver.kt
**Водитель** - движется прямо с небольшими отклонениями:
```kotlin
// Прямолинейное движение
x += скорость × cos(направление)
y += скорость × sin(направление)
```

### Main.kt
**Запускает симуляцию**:
- Создает 3 пешеходов и 2 водителей
- Каждый движется в отдельном потоке
- Выводит позиции каждую секунду
- Показывает финальные результаты


**Результат:** Пешеходы блуждают случайно, водители едут прямо - все одновременно в разных потоках!

Краткий конспект по конструкторам в Kotlin:

## **Первичный конструктор (Primary Constructor)**
- Определяется в заголовке класса после имени
- Может принимать параметры для инициализации объекта
- Ключевое слово `constructor` можно опустить, если нет модификаторов доступа/аннотаций
- Может использоваться для объявления свойств напрямую

```kotlin
// С ключевым словом constructor
class Person constructor(_name: String, _age: Int)

// Без constructor (более распространено)
class Person(_name: String, _age: Int)

// С объявлением свойств
class Person(val name: String, var age: Int)
```

## **Вторичный конструктор (Secondary Constructor)**
- Определяется в теле класса с помощью `constructor`
- Может быть несколько вторичных конструкторов
- Должен вызывать первичный конструктор через `this()`, если он определен

```kotlin
class Person {
    constructor(name: String) { ... }
    constructor(name: String, age: Int) { ... }
}
```

## **Связь между конструкторами**
- Если есть первичный конструктор, вторичные **обязаны** вызывать его через `this`
- Первичный конструктор выполняется первым
- Можно делегировать вызовы между конструкторами

```kotlin
class Person(val name: String) {
    var age: Int = 0
    
    constructor(name: String, age: Int) : this(name) {
        this.age = age
    }
}
```

## **Ключевые различия**

| Первичный конструктор | Вторичный конструктор |
|----------------------|----------------------|
| Только один | Может быть несколько |
| В заголовке класса | В теле класса |
| Может объявлять свойства | Не может объявлять свойства |
| Не содержит кода выполнения | Содержит код выполнения |

## **Блоки инициализации (init)**
- Выполняются при создании объекта
- Может быть несколько блоков `init`
- Выполняются в порядке объявления

```kotlin
class Person(name: String, age: Int) {
    init {
        // код инициализации
    }
}
```




Отлично! Теперь видна полная картина с конструкторами. Вот анализ из файла **Human.kt**:

## **Первичный конструктор класса Human**

```kotlin
open class Human(
    protected var fullName: String,    // свойство с модификатором protected
    var age: Int,                      // публичное свойство
    override var currentSpeed: Double  // свойство, переопределяющее интерфейс
) : Movable {  // реализация интерфейса Movable
    // ...
}
```

## **Первичные конструкторы**

### 1. Простой первичный конструктор в интерфейсе
```kotlin
// Movable.kt
interface Movable {
    var x: Double
    var y: Double
    var currentSpeed: Double
}
```

### 2. Первичный конструктор с параметрами в классе Driver
```kotlin
// Driver.kt
class Driver(
    fullName: String,           
    age: Int,                    
    currentSpeed: Double,       
    var category: String,       
    var car: String,            
) : Human(fullName, age, currentSpeed) {  
}
```

## **Наследование и вызов конструктора родителя**

### Конструктор класса Driver вызывает конструктор Human:
```kotlin
class Driver(
    fullName: String,
    age: Int,
    currentSpeed: Double,
    var category: String,
    var car: String,
) : Human(fullName, age, currentSpeed) {  
}
```

## **Использование конструкторов в main()**

### Создание объектов через конструкторы:
```kotlin
// Main.kt
fun main() {
    Human("Иван Иванов", 25, 2.5)           
    Human("Петр Петров", 30, 3.0)           
    Human("Анна Сидорова", 22, 1.8)         
  
    Driver("Мария Николаева", 35, 60.0, "B", "Mercedes")    
    Driver("Алексей Козлов", 28, 80.0, "C", "Volvo")        
}
```

## **Вторичный конструктор для класса Human**
```kotlin
open class Human(
    protected var fullName: String,
    var age: Int,
    override var currentSpeed: Double
) : Movable {
    override var x: Double = 0.0
    override var y: Double = 0.0
    constructor(name: String, age: Int) : this(name, age, 1.5) {
        println("Создан пешеход $name с стандартной скоростью 1.5 м/с")
    }
    constructor(name: String) : this(name, 25, 1.5) {
        println("Создан пешеход $name с стандартными параметрами")
    }
}
```

## **Вторичный конструктор для класса Driver**

```kotlin
class Driver(
    fullName: String,
    age: Int,
    currentSpeed: Double,
    var category: String,
    var car: String,
) : Human(fullName, age, currentSpeed) {

    private var direction: Double = Random.nextDouble(0.0, 2 * Math.PI)
    constructor(name: String, age: Int, category: String, car: String) 
        : this(name, age, 60.0, category, car) {
        println("Водитель $name создан со стандартной скоростью 60 км/ч")
    }
    constructor(name: String, car: String) 
        : this(name, 30, 60.0, "B", car) {
        println("Водитель $name создан со стандартными параметрами")
    }

}
```

## **Использование в main()**

```kotlin
fun main() {
    println("=== РАЗНЫЕ КОНСТРУКТОРЫ ===")
    val human1 = Human("Иван Иванов", 25, 2.5)
    val driver1 = Driver("Мария Николаева", 35, 60.0, "B", "Mercedes")
    val human2 = Human("Петр Петров", 30)          
    val human3 = Human("Анна Сидорова")            
    val driver2 = Driver("Алексей Козлов", 28, "C", "Volvo")  
    val driver3 = Driver("Сергей Смирнов", "Toyota")          
    println("\n=== ДВИЖЕНИЕ ===")
    val movables = listOf(human1, human2, human3, driver1, driver2, driver3)
    movables.forEach { it.move() }
}
```
