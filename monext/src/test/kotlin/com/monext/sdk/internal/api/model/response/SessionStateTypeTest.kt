package com.monext.sdk.internal.api.model.response

import com.monext.sdk.PaymentResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionStateTypeTest {

    @Test
    fun toTransactionState_paymentRedirectWithJavascript_returnsPaymentIncomplete() {
        // Given
        val type = SessionStateType.PAYMENT_REDIRECT_WITH_JAVASCRIPT

        // When
        val result = type.toTransactionState()

        // Then
        assertEquals(PaymentResult.TransactionState.PAYMENT_INCOMPLETE, result)
    }

    @Test
    fun toTransactionState_paymentMethodsList_returnsPaymentIncomplete() {
        assertEquals(
            PaymentResult.TransactionState.PAYMENT_INCOMPLETE,
            SessionStateType.PAYMENT_METHODS_LIST.toTransactionState()
        )
    }

    @Test
    fun toTransactionState_paymentRedirectNoResponse_returnsPaymentIncomplete() {
        assertEquals(
            PaymentResult.TransactionState.PAYMENT_INCOMPLETE,
            SessionStateType.PAYMENT_REDIRECT_NO_RESPONSE.toTransactionState()
        )
    }

    @Test
    fun toTransactionState_paymentSuccess_returnsPaymentSuccess() {
        assertEquals(
            PaymentResult.TransactionState.PAYMENT_SUCCESS,
            SessionStateType.PAYMENT_SUCCESS.toTransactionState()
        )
    }

    @Test
    fun toTransactionState_paymentFailure_returnsPaymentFailure() {
        assertEquals(
            PaymentResult.TransactionState.PAYMENT_FAILURE,
            SessionStateType.PAYMENT_FAILURE.toTransactionState()
        )
    }

    @Test
    fun toTransactionState_paymentCanceled_returnsPaymentCanceled() {
        assertEquals(
            PaymentResult.TransactionState.PAYMENT_CANCELED,
            SessionStateType.PAYMENT_CANCELED.toTransactionState()
        )
    }

    @Test
    fun toTransactionState_tokenExpired_returnsTokenExpired() {
        assertEquals(
            PaymentResult.TransactionState.TOKEN_EXPIRED,
            SessionStateType.TOKEN_EXPIRED.toTransactionState()
        )
    }

    @Test
    fun toTransactionState_unknown_returnsNull() {
        assertNull(SessionStateType.UNKNOWN.toTransactionState())
    }

    @Test
    fun isFinalState_paymentRedirectWithJavascript_returnsFalse() {
        // Given le flux Javascript est un état intermédiaire (spinner + exécution du JS)
        assertEquals(false, SessionStateType.PAYMENT_REDIRECT_WITH_JAVASCRIPT.isFinalState())
    }
}
