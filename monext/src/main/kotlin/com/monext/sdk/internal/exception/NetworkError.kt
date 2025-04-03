package com.monext.sdk.internal.exception

internal sealed class NetworkError: Exception() {
    class BadRequest: NetworkError()
    class Unauthorized: NetworkError()
    class PaymentRequired: NetworkError()
    class Forbidden: NetworkError()
    class NotFound: NetworkError()
    class RequestEntityTooLarge: NetworkError()
    class UnprocessableEntity: NetworkError()
    data class Http(val responseCode: Int): NetworkError()

    data class ParseError(override val cause: Throwable) : NetworkError()
}