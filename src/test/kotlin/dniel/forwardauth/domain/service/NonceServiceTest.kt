package dniel.forwardauth.domain.service

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

internal class NonceServiceTest {

    @Test
    fun should_generate_a_nonce() {
        val service = NonceGeneratorService()
        val nonce = service.generate()

        assertNotNull(nonce, "should have generated a nonce.")
    }

    @Test
    fun should_generate_different_nonces() {
        val service = NonceGeneratorService()
        val nonce1 = service.generate()
        val nonce2 = service.generate()

        assertNotSame(nonce1, nonce2, "should have generated different nonces")
    }


}