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

package eu.cdevreeze.tqa2.aspect

import eu.cdevreeze.tqa2.aspect.Aspect.OccAspect
import eu.cdevreeze.yaidom2.core.EName

/**
 * Aspect (of a fact in an XBRL instance).
 *
 * See http://www.xbrl.org/specification/variables/REC-2009-06-22/variables-REC-2009-06-22.html#sec-aspects.
 *
 * @author Chris de Vreeze
 */
sealed trait Aspect {

  /**
   * Returns true if this aspect applies to both tuples and numeric and non-numeric items
   */
  final def appliesToAllFacts: Boolean = {
    appliesToTuples && appliesToNonNumericItems
  }

  /**
   * Returns true if this aspect applies only to (some or all) items and not to tuples
   */
  final def appliesToItemsOnly: Boolean = {
    !appliesToTuples
  }

  /**
   * Returns true if this aspect applies only to numeric items and neither to non-numeric items nor to tuples
   */
  final def appliesToNumericItemsOnly: Boolean = {
    appliesToItemsOnly && !appliesToNonNumericItems
  }

  /**
   * Returns true if this aspect applies to tuples (as well as items)
   */
  final def appliesToTuples: Boolean = {
    this == Aspect.ConceptAspect || this == Aspect.LocationAspect
  }

  /**
   * Returns true if this aspect applies to non-numeric items (and not just numeric ones)
   */
  final def appliesToNonNumericItems: Boolean = this match {
    case Aspect.LocationAspect | Aspect.ConceptAspect | Aspect.EntityIdentifierAspect | Aspect.PeriodAspect =>
      true
    case asp if OccAspect.occAspects.toSeq.contains(asp) =>
      true
    case Aspect.DimensionAspect(_) =>
      true
    case _ =>
      false
  }

  /**
   * Returns true if this aspect is included in all aspect models.
   *
   * Assuming the existence of only 2 aspect models (dimensional and non-dimensional) this is the same
   * as methods `isIncludedInDimensionalAspectModel` and `isIncludedInNonDimensionalAspectModel` both returning true.
   */
  final def isIncludedInAllAspectModels: Boolean = {
    isIncludedInDimensionalAspectModel && isIncludedInNonDimensionalAspectModel
  }

  final def isIncludedInDimensionalAspectModel: Boolean = {
    this != Aspect.CompleteSegmentAspect && this != Aspect.CompleteScenarioAspect
  }

  final def isIncludedInNonDimensionalAspectModel: Boolean = this match {
    case Aspect.NonXDTSegmentAspect | Aspect.NonXDTScenarioAspect =>
      false
    case Aspect.DimensionAspect(_) =>
      false
    case _ =>
      true
  }
}

object Aspect {

  /**
   * All well-known aspects, so all aspects except for dimension aspects (since we do not know them upfront).
   */
  val wellKnownAspects: Set[Aspect] = {
    Set[Aspect](LocationAspect, ConceptAspect, PeriodAspect, EntityIdentifierAspect, UnitAspect)
      .union(OccAspect.occAspects.asInstanceOf[Set[Aspect]])
  }

  // First the required aspects, for both aspect models (dimensional and non-dimensional)

  case object LocationAspect extends Aspect {

    override def toString: String = "location"
  }

  case object ConceptAspect extends Aspect {

    override def toString: String = "concept"
  }

  case object EntityIdentifierAspect extends Aspect {

    override def toString: String = "entityIdentifier"
  }

  case object PeriodAspect extends Aspect {

    override def toString: String = "period"
  }

  case object UnitAspect extends Aspect {

    override def toString: String = "unit"
  }

  // Next the aspects that belong to one of the aspect models

  /**
   * Open context component aspect, so a segment or a scenario.
   */
  sealed trait OccAspect extends Aspect

  object OccAspect {

    val occAspects: Set[OccAspect] = {
      Set(NonXDTSegmentAspect, CompleteSegmentAspect, NonXDTScenarioAspect, CompleteScenarioAspect)
    }
  }

  /**
   * NonXDTSegmentAspect, in the dimensional aspect model
   */
  case object NonXDTSegmentAspect extends OccAspect {

    override def toString: String = "nonXDTSegment"
  }

  /**
   * CompleteSegmentAspect, in the non-dimensional aspect model
   */
  case object CompleteSegmentAspect extends OccAspect {

    override def toString: String = "completeSegment"
  }

  /**
   * NonXDTScenarioAspect, in the dimensional aspect model
   */
  case object NonXDTScenarioAspect extends OccAspect {

    override def toString: String = "nonXDTScenario"
  }

  /**
   * CompleteScenarioAspect, in the non-dimensional aspect model
   */
  case object CompleteScenarioAspect extends OccAspect {

    override def toString: String = "completeScenario"
  }

  /**
   * A dimensional aspect (for either an explicit or typed dimension), in the dimensional aspect model
   */
  final case class DimensionAspect(dimension: EName) extends Aspect {

    override def toString: String = dimension.toString
  }
}
