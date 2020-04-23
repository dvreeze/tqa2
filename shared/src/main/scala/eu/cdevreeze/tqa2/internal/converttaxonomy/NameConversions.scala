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

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.yaidom2.core.EName

/**
 * Converter from element names in standard taxonomies to their counterparts in the locator-free model.
 *
 * @author Chris de Vreeze
 */
object NameConversions {

  def convertLinkName(inputLinkName: EName): EName = {
    inputLinkName match {
      case ENames.LinkDefinitionLinkEName   => ENames.CLinkDefinitionLinkEName
      case ENames.LinkPresentationLinkEName => ENames.CLinkPresentationLinkEName
      case ENames.LinkCalculationLinkEName  => ENames.CLinkCalculationLinkEName
      case ENames.LinkLabelLinkEName        => ENames.CLinkLabelLinkEName
      case ENames.LinkReferenceLinkEName    => ENames.CLinkReferenceLinkEName
      case ENames.GenLinkEName              => ENames.CGenLinkEName
      case n                                => n
    }
  }

  def convertArcName(inputArcName: EName): EName = {
    inputArcName match {
      case ENames.LinkDefinitionArcEName   => ENames.CLinkDefinitionArcEName
      case ENames.LinkPresentationArcEName => ENames.CLinkPresentationArcEName
      case ENames.LinkCalculationArcEName  => ENames.CLinkCalculationArcEName
      case ENames.LinkLabelArcEName        => ENames.CLinkLabelArcEName
      case ENames.LinkReferenceArcEName    => ENames.CLinkReferenceArcEName
      case n                               => n
    }
  }

  def convertResourceName(inputResourceName: EName): EName = {
    inputResourceName match {
      case ENames.LinkLabelEName     => ENames.CLinkLabelEName
      case ENames.LinkReferenceEName => ENames.CLinkReferenceEName
      case n                         => n
    }
  }

  /**
   * Converts names in link:usedOn element text, either for role types (link or resource names) or arcrole types
   * (arc names).
   */
  def convertLinkOrResourceOrArcName(inputLinkOrResourceOrArcName: EName): EName = {
    inputLinkOrResourceOrArcName match {
      case ENames.LinkDefinitionLinkEName   => ENames.CLinkDefinitionLinkEName
      case ENames.LinkPresentationLinkEName => ENames.CLinkPresentationLinkEName
      case ENames.LinkCalculationLinkEName  => ENames.CLinkCalculationLinkEName
      case ENames.LinkLabelLinkEName        => ENames.CLinkLabelLinkEName
      case ENames.LinkReferenceLinkEName    => ENames.CLinkReferenceLinkEName
      case ENames.GenLinkEName              => ENames.CGenLinkEName
      case ENames.LinkLabelEName            => ENames.CLinkLabelEName
      case ENames.LinkReferenceEName        => ENames.CLinkReferenceEName
      case ENames.LinkDefinitionArcEName    => ENames.CLinkDefinitionArcEName
      case ENames.LinkPresentationArcEName  => ENames.CLinkPresentationArcEName
      case ENames.LinkCalculationArcEName   => ENames.CLinkCalculationArcEName
      case ENames.LinkLabelArcEName         => ENames.CLinkLabelArcEName
      case ENames.LinkReferenceArcEName     => ENames.CLinkReferenceArcEName
      case n                                => n
    }
  }
}
