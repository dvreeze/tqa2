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

package eu.cdevreeze.tqa2.internal.standardtaxonomy.taxonomy.builder

import java.net.URI

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa2.internal.standardtaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.internal.standardtaxonomy.taxonomy.TaxonomyBase

/**
 * Default implementation of TaxonomyBaseBuilder.
 *
 * @author Chris de Vreeze
 */
final class DefaultTaxonomyBaseBuilder(
    val documentBuilder: DocumentBuilder,
    val dtsUriCollector: DtsUriCollector,
    val extraSubstitutionGroupMap: SubstitutionGroupMap
) extends TaxonomyBaseBuilder {

  def withExtraSubstitutionGroupMap(newExtraSubstitutionGroupMap: SubstitutionGroupMap): DefaultTaxonomyBaseBuilder = {
    new DefaultTaxonomyBaseBuilder(documentBuilder, dtsUriCollector, newExtraSubstitutionGroupMap)
  }

  /**
   * Given an entrypoint as URI set, builds the DTS of that entrypoint, as TaxonomyBase.
   */
  def build(entrypoint: Set[URI]): TaxonomyBase = {
    val dtsDocUris = dtsUriCollector.findAllDtsUris(entrypoint, { uri =>
      TaxonomyElem(documentBuilder.build(uri).documentElement)
    })

    val rootElems: Seq[TaxonomyElem] =
      dtsDocUris.toSeq.sortBy(_.toString).map(u => documentBuilder.build(u)).map(d => TaxonomyElem(d.documentElement))

    TaxonomyBase.build(rootElems, extraSubstitutionGroupMap)
  }
}

object DefaultTaxonomyBaseBuilder {

  def withDocumentBuilder(documentBuilder: DocumentBuilder): HasDocumentBuilder = {
    // Consider (re-)using a DocumentBuilder that is a CachingDocumentBuilder, for obvious reasons.

    new HasDocumentBuilder(documentBuilder)
  }

  final class HasDocumentBuilder(val documentBuilder: DocumentBuilder) {

    def withDtsUriCollector(dtsUriCollector: DtsUriCollector): DefaultTaxonomyBaseBuilder = {
      new DefaultTaxonomyBaseBuilder(documentBuilder, dtsUriCollector, SubstitutionGroupMap.Empty)
    }
  }
}
