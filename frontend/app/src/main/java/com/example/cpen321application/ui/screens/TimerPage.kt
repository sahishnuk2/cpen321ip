package com.example.cpen321application.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private enum class TimerState {
    NOT_STARTED,
    RUNNING,
    PAUSED
}

private data class SingaporeFact(
    val fact: String,
    val example: String? = null
)

@Composable
fun TimerPage(onBackClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    var hoursInput by remember { mutableStateOf("") }
    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }

    var hoursError by remember { mutableStateOf<String?>(null) }
    var minutesError by remember { mutableStateOf<String?>(null) }
    var secondsError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    var remainingSeconds by remember { mutableStateOf(0L) }
    var timerState by remember { mutableStateOf(TimerState.NOT_STARTED) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    var showFacts by remember { mutableStateOf(false) }
    var factIndex by remember { mutableStateOf(0) }

    val facts = remember {
        listOf(
            SingaporeFact(
                fact = "\"Can\" can mean yes or okay.",
                example = """
                A: "Would you like a bottle or a can?"
                B: "Bottle."
                A: "Bottle?"
                B: "Can."
                
                Here, "can" means "okay, a bottle is fine" instead of "a can of drink"
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Paiseh\" means embarrassed, shy, or sorry.",
                example = """
                "Paiseh, I am late."
                
                Meaning:
                "Sorry, I am late."
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Can is can\" means something is possible, but it may be difficult or inconvenient.",
                example = """
                A: "Can finish this assignment tonight?"
                B: "Can is can, but very tiring."
                
                Meaning:
                "It is possible, but it will be difficult."
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Lah\" adds emotion, emphasis, or a casual tone.",
                example = """
                "Relax lah." — reassurance
                "Why like that lah?" — frustration
                "Can lah." — confidence
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Kiasu\" means being afraid of missing out or losing an advantage.",
                example = """
                Some people queue very early for popular food, limited-edition items, or free gifts.
                
                A kiasu person wants to make sure they do not miss out.
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "Singlish often shortens sentences to communicate quickly.",
                example = """
                "Can finish today?"
                
                Meaning:
                "Can you finish this by today?"
                
                Fewer words, same pressure.
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Bo jio\" means someone did not invite you.",
                example = """
                "You all went for supper and never tell me. Bo jio!"
                
                Meaning:
                "You went for supper without inviting me!"
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Eye power\" means watching other people work without helping.",
                example = """
                "Don't just eye power lah. Come and help us."
                
                Meaning:
                "Do not just watch. Come and help us."
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Kaypoh\" describes someone who is overly curious or nosy.",
                example = """
                "Why you so kaypoh?"
                
                Meaning:
                "Why are you being so nosy?"
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Agak-agak\" means to estimate or make a rough guess.",
                example = """
                A: "Do you think this is enough water in the drink?"
                B: "Just agak-agak lah"
                
                Meaning:
                "Just estimate how much water to add."
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Abuden\" is a sarcastic way of saying \"obviously\" or \"of course\".",
                example = """
                A: "You came here to eat?"
                B: "Abuden?"
                
                Meaning:
                "Obviously!"
            """.trimIndent()
            ),

            SingaporeFact(
                fact = "\"Alamak\" expresses surprise, shock, or frustration.",
                example = """
                "Alamak, I forgot my keys!"
                
                Meaning:
                "Oh no, I forgot my keys!"
            """.trimIndent()
            )
        )
    }
    var shuffledFacts by remember { mutableStateOf(facts) }

    fun runCountdown() {
        timerState = TimerState.RUNNING

        timerJob = coroutineScope.launch {
            while (remainingSeconds > 0 && isActive) {
                delay(1_000)
                remainingSeconds--
            }

            if (remainingSeconds == 0L && isActive) {
                shuffledFacts = facts.shuffled()
                showFacts = true
                factIndex = 0
            }
        }
    }

    fun startTimer() {
        hoursError = null
        minutesError = null
        secondsError = null
        timeError = null

        val hours = if (hoursInput.isBlank()) 0L else hoursInput.toLongOrNull()
        val minutes = if (minutesInput.isBlank()) 0 else minutesInput.toIntOrNull()
        val seconds = if (secondsInput.isBlank()) 0 else secondsInput.toIntOrNull()

        if (hours == null || hours < 0) {
            hoursError = "Invalid input"
        }

        if (minutes == null) {
            minutesError = "Invalid input"
        } else if (minutes !in 0..59) {
            minutesError = "Enter a value from 0 to 59"
        }

        if (seconds == null) {
            secondsError = "Invalid input"
        } else if (seconds !in 0..59) {
            secondsError = "Enter a value from 0 to 59"
        }

        if (hoursError != null || minutesError != null || secondsError != null) {
            return
        }

        val validHours = hours ?: return
        val validMinutes = minutes ?: return
        val validSeconds = seconds ?: return

        if (validHours > (Long.MAX_VALUE - validMinutes * 60L - validSeconds) / 3_600L) {
            hoursError = "Number is too large"
            return
        }

        val totalSeconds = validHours * 3_600L + validMinutes * 60L + validSeconds

        if (totalSeconds == 0L) {
            timeError = "Input a valid time"
            return
        }

        remainingSeconds = totalSeconds
        runCountdown()
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        remainingSeconds = 0L
        timerState = TimerState.NOT_STARTED
        timeError = null
    }

    fun leaveTimerPage() {
        timerJob?.cancel()
        timerJob = null
        onBackClick()
    }

    if (showFacts) {
        SingaporeFactsPage(
            fact = shuffledFacts[factIndex],
            onNextFact = {
                factIndex = (factIndex + 1) % shuffledFacts.size
            },
            onMainMenu = {
                timerJob?.cancel()
                onBackClick()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(remainingSeconds),
            fontSize = 56.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (timerState == TimerState.NOT_STARTED) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                TimerInput(
                    label = "Hours",
                    value = hoursInput,
                    error = hoursError,
                    onValueChange = {
                        hoursInput = it
                        hoursError = null
                        timeError = null
                    }
                )

                TimerInput(
                    label = "Minutes",
                    value = minutesInput,
                    error = minutesError,
                    onValueChange = {
                        minutesInput = it
                        minutesError = null
                        timeError = null
                    }
                )

                TimerInput(
                    label = "Seconds",
                    value = secondsInput,
                    error = secondsError,
                    onValueChange = {
                        secondsInput = it
                        secondsError = null
                        timeError = null
                    }
                )
            }

            timeError?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimerButton(
                text = "Start",
                enabled = timerState == TimerState.NOT_STARTED,
                onClick = { startTimer() }
            )

            TimerButton(
                text = "Pause",
                enabled = timerState == TimerState.RUNNING,
                onClick = {
                    timerJob?.cancel()
                    timerJob = null
                    timerState = TimerState.PAUSED
                }
            )

            TimerButton(
                text = "Resume",
                enabled = timerState == TimerState.PAUSED,
                onClick = { runCountdown() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimerButton(
                text = "Reset",
                enabled = timerState != TimerState.NOT_STARTED,
                onClick = { resetTimer() }
            )

            TimerButton(
                text = "Back",
                enabled = true,
                onClick = { leaveTimerPage() }
            )
        }
    }
}

@Composable
private fun TimerInput(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.width(105.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(105.dp)
        )

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimerButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(82.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SingaporeFactsPage(
    fact: SingaporeFact,
    onNextFact: () -> Unit,
    onMainMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Singlish Lesson Time!", fontSize = 32.sp)

        Text(text = "🇸🇬", fontSize = 32.sp)

        Text(
            text = "(Singaporean slang)",
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = fact.fact,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )

        fact.example?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onNextFact,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            ) {
                Text("Next Fact")
            }

            Button(
                onClick = onMainMenu,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            ) {
                Text("Main Menu")
            }
        }
    }
}

private fun formatTime(totalSeconds: Long): String {
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
