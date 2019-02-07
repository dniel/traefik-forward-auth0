package dniel.forwardauth.domain.service

import dniel.forwardauth.domain.Nonce
import dniel.forwardauth.domain.State
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class NonceGeneratorService {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    private val uuid = UUID.randomUUID()

    fun generate(): Nonce {
        return Nonce(uuid.toString().replace("-", ""))
    }

}