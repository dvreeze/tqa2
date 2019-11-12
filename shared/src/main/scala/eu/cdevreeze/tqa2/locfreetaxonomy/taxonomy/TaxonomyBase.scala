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

package eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NamedGlobalSchemaComponent
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal.DefaultSchemaQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal.DefaultTaxonomySchemaQueryApi
import eu.cdevreeze.yaidom2.core.EName

/**
 * Taxonomy base, which is like a taxonomy without knowledge about relationships. It forms the basis for building BasicTaxonomy
 * instances.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyBase private(
  val rootElems: Seq[TaxonomyElem],
  val extraProvidedSubstitutionGroupMap: SubstitutionGroupMap,
  val netSubstitutionGroupMap: SubstitutionGroupMap,
  val namedGlobalSchemaComponentMap: Map[EName, Seq[NamedGlobalSchemaComponent]]
) extends DefaultTaxonomySchemaQueryApi with DefaultSchemaQueryApi {

  override def substitutionGroupMap: SubstitutionGroupMap = netSubstitutionGroupMap
}

object TaxonomyBase {

  def build(rootElems: Seq[TaxonomyElem], extraProvidedSubstitutionGroupMap: SubstitutionGroupMap): TaxonomyBase = {
    val namedGlobalSchemaComponentMap: Map[EName, Seq[NamedGlobalSchemaComponent]] = computeNamedGlobalSchemaComponentMap(rootElems)

    val globalElementDeclarationMap: Map[EName, GlobalElementDeclaration] = namedGlobalSchemaComponentMap.view
      .mapValues(_.collect { case e: GlobalElementDeclaration => e }).filter(_._2.nonEmpty).mapValues(_.head).toMap

    val derivedSubstitutionGroupMap: SubstitutionGroupMap = computeDerivedSubstitutionGroupMap(globalElementDeclarationMap)

    val netSubstitutionGroupMap: SubstitutionGroupMap = derivedSubstitutionGroupMap.append(extraProvidedSubstitutionGroupMap)

    new TaxonomyBase(rootElems, extraProvidedSubstitutionGroupMap, netSubstitutionGroupMap, namedGlobalSchemaComponentMap)
  }

  private def computeNamedGlobalSchemaComponentMap(rootElems: Seq[TaxonomyElem]): Map[EName, Seq[NamedGlobalSchemaComponent]] = {
    findAllXsdElems(rootElems).flatMap(_.filterDescendantElems(isNamedGlobalSchemaComponent))
      .collect { case e: NamedGlobalSchemaComponent => e }.groupBy(_.targetEName)
  }

  private def findAllXsdElems(rootElems: Seq[TaxonomyElem]): Seq[XsSchema] = {
    rootElems.flatMap(_.findTopmostElemsOrSelf(_.isRootElement)).flatMap {
      case e: XsSchema => Seq(e)
      case _ => Seq.empty
    }
  }

  private def isNamedGlobalSchemaComponent(e: TaxonomyElem): Boolean = e match {
    case _: NamedGlobalSchemaComponent => true
    case _ => false
  }

  /**
   * Returns the SubstitutionGroupMap that can be derived from this taxonomy base alone.
   * This is an expensive operation that should be performed only once, if possible.
   */
  private def computeDerivedSubstitutionGroupMap(globalElementDeclarationMap: Map[EName, GlobalElementDeclaration]): SubstitutionGroupMap = {
    val rawMappings: Map[EName, EName] =
      globalElementDeclarationMap.toSeq.flatMap {
        case (en, decl) => decl.substitutionGroupOption.map(sg => en -> sg)
      }.toMap

    val substGroups: Set[EName] = rawMappings.values.toSet

    val mappings: Map[EName, EName] = rawMappings.filter(kv => substGroups.contains(kv._1))

    SubstitutionGroupMap.from(mappings)
  }
}
