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
 * Query part of Command Query Separation.
 * <p>This package contains the query part of the Command Query Separation and its done because we want to have one way to
 * retreieve data for
 * the UI in a way that is quick and not dependent on how the domain logic is written when we present it in the UI.</p>
 *
 * <p>The first architectural choice that I made, and in hindsight got accepted with relative ease, was to employ
 * <a href="http://dddstepbystep.com/wikis/ddd/blogged-command-query-separation-as-an-architectural-concept.aspx"
 * target="_blank">CQS</a> to simplify the system.</p>
 *
 * <p>Earlier projects had suffered quite badly from being abstractions of
 * legacy systems with new functionality bolted on. As </p>
 *
 * <p>far as possible I wanted to eliminate this
 * problem from our project, as I was all too aware that this was an easy trap for the team to fall into,
 * and would burn a lot of development time very fast.</p>
 *
 * <p>To make the system simpler I chose to separate
 * our query mechanisms from the &quot;domain&quot; type stuff, our commands and data writing/updating code.</p>
 *
 * <p>CQS is a pretty simple concept. One path through your system is responsible for querying
 * of anything much more than &quot;Get By ID&quot; type calls. This side of our system is responsible for reading data
 * from legacy systems,
 * reading data from legacy web services, and for reading data from our application database.</p>
 *
 * @link http://devlicio.us/blogs/casey/archive/2009/06/22/we-are-not-doing-ddd-part-two-cqs.aspx
 *
 * <h3>What Does CQS Mean at an Architectural Level</h3>
 *
 * <p>
 * In DDD, as presented by Eric Evans originally, the Repository concept is responsible for abstracting your
 * Entities and Value Objects away from the way they are persisted.
 * To retrieve an Entity, or a set of Entities, you would use a Repository method like .
 * GetAllOutstandingInvoices or pass a Specification into a generic query method.</p>
 *
 * <p>
 * This is a perfectly viable and valid way of querying your Entities, but it does have some issues
 * around some aspects of an application, these are generally referred to as Reporting issues.
 * Reporting issues may well be reports in the classic database style,
 * or may be such things as user interfaces that require searching, sorting, paging and filtering of data.</p>
 *
 * <p>
 * The concept of applying CQS at an architectural level says, our Domain and transactional operations
 * will use the Repositories, but for Reporting operations, we will use a separate mechanism.
 * The Command is the Domain operations, the Query is the Reporting operations.</p>
 *
 * <p>
 * Intressant side som snakker mye om QCS og DDDD som det tydeligvis ogs&aring; f&aring;tt navn som.
 * <a href="http://jonathan-oliver.blogspot.com/search/label/DDDD" target="_blank">Jonathan Oliver blog</a>
 * </p>
 */
package dniel.forwardauth.application.reporting;