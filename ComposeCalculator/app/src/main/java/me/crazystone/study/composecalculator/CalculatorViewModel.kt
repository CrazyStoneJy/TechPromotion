package me.crazystone.study.composecalculator

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CalculatorViewModel: ViewModel() {

    private var _showText = MutableStateFlow("")
    val showText: StateFlow<String> = _showText.asStateFlow()

    private var _resultText = MutableStateFlow("")
    val resultText: StateFlow<String> = _resultText.asStateFlow()

    private var _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    private var _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    companion object {
        const val OP_ADD = "+"
        const val OP_SUB = "−"
        const val OP_MUL = "×"
        const val OP_DIV = "÷"
        const val OP_PERCENT = "%"
        const val ERROR_MESSAGE = "表达式错误"
    }

    private fun tryCalculate() {
        if (_showText.value.isNotEmpty() && isValidExpression(_showText.value)) {
            val result = calculate(_showText.value)
            _resultText.value = formatResult(result)
        } else {
            _resultText.value = ""  // 清除结果，避免显示旧的计算结果
        }
    }

    private fun findOp(str: String): Int {
        var i = str.length - 1
        while (i >= 0) {
            val s = str[i] + ""
            if (s == OP_ADD || s == OP_SUB || s == OP_MUL || s == OP_DIV) {
                --i
            } else {
                break
            }
        }
        return str.length - 1 - i
    }

    private fun calculate(expression: String): Double {
        try {
            // 分割数字和运算符
            val numbers = mutableListOf<Double>()
            val operators = mutableListOf<String>()
            var currentNumber = ""
            var isNegative = false
            var percentCount = 0

            for (i in expression.indices) {
                val char = expression[i]
                when {
                    // 拼接当前的数字
                    (char.isDigit() || char == '.') -> currentNumber += char
                    // 处理负号
                    (char + "") == OP_SUB && (i == 0 || expression[i-1] in "$OP_MUL$OP_DIV$OP_ADD$OP_SUB") -> {
                        isNegative = true
                    }
                    // 处理百分号
                    (char + "") == OP_PERCENT -> {
                        if (currentNumber.isNotEmpty()) {
                            var num = currentNumber.toDouble()
                            // 对于每个百分号，都除以100
                            num = num / Math.pow(100.0, percentCount + 1.0)
                            numbers.add(if (isNegative) -num else num)
                            currentNumber = ""
                            isNegative = false
                            percentCount = 0
                        } else if (numbers.isNotEmpty()) {
                            // 如果前面已经有数字，对最后一个数字应用百分号
                            val lastIndex = numbers.lastIndex
                            numbers[lastIndex] = numbers[lastIndex] / 100.0
                        }
                    }
                    // 处理运算符
                    char in "$OP_MUL$OP_DIV$OP_ADD$OP_SUB" -> {
                        if (currentNumber.isNotEmpty()) {
                            var num = currentNumber.toDouble()
                            // 如果有累积的百分号，应用它们
                            if (percentCount > 0) {
                                num = num / Math.pow(100.0, percentCount.toDouble())
                            }
                            numbers.add(if (isNegative) -num else num)
                            currentNumber = ""
                            isNegative = false
                            percentCount = 0
                        }
                        operators.add(char.toString())
                    }
                }
            }
            if (currentNumber.isNotEmpty()) {
                var num = currentNumber.toDouble()
                // 如果有累积的百分号，应用它们
                if (percentCount > 0) {
                    num = num / Math.pow(100.0, percentCount.toDouble())
                }
                numbers.add(if (isNegative) -num else num)
            }

            // 如果只有一个数字，直接返回
            if (operators.isEmpty()) {
                return numbers[0]
            }

            // 先计算乘除
            var i = 0
            while (i < operators.size) {
                when (operators[i]) {
                    OP_MUL -> {
                        numbers[i] = numbers[i] * numbers[i + 1]
                        numbers.removeAt(i + 1)
                        operators.removeAt(i)
                        i--
                    }
                    OP_DIV -> {
                        if (numbers[i + 1] == 0.0) throw ArithmeticException("Division by zero")
                        numbers[i] = numbers[i] / numbers[i + 1]
                        numbers.removeAt(i + 1)
                        operators.removeAt(i)
                        i--
                    }
                }
                i++
            }

            // 再计算加减
            i = 0
            while (i < operators.size) {
                when (operators[i]) {
                    OP_ADD -> {
                        numbers[i] = numbers[i] + numbers[i + 1]
                        numbers.removeAt(i + 1)
                        operators.removeAt(i)
                        i--
                    }
                    OP_SUB -> {
                        numbers[i] = numbers[i] - numbers[i + 1]
                        numbers.removeAt(i + 1)
                        operators.removeAt(i)
                        i--
                    }
                }
                i++
            }

            return numbers[0]
        } catch (e: Exception) {
            Log.e("Calculator", "Calculation error: ${e.message}")
            return Double.NaN
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatResult(result: Double): String {
        return if (result.isNaN()) {
            "Error"
        } else if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            String.format("%.8f", result).trimEnd('0').trimEnd('.')
        }
    }

    private fun isValidExpression(expression: String): Boolean {
        if (expression.isEmpty()) return false
        
        // 如果只有一个百分号，是无效的
        if (expression == OP_PERCENT) return false
        
        // 检查是否以运算符结尾（除了百分号的情况）
        val lastChar = expression.last()
        if (lastChar in "$OP_ADD$OP_MUL$OP_DIV" || 
            (lastChar.toString() == OP_SUB && expression.length > 1)) return false

        // 检查是否有连续的运算符（除了负号）
        // for (i in 1 until expression.length) {
        //     val current = expression[i].toString()
        //     val previous = expression[i-1].toString()
        //     if (current in "$OP_ADD$OP_MUL$OP_DIV" && previous in "$OP_ADD$OP_MUL$OP_DIV$OP_SUB") {
        //         return false
        //     }
        // }

        // 检查小数点格式
        val numbers = expression.split(Regex("[$OP_ADD$OP_SUB$OP_MUL$OP_DIV$OP_PERCENT]"))
        for (num in numbers) {
            if (num.count { it == '.' } > 1) return false
        }

        return true
    }

    private fun startCalculationAnimation() {
        viewModelScope.launch {
            _isCalculating.value = true
            delay(300) // 动画持续时间
            _showText.value = _resultText.value
            _isCalculating.value = false
        }
    }

    fun dealOperations(operation: Pair<OperationType, String>) {
        // 如果当前是错误状态，且输入新的数字或运算符，则清除错误信息
        if (_isError.value && (operation.first == OperationType.NUMBER || operation.first == OperationType.OPERATOR)) {
            _isError.value = false
            _showText.value = ""
            _resultText.value = ""
        }
        
        when (operation.first) {
            OperationType.NUMBER -> {
                _showText.value += operation.second
                tryCalculate()
            }
            OperationType.FUNCTION -> {
                when (operation.second) {
                    "DEL" -> {
                        _showText.value = _showText.value.dropLast(1)
                        tryCalculate()
                    }
                    "AC" -> {
                        _showText.value = ""
                        _resultText.value = ""
                        _isError.value = false
                    }
                }
            }
            OperationType.OPERATOR -> {
                when (operation.second) {
                    OP_ADD, OP_MUL, OP_DIV -> {
                        val opCount = findOp(showText.value)
                        _showText.value = _showText.value.dropLast(opCount)
                        _showText.value += operation.second
                        tryCalculate()
                    }
                    OP_SUB -> {
                        if (_showText.value.isEmpty()) {
                            _showText.value += operation.second
                            return
                        }
                        val lastOp = (showText.value[showText.value.length - 1] + "")
                        if (lastOp != OP_SUB) {
                            _showText.value += operation.second
                            tryCalculate()
                        }
                    }
                    OP_PERCENT -> {
                        if (_showText.value.isNotEmpty()) {
                            _showText.value += operation.second
                            tryCalculate()
                        }
                    }
                }
            }
            OperationType.EQUAL -> {
                if (_showText.value.isNotEmpty()) {
                    if (isValidExpression(_showText.value)) {
                        val result = calculate(_showText.value)
                        _resultText.value = formatResult(result)
                        _showText.value = _resultText.value
                    } else {
                        _isError.value = true
                        _showText.value = ERROR_MESSAGE
                        _resultText.value = ""
                    }
                }
            }
        }
    }
}