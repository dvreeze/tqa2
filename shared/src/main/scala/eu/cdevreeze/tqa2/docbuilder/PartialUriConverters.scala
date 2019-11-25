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

package eu.cdevreeze.tqa2.docbuilder

import java.net.URI

/**
 * Support for creating partial URI converters.
 *
 * @author Chris de Vreeze
 */
object PartialUriConverters {

  def identity: PartialUriConverter = { uri: URI => Some(uri) }

  /**
   * Turns the given catalog into a partial URI converter. It can return absolute and/or relative
   * URIs. Relative URIs are typically meant to be resolved inside ZIP files.
   *
   * The partial URI converter is only defined for URIs matching URI start strings in the catalog.
   */
  def fromCatalog(catalog: SimpleCatalog): PartialUriConverter = {
    catalog.findMappedUri
  }
}
