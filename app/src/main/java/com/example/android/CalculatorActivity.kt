package com.example.android

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class CalculatorActivity : Activity() {

    private lateinit var tvDisplay: TextView
    private var currentInput = StringBuilder()
    private var hasOperator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        initializeViews()
        setupNumberButtons()
        setupOperationButtons()
        setupControlButtons()
    }

    private fun initializeViews() {
        tvDisplay = findViewById(R.id.tvDisplay)
    }

    private fun setupNumberButtons() {
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        numberButtons.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener {
                appendNumber((it as Button).text.toString())
            }
        }
    }

    private fun setupOperationButtons() {
        findViewById<Button>(R.id.btnAdd).setOnClickListener { appendOperator("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { appendOperator("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { appendOperator("*") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { appendOperator("/") }
    }

    private fun setupControlButtons() {
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearCalculator() }
    }

    private fun appendNumber(number: String) {
        if (currentInput.isEmpty() || (currentInput.length == 1 && currentInput[0] == '0')) {
            currentInput.clear()
        }
        currentInput.append(number)
        updateDisplay()
    }

    private fun appendOperator(operator: String) {
        if (currentInput.isNotEmpty() && !hasOperator) {
            val lastChar = currentInput.last()
            if (lastChar !in setOf('+', '-', '*', '/')) {
                currentInput.append(operator)
                hasOperator = true
                updateDisplay()
            }
        } else if (currentInput.isEmpty() && operator == "-") {
            currentInput.append(operator)
            updateDisplay()
        }
    }

    private fun calculateResult() {
        if (currentInput.isEmpty() || !hasOperator) return

        try {
            val expression = currentInput.toString()
            val result = evaluateExpression(expression)

            currentInput.clear()
            currentInput.append(removeTrailingZeros(result))
            hasOperator = false
            updateDisplay()
        } catch (e: Exception) {
            tvDisplay.text = "Error"
            currentInput.clear()
            hasOperator = false
        }
    }

    private fun evaluateExpression(expression: String): Double {
        val operators = setOf('+', '-', '*', '/')
        var operatorIndex = -1
        var operatorChar = ' '

        for (i in 1 until expression.length) {
            if (expression[i] in operators) {
                operatorIndex = i
                operatorChar = expression[i]
                break
            }
        }

        if (operatorIndex == -1) return expression.toDouble()

        val leftOperand = expression.substring(0, operatorIndex).toDouble()
        val rightOperand = expression.substring(operatorIndex + 1).toDouble()

        return when (operatorChar) {
            '+' -> leftOperand + rightOperand
            '-' -> leftOperand - rightOperand
            '*' -> leftOperand * rightOperand
            '/' -> {
                if (rightOperand == 0.0) throw ArithmeticException("Division by zero")
                leftOperand / rightOperand
            }
            else -> throw IllegalArgumentException("Unknown operator")
        }
    }

    private fun removeTrailingZeros(number: Double): String {
        return if (number % 1 == 0.0) {
            number.toInt().toString()
        } else {
            String.format("%.2f", number).replace(",", ".")
        }
    }

    private fun clearCalculator() {
        currentInput.clear()
        hasOperator = false
        tvDisplay.text = "0"
    }

    private fun updateDisplay() {
        tvDisplay.text = if (currentInput.isEmpty()) "0" else currentInput.toString()
    }
}