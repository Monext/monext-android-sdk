package com.monext.sdk.internal.exception

enum class ThreeDsExceptionType() {
    INITIALISATION_FAILED,
    NOT_INITIALISED,
    UNSUPPORTED_NETWORK,
    THREE_DS_KEY_ERROR
}

internal class ThreeDsException(val type: ThreeDsExceptionType, override val message: String?, override val cause: Throwable?): Throwable() {

    internal constructor(type: ThreeDsExceptionType, message: String?) : this(type, message, null)
}