import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.a22practicheskaya.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoryGame()
        }
    }
}

@Composable
fun MemoryGame() {
    val context = LocalContext.current
    val images = listOf(
        R.drawable.animal0, R.drawable.animal1, R.drawable.animal2,
        R.drawable.animal3, R.drawable.animal4, R.drawable.animal5
    )

    val cardPairs = (images + images).shuffled()
    var openCards by remember { mutableStateOf(emptyList<Int>()) }
    var matchedPairs by remember { mutableStateOf(0) }
    var moves by remember { mutableStateOf(0) }
    val highScores = remember { mutableStateOf(getHighScores(context)) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ходы: $moves",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(cardPairs) { index, card ->
                    Card(
                        card = card,
                        isFaceUp = index in openCards,
                        onClick = {
                            when {
                                openCards.contains(index) -> return@Card
                                openCards.size < 2 -> {
                                    openCards = openCards + index
                                    moves++
                                }
                                else -> {
                                    val firstIndex = openCards[0]
                                    val secondIndex = openCards[1]
                                    if (cardPairs[firstIndex] == cardPairs[secondIndex]) {
                                        matchedPairs++
                                    } else {
                                        openCards = emptyList()
                                    }
                                    openCards = if (openCards.size == 2) listOf(index) else openCards + index
                                    moves++
                                }
                            }

                            if (matchedPairs == cardPairs.size / 2) {
                                Toast.makeText(context, "Игра завершена! Ходы: $moves", Toast.LENGTH_SHORT).show()
                                saveHighScore(context, moves)
                                highScores.value = getHighScores(context)
                            }
                        }
                    )
                }
            }

            Text(
                text = "Рекорды: ${highScores.value.joinToString(", ")}",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun Card(card: Int, isFaceUp: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
            .size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isFaceUp) {
            Image(
                painter = painterResource(id = card),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun getHighScores(context: Context): List<Int> {
    val sharedPreferences = context.getSharedPreferences("MemoryGame", Context.MODE_PRIVATE)
    return sharedPreferences.getString("high_scores", "")?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
}

fun saveHighScore(context: Context, score: Int) {
    val highScores = getHighScores(context).toMutableList()
    highScores.add(score)
    highScores.sort()
    if (highScores.size > 5) {
        highScores.removeAt(highScores.size - 1)
    }
    val sharedPreferences = context.getSharedPreferences("MemoryGame", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putString("high_scores", highScores.joinToString(","))
    }
}