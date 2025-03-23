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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dicenew.ui.theme.DiceNewTheme
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiceNewTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    var showHomeScreen by remember { mutableStateOf(true) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showHomeScreen) {
                HomeScreen(onNewGameClick = { showHomeScreen = false })
            } else {
                GameScreen(onHomeClick = { showHomeScreen = true })
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
fun GameScreen(onHomeClick: () -> Unit) {
    var showDiesThrow by remember { mutableStateOf(false) }
    var userDiceResults by remember { mutableStateOf(List(5) { 1 }) }
    var computerDiceResults by remember { mutableStateOf(List(5) { 1 }) }
    var userScore by remember { mutableIntStateOf(0) }
    var computerScore by remember { mutableIntStateOf(0) }
    var rollCount by remember { mutableIntStateOf(0) }
    var isScoreButtonEnabled by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }

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
                Button(
                    onClick = { showQuitDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {

                    Image(
                        modifier = Modifier.size(50.dp),
                        painter = painterResource(R.drawable.home),
                        contentDescription = "Home Button"
                    )
                }
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Image(
                        modifier = Modifier
                            .size(30.dp),
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
                        modifier = Modifier
                            .size(30.dp),
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopEnd
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
            if (showDiesThrow) {
                GifImageOnce(
                    gifResId = R.drawable.dice_roll,
                    modifier = Modifier.size(200.dp),
                    onGifEnd = {
                        showDiesThrow = false
                        userDiceResults = List(5) { (1..6).random() }
                        isScoreButtonEnabled = true

                        if (rollCount >= 3) {  // If the player reaches 3 rolls, score automatically
                            userScore += userDiceResults.sum()
                            isScoreButtonEnabled = false
                            rollCount = 0 // Reset roll count

                            // Computer turn
                            computerDiceResults = computerTurn()
                            computerScore += computerDiceResults.sum()
                        }

                    }
                )
            }
        }

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
                    userDiceResults.forEach { diceValue ->
                        Image(
                            painter = painterResource(id = getDiceDrawable(diceValue)),
                            contentDescription = "Dice $diceValue",
                            modifier = Modifier.size(50.dp)
                        )
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
                    if (rollCount < 3) {  // Prevent extra rolls
                        showDiesThrow = true
                        rollCount++
                    }
                },
                enabled = rollCount < 3 && !showDiesThrow, // Disable after 3 rolls
                modifier = Modifier
                    .width(140.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(
                    text = when {
                        showDiesThrow -> "Rolling.."
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
                        computerDiceResults = computerTurn()
                        computerScore += computerDiceResults.sum()
                        rollCount = 0 // Reset for next round
                    }
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
                enabled = isScoreButtonEnabled && !showDiesThrow
            ) {
                Text(
                    if (!showDiesThrow && rollCount > 0) "Score" else "",
                    color = Color.White,
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium)
                )
            }
        }
        if (showQuitDialog) {
            QuitDialogBox(
                onQuit = {
                    showQuitDialog = false
                    onHomeClick() // Go to home screen
                },
                onContinue = {
                    showQuitDialog = false // Close the dialog
                }
            )
        }
    }
}

@Composable
fun QuitDialogBox(onQuit: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onContinue() },
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
        }
    )


}


@Composable
fun AboutDialogBox(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    text = "I confirm that I understand what plagiarism is and have read and understood " +
                            "the section on Assessment Offences in the Essential Information for Students. " +
                            "The work that I have submitted is entirely my own. Any work from other authors is duly referenced and acknowledged.",
                    fontSize = 16.sp, fontWeight = FontWeight.Normal
                )
            }
        }
    )
}

@SuppressLint("ResourceType")
@Composable
fun GifImageOnce(@DrawableRes gifResId: Int, modifier: Modifier = Modifier, onGifEnd: () -> Unit) {
    val context = LocalContext.current
    val imageView = remember { ImageView(context) }

    LaunchedEffect(gifResId) {
        val inputStream: InputStream = context.resources.openRawResource(gifResId)
        val source = ImageDecoder.createSource(inputStream.readBytes())
        val drawable: Drawable = ImageDecoder.decodeDrawable(source)

        // Load and prepare the sound
        val diceRollSound: MediaPlayer = MediaPlayer.create(context, R.raw.dice_rolling)

        if (drawable is AnimatedImageDrawable) {
            drawable.repeatCount = 0  // Play GIF once
            diceRollSound.start()
            drawable.start()
            imageView.setImageDrawable(drawable)

            // Handle animation end
            drawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    onGifEnd() // Call the callback when finished
                }
            })

            // Release sound resources after completion
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

fun computerTurn(): List<Int> {
    val initialRoll = List(5) { (1..6).random() }
    return if (initialRoll.count { it >= 4 } >= 2) {  // Keep high values
        initialRoll
    } else {
        List(5) { (1..6).random() } // Re-roll all dice
    }
}

