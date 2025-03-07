package me.crazystone.study.composecalculator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.crazystone.study.composecalculator.CalculatorViewModel
import me.crazystone.study.composecalculator.OperationType
import me.crazystone.study.composecalculator.operations
import me.crazystone.study.composecalculator.ui.theme.lightGrey

@Composable
fun Calculator(viewModel: CalculatorViewModel) {
    val showText = viewModel.showText.collectAsState()
    val resultText = viewModel.resultText.collectAsState()
    val isError = viewModel.isError.collectAsState()
    val isCalculating = viewModel.isCalculating.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                // 表达式/结果文本
                Text(
                    text = showText.value,
                    fontSize = 48.sp,
                    color = if (isError.value) Color.Red else Color.Black,
                    fontWeight = FontWeight.Bold,
                    lineHeight = TextUnit(54F, TextUnitType.Sp)
                )

                // 实时计算结果（非计算状态）
                if (!isCalculating.value && !isError.value && resultText.value.isNotEmpty() && showText.value != resultText.value) {
                    Text(
                        text = "= ${resultText.value}",
                        fontSize = 32.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal,
                        lineHeight = TextUnit(38F, TextUnitType.Sp)
                    )
                }
            }
        }
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(lightGrey)
            ) {}
            OperationPanel(viewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperationPanel(viewModel: CalculatorViewModel) {

    FlowRow(
        horizontalArrangement = Arrangement.Center
    ) {
        operations.forEach { operation ->
            OperationItem(
                operation = operation,
                onItemClick = {
                    viewModel.dealOperations(operation)
                }
            )
        }
    }
}

@Composable
fun OperationItem(
    operation: Pair<OperationType, String>,
    onItemClick: (item: Pair<OperationType, String>) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp.value
    val width = screenWidth / 4
    val isFunc = operation.first == OperationType.FUNCTION
    Column(
        modifier = Modifier
            .height(100.dp)
            .width(width.dp)
            .clickable(onClick = { onItemClick(operation) }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = operation.second,
            fontSize = if (isFunc) 30.sp else 36.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}