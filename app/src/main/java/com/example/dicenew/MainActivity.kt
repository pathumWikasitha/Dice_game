package com.example.dicenew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
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
import androidx.compose.runtime.getValue
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
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.dicenew.ui.theme.DiceNewTheme

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
                HomeScreen(onPlayClick = { showHomeScreen = false })
            } else {
                GameScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(onPlayClick: () -> Unit) {
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
            onClick = { onPlayClick() }, // Now updates showHomeScreen
            modifier = Modifier
                .width(140.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text(
                "Play",
                color = Color.White,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showAboutDialog = true },
            modifier = Modifier
                .width(140.dp)
                .height(50.dp),
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

@OptIn(UnstableApi::class)
@Composable
fun GameScreen() {
    var showVideo by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val videoUri = RawResourceDataSource.buildRawResourceUri(R.raw.dice_rolling)

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    Image(
        painter = painterResource(id = R.drawable.screen2_bg),
        contentDescription = "Game Screen",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dice_1),
                    contentDescription = "Dice 1",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dice_2),
                    contentDescription = "Dice 2",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dice_3),
                    contentDescription = "Dice 3",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dice_4),
                    contentDescription = "Dice 4",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.dice_5),
                    contentDescription = "Dice 5",
                    modifier = Modifier.size(50.dp)
                )
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(35.dp)
        ) {
            Button(
                onClick = {
                    showVideo = true
                    exoPlayer.playWhenReady = true
                    exoPlayer.play()
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(
                    "Throw",
                    color = Color.White,
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium)
                )
            }
            Spacer(modifier = Modifier.padding(20.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .width(140.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
            ) {
                Text(
                    "Score",
                    color = Color.White,
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium)
                )
            }
        }

    }
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
//
//@Composable
//fun ExoplayerExample() {
//
//    val context = LocalContext.current
//
//    val mediaItem = MediaBrowser.MediaItem.Builder()
//        .setUri(R.raw.)
//        .build()
//    val exoPlayer = remember(context, mediaItem) {
//        ExoPlayer.Builder(context)
//            .build()
//            .also { exoPlayer ->
//                exoPlayer.setMediaItem(mediaItem)
//                exoPlayer.prepare()
//                exoPlayer.playWhenReady = false
//                exoPlayer.repeatMode = REPEAT_MODE_OFF
//            }
//    }
//
//    DisposableEffect(
//        AndroidView(factory = {
//            StyledPlayerView(context).apply {
//                player = exoPlayer
//            }
//        })
//    ) {
//        onDispose { exoPlayer.release() }
//    }
//}