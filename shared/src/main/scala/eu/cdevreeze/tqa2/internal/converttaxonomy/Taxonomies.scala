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

package eu.cdevreeze.tqa2.internal.converttaxonomy

import java.net.URI

/**
 * Helper functions for taxonomies. For example, functions that know the distinction between core and "proper" taxonomy documents.
 *
 * @author Chris de Vreeze
 */
private[converttaxonomy] object Taxonomies {

  /**
   * Returns true if the document URI belongs to www.w3.org, www.xbrl.org or www.locfreexbrl.org. Note that among the
   * schemas there is also one linkbase file (having one simple link), namely http://www.xbrl.org/2016/severities.xml.
   */
  def isCoreDocumentUri(uri: URI): Boolean = {
    val host = Option(uri.getHost).getOrElse("")

    Set("www.w3.org", "www.xbrl.org", "www.locfreexbrl.org").contains(host)
  }

  /**
   * Returns true if function isCoreDocumentUri returns false.
   */
  def isProperTaxonomyDocumentUri(uri: URI): Boolean = {
    !isCoreDocumentUri(uri)
  }

}
