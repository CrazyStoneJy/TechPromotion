package me.crazystone.study.composecalculator

import me.crazystone.study.composecalculator.CalculatorViewModel.Companion.OP_ADD
import me.crazystone.study.composecalculator.CalculatorViewModel.Companion.OP_DIV
import me.crazystone.study.composecalculator.CalculatorViewModel.Companion.OP_MUL
import me.crazystone.study.composecalculator.CalculatorViewModel.Companion.OP_SUB

enum class OperationType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    EQUAL
}

val operations = listOf(
    Pair(OperationType.FUNCTION, "AC"),
    Pair(OperationType.FUNCTION, "DEL"),
    Pair(OperationType.NUMBER, "%"),
    Pair(OperationType.OPERATOR, OP_DIV),

    Pair(OperationType.NUMBER, "7"),
    Pair(OperationType.NUMBER, "8"),
    Pair(OperationType.NUMBER, "9"),
    Pair(OperationType.OPERATOR, OP_MUL),

    Pair(OperationType.NUMBER, "4"),
    Pair(OperationType.NUMBER, "5"),
    Pair(OperationType.NUMBER, "6"),
    Pair(OperationType.OPERATOR, OP_SUB),

    Pair(OperationType.NUMBER, "1"),
    Pair(OperationType.NUMBER, "2"),
    Pair(OperationType.NUMBER, "3"),
    Pair(OperationType.OPERATOR, OP_ADD),

    Pair(OperationType.FUNCTION, " "),
    Pair(OperationType.NUMBER, "0"),
    Pair(OperationType.NUMBER, "."),
    Pair(OperationType.EQUAL, "=")
)