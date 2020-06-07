package dniel.forwardauth.domain.shared

import dniel.forwardauth.domain.shared.stereotypes.Repository

/**
 * Repository for Tokens.
 * This will store verified tokens so that an already verified
 * token is reused for next request to increase performance.
 */
interface TokenRepository : Repository {

}