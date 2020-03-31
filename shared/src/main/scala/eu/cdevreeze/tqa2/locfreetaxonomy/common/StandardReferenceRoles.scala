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
 * Well-known standard reference roles.
 *
 * @author Chris de Vreeze
 */
object StandardReferenceRoles {

  val Reference: String = makeReferenceRole("reference")
  val DefinitionRef: String = makeReferenceRole("definitionRef")
  val DisclosureRef: String = makeReferenceRole("disclosureRef")
  val MandatoryDisclosureRef: String = makeReferenceRole("mandatoryDisclosureRef")
  val RecommendedDisclosureRef: String = makeReferenceRole("recommendedDisclosureRef")
  val UnspecifiedDisclosureRef: String = makeReferenceRole("unspecifiedDisclosureRef ")
  val PresentationRef: String = makeReferenceRole("presentationRef")
  val MeasurementRef: String = makeReferenceRole("measurementRef")
  val CommentaryRef: String = makeReferenceRole("commentaryRef")
  val ExampleRef: String = makeReferenceRole("exampleRef")

  /**
   * Alias for Reference. It is the default reference role, or, in other words, the standard reference role.
   */
  val StandardReference: String = Reference

  val allRoles: Set[String] = Set(
    Reference, DefinitionRef, DisclosureRef, MandatoryDisclosureRef, RecommendedDisclosureRef, UnspecifiedDisclosureRef,
    PresentationRef, MeasurementRef, CommentaryRef, ExampleRef
  )

  private def makeReferenceRole(suffix: String): String = {
    s"http://www.xbrl.org/2003/role/$suffix"
  }
}
