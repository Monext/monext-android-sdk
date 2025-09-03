package com.monext.sdk

import android.app.Application
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.contract.TaskResultContracts
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.SessionInfo
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.data.sessionstate.PaymentMethodsList
import com.monext.sdk.internal.presentation.PaymentContainer
import com.monext.sdk.internal.presentation.SessionStateViewModel
import com.monext.sdk.internal.presentation.common.HeaderSection
import com.monext.sdk.internal.presentation.common.PaymentMethodsScreen
import com.monext.sdk.internal.presentation.common.PaymentOverlay
import com.monext.sdk.internal.preview.PreviewWrapper
import kotlinx.coroutines.launch

/**
 * Used to present the payment sheet. The preferred method to integrate the payment process in an application is with [PaymentBox].
 * Use this method if you need more extensive control than [PaymentBox] provides.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSheet(isShowing: Boolean, sessionToken: String, sdkContext: MnxtSDKContext, onResult: (PaymentResult) -> Unit, onIsShowingChange: ((Boolean) -> Unit)? = null) {

    if (isShowing) {

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val context = LocalContext.current

        val viewModel: SessionStateViewModel = viewModel(
            factory = SessionStateViewModel.Factory,
            extras = MutableCreationExtras().apply {
                set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, context.applicationContext as Application)
                set(SessionStateViewModel.ANDROID_CONTEXT, context)
                set(SessionStateViewModel.INTERNAL_SDK_CONTEXT, InternalSDKContext(sdkContext))
                set(SessionStateViewModel.LANGUAGE_KEY, sdkContext.config.language)
                set(SessionStateViewModel.INSPECTION_MODE_KEY, LocalInspectionMode.current)
            }
        )

        val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
        val scope = rememberCoroutineScope()

        val isShowingChangeWrapper : ((Boolean) -> Unit) =  {
            scope.launch {
                sheetState.hide()
                viewModel.clearSession()
                if (!sheetState.isVisible) {
                    onIsShowingChange?.invoke(false)
                }
            }
        }

        val sheetRadius = 8.dp
        ModalBottomSheet(
            onDismissRequest = {
                onResult(
                    PaymentResult.SheetDismissed(
                        viewModel.sessionState.value?.type?.toTransactionState()
                    )
                )
            },
            modifier = Modifier.statusBarsPadding().testTag("payment_bottom_sheet"),
            sheetState = sheetState,
            shape = RoundedCornerShape(
//                topStart = sdkContext.appearance.cardRadius,
//                topEnd = sdkContext.appearance.cardRadius,
                topStart = sheetRadius,
                topEnd = sheetRadius,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            containerColor = sdkContext.appearance.backgroundColor,
            dragHandle = {}
        ) {

            LaunchedEffect(sdkContext) {
                viewModel.updateContext(InternalSDKContext(sdkContext))
            }

            LaunchedEffect(Unit) {
                viewModel.initializeSessionState(sessionToken)
            }

            val sessionLoading by viewModel.sessionLoading.collectAsStateWithLifecycle()
            val canPayGooglePay by viewModel.canPayGooglePay.collectAsStateWithLifecycle()
            var showingOverlay by remember { mutableStateOf(PaymentOverlayToggle.off()) }

            val gPayLauncher = rememberLauncherForActivityResult(TaskResultContracts.GetPaymentDataResult()) { taskResult ->
                when (taskResult.status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        Log.d("GPAY", "payment success")
                        taskResult.result?.let { paymentData ->
                            viewModel.makeGooglePayPayment(paymentData) { showingOverlay = it }
                        }
                    }
                    CommonStatusCodes.CANCELED -> {
                        Log.d("GPAY", "payment canceled")
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        Log.d("GPAY", "payment error")
                        Log.e("GPAY", "${taskResult.status}")
                    }
                    CommonStatusCodes.INTERNAL_ERROR -> {
                        Log.d("GPAY", "internal error")
                    }
                    CommonStatusCodes.DEVELOPER_ERROR -> {
                        Log.d("GPAY", "developer error")
                    }
                }
            }
            CompositionLocalProvider(
                LocalAppearance provides sdkContext.appearance,
                LocalEnvironment provides sdkContext.environment,
                LocalSessionStateRepo provides viewModel.sessionStateRepository
            ) {

                Box {

                    Column(Modifier.animateContentSize()) {

                        HeaderSection {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
//                                if (!sheetState.isVisible) {
                                onResult(
                                    PaymentResult.SheetDismissed(
                                        viewModel.sessionState.value?.type?.toTransactionState()
                                    )
                                )
                                if (!sheetState.isVisible) {
                                    onIsShowingChange?.invoke(false)
                                }
//                                }
                            }
                        }

                        PaymentContainer(
                            sessionState,
                            paymentMethodsScreen = @Composable { list: PaymentMethodsList, info: SessionInfo ->
                                PaymentMethodsScreen(
                                    paymentMethodsList = list,
                                    paymentInfo = info,
                                    sessionLoading = sessionLoading,
                                    showsGooglePay = canPayGooglePay,
                                    gPayConfig = sdkContext.googlePayConfiguration,
                                    allowedPaymentMethods = viewModel.googlePayUtil?.allowedPaymentMethods.toString(),
                                    onClickGooglePay = {
                                        viewModel.loadPaymentDataTask(gPayLauncher::launch)
                                    },
                                    onMakePayment = { attempt ->
                                        viewModel.makePayment(attempt, context) { showingOverlay = it }
                                    }
                                )
                            },
                            onRedirectionComplete = { viewModel.updateSessionState(sessionToken) },
                            onRetry = { /* TODO: implement RETRY */ },
                            onResult = onResult,
                            onIsShowingChange = isShowingChangeWrapper
                        )
                    }

                    if (showingOverlay.showPaymentOverlay) {
                        PaymentOverlay(
                            params = showingOverlay,
                            Modifier.matchParentSize()
                        )
                    }
                }
            }
        }
    }
}

internal data class PaymentOverlayToggle(
    val showPaymentOverlay: Boolean,
    val paymentMethodCardCode: PaymentMethodCardCode?
) {
    companion object {
        fun on(cardCode: PaymentMethodCardCode?): PaymentOverlayToggle = PaymentOverlayToggle(true, cardCode)
        fun off(): PaymentOverlayToggle = PaymentOverlayToggle(false, null)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun PaymentSheetPreview() {

    PreviewWrapper {

        var showSheet by remember { mutableStateOf(true) }

        Column(
            Modifier.Companion.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Button({ showSheet = true }) {
                Text("Show Sheet")
            }
        }

        PaymentSheet(showSheet, "", MnxtSDKContext(MnxtEnvironment.Sandbox), {})
        {showSheet = it}
    }
}