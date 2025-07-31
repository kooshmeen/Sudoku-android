/**
 * Displays best times for each difficulty.
 */

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.utils.BestTimeManager

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    var refresh by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val difficulties = listOf("Easy", "Medium", "Hard")

    // Make best times reactive to refresh state
    val bestTimes = remember(refresh) {
        difficulties.associateWith { difficulty ->
            Pair(
                BestTimeManager.getBestTimeFormatted(context, difficulty),
                BestTimeManager.getBestTimeNoMistakeFormatted(context, difficulty)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Best Times",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        difficulties.forEach { difficulty ->
            val (bestTime, bestTimeNoMistakes) = bestTimes[difficulty] ?: Pair("No best time", "No best time")
            Text(
                text = "$difficulty: $bestTime",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$difficulty (No Mistakes): $bestTimeNoMistakes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.Center,

        ){
            Button(
                onClick = {
                    BestTimeManager.clearBestTimes(context)
                    refresh = !refresh // Toggle to force recomposition
                          },
                modifier = Modifier.padding(top = 16.dp, end = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = "Reset Times")
            }
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(start = 4.dp, top = 16.dp),
            ) {
                Text(text = "Back to Menu")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenPreview() {
    RecordScreen(
        modifier = Modifier.fillMaxSize(),
        onNavigateBack = {}
    )
}