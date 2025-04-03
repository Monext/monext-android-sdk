
# Monext Android SDK – Getting Started

Quickly integrate payments into your app with our easy-to-use SDK.


## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Security Best Practices](#security-best-practices)
- [Usage](#usage)
- [Payment Process](#payment-process)
- [UI Customization](#ui-customization)
- [Google Pay Integration](#google-pay-integration)

## Overview

The Monext Android SDK is designed for seamless integration of payment features. It provides a drop-in Jetpack Compose view that can be easily embedded into your existing U

**Minimum Requirement:**
- Android 8.0 (API 26)

Before using the SDK, ensure you have an active Monext merchant account. Visit our [website](https://monext.fr) for details.

## Installation

Monext Android SDK is available via [Gradle](https://docs.gradle.org/current/userguide/userguide.html).

### Gradle installation

**Add the Package Dependency:**  
   ```kotlin
   implementation("com.monext:TODO:(insert latest version here)")
   ```

## Security Best Practices

⚠️ **Important:** API calls to Monext **must** be made from your backend server—not directly from your mobile application. This protects sensitive credentials such as:

- **BasicToken**
- **MerchantID**

Never expose these credentials in your app’s code.


## Usage

Once the SDK is installed and your Monext account is set up, you can integrate payment functionality into your app.

### Integrating `PaymentBox`

The main component is `PaymentBox`, a ready-to-use Jetpack Compose view that manages the entire payment process. It integrates the `PaymentSheet` composable:

```kotlin

@Composable fun ContentView {
    
    var sessionToken: String? by rememberSaveable { mutableStateOf(null) }
    
    Column {

        if (sessionToken != null) {

            PaymentBox(
                sessionToken,
                context = MnxtSDKContext(
                    environment = MnxtEnvironment.Sandbox,
                    appearance = selectedTheme.appearance()
                ),
               onResult = { result ->
                  when (result) {
                     is PaymentResult.SheetDismissed ->
                        Log.d("APP", "SheetDismissed: " + (result.currentState?.toString() ?: "TransactionState UNKNOWN"))
                     is PaymentResult.PaymentCompleted ->
                        Log.d("APP", "PaymentCompleted: " + (result.finalState?.toString() ?: "TransactionState UNKNOWN"))
                  }
               }
            ) { showSheet ->
                Button(showSheet) {
                    Text("Checkout")
                }
            }
        }
    }
}
```

### Using `PaymentSheet` for Custom UI

For greater control over the user interface, use the `PaymentSheet` composable directly:

```kotlin
@Composable
fun CustomPaymentSheet {

   var sessionToken: String? = ...
   val context = MnxtSDKContext(...)

   var showPaymentSheet by rememberSaveable { mutableStateOf(false) }

   Box {

      Button({
         if (sessionToken != null) {
            showPaymentSheet = true
         }
      }) {
          Text("Checkout")
      }

      PaymentSheet(showPaymentSheet, sessionToken ?: "", context) { result ->
         showPaymentSheet = false
         onResult(result)
      }
   }
}
```


## Payment Process

1. **Create a Monext Payment Session Token:**  
   Your backend must create a payment session via the Monext Retail API. Refer to the [session creation documentation](https://api-docs.retail.monext.com/reference/sessioncreate).

2. **Pass the Required Parameters to `PaymentBox`:**
    - `sessionToken`
    - `context`: an instance of MnxtContext  
      *(See [UI Customization](#ui-customization))*
    - Button content and `onResult` closure to handle payment outcomes

3. **Handle Payment Results:**  
   The `onResult` closure receives a `PaymentResult` when the payment session ends. The payment sheet dismisses automatically.

4. **Get a session detail**
   You can then retrieve the payment data via an API call with a GET Session, for more information: [Documentation](https://api-docs.retail.monext.com/reference/sessionget).

## UI Customization

Customize the payment sheet using `MnxtSDKConfiguration`. Modify colors, texts, and themes to match your branding.

All available UI customizations are contained in this class. You are not required to modify any element, the default is a light theme.
It is recommended to provide the `headerTitle` or `headerImage` at a minimum to identify your brand.


## Google Pay Integration

Monext Android SDK supports Google Pay . Setup requires:

**TODO**

