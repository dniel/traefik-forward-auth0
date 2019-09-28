package dniel.forwardauth.domain.service

import dniel.forwardauth.domain.Nonce
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class NonceGeneratorService {

    fun generate(): Nonce {
        return Nonce(UUID.randomUUID().toString().replace("-", ""))
    }

}