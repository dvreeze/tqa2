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

package eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder

import java.net.URI

/**
 * API for DTS URI collectors, in the locator-free taxonomy model.
 *
 * @author Chris de Vreeze
 */
trait DtsUriCollector {

  /**
   * Given an entrypoint as URI set, finds all document URIs of documents in the DTS of that entrypoint.
   */
  def findAllDtsUris(entrypoint: Set[URI]): Set[URI]
}
