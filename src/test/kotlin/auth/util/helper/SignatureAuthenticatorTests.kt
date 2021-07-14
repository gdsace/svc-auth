package auth.util.helper

import auth.model.service.Service
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class SignatureAuthenticatorTests {
    var mockServiceProps = Services()
    val validSignature = "hCcSOrZF8l7wdS8a8Sd17PMGhAbViKGb8J1+HrbTZSC4aRijIuoB10/Bi6eT+9BGHbmPu1ET9iJpJyq2MHrVkg=="
    val invalidSignature = "XCcSOrZF8l7wdS8a8Sd17PMGhAbViKGb8J1+HrbTZSC4aRijIuoB10/Bi6eT+9BGHbmPu1ET9iJpJyq2MHrVkg=="
    val mockSignatureLifetimeClockSkew: Long = 10000
    val validNonce = "1538646696023"
    val invalidNonce = "1538646696024"
    val mockService = Service(
        mapOf(
            "guid" to "serviceMock",
            "publicKey" to "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKYeZ1pJWL7adv4bRdutKtxVYmdkWrbSbBgN/edB54Fk8G8RukHeuXC7dFIgtndB1TR4xHbfIEJ+Uo6RhvoKZvcCAwEAAQ==",
            "payload" to mapOf(
                "userId" to "api-tester",
                "authorization" to arrayListOf(mapOf("id" to "*", "scopes" to arrayListOf("foobar:read")))
            )
        )
    )

    @MockK
    lateinit var mockProperties: Properties

    @InjectMockKs
    lateinit var signatureAuthenticator: SignatureAuthenticator

    @BeforeAll
    fun setupMock() {
        mockServiceProps.signatureLifetimeClockSkew = mockSignatureLifetimeClockSkew
        MockKAnnotations.init(this)
    }

    @Test
    fun verifyServicePassed() {
        val result = signatureAuthenticator.verifyService(validNonce, validSignature, mockService)

        assertTrue(result)
    }

    @Test
    fun verifyServiceFailedWithInvalidPayload() {
        val result = signatureAuthenticator.verifyService(invalidNonce, validSignature, mockService)

        assertFalse(result)
    }

    @Test
    fun verifyServiceFailedWithInvalidSignature() {
        val result = signatureAuthenticator.verifyService(validNonce, invalidSignature, mockService)

        assertFalse(result)
    }

    @Test
    fun verifyNoncePassed() {
        every { mockProperties.service } returns mockServiceProps
        val nonceToVerify = (System.currentTimeMillis() - 5000).toString()

        val result = signatureAuthenticator.verifyNonce(nonceToVerify)

        assertTrue(result)
    }

    @Test
    fun verifyNonceFailed() {
        every { mockProperties.service } returns mockServiceProps
        val nonceToVerify = (System.currentTimeMillis() - mockSignatureLifetimeClockSkew).toString()

        val result = signatureAuthenticator.verifyNonce(nonceToVerify)

        assertFalse(result)
    }

    @Test
    fun verifyNonceFailedWithFutureTime() {
        val nonceToVerify = (System.currentTimeMillis() + mockSignatureLifetimeClockSkew).toString()

        val result = signatureAuthenticator.verifyNonce(nonceToVerify)

        assertFalse(result)
    }
}
