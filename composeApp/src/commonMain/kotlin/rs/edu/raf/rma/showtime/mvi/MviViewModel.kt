package rs.edu.raf.rma.showtime.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event, Effect>(initialState: State) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>()
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    fun setEvent(event: Event) {
        viewModelScope.launch { handleEvent(event) }
    }

    protected fun setState(reducer: State.() -> State) {
        _state.getAndUpdate(reducer)
    }

    protected fun setEffect(effect: Effect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    protected abstract suspend fun handleEvent(event: Event)
}
