package com.example.dicenew

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
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
    // Detect device orientation
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var showAboutDialog by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(id = if (isPortrait) R.drawable.screen1_bg else R.drawable.screen1_landscape),
        contentDescription = "Home Screen",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Button(
            onClick = { onNewGameClick() },
            modifier = Modifier
                .width(if (isPortrait) 170.dp else 165.dp)
                .height(if (isPortrait) 55.dp else 50.dp),
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
                .width(if (isPortrait) 170.dp else 165.dp)
                .height(if (isPortrait) 55.dp else 50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
        ) {
            Text(
                "About",
                color = Color.White,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = if (isPortrait) Modifier.height(140.dp) else Modifier.height(30.dp))
    }

    if (showAboutDialog) {
        AboutDialogBox(onDismiss = { showAboutDialog = false })
    }
}


@Composable
fun GameScreen(
    onHomeClick: () -> Unit,
    humanWins: Int,
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

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    if (openDialog.value) {
        // Set Target Score
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text("Set Target Score") },
            text = {
                Column {
                    Text("Please set the target score to win the game.")
                    TextField(
                        value = inputText,
                        onValueChange = {
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
                    targetScore = inputText.toIntOrNull() ?: 101
                    openDialog.value = false
                }) {
                    Text("OK")
                }
            })
    }
    // Background Image
    Image(
        painter = painterResource(id = if (isPortrait) R.drawable.screen2_bg else R.drawable.screen2_bg_landscape),
        contentDescription = "Game Screen",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    fun handleDiceRoll() {
        showDiceThrow = true
        rollCount++
    }

    // Function to handle scoring logic
    fun handleScore() {
        if (rollCount > 0) {  // Ensure the player has thrown at least once
            userScore += userDiceResults.sum()
            isScoreButtonEnabled = false
            selectedDice = List(5) { false }

            // Make the computer use all remaining rolls
            repeat(3 - rollCount) {
                computerDiceResults = computerTurn(computerScore, userScore)
            }

            computerScore += computerDiceResults.sum()
            rollCount = 0

            if (computerScore >= targetScore || userScore >= targetScore) {
                when {
                    // Tie condition: Both players have scored the same and targetScore or more
                    computerScore == userScore -> {
                        winner = "Tie! Keep Rolling..."
                        gameOver = false
                    }

                    // If Computer wins
                    computerScore > userScore -> {
                        winner = "You Lose!"
                        updateScores(humanWins, computerWins + 1)
                        gameOver = true
                    }

                    // If User wins
                    else -> {
                        winner = "You Win!"
                        updateScores(humanWins + 1, computerWins)
                        gameOver = true
                    }
                }
            } else {
                winner = ""
                gameOver = false
            }
        }
    }

    // Reusable function for dice row
    @Composable
    fun diceRow(
        diceResults: List<Int>,
        onDiceClick: ((Int) -> Unit)? = null,
        selectedDice: List<Boolean>? = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isPortrait) 20.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            diceResults.forEachIndexed { index, diceValue ->
                val boxModifier = Modifier
                    .size(if (isPortrait) 55.dp else 50.dp)
                    .padding(if (isPortrait) 4.dp else 2.dp)

                val clickModifier = if (onDiceClick != null && selectedDice != null) {
                    boxModifier.clickable { onDiceClick(index) }
                } else {
                    boxModifier
                }

                val borderModifier = if (selectedDice != null) {
                    clickModifier.border(
                        width = if (isPortrait) 3.dp else 2.dp,
                        color = if (selectedDice[index]) Color(0xFF00BFFF) else Color.Transparent,
                        shape = RoundedCornerShape(if (isPortrait) 8.dp else 6.dp)
                    )
                } else {
                    clickModifier
                }

                Box(modifier = borderModifier, contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = getDiceDrawable(diceValue)),
                        contentDescription = "Dice $diceValue",
                        modifier = Modifier.size(if (isPortrait) 50.dp else 45.dp)
                    )
                }
            }
        }
    }

    // Reusable function for player info
    @Composable
    fun playerInfo(score: Int, iconResId: Int) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(120.dp)
        ) {
            Image(
                modifier = Modifier.size(if (isPortrait) 28.dp else 20.dp),
                painter = painterResource(iconResId),
                contentDescription = "Player Icon"
            )
            Spacer(modifier = Modifier.width(if (isPortrait) 30.dp else 15.dp))
            Text(
                text = "$score",
                style = TextStyle(
                    fontSize = if (isPortrait) 24.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(end = 10.dp)
            )
        }
    }

    // Reusable function for game buttons
    @Composable
    fun gameButtons() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isPortrait) 50.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { handleDiceRoll() },
                enabled = rollCount < 3 && !showDiceThrow && !gameOver,
                modifier = Modifier
                    .width(if (isPortrait) 140.dp else 100.dp)
                    .height(if (isPortrait) 55.dp else 40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(
                    text = when {
                        showDiceThrow -> "Rolling.." // Show "Rolling..." while dice are being thrown
                        rollCount == 0 -> "Throw"
                        else -> "ReRoll"
                    },
                    color = Color.White,
                    style = TextStyle(
                        fontSize = if (isPortrait) 22.sp else 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Button(
                onClick = { handleScore() },
                modifier = Modifier
                    .width(if (isPortrait) 140.dp else 100.dp)
                    .height(if (isPortrait) 55.dp else 40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
                enabled = isScoreButtonEnabled && !showDiceThrow && rollCount > 0
            ) {
                Text(
                    if (!showDiceThrow && rollCount > 0) "Score" else "",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = if (isPortrait) 22.sp else 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
    // Portrait Layout
    if (isPortrait) {
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
                        modifier = Modifier
                            .width(250.dp)
                            .padding(start = 20.dp),
                    ) {
                        Text(
                            text = "H:${humanWins} / C:${computerWins}",
                            color = Color.White,
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Column {
                    playerInfo(userScore, R.drawable.user)
                    Spacer(modifier = Modifier.height(10.dp))
                    playerInfo(computerScore, R.drawable.computer)
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            // COMPUTER DICE SECTION
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "COMPUTER",
                        fontSize = 25.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    diceRow(computerDiceResults)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            // DICE ANIMATION SECTION
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
                            selectedDice = List(5) { false }

                            if (rollCount >= 3) {
                                userScore += userDiceResults.sum()
                                isScoreButtonEnabled = false
                                rollCount = 0

                                computerDiceResults =
                                    computerTurn(
                                        computerScore = computerScore,
                                        humanScore = userScore
                                    )
                                computerScore += computerDiceResults.sum()
                            }

                        })
                }
            }
            Spacer(modifier = Modifier.height(60.dp))

            // USER DICE SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    diceRow(userDiceResults, onDiceClick = { index ->
                        if (rollCount > 0) {
                            selectedDice = selectedDice
                                .toMutableList()
                                .apply {
                                    this[index] = !this[index]
                                }
                        }
                    }, selectedDice = selectedDice)

                    Text(
                        text = "YOU",
                        fontSize = 25.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // BUTTON SECTION
            gameButtons()

            if (showQuitDialog) {
                QuitDialogBox(onQuit = {
                    showQuitDialog = false
                    onHomeClick()
                }, onContinue = {
                    showQuitDialog = false
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
    } else {
        // Landscape Layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Target Score & Wins
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Target: $targetScore",
                    color = Color.White,
                    style = TextStyle(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "H:$humanWins / C:$computerWins",
                    color = Color.White,
                    style = TextStyle(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Computer Dice, Animation, and User Dice
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COMPUTER",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(5.dp))
                diceRow(computerDiceResults)

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showDiceThrow) {
                        GifImageOnce(
                            gifResId = R.drawable.dice_roll,
                            modifier = Modifier.size(100.dp),
                            onGifEnd = {
                                showDiceThrow = false
                                userDiceResults = userDiceResults.mapIndexed { index, oldValue ->
                                    if (selectedDice[index]) oldValue else (1..6).random()
                                }
                                isScoreButtonEnabled = true
                                selectedDice = List(5) { false }

                                if (rollCount >= 3) {
                                    userScore += userDiceResults.sum()
                                    isScoreButtonEnabled = false
                                    rollCount = 0

                                    // Computer turn
                                    computerDiceResults = computerTurn(computerScore, userScore)
                                    computerScore += computerDiceResults.sum()
                                }
                            })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    diceRow(userDiceResults, onDiceClick = { index ->
                        if (rollCount > 0) {
                            selectedDice = selectedDice
                                .toMutableList()
                                .apply {
                                    this[index] = !this[index]
                                }
                        }
                    }, selectedDice = selectedDice)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "YOU",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )

                    gameButtons()
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.End
            ) {
                playerInfo(userScore, R.drawable.user)
                Spacer(modifier = Modifier.height(10.dp))
                playerInfo(computerScore, R.drawable.computer)
            }
        }
        if (!gameOver) {
            BackHandler {
                showQuitDialog = true
            }
        }

        if (showQuitDialog) {
            QuitDialogBox(onQuit = {
                showQuitDialog = false
                onHomeClick()
            }, onContinue = {
                showQuitDialog = false
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

