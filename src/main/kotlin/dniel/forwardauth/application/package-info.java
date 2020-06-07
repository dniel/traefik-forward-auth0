/**
 * The Application service layer.
 * <p>The application service layer is quite complex and spans three different architecture principles,
 * Domain Driven Design CQRS and Hexagonal Architecture.</p>
 * <p>In CQRS, the application service layer defines a set of services that act as boundary to the domain
 * layer. Any interaction with the domain layer passes through these application services.
 * The application services interface with domain and infrastructure layers to get the job done.
 * The domain layer also can talk to infrastructure layer.
 * </p>
 * <p>In Hexagonal Architecture, the application service layer defines the boundary between the outer
 * adapter and ports ring and the inner core application ring.</p>
 * </p>
 * <p>
 * TODO describe DDD part.
 * </p>
 *
 * <h4>References</h4>
 * li>
 * <ul>https://martinfowler.com/bliki/CQRS.html</ul>
 * <ul><a href="http://stochastyk.blogspot.com/2008/05/domain-services-in-domain-driven-design.html">Blog post
 * reference.</a></ul>
 * <ul><a href="https://martinfowler.com/tags/domain%20driven%20design.html">Marting Fowlers page about DDD.</a></ul>
 * <ul><a href="http://tech.groups.yahoo.com/group/domaindrivendesign/message/4342">
 * Another post about Application Services</a>:
 * <p>I've seen certain typical responsibilities – notification of stakeholders (including other systems) that an
 * event has occurred, control of transactions, interaction with external systems, initial access of domain
 * objects, etc. that I just don't feel are appropriate to allocate to the domain layer, based on some vague
 * romantic notion of reusability, separation of concerns.
 * </p>
 * </ul>
 * </li>
 */
package dniel.forwardauth.application;