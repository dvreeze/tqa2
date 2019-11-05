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

/**
 * Well-known standard label roles.
 *
 * @author Chris de Vreeze
 */
object StandardLabelRoles {

  val Label: String = makeLabelRole("label")
  val TerseLabel: String = makeLabelRole("terseLabel")
  val VerboseLabel: String = makeLabelRole("verboseLabel")
  val PositiveLabel: String = makeLabelRole("positiveLabel")
  val PositiveTerseLabel: String = makeLabelRole("positiveTerseLabel")
  val PositiveVerboseLabel: String = makeLabelRole("positiveVerboseLabel")
  val NegativeLabel: String = makeLabelRole("negativeLabel")
  val NegativeTerseLabel: String = makeLabelRole("negativeTerseLabel")
  val NegativeVerboseLabel: String = makeLabelRole("negativeVerboseLabel")
  val ZeroLabel: String = makeLabelRole("zeroLabel")
  val ZeroTerseLabel: String = makeLabelRole("zeroTerseLabel")
  val ZeroVerboseLabel: String = makeLabelRole("zeroVerboseLabel")
  val TotalLabel: String = makeLabelRole("totalLabel")
  val PeriodStartLabel: String = makeLabelRole("periodStartLabel")
  val PeriodEndLabel: String = makeLabelRole("periodEndLabel")
  val Documentation: String = makeLabelRole("documentation")
  val DefinitionGuidance: String = makeLabelRole("definitionGuidance")
  val DisclosureGuidance: String = makeLabelRole("disclosureGuidance")
  val PresentationGuidance: String = makeLabelRole("presentationGuidance")
  val MeasurementGuidance: String = makeLabelRole("measurementGuidance")
  val CommentaryGuidance: String = makeLabelRole("commentaryGuidance")
  val ExampleGuidance: String = makeLabelRole("exampleGuidance")

  /**
   * Alias for Label. It is the default label role, or, in other words, the standard label role.
   */
  val StandardLabel: String = Label

  private def makeLabelRole(suffix: String): String = {
    s"http://www.xbrl.org/2003/role/$suffix"
  }
}
