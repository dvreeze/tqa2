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

package eu.cdevreeze.tqa2.common.xmlschema

import scala.annotation.tailrec

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.yaidom2.core.EName

/**
 * A collection of substitution groups, typically extracted from a taxonomy. It maps substitution groups
 * to their own substitution groups, if any. Well-known substitution groups such as xbrli:item, xbrli:tuple,
 * xbrldt:hypercubeItem and xbrldt:dimensionItem must not occur as keys in the mapping, but typically do occur
 * as mapped values.
 *
 * This class is essential for providing the necessary context in order to determine whether a global element
 * declaration is a concept declaration, and, if so, what kind of concept declaration.
 *
 * Cycles are not allowed when following mappings, but this is not checked.
 *
 * @author Chris de Vreeze
 */
final case class SubstitutionGroupMap(mappings: Map[EName, EName]) {
  require(
    !mappings.keySet.exists(SubstitutionGroupMap.StandardConceptSubstitutionGroups),
    s"No standard substitution groups allowed as mapping keys")

  import eu.cdevreeze.tqa2.ENames._

  /**
   * A map from substitution groups to other substitution groups directly derived from them.
   * In other words, the reverse of `effectiveMappings`.
   */
  val substitutionGroupDerivations: Map[EName, Set[EName]] = {
    effectiveMappings.toSeq.groupBy(_._2).view.mapValues(_.map(_._1).toSet).toMap
  }

  /**
   * The mappings, plus the implicit mappings for hypercube and dimension items.
   */
  def effectiveMappings: Map[EName, EName] = {
    mappings ++ Map(XbrldtHypercubeItemEName -> XbrliItemEName, XbrldtDimensionItemEName -> XbrliItemEName)
  }

  /**
   * Finds all transitively inherited substitution groups from the given substitution group, as found in this mapping,
   * returning this substitution group as well. The result is returned in order of ancestry, this substitution group first.
   */
  def transitivelyInheritedSubstitutionGroupsIncludingSelf(substGroup: EName): Seq[EName] = {
    substGroup +: transitivelyInheritedSubstitutionGroups(substGroup)
  }

  /**
   * Finds all transitively inherited substitution groups from the given substitution group, as found in this mapping.
   * The result is returned in order of ancestry, this substitution group's own substitution group first.
   */
  def transitivelyInheritedSubstitutionGroups(substGroup: EName): Seq[EName] = {
    val effMappings: Map[EName, EName] = effectiveMappings

    @tailrec
    def transitivelyInheritedSubstitutionGroupReversed(sg: EName, acc: List[EName]): List[EName] = {
      val directlyInheritedSubstGroupOption = effMappings.get(sg)

      directlyInheritedSubstGroupOption match {
        case None      => acc
        case Some(isg) =>
          // Recursive call
          transitivelyInheritedSubstitutionGroupReversed(isg, isg :: acc)
      }
    }

    transitivelyInheritedSubstitutionGroupReversed(substGroup, Nil).reverse.toIndexedSeq
  }

  /**
   * Returns `SubstitutionGroupMap(this.mappings ++ sgm.mappings)`.
   */
  def append(sgm: SubstitutionGroupMap): SubstitutionGroupMap = {
    SubstitutionGroupMap(this.mappings ++ sgm.mappings)
  }

  def isOrInheritsItemOrTupleSubstitutionGroup(sg: EName): Boolean = {
    isOrInheritsItemSubstitutionGroup(sg) || isOrInheritsTupleSubstitutionGroup(sg)
  }

  def isOrInheritsItemSubstitutionGroup(sg: EName): Boolean = {
    transitivelyInheritedSubstitutionGroupsIncludingSelf(sg).contains(ENames.XbrliItemEName)
  }

  def isOrInheritsTupleSubstitutionGroup(sg: EName): Boolean = {
    transitivelyInheritedSubstitutionGroupsIncludingSelf(sg).contains(ENames.XbrliTupleEName)
  }

  def isOrInheritsHypercubeItemSubstitutionGroup(sg: EName): Boolean = {
    transitivelyInheritedSubstitutionGroupsIncludingSelf(sg).contains(ENames.XbrldtHypercubeItemEName)
  }

  def isOrInheritsDimensionItemSubstitutionGroup(sg: EName): Boolean = {
    transitivelyInheritedSubstitutionGroupsIncludingSelf(sg).contains(ENames.XbrldtDimensionItemEName)
  }
}

object SubstitutionGroupMap {

  import eu.cdevreeze.tqa2.ENames._

  val StandardConceptSubstitutionGroups: Set[EName] =
    Set(XbrliItemEName, XbrliTupleEName, XbrldtHypercubeItemEName, XbrldtDimensionItemEName)

  val Empty = SubstitutionGroupMap(Map.empty)

  /**
   * Safe construction method, filtering away standard substitution groups from the mapping keys provided.
   */
  def from(mappings: Map[EName, EName]): SubstitutionGroupMap = {
    SubstitutionGroupMap(mappings.filter(kv => !SubstitutionGroupMap.StandardConceptSubstitutionGroups.contains(kv._1)))
  }
}
