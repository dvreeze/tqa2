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
 * @author Chris de Vreeze
 */
package object docbuilder {

  type PartialUriConverter = URI => Option[URI]

  type PartialUriResolver = URI => Option[XmlInputSource]
}
