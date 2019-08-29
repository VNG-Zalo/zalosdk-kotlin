package com.zing.zalo.zalosdk.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.zing.zalo.zalosdk.auth.validateauthcode.ValidateOAuthCodeCallback
import com.zing.zalo.zalosdk.auth.validateauthcode.ValidateOAuthCodeTask
import com.zing.zalo.zalosdk.core.http.HttpClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class ValidateOAuthCodeTest {
    private val resultAuth =
        "{\"data\":{\"msg\":\"The code is still valid\",\"uid\":7793733042068913573,\"expires_in\":1568446493935},\"error\":0}"

    @get:Rule
    private lateinit var context: Context
    private lateinit var validateOAuthCodeTask: ValidateOAuthCodeTask

    @MockK
    private lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `validate auth code`() {
        //1. setup
        val appID = "1829577289837795818"
        val authCode =
            "QmSGfhENW4CrBKIGkuMZHH1XHEppsxP7K6GBn-7zZsG9NWsxlkRDR4PZAihHjizZDWC_oA6kacGj2nJBpQkG6NSJNCdwhBrTMKjDmeVsf6mlLIBwf-kWMKLU7hc2j8SiKq16cUApk3KN5pF7u-_yN7ugT-RdqQDo2pCoiOM2x1y68dkkcvUi0p4wMTRbnerZS05yiCl1e59JOq_tzA-RAoaZL92qhVuxN61kfuJrJ0aRaBdgemzsBa2qXVUnQcSuTRxspU9dRXWJee--nme_UIsmXR9P9d7UAn3IdA0y"
        val version = "4.0"

        //2a. run & verify
        validateOAuthCodeTask =
            ValidateOAuthCodeTask(
                authCode,
                appID,
                version,
                true,
                object : ValidateOAuthCodeCallback {
                    override fun onValidateComplete(
                        validated: Boolean,
                        errorCode: Int,
                        userId: Long,
                        authCode: String?
                    ) {
                        Assert.assertEquals(7793733042068913573, userId)
                    }
                })
        validateOAuthCodeTask.execute()
    }

    @Test
    fun `validate auth code using mock`() {
        //1. setup
        val appID = "1829577289837795818"
        val authCode =
            "QmSGfhENW4CrBKIGkuMZHH1XHEppsxP7K6GBn-7zZsG9NWsxlkRDR4PZAihHjizZDWC_oA6kacGj2nJBpQkG6NSJNCdwhBrTMKjDmeVsf6mlLIBwf-kWMKLU7hc2j8SiKq16cUApk3KN5pF7u-_yN7ugT-RdqQDo2pCoiOM2x1y68dkkcvUi0p4wMTRbnerZS05yiCl1e59JOq_tzA-RAoaZL92qhVuxN61kfuJrJ0aRaBdgemzsBa2qXVUnQcSuTRxspU9dRXWJee--nme_UIsmXR9P9d7UAn3IdA0y"
        val version = "4.0"

        //2. run with mock & verify

        //2.a ok request
        every { httpClient.send(any()).getText() } returns resultAuth

        validateOAuthCodeTask =
            ValidateOAuthCodeTask(
                authCode,
                appID,
                version,
                true,
                object : ValidateOAuthCodeCallback {
                    override fun onValidateComplete(
                        validated: Boolean,
                        errorCode: Int,
                        userId: Long,
                        authCode: String?
                    ) {
                        Assert.assertEquals(7793733042068913573, userId)
                    }
                })

        validateOAuthCodeTask.setHttpClient(httpClient)
        validateOAuthCodeTask.execute()

        //2.b fail reques
        every { httpClient.send(any()).getText() } returns "123"


        validateOAuthCodeTask =
            ValidateOAuthCodeTask(
                authCode,
                appID,
                version,
                true,
                object : ValidateOAuthCodeCallback {
                    override fun onValidateComplete(
                        validated: Boolean,
                        errorCode: Int,
                        userId: Long,
                        authCode: String?
                    ) {
                        Assert.assertEquals(-1, userId)
                        Assert.assertEquals(false, validated)
                    }
                })

        validateOAuthCodeTask.setHttpClient(httpClient)
        validateOAuthCodeTask.execute()


    }
}