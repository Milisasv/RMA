package rs.edu.raf.rma.showtime.ui.quiz

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.domain.QuizRepository
import rs.edu.raf.rma.showtime.domain.QuizResult
import rs.edu.raf.rma.showtime.mvi.MviViewModel
import kotlin.math.min

class QuizViewModel(
    private val quizRepository: QuizRepository,
) : MviViewModel<QuizContract.UiState, QuizContract.UiEvent, QuizContract.SideEffect>(QuizContract.UiState()) {

    private var timerJob: Job? = null

    override suspend fun handleEvent(event: QuizContract.UiEvent) {
        when (event) {
            QuizContract.UiEvent.Start -> start()
            is QuizContract.UiEvent.Answer -> answer(event.answerId)
            QuizContract.UiEvent.AskAbandon,
            QuizContract.UiEvent.DismissAbandon -> setState { QuizContract.Reducer.reduce(this, event) }
            QuizContract.UiEvent.ConfirmAbandon -> abandon()
            QuizContract.UiEvent.Reset -> reset()
        }
    }

    private suspend fun start() {
        timerJob?.cancel()
        setState { QuizContract.UiState(isLoading = true) }
        runCatching { quizRepository.buildSession() }
            .onSuccess { questions ->
                setState { copy(questions = questions, isLoading = false, error = null) }
                startTimer()
            }
            .onFailure { throwable -> setState { QuizContract.UiState(error = throwable.message) } }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (state.value.timeLeftSeconds > 0 && state.value.result == null) {
                delay(1000)
                setState { copy(timeLeftSeconds = (timeLeftSeconds - 1).coerceAtLeast(0)) }
            }
            if (state.value.result == null && state.value.questions.isNotEmpty()) {
                finish()
            }
        }
    }

    private fun answer(answerId: String) {
        val question = state.value.currentQuestion ?: return
        if (state.value.selectedAnswerId != null) return
        val isCorrect = answerId == question.correctAnswerId
        viewModelScope.launch {
            setState {
                copy(
                    selectedAnswerId = answerId,
                    showFeedback = true,
                    correctCount = correctCount + if (isCorrect) 1 else 0,
                )
            }
            delay(850)
            if (state.value.result != null) return@launch
            if (state.value.currentIndex == state.value.questions.lastIndex) {
                finish()
            } else {
                setState { copy(currentIndex = currentIndex + 1, selectedAnswerId = null, showFeedback = false) }
            }
        }
    }

    private suspend fun finish() {
        timerJob?.cancel()
        val s = state.value
        val used = 60 - s.timeLeftSeconds
        val wrong = s.questions.size - s.correctCount
        val score = min(100.0, s.correctCount * (9.0 + s.timeLeftSeconds / 60.0))
        val result = QuizResult(
            score = score,
            correct = s.correctCount,
            wrong = wrong,
            usedSeconds = used,
        )
        quizRepository.saveResult(result)
        setState { copy(result = result, selectedAnswerId = null, showFeedback = false) }
    }

    private fun abandon() {
        timerJob?.cancel()
        setState { QuizContract.UiState() }
        setEffect(QuizContract.SideEffect.ExitQuiz)
    }

    private fun reset() {
        timerJob?.cancel()
        setState { QuizContract.Reducer.reduce(this, QuizContract.UiEvent.Reset) }
    }
}
