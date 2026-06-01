package rs.edu.raf.rma.showtime.ui.common

import androidx.compose.runtime.Composable

@Composable
actual fun ShowtimeBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Desktop nema Android sistemsko back dugme.
}
