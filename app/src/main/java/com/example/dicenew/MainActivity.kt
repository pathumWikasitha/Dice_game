package com.example.dicenew

import android.annotation.SuppressLint
import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.isDigitsOnly
import com.example.dicenew.ui.theme.DiceNewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showHomeScreen by rememberSaveable { mutableStateOf(true) }
            DiceNewTheme {
                MyApp(
                    showHomeScreen = showHomeScreen,
                    onNewGameClick = { showHomeScreen = false },
                    onBackToHome = { showHomeScreen = true })
            }
        }
    }
}


@Composable
fun MyApp(showHomeScreen: Boolean, onNewGameClick: () -> Unit, onBackToHome: () -> Unit) {
    var humanWins by rememberSaveable { mutableIntStateOf(0) }
    var computerWins by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showHomeScreen) {
                HomeScreen(onNewGameClick)
            } else {
                GameScreen(
                    onHomeClick = onBackToHome,
                    humanWins = humanWins,
                    computerWins = computerWins,
                    updateScores = { newHumanWins, newComputerWins ->
                        humanWins = newHumanWins
                        computerWins = newComputerWins
                    }
                )

            }
        }
    }
}


@Composable
fun HomeScreen(onNewGameClick: () -> Unit) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(id = R.drawable.screen1_bg),
        contentDescription = "Home Screen",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onNewGameClick() },
            modifier = Modifier
                .width(170.dp)
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text(
                "New Game",
                color = Color.White,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showAboutDialog = true },
            modifier = Modifier
                .width(150.dp)
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
        ) {
            Text(
                "About",
                color = Color.White,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showAboutDialog) {
        AboutDialogBox(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun GameScreen(
    onHomeClick: () -> Unit, humanWins: Int,
    computerWins: Int,
    updateScores: (Int, Int) -> Unit
) {
    var showDiceThrow by rememberSaveable { mutableStateOf(false) }
    var userDiceResults by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var computerDiceResults by rememberSaveable { mutableStateOf(List(5) { 1 }) }
    var targetScore by rememberSaveable { mutableIntStateOf(101) }
    var userScore by rememberSaveable { mutableIntStateOf(0) }
    var computerScore by rememberSaveable { mutableIntStateOf(0) }
    var rollCount by rememberSaveable { mutableIntStateOf(0) }
    var isScoreButtonEnabled by rememberSaveable { mutableStateOf(false) }
    var showQuitDialog by rememberSaveable { mutableStateOf(false) }
    var selectedDice by rememberSaveable { mutableStateOf(List(5) { false }) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var winner by rememberSaveable { mutableStateOf("") }
    var inputText by rememberSaveable { mutableStateOf(targetScore.toString()) }
    val openDialog = rememberSaveable { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text("Set Target Score") },
            text = {
                Column {
                    Text("Please set the target score to win the game.")
                    TextField(
                        value = inputText,
                        onValueChange = {
                            // Only allow numeric input
                            if (it.isDigitsOnly()) {
                                inputText = it
                            }
                        },
                        label = { Text("Target Score") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    targetScore =
                        inputText.toIntOrNull() ?: 101  // Default to 101 if input is invalid
                    openDialog.value = false
                }) {
                    Text("OK")
                }
            })
    }


    Image(
        painter = painterResource(id = R.drawable.screen2_bg),
        contentDescription = "Game Screen",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {

//                Button(
//                    onClick = { showQuitDialog = true },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                ) {
//
//                    Image(
//                        modifier = Modifier.size(50.dp),
//                        painter = painterResource(R.drawable.home),
//                        contentDescription = "Home Button"
//                    )
//                }
                Column(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(start = 20.dp)
                ) {
                    Text(
                        "Target : $targetScore",
                        color = Color.White,
                        style = TextStyle(fontSize = 22.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.padding(start = 20.dp),
                ) {
                    Text(
                        text = "H:${humanWins} / C:${computerWins}",
                        color = Color.White,
                        style = TextStyle(fontSize = 22.sp), fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
//                Column(
//                    modifier = Modifier.width(100.dp).padding(end = 20.dp)
//                ) {
//                    Text("Target : $targetScore", color = Color.White, style = TextStyle(fontSize = 15.sp))
//                }
//                Spacer(modifier = Modifier.height(10.dp))
//                Column(
//                    modifier = Modifier.padding(end = 20.dp),
//                ) {
//                    Text(
//                        text = "H:${humanWins} / C:${computerWins}",
//                        color = Color.White,
//                        style = TextStyle(fontSize = 15.sp)
//                    )
//                }
//                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Image(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.drawable.user),
                        contentDescription = "User Icon"
                    )
                    Spacer(modifier = Modifier.width(30.dp))
                    Column(
                        modifier = Modifier.width(50.dp)
                    ) {
                        Text(
                            text = "$userScore",
                            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Image(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.drawable.computer),
                        contentDescription = "Computer Icon"
                    )
                    Spacer(modifier = Modifier.width(30.dp))
                    Column(
                        modifier = Modifier.width(50.dp)
                    ) {
                        Text(
                            text = "$computerScore",
                            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                }

            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        // COMPUTER DICE SECTION

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), contentAlignment = Alignment.TopEnd
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "COMPUTER",
                    fontSize = 25.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    computerDiceResults.forEach { diceValue ->
                        Image(
                            painter = painterResource(id = getDiceDrawable(diceValue)),
                            contentDescription = "Dice $diceValue",
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }

        // ANIMATION SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(180.dp)
                .padding(bottom = 50.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (showDiceThrow) {
                GifImageOnce(gifResId = R.drawable.dice_roll,
                    modifier = Modifier.size(200.dp),
                    onGifEnd = {
                        showDiceThrow = false
                        userDiceResults = userDiceResults.mapIndexed { index, oldValue ->
                            if (selectedDice[index]) oldValue else (1..6).random()
                        }
                        isScoreButtonEnabled = true
                        selectedDice = List(5) { false } // remove selection border after reRoll

                        if (rollCount >= 3) {  // If the player reaches 3 rolls, score automatically
                            userScore += userDiceResults.sum()
                            isScoreButtonEnabled = false
                            rollCount = 0 // Reset roll count

                            // Computer turn
                            computerDiceResults =
                                computerTurn(computerScore = computerScore, humanScore = userScore)
                            computerScore += computerDiceResults.sum()
                        }

                    })
            }
        }
        // USER DICE SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    userDiceResults.forEachIndexed { index, diceValue ->
                        Box(modifier = Modifier
                            .size(55.dp)
                            .padding(4.dp)
                            .clickable { // Allow dice select
                                if (rollCount > 0) {
                                    selectedDice = selectedDice
                                        .toMutableList()
                                        .apply {
                                            this[index] = !this[index] // Toggle selection
                                        }
                                }
                            }
                            .border(
                                width = 3.dp,
                                color = if (selectedDice[index]) Color(0xFF00BFFF)
                                else Color.Transparent,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = getDiceDrawable(diceValue)),
                                contentDescription = "Dice $diceValue",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                }
                Text(
                    text = "YOU",
                    fontSize = 25.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // BUTTON SECTION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp),
            horizontalArrangement = Arrangement.SpaceEvenly

        ) {
            Button(
                onClick = {
                    if (rollCount < 3 && !gameOver && !showDiceThrow) {  // Prevent extra rolls and allow throw if both tied
                        showDiceThrow = true
                        rollCount++
                    }
                },
                enabled = rollCount < 3 && !showDiceThrow && !gameOver, // Disable after 3 rolls
                modifier = Modifier
                    .width(140.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(
                    text = when {
                        showDiceThrow -> "Rolling.."
                        rollCount == 0 -> "Throw"
                        rollCount == 1 || rollCount == 2 -> "ReRoll"
                        else -> "Throw"
                    },
                    color = Color.White,
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium)
                )
            }
            Button(
                onClick = {
                    if (rollCount > 0) { // Ensure the player has thrown at least once
                        userScore += userDiceResults.sum()
                        isScoreButtonEnabled = false
                        selectedDice = List(5) { false } // remove selection border after score

                        // Make the computer use all remaining rolls
                        repeat(3 - rollCount) {
                            computerDiceResults =
                                computerTurn(computerScore = computerScore, humanScore = userScore)
                        }

                        computerScore += computerDiceResults.sum()
                        rollCount = 0 // Reset for next round

                        if (computerScore >= targetScore || userScore >= targetScore) {
                            if (computerScore == userScore) {
                                // Tie condition: Both players have scored the same and targetScore or more
                                winner = "Tie! Keep Rolling..."
                                gameOver = false // Allow user to continue rolling
                            } else if (computerScore > userScore) {
                                // Computer wins
                                winner = "You Lose!"
                                updateScores(
                                    humanWins,
                                    computerWins + 1
                                ) //update score of the computer
                                gameOver = true
                            } else {
                                // User wins
                                winner = "You Win!"
                                updateScores(humanWins + 1, computerWins) //update score of the user
                                gameOver = true
                            }
                        } else {
                            winner = ""
                            gameOver = false
                        }
                    }
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
                enabled = isScoreButtonEnabled && !showDiceThrow && rollCount > 0
            ) {
                Text(
                    if (!showDiceThrow && rollCount > 0) "Score" else "",
                    color = Color.White,
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium)
                )
            }
        }
        if (showQuitDialog) {
            QuitDialogBox(onQuit = {
                showQuitDialog = false
                onHomeClick() // Go to home screen
            }, onContinue = {
                showQuitDialog = false // Close the dialog
            })
        }

        if (gameOver) {
            AlertDialog(onDismissRequest = {},
                confirmButton = {
                    BackHandler {
                        onHomeClick()
                    }
                }, title = {
                    Text(
                        text = winner,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (winner == "You Win!") Color.Green else Color.Red
                    )
                }, text = {
                    Text(
                        "Game Over! Press the Back button to return to the Home Screen.",
                        fontSize = 18.sp
                    )
                })
        } else {
            // Prevent re-roll for the computer during the tie
            if (computerScore != userScore || userScore < targetScore) {
                BackHandler {
                    showQuitDialog = true
                }
            }
        }


    }
}


@Composable
fun QuitDialogBox(onQuit: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(onDismissRequest = { onContinue() },
        confirmButton = {
            TextButton(onClick = onQuit) {
                Text("Quit")
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) {
                Text("Continue")
            }
        },
        title = { Text("Are you sure?", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Text("Do you want to quit the game?", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        })


}


@Composable
fun AboutDialogBox(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Ok")
            }
        },
        title = { Text("About the Author", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Name: Pathum Wikasitha", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text("Student ID: w1953264", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "I confirm that I understand what plagiarism is and have read and understood " + "the section on Assessment Offences in the Essential Information for Students. " + "The work that I have submitted is entirely my own. Any work from other authors is duly referenced and acknowledged.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        })
}

@SuppressLint("ResourceType")
@Composable
fun GifImageOnce(@DrawableRes gifResId: Int, modifier: Modifier = Modifier, onGifEnd: () -> Unit) {
    val context = LocalContext.current
    val imageView = remember { ImageView(context) }

    LaunchedEffect(gifResId) {
        val source = ImageDecoder.createSource(context.resources, gifResId)
        val drawable: Drawable = ImageDecoder.decodeDrawable(source)

        val diceRollSound: MediaPlayer = MediaPlayer.create(context, R.raw.dice_rolling)

        if (drawable is AnimatedImageDrawable) {
            drawable.repeatCount = 0
            diceRollSound.start()
            drawable.start()
            imageView.setImageDrawable(drawable)

            drawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    onGifEnd()
                }
            })

            diceRollSound.setOnCompletionListener {
                it.release()
            }
        }
    }

    AndroidView(factory = { imageView }, modifier = modifier)
}


fun getDiceDrawable(value: Int): Int {
    return when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
}

/**
 * Computer Player Strategy for Dice ReRolling
 *
 * Strategy Overview:
 * - The computer can roll up to 3 times (2 reRolls maximum).
 * - The computer cannot see the human player's dice, but it knows both players' scores.
 * - The goal is to maximize the score while considering the current game state.
 *
 * Strategy Logic:
 * 1. Always keep dice values of 5 or 6 (high-value rolls).
 * 2. Always reRoll dice values of 1 or 2 (low-value rolls).
 * 3. If the computer is behind in score, reRoll 3s and 4s to increase the chance of getting higher values.
 * 4. If the computer is ahead, keep 3s and 4s for a safer play style.
 *
 * Justification:
 * - Keeping high values ensures a strong score.
 * - ReRolling low values improves the overall dice total.
 * - Adapting the strategy based on the score difference provides a competitive edge.
 *
 * Advantages:
 * - More optimized than random reRolling.
 * - Dynamically adjusts based on the game state.
 * - Ensures better decision-making without cheating (not seeing human dice).
 *
 * Disadvantages:
 * - Does not account for the probability of specific dice outcomes.
 * - In some cases, it may not maximize the best possible score.
 */

fun computerTurn(computerScore: Int, humanScore: Int): List<Int> {
    var computerDice = List(5) { (1..6).random() } // Initial roll
    val scoreDifference = computerScore - humanScore
    val maxRolls = 2 // The computer can reRoll up to 2 times

    repeat(maxRolls) { // Allow up to 2 reRolls based on strategy
        val reRollDice = computerDice.map { die ->
            when {
                die >= 5 -> false // Keep 5s and 6s
                die <= 2 -> true  // Always reRoll 1s and 2s
                scoreDifference < 0 && die == 3 -> true  // If losing, reRoll 3s
                scoreDifference < 0 -> true  // If losing, reRoll 4s
                else -> false // Otherwise, keep the dice
            }
        }

        computerDice = computerDice.mapIndexed { index, oldValue ->
            if (reRollDice[index]) (1..6).random() else oldValue
        }
    }

    return computerDice
}

