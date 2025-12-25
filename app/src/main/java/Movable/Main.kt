//import android.graphics.Color
//import android.widget.Button
//import interfaces.Movable
//import classes.Human
//import classes.Driver
//import com.example.android.R
//
//fun main() {
//    val movables: List<Movable> = listOf(
//        Human("Иван Иванов", 25, 2.5),
//        Human("Петр Петров", 30, 3.0),
//        Human("Анна Сидорова", 22, 1.8),
//        Driver("Иван Иванов", 25, 110.0, "B", "Mercedes")
//    )
//
//    movables.forEach { movable ->
//        Thread {
//            repeat(10) {
//                movable.move()
//                Thread.sleep(1000)
//            }
//        }.start()
//    }
//}
////private fun setBackGroundColor(){
////    val randomColor = Color.rgb(
////        (0..255).random(),
////        (0..255).random(),
////        (0..255).random()
////    )
////    val colorEq = findViewById<Button>(R.id.btnEquals)
////    colorEq.setBackgroundColor(randomColor)
//////        (0xFFFF0000.toInt())
////
////}