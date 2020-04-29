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

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.common.xpointer.ShorthandPointer
import eu.cdevreeze.tqa2.common.xpointer.XPointer
import eu.cdevreeze.tqa2.locfreetaxonomy.dom._
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal.DefaultSchemaQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal.DefaultTaxonomySchemaQueryApi
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.anyElem

import scala.reflect.classTag

/**
 * Taxonomy base, which is like a taxonomy without knowledge about relationships. It forms the basis for building BasicTaxonomy
 * instances.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyBase private (
    val documents: Seq[TaxonomyDocument],
    val documentMap: Map[URI, TaxonomyDocument],
    val rootElems: Seq[TaxonomyElem],
    val rootElemMap: Map[URI, TaxonomyElem],
    val elemMap: Map[URI, Map[String, TaxonomyElem]],
    val conceptDeclarations: Seq[ConceptDeclaration],
    val conceptDeclarationsByEName: Map[EName, ConceptDeclaration],
    val extraProvidedSubstitutionGroupMap: SubstitutionGroupMap,
    val netSubstitutionGroupMap: SubstitutionGroupMap,
    val namedGlobalSchemaComponentMap: Map[EName, Seq[NamedGlobalSchemaComponent]]
) extends DefaultTaxonomySchemaQueryApi
    with DefaultSchemaQueryApi {

  override def substitutionGroupMap: SubstitutionGroupMap = netSubstitutionGroupMap

  def findAllLinkbases: Seq[Linkbase] = {
    rootElems.flatMap(_.findTopmostElemsOrSelfOfType(classTag[Linkbase])(anyElem))
  }

  def findDocumentByUri(uri: URI): Option[TaxonomyDocument] = {
    require(uri.isAbsolute, s"Expected absolute URI but got relative URI '$uri'")
    require(Option(uri.getFragment).isEmpty, s"Fragment not allowed, but got URI '$uri'")

    documentMap.get(uri)
  }

  def getDocumentByUri(uri: URI): TaxonomyDocument = {
    findDocumentByUri(uri).getOrElse(sys.error(s"Could not find document with URI '$uri"))
  }

  def findElemByUri(uri: URI): Option[TaxonomyElem] = {
    require(uri.isAbsolute, s"Expected absolute URI but got relative URI '$uri'")
    val docUri = withoutFragment(uri)
    val fragmentOption: Option[String] = Option(uri.getFragment)
    val xpointers: Seq[XPointer] = fragmentOption.map(fragment => XPointer.parseXPointers(fragment)).getOrElse(Seq.empty)

    xpointers match {
      case Seq(ShorthandPointer(id)) =>
        // Fast lookup (the normal situation)
        elemMap.get(docUri).flatMap(_.get(id))
      case Nil =>
        rootElemMap.get(docUri)
      case _ =>
        rootElemMap.get(docUri).flatMap(rootElem => XPointer.findElem(rootElem, xpointers))
    }
  }

  def getElemByUri(uri: URI): TaxonomyElem = {
    findElemByUri(uri).getOrElse(sys.error(s"Could not find element with URI '$uri"))
  }

  private def withoutFragment(uri: URI): URI = {
    val fragment: String = null // scalastyle:off
    new URI(uri.getScheme, uri.getSchemeSpecificPart, fragment)
  }
}

object TaxonomyBase {

  def build(documents: Seq[TaxonomyDocument], extraProvidedSubstitutionGroupMap: SubstitutionGroupMap): TaxonomyBase = {
    require(documents.forall(_.docUriOption.nonEmpty), s"Missing document URIs not allowed")

    val rootElems: Seq[TaxonomyElem] = documents.map(_.documentElement)

    val namedGlobalSchemaComponentMap: Map[EName, Seq[NamedGlobalSchemaComponent]] = computeNamedGlobalSchemaComponentMap(rootElems)

    val globalElementDeclarationMap: Map[EName, GlobalElementDeclaration] = namedGlobalSchemaComponentMap.view
      .mapValues(_.collect { case e: GlobalElementDeclaration => e })
      .filter(_._2.nonEmpty)
      .mapValues(_.head)
      .toMap

    val derivedSubstitutionGroupMap: SubstitutionGroupMap = computeDerivedSubstitutionGroupMap(globalElementDeclarationMap)

    val netSubstitutionGroupMap: SubstitutionGroupMap = derivedSubstitutionGroupMap.append(extraProvidedSubstitutionGroupMap)

    val conceptDecls: Seq[ConceptDeclaration] = findAllConceptDeclarations(rootElems, netSubstitutionGroupMap)

    val conceptDeclMap: Map[EName, ConceptDeclaration] = conceptDecls.groupBy(_.targetEName).view.mapValues(_.head).toMap

    val documentMap: Map[URI, TaxonomyDocument] = {
      documents.groupBy(_.docUriOption.ensuring(_.nonEmpty).get).view.mapValues(_.head).toMap
    }

    val rootElemMap: Map[URI, TaxonomyElem] = {
      documentMap.view.mapValues(_.documentElement).toMap
    }

    val elemMap: Map[URI, Map[String, TaxonomyElem]] = computeElemMap(rootElems)

    new TaxonomyBase(
      documents,
      documentMap,
      rootElems,
      rootElemMap,
      elemMap,
      conceptDecls,
      conceptDeclMap,
      extraProvidedSubstitutionGroupMap,
      netSubstitutionGroupMap,
      namedGlobalSchemaComponentMap
    )
  }

  private def computeNamedGlobalSchemaComponentMap(rootElems: Seq[TaxonomyElem]): Map[EName, Seq[NamedGlobalSchemaComponent]] = {
    findAllXsdElems(rootElems)
      .flatMap(_.findAllDescendantElemsOfType(classTag[NamedGlobalSchemaComponent]))
      .groupBy(_.targetEName)
  }

  private def findAllConceptDeclarations(
      rootElems: Seq[TaxonomyElem],
      substitutionGroupMap: SubstitutionGroupMap): Seq[ConceptDeclaration] = {

    val globalElemDecls: Seq[GlobalElementDeclaration] = findAllXsdElems(rootElems).flatMap { rootElem =>
      rootElem.findTopmostElemsOfType(classTag[GlobalElementDeclaration])(anyElem)
    }

    val conceptDeclarationBuilder = new ConceptDeclaration.Builder(substitutionGroupMap)

    globalElemDecls.flatMap(decl => conceptDeclarationBuilder.optConceptDeclaration(decl))
  }

  private def findAllXsdElems(rootElems: Seq[TaxonomyElem]): Seq[XsSchema] = {
    rootElems.flatMap(_.findTopmostElemsOrSelf(_.isRootElement)).flatMap {
      case e: XsSchema => Seq(e)
      case _           => Seq.empty
    }
  }

  /**
   * Returns the SubstitutionGroupMap that can be derived from this taxonomy base alone.
   * This is an expensive operation that should be performed only once, if possible.
   */
  private def computeDerivedSubstitutionGroupMap(
      globalElementDeclarationMap: Map[EName, GlobalElementDeclaration]): SubstitutionGroupMap = {
    val rawMappings: Map[EName, EName] =
      globalElementDeclarationMap.toSeq.flatMap {
        case (en, decl) => decl.substitutionGroupOption.map(sg => en -> sg)
      }.toMap

    val substGroups: Set[EName] = rawMappings.values.toSet

    val mappings: Map[EName, EName] = rawMappings.filter(kv => substGroups.contains(kv._1))

    SubstitutionGroupMap.from(mappings)
  }

  private def computeElemMap(rootElems: Seq[TaxonomyElem]): Map[URI, Map[String, TaxonomyElem]] = {
    rootElems.map { rootElem =>
      val elemMapInDoc: Map[String, TaxonomyElem] = rootElem
        .filterDescendantElemsOrSelf(_.attrOption(ENames.IdEName).nonEmpty)
        .map(e => e.attr(ENames.IdEName) -> e)
        .toMap

      rootElem.docUri -> elemMapInDoc
    }.toMap
  }
}
