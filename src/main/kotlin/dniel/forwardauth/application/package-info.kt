/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * # The Application service layer.
 * The application service layer is quite complex and spans three different architecture principles,
 * Domain Driven Design, CQRS and Hexagonal Architecture.</p>
 *
 * ## In CQRS
 * The application service layer defines a set of services that act as boundary to the domain
 * layer. Any interaction with the domain layer passes through these application services.
 * The application services interface with domain and infrastructure layers to get the job done.
 *
 * ### In Hexagonal Architecture
 * The application service layer defines the boundary between the outer
 * adapter and ports ring and the inner core application ring.
 *
 * The point of using either of them is that you focus on the real problem you are trying to solve,
 * without using any technology or framework. The application is technology-agnostic,
 * and it is easy to migrate from a framework to another.
 * Both of them are called "clean" architectures because of that.
 * You have your application core free of framework code, annotations, etc.
 *
 * ## In DDD
 * - the (public front-facing) api of a domain
 * - responsible for loading and saving aggregates
 * - can access repositories and other infrastructure services
 * - is not part of a domains ubiquitous language
 * - should/could be a very thin layer on top of the domain (that mostly handles load/save of aggregates and delegates the rest to the domain)
 * - can contain pure read operations (for example the reporting part of the CQRS)
 *
 * ## References
 * - https://martinfowler.com/bliki/CQRS.html</ul>
 * - <a href="http://stochastyk.blogspot.com/2008/05/domain-services-in-domain-driven-design.html">Blog post reference.</a>
 * - <a href="https://martinfowler.com/tags/domain%20driven%20design.html">Marting Fowlers page about DDD.</a>
 * - <a href="http://tech.groups.yahoo.com/group/domaindrivendesign/message/4342">Another post about Application Services</a>
 * - <a href="https://en.wikipedia.org/wiki/Hexagonal_architecture_(software)">Hexagonal Architecture</a>
 *
 */
package dniel.forwardauth.application