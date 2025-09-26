import com.monext.sdk.SdkTestHelper.Companion.createPaymentMethodData
import com.monext.sdk.internal.api.model.PaymentMethodCardCode
import com.monext.sdk.internal.data.PaymentMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaymentMethodTest {

    @Test
    fun testFromData_CB() {
        val data = createPaymentMethodData(PaymentMethodCardCode.CB)
        val method = PaymentMethod.fromData(data)
        assertTrue(method is PaymentMethod.CB)
        assertEquals(PaymentMethodCardCode.CB, method.cardCode)
        assertEquals(true, method.isCard)
    }

    @Test
    fun testFromData_MCVISA() {
        val data = createPaymentMethodData(PaymentMethodCardCode.MCVISA)
        val method = PaymentMethod.fromData(data)
        assertTrue(method is PaymentMethod.MCVisa)
        assertEquals(PaymentMethodCardCode.MCVISA, method.cardCode)
        assertEquals(true, method.isCard)
    }

    @Test
    fun testFromData_AMEX() {
        val data = createPaymentMethodData(PaymentMethodCardCode.AMEX)
        val method = PaymentMethod.fromData(data)
        assertTrue(method is PaymentMethod.Amex)
        assertEquals(PaymentMethodCardCode.AMEX, method.cardCode)
        assertEquals(true, method.isCard)
    }

    @Test
    fun testFromData_GooglePay() {
        val data = createPaymentMethodData("GOOGLE_PAY")
        val method = PaymentMethod.fromData(data)
        assertTrue(method is PaymentMethod.GooglePay)
        assertEquals("GOOGLE_PAY", method.cardCode)
        assertEquals(false, method.isCard)
    }

    @Test
    fun testFromData_AlternativePaymentMethod() {
        val data = createPaymentMethodData("SOMETHING_ELSE", hasForm = true)
        val method = PaymentMethod.fromData(data)
        assertTrue(method is PaymentMethod.AlternativePaymentMethod)
        assertEquals("SOMETHING_ELSE", method.cardCode)
        assertEquals(false, method.isCard)
    }

    @Test
    fun testFromData_null() {
        val data = createPaymentMethodData("UNKNOWN")
        val method = PaymentMethod.fromData(data)
        assertNull(method)
    }
}