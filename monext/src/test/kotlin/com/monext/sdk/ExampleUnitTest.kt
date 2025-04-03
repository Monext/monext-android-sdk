package com.monext.sdk

import com.monext.sdk.internal.ext.passesLuhnCheck
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun passesLuhnCheck_success() {

        val validExamples = listOf(
            "4970109000000007",
            "4970105151515140",
            "5476430999999892",
            "5137340014122340",
            "375989111111119",
            "5395953000905432",
            "5033951000000004",
            "4970107009012080",
            "4970107009012080",
            "4970105555555544",
            "5476431111111119"
        )

        for (valid in validExamples) {
            assert(valid.passesLuhnCheck())
        }
    }

    @Test
    fun passesLuhnCheck_failure() {

        val invalidExamples = listOf(
            "4970109000000006",
            "4970105151515149",
            "5476430999999893",
            "5137340014122342",
            "375989111111118",
            "5395953000905436",
            "5033951000000005",
            "4970107009012081",
            "4970107009012081",
            "4970105555555545",
            "5476431111111110"
        )

        for (invalid in invalidExamples) {
            assertFalse(invalid.passesLuhnCheck())
        }
    }
}