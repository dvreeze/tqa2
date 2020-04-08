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

import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper

/**
 * Converter from standard taxonomy non-entrypoint schema documents to locator-free taxonomy schema documents.
 *
 * @author Chris de Vreeze
 */
final class NonEntrypointSchemaConverter(val namespacePrefixMapper: NamespacePrefixMapper) {

  /**
   * Converts a non-entrypoint schema in the given (2nd parameter) TaxonomyBase to its locator-free counterpart, resulting in
   * a locator-free XsSchema returned by this function.
   *
   * The input TaxonomyBase parameter (2nd parameter) should be closed under DTS discovery rules.
   */
  def convertSchema(
      inputSchema: standardtaxonomy.dom.XsSchema,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): XsSchema = {

    ???
  }
}
