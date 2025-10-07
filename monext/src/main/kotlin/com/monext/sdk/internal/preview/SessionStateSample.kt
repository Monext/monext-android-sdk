package com.monext.sdk.internal.preview

import com.monext.sdk.internal.data.sessionstate.AdditionalData
import com.monext.sdk.internal.data.sessionstate.FormOption
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.data.sessionstate.PaymentMethodData
import com.monext.sdk.internal.data.sessionstate.PaymentMethodsList
import com.monext.sdk.internal.data.sessionstate.PaymentRedirectNoResponse
import com.monext.sdk.internal.data.sessionstate.PaymentSuccess
import com.monext.sdk.internal.api.model.PointOfSaleAddress
import com.monext.sdk.internal.data.sessionstate.RedirectionData
import com.monext.sdk.internal.api.model.SessionInfo
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.data.sessionstate.FailureMessage
import com.monext.sdk.internal.data.sessionstate.PaymentFailure
import com.monext.sdk.internal.data.sessionstate.Ticket
import com.monext.sdk.internal.data.sessionstate.Wallet

internal interface PreviewSamples {

    companion object {

        val sessionInfo = SessionInfo(
            formattedAmount = "EUR111.00",
            orderDate = "03/21/2025 10:44",
            orderRef = "Default_134",
            formattedOrderAmount = "EUR111.00",
            orderAmountSmallestUnit = 11100,
            amountSmallestUnit = 11100,
            currencyDigits = 2,
            merchantCountry = "FR",
            currencyCode = "EUR",
            buyerIp = null,
            orderDeliveryMode = null,
            orderDeliveryTime = null
        )

        val posAddress = PointOfSaleAddress(
            address1 = "420 Easy Street",
            address2 = "",
            zipCode = "101010",
            city = "ANYWHERE"
        )

        val wallets = listOf(
            Wallet(
                cardCode = PaymentMethodCardCode.PAYPAL,
                index = 1,
                cardType = PaymentMethodCardCode.PAYPAL,
                isDefault = true,
                isExpired = false,
                expiredMore6Months = false,
                hasCustomLogo = false,
                customLogoRatio = 0,
                hasCustomLogoUrl = false,
                hasCustomLogoBase64 = false,
                isPmAPI = false,
                hasSpecificDisplay = false,
                options = emptyList(),
                additionalData = AdditionalData(
                    date = null,
                    holder = null,
                    pan = null,
                    merchantCapabilities = null,
                    networks = null,
                    applePayMerchantId = null,
                    applePayMerchantName = null,
                    savePaymentDataChecked = null,
                    email = "leia.organa@alderaan.gov"
                ),
                confirm = emptyList()
            ),
            Wallet(
                cardCode = PaymentMethodCardCode.CB,
                index = 2,
                cardType = PaymentMethodCardCode.CB,
                isDefault = false,
                isExpired = false,
                expiredMore6Months = false,
                hasCustomLogo = false,
                customLogoRatio = 0,
                hasCustomLogoUrl = false,
                hasCustomLogoBase64 = false,
                isPmAPI = false,
                hasSpecificDisplay = false,
                options = emptyList(),
                additionalData = AdditionalData(
                    date = "1230",
                    holder = "",
                    pan = "***-XX07",
                    merchantCapabilities = null,
                    networks = null,
                    applePayMerchantId = null,
                    applePayMerchantName = null,
                    savePaymentDataChecked = null,
                    email = null
                ),
                confirm = listOf(FormOption.CVV)
            )
        )

        val emptyAdditionalData = AdditionalData(
            merchantCapabilities = null,
            networks = null,
            applePayMerchantId = null,
            applePayMerchantName = null,
            savePaymentDataChecked = null,
            email = null,
            date = null,
            holder = null,
            pan = null
        )

        val paymentMethodsData = listOf<PaymentMethodData>(
            PaymentMethodData(
                cardCode = PaymentMethodCardCode.CB,
                contractNumber = "CB_FAKE",
                paymentMethodAction = 0,
                state = "AVAILABLE",
                hasLogo = false,
                hasForm = false,
                isIsolated = false,
                disabled = false,
                shouldBeInTopPosition = false,
                options = listOf(
                    FormOption.SAVE_PAYMENT_DATA,
                    FormOption.EXPI_DATE,
                    FormOption.CVV,
                    FormOption.ALT_NETWORK
                ),
                additionalData = emptyAdditionalData,
                requestContext = null
            ),
            PaymentMethodData(
                cardCode = PaymentMethodCardCode.AMEX,
                contractNumber = "AMEX_FAKE",
                paymentMethodAction = 0,
                state = "AVAILABLE",
                hasLogo = false,
                hasForm = false,
                isIsolated = false,
                disabled = false,
                shouldBeInTopPosition = false,
                options = listOf(
                    FormOption.SAVE_PAYMENT_DATA,
                    FormOption.HOLDER,
                    FormOption.EXPI_DATE,
                    FormOption.CVV,
                ),
                additionalData = emptyAdditionalData,
                requestContext = null
            ),
            PaymentMethodData(
                cardCode = PaymentMethodCardCode.PAYPAL,
                contractNumber = "PAYPAL_FAKE",
                paymentMethodAction = 0,
                state = "AVAILABLE",
                hasLogo = false,
                hasForm = false,
                isIsolated = false,
                disabled = false,
                shouldBeInTopPosition = false,
                options = listOf(
                    FormOption.SAVE_PAYMENT_DATA
                ),
                additionalData = emptyAdditionalData,
                requestContext = null
            ),
            PaymentMethodData(
                cardCode = PaymentMethodCardCode.IDEAL,
                contractNumber = "IDEAL_FAKE",
                paymentMethodAction = 0,
                state = "AVAILABLE",
                hasLogo = false,
                hasForm = false,
                isIsolated = false,
                disabled = false,
                shouldBeInTopPosition = false,
                options = emptyList(),
                additionalData = emptyAdditionalData,
                requestContext = null
            )
        )

        val paymentMethodsList = PaymentMethodsList(
            wallets = wallets,
            isOriginalCreditTransfer = false,
            needsDeviceFingerprint = false,
            paymentMethodsData = paymentMethodsData,
            scoringNeeded = false,
            sensitiveInputContentMasked = false,
            shouldChangePaymentMethodPosition = false
        )

        val sessionStatePaymentMethodsList = SessionState(
            token = "fake_token",
            type = SessionStateType.PAYMENT_METHODS_LIST,
            creationDate = "Fri Mar 21 10:45:00 CET 2025",
            cancelUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
            pointOfSale = "POS_Fake",
            language = "en",
            returnUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
            automaticRedirectAtSessionsEnd = false,
            info = sessionInfo,
            pointOfSaleAddress = posAddress,
            isSandbox = true,
            paymentMethodsList = paymentMethodsList,
            paymentRedirectNoResponse = null,
            paymentSuccess = null,
            paymentFailure = null
        )

        val paymentSuccess = PaymentSuccess(
            ticket = listOf(
                Ticket(
                    interline = false,
                    style = null,
                    key = "Date and time ",
                    value = "ON  25/03/2025 AT  12:33 CET",
                    t = 0
                ),
                Ticket(
                    interline = true,
                    style = null,
                    key = "Store ",
                    value = "POS_MyLuckyDay",
                    t = 0
                ),
                Ticket(
                    interline = false,
                    style = null,
                    key = "Terminal/Acceptor ",
                    value = "60 185 658 219 108",
                    t = 0
                ),
                Ticket(
                    interline = false,
                    style = null,
                    key = "Transaction number ",
                    value = "PPL25032512332065902",
                    t = 0
                ),
                Ticket(
                    interline = false,
                    style = null,
                    key = "Transaction type ",
                    value = "DEBIT VADS @",
                    t = 0
                ),
                Ticket(
                    interline = false,
                    style = "ticketAmount",
                    key = "AMOUNT \u003d ",
                    value = "333,00 EUR",
                    t = 0
                ),
                Ticket(
                    interline = false,
                    style = "ticketKeep",
                    key = null,
                    value = "CUSTOMER RECEIPT TO KEEP",
                    t = 0
                )
            ),
            paymentCard = "PAYPAL",
            selectedCardCode = "PAYPAL",
            selectedContractNumber = "PAYPAL_FAKE",
            displayTicket = true,
            fragmented = false
        )
        val paymentFailure = PaymentFailure(
            message = FailureMessage(true, "Paiement refusé", "ERROR"),
            selectedCardCode="CB",
            selectedContractNumber = "CB")

        val sessionStateSuccess = SessionState(
            token = "fake_token",
            type = SessionStateType.PAYMENT_SUCCESS,
            creationDate = "Tue Mar 25 12:33:22 CET 2025",
            cancelUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
            pointOfSale = "POS_Fake",
            language = "en",
            returnUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
            automaticRedirectAtSessionsEnd = false,
            info = sessionInfo,
            pointOfSaleAddress = posAddress,
            isSandbox = true,
            paymentMethodsList = null,
            paymentRedirectNoResponse = null,
            paymentSuccess = paymentSuccess,
            paymentFailure = null
        )

        fun buildSessionState(automaticRedirect : Boolean, type: SessionStateType) : SessionState {

            var paymentSuccessToUse : PaymentSuccess? = null
            if (type.equals(SessionStateType.PAYMENT_SUCCESS)) {
                paymentSuccessToUse = paymentSuccess
            }
            val paymentFailureToUse: PaymentFailure? = when(type) {
                SessionStateType.PAYMENT_FAILURE, SessionStateType.TOKEN_EXPIRED -> paymentFailure
                else -> null;
            }

            return SessionState(
                token = "fake_token",
                type = type,
                creationDate = "Tue Mar 25 12:33:22 CET 2025",
                cancelUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
                pointOfSale = "POS_Fake",
                language = "en",
                returnUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
                automaticRedirectAtSessionsEnd = automaticRedirect,
                info = sessionInfo,
                pointOfSaleAddress = posAddress,
                isSandbox = true,
                paymentMethodsList = null,
                paymentRedirectNoResponse = null,
                paymentSuccess = paymentSuccessToUse,
                paymentFailure = paymentFailureToUse)
        }

        val paymentRedirectNoResponse = PaymentRedirectNoResponse(
            cardCode = PaymentMethodCardCode.CB,
            contractNumber = "CB_FAKE",
            walletCardIndex = 0,
            redirectionData = RedirectionData(
                requestType = "POST",
                requestUrl = "https://3ds-acs.test.modirum.com/mdpayacs/3ds-method",
                requestFields = mapOf(
                    "threeDSMethodData" to "testData",
                    "3DSMethodData" to "testData"
                ),
                iframeEmbeddable = true,
                iframeHeight = 1,
                iframeWidth = 1,
                timeoutInMs = 10_000,
                hasPartnerLogo = true,
                partnerLogoKey = "cb",
                isCompletionMethod = true
            )
        )
    }
}