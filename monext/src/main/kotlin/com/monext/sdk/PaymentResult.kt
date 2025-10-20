package com.monext.sdk

/** Result type of a payment transaction */
sealed interface PaymentResult {

    /** Used to determine the transaction state when receiving the [PaymentResult] */
    enum class TransactionState {

        /**
         * The transaction has not terminated. This occurs when the user dismisses the [PaymentSheet]
         * before finalizing their payment.
         */
        PAYMENT_INCOMPLETE,

        /**
         * The transaction completed successfully.
         * This is a terminal state.
         */
        PAYMENT_SUCCESS,

        /**
         * The transaction failed.
         * This is a terminal state.
         */
        PAYMENT_FAILURE,

        /**
         * The transaction pending.
         * This is a terminal state.
         */
        PAYMENT_PENDING,

        /**
         * The user canceled the payment.
         * This is a terminal state.
         */
        PAYMENT_CANCELED,

        /**
         * The provided token has expired.
         * This is a terminal state.
         */
        TOKEN_EXPIRED
    }

    /** The payment sheet has been dismissed by the user. Check the TransactionState to determine if the payment has completed */
    data class SheetDismissed(val currentState: TransactionState?): PaymentResult

    /** The payment transaction has reached a terminal state. */
    data class PaymentCompleted(val finalState: TransactionState?): PaymentResult
}