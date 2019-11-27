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

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkArc
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.RelationshipFactory
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase

/**
 * Default implementation of TaxonomyBuilder.
 *
 * @author Chris de Vreeze
 */
final class DefaultTaxonomyBuilder(
    val documentBuilder: DocumentBuilder,
    val dtsUriCollector: DtsUriCollector,
    val extraSubstitutionGroupMap: SubstitutionGroupMap,
    val relationshipFactory: RelationshipFactory,
    val arcFilter: XLinkArc => Boolean
) extends TaxonomyBuilder {

  def withExtraSubstitutionGroupMap(newExtraSubstitutionGroupMap: SubstitutionGroupMap): DefaultTaxonomyBuilder = {
    new DefaultTaxonomyBuilder(documentBuilder, dtsUriCollector, newExtraSubstitutionGroupMap, relationshipFactory, arcFilter)
  }

  def withArcFilter(newArcFilter: XLinkArc => Boolean): DefaultTaxonomyBuilder = {
    new DefaultTaxonomyBuilder(documentBuilder, dtsUriCollector, extraSubstitutionGroupMap, relationshipFactory, newArcFilter)
  }

  /**
   * Given an entrypoint as URI set, builds the DTS of that entrypoint, as BasicTaxonomy.
   */
  def build(entrypoint: Set[URI]): BasicTaxonomy = {
    val dtsDocUris = dtsUriCollector.findAllDtsUris(entrypoint, { uri => TaxonomyElem(documentBuilder.build(uri).documentElement) })

    val rootElems: Seq[TaxonomyElem] =
      dtsDocUris.toSeq.sortBy(_.toString).map(u => documentBuilder.build(u)).map(d => TaxonomyElem(d.documentElement))

    val taxonomyBase = TaxonomyBase.build(rootElems, extraSubstitutionGroupMap)

    BasicTaxonomy.build(taxonomyBase, relationshipFactory, arcFilter)
  }
}

object DefaultTaxonomyBuilder {

  def withDocumentBuilder(documentBuilder: DocumentBuilder): HasDocumentBuilder = {
    // Consider (re-)using a DocumentBuilder that is a CachingDocumentBuilder, for obvious reasons.

    new HasDocumentBuilder(documentBuilder)
  }

  final class HasDocumentBuilder(val documentBuilder: DocumentBuilder) {

    def withDtsUriCollector(dtsUriCollector: DtsUriCollector): HasDtsUriCollector = {
      new HasDtsUriCollector(documentBuilder, dtsUriCollector)
    }
  }

  final class HasDtsUriCollector(val documentBuilder: DocumentBuilder, val dtsUriCollector: DtsUriCollector) {

    def withRelationshipFactory(relationshipFactory: RelationshipFactory): DefaultTaxonomyBuilder = {
      new DefaultTaxonomyBuilder(documentBuilder, dtsUriCollector, SubstitutionGroupMap.Empty, relationshipFactory, _ => true)
    }

    def withDefaultRelationshipFactory: DefaultTaxonomyBuilder = {
      withRelationshipFactory(DefaultRelationshipFactory)
    }
  }
}
