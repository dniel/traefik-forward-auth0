package dniel.forwardauth.domain.service

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

internal class NonceServiceTest {

    @Test
    fun should_generate_a_nonce() {
        val service = NonceService()
        val nonce = service.create()

        assertNotNull(nonce, "should have generated a nonce.")
    }

    @Test
    fun should_generate_different_nonces() {
        val service = NonceService()
        val nonce1 = service.create()
        val nonce2 = service.create()

        assertNotSame(nonce1, nonce2, "should have generated different nonces")
    }


}