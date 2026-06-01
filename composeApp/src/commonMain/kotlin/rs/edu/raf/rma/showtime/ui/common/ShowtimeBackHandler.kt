package rs.edu.raf.rma.showtime.ui.common

import androidx.compose.runtime.Composable

@Composable
expect fun ShowtimeBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
