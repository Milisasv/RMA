package rs.edu.raf.rma.core.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: Int? = null,
    val username: String = "",
) {
    companion object {
        fun empty(): AuthData = AuthData(
            accessToken = "",
            refreshToken = "",
            userId = null,
            username = "",
        )
    }
}
