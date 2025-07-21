/**
 * InputRow.kt
 * This file defines a composable function for displaying a row of input fields in the Sudoku app.
 * It will display the numbers 1-9 in a horizontal row, allowing users to input values.
 * When a number is selected, it will call the onInputChange callback with the index and the new value.
 */

package com.kooshmeen.sudoku.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

            @Composable
            fun InputRow(
                input: List<Int>,
                onInputChange: (Int, String) -> Unit,
                selectedNumber: Int? = null,
                modifier: Modifier = Modifier
            ) {
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (index in input.indices) {
                        val number = input[index]
                        val isSelected = selectedNumber == number

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable {
                                    onInputChange(index, number.toString())
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

@Preview
@Composable
fun InputRowPreview() {
    InputRow(
        input = List(9) { it + 1 },
        onInputChange = { index, value -> /* Handle input change */ },
        selectedNumber = null,
        modifier = Modifier
    )
}