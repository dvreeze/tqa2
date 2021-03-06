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

package eu.cdevreeze.tqa2.locfreetaxonomy.dom

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.FragmentKey
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.locfreetaxonomy.common.PeriodType
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

/**
 * Concept declaration, wrapping a `GlobalElementDeclaration`. It must be in substitution group xbrli:item or xbrli:tuple,
 * either directly or indirectly.
 *
 * There are no sub-classes for domain members, because as global element declarations they are defined in the Dimensions specification
 * in the exact same way that primary items are defined. Therefore primary items and dimension members are indistinguishable.
 *
 * In order to build a `ConceptDeclaration` from a `GlobalElementDeclaration`, the builder needs a `SubstitutionGroupMap` as context.
 * The created `ConceptDeclaration` does not retain that used `SubstitutionGroupMap`. As a consequence, these concept declaration objects
 * only make sense in a context where the used substitution group map is fixed. In taxonomies that know their substitution group map, this
 * is clearly the case. In other words, outside the context of a taxonomy that knows its substitution group map, concept declarations
 * are not "portable" objects, whereas the underlying global element declarations are.
 *
 * @author Chris de Vreeze
 */
sealed trait ConceptDeclaration {

  def globalElementDeclaration: GlobalElementDeclaration

  final def key: FragmentKey = {
    globalElementDeclaration.fragmentKey
  }

  final def targetEName: EName = {
    globalElementDeclaration.targetEName
  }

  final def isAbstract: Boolean = {
    globalElementDeclaration.isAbstract
  }

  final def isConcrete: Boolean = {
    globalElementDeclaration.isConcrete
  }

  final def substitutionGroupOption: Option[EName] = {
    globalElementDeclaration.substitutionGroupOption
  }

  final def backingElem: BackingNodes.Elem = {
    globalElementDeclaration.underlyingElem
  }

  final override def equals(other: Any): Boolean = other match {
    case other: ConceptDeclaration => globalElementDeclaration == other.globalElementDeclaration
    case _ => false
  }

  final override def hashCode: Int = {
    globalElementDeclaration.hashCode
  }
}

/**
 * Item declaration. It must be in the xbrli:item substitution group, directly or indirectly.
 */
sealed trait ItemDeclaration extends ConceptDeclaration {

  final def periodType: PeriodType = {
    globalElementDeclaration.periodTypeOption.getOrElse(sys.error(s"Missing xbrli:periodType attribute"))
  }
}

/**
 * Tuple declaration. It must be in the xbrli:tuple substitution group, directly or indirectly.
 */
final class TupleDeclaration private[dom] (val globalElementDeclaration: GlobalElementDeclaration) extends ConceptDeclaration

/**
 * Primary item declaration. It must be in the xbrli:item substitution group but neither in the xbrldt:hypercubeItem nor in the xbrldt:dimensionItem substitution groups.
 *
 * A primary item may be used as explicit dimension member.
 *
 * Note that in the Dimensions specification, primary item declarations and domain-member declarations have exactly the same
 * definition! Although in a taxonomy the dimensional relationships make clear whether an item plays the role of primary item
 * or of domain-member, here we call each such item declaration a primary item declaration.
 */
final class PrimaryItemDeclaration private[dom] (val globalElementDeclaration: GlobalElementDeclaration) extends ItemDeclaration

/**
 * Hypercube declaration. It must be an abstract item declaration in the xbrldt:hypercubeItem substitution group.
 */
final class HypercubeDeclaration private[dom] (val globalElementDeclaration: GlobalElementDeclaration) extends ItemDeclaration {

  def hypercubeEName: EName = {
    targetEName
  }
}

/**
 * Dimension declaration. It must be an abstract item declaration in the xbrldt:dimensionItem substitution group.
 */
sealed trait DimensionDeclaration extends ItemDeclaration {

  final def isTyped: Boolean = {
    globalElementDeclaration.attrOption(ENames.CXbrldtTypedDomainKeyEName).isDefined
  }

  final def dimensionEName: EName = {
    targetEName
  }
}

/**
 * Explicit dimension declaration. It must be a dimension declaration without attribute cxbrldt:typedDomainKey, among other requirements.
 */
final class ExplicitDimensionDeclaration private[dom] (val globalElementDeclaration: GlobalElementDeclaration) extends DimensionDeclaration {
  require(!isTyped, s"${globalElementDeclaration.targetEName} is typed and therefore not an explicit dimension")
}

/**
 * Typed dimension declaration. It must be a dimension declaration with an attribute cxbrldt:typedDomainKey, among other requirements.
 */
final class TypedDimensionDeclaration private[dom] (val globalElementDeclaration: GlobalElementDeclaration) extends DimensionDeclaration {
  require(isTyped, s"${globalElementDeclaration.targetEName} is not typed and therefore not a typed dimension")

  /**
   * Returns the value of the cxbrldt:typedDomainKey attribute, as EName.
   */
  def typedDomainKey: EName = {
    globalElementDeclaration.attrAsResolvedQName(ENames.CXbrldtTypedDomainKeyEName)
  }

  /**
   * Returns the optional value of the cxbrldt:typedDomainKey attribute, as optional EName.
   * Consider calling this method if the "typed dimension declaration" is not known to be schema-valid (in the locator-free model).
   */
  def typedDomainKeyOption: Option[EName] = {
    globalElementDeclaration.attrAsResolvedQNameOption(ENames.CXbrldtTypedDomainKeyEName)
  }
}

object ConceptDeclaration {

  /**
   * Builder of `ConceptDeclaration` objects, given a `SubstitutionGroupMap` object.
   */
  final class Builder(val substitutionGroupMap: SubstitutionGroupMap) {

    /**
     * Optionally turns the global element declaration into a `ConceptDeclaration`, if it is indeed a concept.
     * This creation cannot fail (assuming that the SubstitutionGroupMap cannot be corrupted).
     */
    def optConceptDeclaration(elemDecl: GlobalElementDeclaration): Option[ConceptDeclaration] = {
      val allSubstGroups: Set[EName] =
        elemDecl.findAllOwnOrTransitivelyInheritedSubstitutionGroups(substitutionGroupMap)

      val isHypercube = allSubstGroups.contains(ENames.XbrldtHypercubeItemEName)
      val isDimension = allSubstGroups.contains(ENames.XbrldtDimensionItemEName)
      val isItem = allSubstGroups.contains(ENames.XbrliItemEName)
      val isTuple = allSubstGroups.contains(ENames.XbrliTupleEName)

      require(!isItem || !isTuple, s"A concept (${elemDecl.targetEName}) cannot be both an item and tuple")
      require(!isHypercube || !isDimension, s"A concept (${elemDecl.targetEName}) cannot be both a hypercube and dimension")
      require(isItem || !isHypercube, s"A concept (${elemDecl.targetEName}) cannot be a hypercube but not an item")
      require(isItem || !isDimension, s"A concept (${elemDecl.targetEName}) cannot be a dimension but not an item")

      if (isTuple) {
        Some(new TupleDeclaration(elemDecl))
      } else if (isItem) {
        if (isHypercube) {
          Some(new HypercubeDeclaration(elemDecl))
        } else if (isDimension) {
          if (elemDecl.attrOption(ENames.CXbrldtTypedDomainKeyEName).isDefined) {
            Some(new TypedDimensionDeclaration(elemDecl))
          } else {
            Some(new ExplicitDimensionDeclaration(elemDecl))
          }
        } else {
          Some(new PrimaryItemDeclaration(elemDecl))
        }
      } else {
        None
      }
    }
  }
}
