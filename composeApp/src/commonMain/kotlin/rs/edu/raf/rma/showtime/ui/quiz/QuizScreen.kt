package rs.edu.raf.rma.showtime.ui.quiz

import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import rs.edu.raf.rma.showtime.ui.common.ShowtimeBackHandler
import rs.edu.raf.rma.showtime.domain.QuizAnswer

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onExit: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                QuizContract.SideEffect.ExitQuiz -> onExit()
            }
        }
    }
    QuizScreen(state = state, eventPublisher = viewModel::setEvent)
}

@Composable
private fun QuizScreen(
    state: QuizContract.UiState,
    eventPublisher: (QuizContract.UiEvent) -> Unit,
) {
    ShowtimeBackHandler(enabled = state.isRunning) {
        eventPublisher(QuizContract.UiEvent.AskAbandon)
    }

    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { eventPublisher(QuizContract.UiEvent.DismissAbandon) },
            title = { Text("Abandon quiz?") },
            text = { Text("Your progress will be lost.") },
            confirmButton = {
                Button(onClick = { eventPublisher(QuizContract.UiEvent.ConfirmAbandon) }) { Text("Abandon") }
            },
            dismissButton = {
                OutlinedButton(onClick = { eventPublisher(QuizContract.UiEvent.DismissAbandon) }) { Text("Continue") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.result != null -> ResultView(state.result, onAgain = { eventPublisher(QuizContract.UiEvent.Reset) })
            state.questions.isEmpty() -> StartView(error = state.error, onStart = { eventPublisher(QuizContract.UiEvent.Start) })
            else -> QuestionView(state = state, onAnswer = { eventPublisher(QuizContract.UiEvent.Answer(it)) })
        }
    }
}

@Composable
private fun StartView(error: String?, onStart: () -> Unit) {
    Text("Movie Knowledge", style = MaterialTheme.typography.headlineMedium)
    Text("10 questions • 60 seconds • no going back")
    Spacer(Modifier.height(20.dp))
    Button(onClick = onStart) { Text("Start quiz") }
    if (error != null) {
        Spacer(Modifier.height(16.dp))
        Text(error, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun QuestionView(state: QuizContract.UiState, onAnswer: (String) -> Unit) {
    val question = state.currentQuestion ?: return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Question ${state.currentIndex + 1}/10", fontWeight = FontWeight.Bold)
        Text("${state.timeLeftSeconds}s", fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(16.dp))
    AnimatedContent(targetState = question.id, label = "quizQuestion") { _ ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(question.prompt, style = MaterialTheme.typography.titleLarge)
            if (question.imageUrl != null) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = question.imageUrl,
                    contentDescription = question.prompt,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            question.answers.forEach { answer ->
                AnswerButton(answer = answer, state = state, onAnswer = onAnswer)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AnswerButton(answer: QuizAnswer, state: QuizContract.UiState, onAnswer: (String) -> Unit) {
    val question = state.currentQuestion ?: return
    val isCorrect = answer.id == question.correctAnswerId
    val isSelected = answer.id == state.selectedAnswerId
    val colors = when {
        state.showFeedback && isCorrect -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        state.showFeedback && isSelected && answer.id != question.correctAnswerId -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        else -> ButtonDefaults.buttonColors()
    }
    Button(
        onClick = { onAnswer(answer.id) },
        enabled = state.selectedAnswerId == null,
        colors = colors,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(answer.text) }
}

@Composable
private fun ResultView(result: rs.edu.raf.rma.showtime.domain.QuizResult, onAgain: () -> Unit) {
    Text("Result", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text("Score: ${result.score.formatScore()}/100", style = MaterialTheme.typography.titleLarge)
    Text("Correct: ${result.correct}")
    Text("Wrong: ${result.wrong}")
    Text("Used time: ${result.usedSeconds}s")
    Spacer(Modifier.height(18.dp))
    Button(onClick = onAgain) { Text("Back to start") }
}

private fun Double.formatScore(): String = (this * 100).roundToInt().let { cents ->
    val whole = cents / 100
    val decimal = (cents % 100).toString().padStart(2, '0')
    "$whole.$decimal"
}
