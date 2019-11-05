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

package eu.cdevreeze.tqa2.locfreetaxonomy.common

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.yaidom2.core.EName

/**
 * The key of a base set, made up by the EName and @xlink:arcrole of the arc, along with the EName and @xlink:role of
 * the parent extended link. Base set keys are essential for finding networks of relationships. Base set keys are as
 * applicable in the locator-free taxonomy model as they are in regular taxonomies.
 *
 * @author Chris de Vreeze
 */
final case class BaseSetKey(
  arcEName: EName,
  arcrole: String,
  extLinkEName: EName,
  extLinkRole: String) {

  /**
   * Returns true if this key is for a standard arc in a standard extended link. This check looks at
   * arc and extended link ENames, not at (arc and extended link) roles.
   */
  def isStandard: Boolean = {
    arcEName.namespaceUriOption.contains(Namespaces.CLinkNamespace) &&
      extLinkEName.namespaceUriOption.contains(Namespaces.CLinkNamespace)
  }

  def withArcrole(newArcrole: String): BaseSetKey = this.copy(arcrole = newArcrole)

  def withExtLinkRole(newExtLinkRole: String): BaseSetKey = this.copy(extLinkRole = newExtLinkRole)
}

object BaseSetKey {

  import ENames._

  val StandardElr = "http://www.xbrl.org/2003/role/link"

  def forLabelArc(arcrole: String, elr: String): BaseSetKey = {
    BaseSetKey(CLinkLabelArcEName, arcrole, CLinkLabelLinkEName, elr)
  }

  def forReferenceArc(arcrole: String, elr: String): BaseSetKey = {
    BaseSetKey(CLinkReferenceArcEName, arcrole, CLinkReferenceLinkEName, elr)
  }

  def forCalculationArc(arcrole: String, elr: String): BaseSetKey = {
    BaseSetKey(CLinkCalculationArcEName, arcrole, CLinkCalculationLinkEName, elr)
  }

  def forPresentationArc(arcrole: String, elr: String): BaseSetKey = {
    BaseSetKey(CLinkPresentationArcEName, arcrole, CLinkPresentationLinkEName, elr)
  }

  def forDefinitionArc(arcrole: String, elr: String): BaseSetKey = {
    BaseSetKey(CLinkDefinitionArcEName, arcrole, CLinkDefinitionLinkEName, elr)
  }

  // BaseSetKey functions for specific label arcs

  def forConceptLabelArc(elr: String): BaseSetKey = {
    forLabelArc("http://www.xbrl.org/2003/arcrole/concept-label", elr)
  }

  def forConceptLabelArcWithStandardElr: BaseSetKey = forConceptLabelArc(StandardElr)

  // BaseSetKey functions for specific reference arcs

  def forConceptReferenceArc(elr: String): BaseSetKey = {
    forReferenceArc("http://www.xbrl.org/2003/arcrole/concept-reference", elr)
  }

  def forConceptReferenceArcWithStandardElr: BaseSetKey = forConceptReferenceArc(StandardElr)

  // BaseSetKey functions for specific calculation arcs

  def forSummationItemArc(elr: String): BaseSetKey = {
    forCalculationArc("http://www.xbrl.org/2003/arcrole/summation-item", elr)
  }

  // BaseSetKey functions for specific presentation arcs

  def forParentChildArc(elr: String): BaseSetKey = {
    forPresentationArc("http://www.xbrl.org/2003/arcrole/parent-child", elr)
  }

  // BaseSetKey functions for specific definition arcs

  def forGeneralSpecialArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://www.xbrl.org/2003/arcrole/general-special", elr)
  }

  def forEssenceAliasArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://www.xbrl.org/2003/arcrole/essence-alias", elr)
  }

  def forSimilarTuplesArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://www.xbrl.org/2003/arcrole/similar-tuples", elr)
  }

  def forRequiresElementArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://www.xbrl.org/2003/arcrole/requires-element", elr)
  }

  def forHypercubeDimensionArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://xbrl.org/int/dim/arcrole/hypercube-dimension", elr)
  }

  def forDimensionDomainArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://xbrl.org/int/dim/arcrole/dimension-domain", elr)
  }

  def forDomainMemberArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://xbrl.org/int/dim/arcrole/domain-member", elr)
  }

  def forDimensionDefaultArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://xbrl.org/int/dim/arcrole/dimension-default", elr)
  }

  def forAllArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://xbrl.org/int/dim/arcrole/all", elr)
  }

  def forNotAllArc(elr: String): BaseSetKey = {
    forDefinitionArc("http://xbrl.org/int/dim/arcrole/notAll", elr)
  }
}
