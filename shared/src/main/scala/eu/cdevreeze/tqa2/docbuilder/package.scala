/*
 * Copyright 2019-2019 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tqa2

import java.net.URI

/**
 * Document builders, for taxonomy files or other XML files (for example, XBRL instances). Most of this support for parsing
 * (or retrieving) documents targets both the JVM and the JS platforms.
 *
 * Typical document builders use an URI resolver, mapping document URIs (mostly HTTP(S) URIs) to local resources. Hence the
 * UriResolver and PartialUriResolver abstractions, where URI resolvers often combine multiple partial URI resolvers. A partial
 * URI resolver could for example resolve URIs in a specific ZIP file, such as a taxonomy package. URI resolvers and partial
 * URI resolvers typically use URI converters (from "remote" URIs to "local" URIs) and partial URI converters.
 *
 * This package also contains a simple XML catalog abstraction, as restricted by the XBRL taxonomy packages standard. Hence the
 * simple catalogs offered here only support URI rewriting rules. Simple catalogs are typically used as (partial) URI converters.
 *
 * Note that the (partial) URI converter and (partial) URI resolver abstractions together facilitate document builders that take
 * multiple ZIP files as input.
 *
 * @author Chris de Vreeze
 */
package object docbuilder {

  type PartialUriConverter = URI => Option[URI]

  type UriConverter = URI => URI

  type PartialUriResolver = URI => Option[XmlInputSource]

  type UriResolver = URI => XmlInputSource
}
