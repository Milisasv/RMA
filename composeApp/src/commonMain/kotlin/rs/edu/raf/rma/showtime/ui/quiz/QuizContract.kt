package rs.edu.raf.rma.showtime.ui.quiz

import rs.edu.raf.rma.showtime.domain.QuizQuestion
import rs.edu.raf.rma.showtime.domain.QuizResult

interface QuizContract {
    data class UiState(
        val questions: List<QuizQuestion> = emptyList(),
        val currentIndex: Int = 0,
        val selectedAnswerId: String? = null,
        val correctCount: Int = 0,
        val timeLeftSeconds: Int = 60,
        val isLoading: Boolean = false,
        val showFeedback: Boolean = false,
        val showAbandonDialog: Boolean = false,
        val result: QuizResult? = null,
        val error: String? = null,
    ) {
        val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)
        val isRunning: Boolean get() = questions.isNotEmpty() && result == null
    }

    sealed class UiEvent {
        data object Start : UiEvent()
        data class Answer(val answerId: String) : UiEvent()
        data object AskAbandon : UiEvent()
        data object DismissAbandon : UiEvent()
        data object ConfirmAbandon : UiEvent()
        data object Reset : UiEvent()
    }

    sealed class SideEffect {
        data object ExitQuiz : SideEffect()
    }

    object Reducer {
        fun reduce(state: UiState, event: UiEvent): UiState = when (event) {
            UiEvent.AskAbandon -> state.copy(showAbandonDialog = true)
            UiEvent.DismissAbandon -> state.copy(showAbandonDialog = false)
            UiEvent.Reset -> UiState()
            UiEvent.Start,
            is UiEvent.Answer,
            UiEvent.ConfirmAbandon -> state
        }
    }
}
