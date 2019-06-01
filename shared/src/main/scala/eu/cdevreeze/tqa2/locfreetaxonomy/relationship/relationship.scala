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

package eu.cdevreeze.tqa2.locfreetaxonomy.relationship

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.datatypes.XsBooleans
import eu.cdevreeze.tqa2.locfreetaxonomy.common.BaseSetKey
import eu.cdevreeze.tqa2.locfreetaxonomy.common.ContextElement
import eu.cdevreeze.tqa2.locfreetaxonomy.common.StandardLabelRoles
import eu.cdevreeze.tqa2.locfreetaxonomy.common.StandardReferenceRoles
import eu.cdevreeze.tqa2.locfreetaxonomy.common.Use
import eu.cdevreeze.tqa2.locfreetaxonomy.dom
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.yaidom2.core.EName

/**
 * Relationship in a locator-free taxonomy. The relationship source and target is always an XLink resource, and more
 * often than not a taxonomy element key in particular.
 *
 * Fast efficient creation of relationship instances is very important for performance.
 *
 * Note that the underlying arc of a relationship may underlie multiple relationships, if the arc has multiple sources
 * and/or targets.
 *
 * Like the underlying DOM model, relationship creation is quite lenient, yet query methods may fail if the underlying
 * DOM model is not schema-valid. It is required for a relationship to have an arcrole attribute on the underlying arc,
 * however, or else relationship creation fails. The arcrole is very fundamental to a relationship; hence this requirement.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.types
sealed abstract class Relationship(
  val arc: dom.XLinkArc,
  val source: dom.XLinkResource,
  val target: dom.XLinkResource) {

  require(arc.from == source.xlinkLabel, s"Arc and 'source' not matching on XLink label in $docUri")
  require(arc.to == target.xlinkLabel, s"Arc and 'target' not matching on XLink label in $docUri")
  require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in $docUri")

  final def validated: this.type = {
    require(arc.findParentElem().nonEmpty, s"Missing parent (exended link) element of an arc in $docUri")
    require(source.findParentElem() == arc.findParentElem(), s"An arc and its source are not in the same extended link in $docUri")
    require(target.findParentElem() == arc.findParentElem(), s"An arc and its target are not in the same extended link in $docUri")
    this
  }

  final def docUri: URI = arc.docUri

  final def baseUri: URI = arc.baseUri

  final def elr: String = arc.elr

  final def arcrole: String = arc.arcrole

  final def baseSetKey: BaseSetKey = arc.baseSetKey

  final def use: Use = arc.use

  final def priority: Int = arc.priority

  final def order: BigDecimal = arc.order

  // TODO Unique relationship key

  /**
   * Returns the optional gpl:preferredLabel attribute on the underlying arc.
   */
  final def genericPreferredLabelOption: Option[String] = {
    arc.attrOption(ENames.GplPreferredLabelEName)
  }
}

/**
 * Standard relationship in the locator-free model, so either an inter-concept relationship or a concept-resource relationship.
 */
sealed abstract class StandardRelationship(
  override val arc: dom.StandardArc,
  override val source: dom.ConceptKey,
  target: dom.XLinkResource) extends Relationship(arc, source, target) {

  final def sourceConcept: EName = source.key
}

/**
 * Non-standard relationship in the locator-free model, so typically a generic relationship.
 *
 * TODO Open up this class for extension
 */
final case class NonStandardRelationship(
  override val arc: dom.XLinkArc,
  override val source: dom.XLinkResource,
  override val target: dom.XLinkResource) extends Relationship(arc, source, target)

/**
 * Unknown relationship. Possibly an invalid relationship.
 */
final case class UnknownRelationship(
  override val arc: dom.XLinkArc,
  override val source: dom.XLinkResource,
  override val target: dom.XLinkResource) extends Relationship(arc, source, target)

/**
 * Inter-concept relationship in the locator-free model.
 */
sealed abstract class InterConceptRelationship(
  override val arc: dom.InterConceptArc,
  source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends StandardRelationship(arc, source, target) {

  final def targetConcept: EName = target.key

  /**
   * For non-dimensional relationships, returns true if the target concept of this relationship matches the source
   * concept of the parameter relationship and both relationships are in the same base set.
   *
   * For dimensional relationships, returns true if this and the parameter relationship form a pair of consecutive
   * relationships.
   *
   * This method does not check for the target relationship type, although for non-dimensional inter-concept relationships
   * the relationship type remains the same, and for dimensional relationships the target relationship type is as expected
   * for consecutive relationships.
   */
  final def isFollowedBy(rel: InterConceptRelationship): Boolean = {
    (this.targetConcept == rel.sourceConcept) &&
      (this.effectiveTargetBaseSetKey == rel.baseSetKey)
  }

  /**
   * Overridable method returning the effective target role, which is the ELR for non-dimensional relationships,
   * but respects the target role attribute for dimensional relationships.
   */
  def effectiveTargetRole: String = elr

  /**
   * Overridable method returning the effective target BaseSetKey, which is the same BaseSetKey for non-dimensional relationships,
   * but respects the rules concerning consecutive relationships for dimensional relationships.
   * This method is used by method followedBy.
   */
  def effectiveTargetBaseSetKey: BaseSetKey = {
    this.baseSetKey.ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

/**
 * Concept-resource relationship in the locator-free model.
 *
 * Note that in regular taxonomies concept-resource relationships may use a locator to a resource for prohibition/overriding.
 * In the locator-free model this means that the locator must have been "resolved" as standard resource, in order to create an
 * instance of this relationship class. Prohibition then typically implies repeating the same standard resource in the prohibited
 * arc as the one prohibited elsewhere in another extended link (and typically in another document).
 */
sealed abstract class ConceptResourceRelationship(
  override val arc: dom.ConceptResourceArc,
  source: dom.ConceptKey,
  override val target: dom.StandardResource) extends StandardRelationship(arc, source, target)

final case class ConceptLabelRelationship(
  override val arc: dom.LabelArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptLabelResource) extends ConceptResourceRelationship(arc, source, target) {

  def resourceRole: String = target.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    target.attrOption(ENames.XmlLangEName).getOrElse(sys.error(s"Missing xml:lang in ${target.name} in ${target.docUri}"))
  }

  def labelText: String = target.text
}

final case class ConceptReferenceRelationship(
  override val arc: dom.ReferenceArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptReferenceResource) extends ConceptResourceRelationship(arc, source, target) {

  def resourceRole: String = target.roleOption.getOrElse(StandardReferenceRoles.StandardReference)

  def referenceElems: Seq[TaxonomyElem] = target.findAllChildElems
}

/**
 * Presentation relationship in the locator-free model.
 */
sealed abstract class PresentationRelationship(
  arc: dom.PresentationArc,
  source: dom.ConceptKey,
  target: dom.ConceptKey) extends InterConceptRelationship(arc, source, target)

final case class ParentChildRelationship(
  override val arc: dom.PresentationArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends PresentationRelationship(arc, source, target)

final case class OtherPresentationRelationship(
  override val arc: dom.PresentationArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends PresentationRelationship(arc, source, target)

/**
 * Calculation relationship in the locator-free model.
 */
sealed abstract class CalculationRelationship(
  arc: dom.CalculationArc,
  source: dom.ConceptKey,
  target: dom.ConceptKey) extends InterConceptRelationship(arc, source, target)

final case class SummationItemRelationship(
  override val arc: dom.CalculationArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends CalculationRelationship(arc, source, target)

final case class OtherCalculationRelationship(
  override val arc: dom.CalculationArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends CalculationRelationship(arc, source, target)

/**
 * Definition relationship in the locator-free model.
 */
sealed abstract class DefinitionRelationship(
  arc: dom.DefinitionArc,
  source: dom.ConceptKey,
  target: dom.ConceptKey) extends InterConceptRelationship(arc, source, target)

final case class GeneralSpecialRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DefinitionRelationship(arc, source, target)

final case class EssenceAliasRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DefinitionRelationship(arc, source, target)

final case class SimilarTuplesRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DefinitionRelationship(arc, source, target)

final case class RequiresElementRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DefinitionRelationship(arc, source, target)

/**
 * Dimensional (definition) relationship in the locator-free model.
 */
sealed abstract class DimensionalRelationship(
  arc: dom.DefinitionArc,
  source: dom.ConceptKey,
  target: dom.ConceptKey) extends DefinitionRelationship(arc, source, target)

sealed abstract class HasHypercubeRelationship(
  arc: dom.DefinitionArc,
  source: dom.ConceptKey,
  target: dom.ConceptKey) extends DimensionalRelationship(arc, source, target) {

  final def primary: EName = sourceConcept

  final def hypercube: EName = targetConcept

  def isAllRelationship: Boolean

  final def isNotAllRelationship: Boolean = !isAllRelationship

  final def closed: Boolean = {
    arc.attrOption(ENames.XbrldtClosedEName).map(v => XsBooleans.parseBoolean(v)).getOrElse(false)
  }

  final def contextElement: ContextElement = {
    val attrValue = arc.attrOption(ENames.XbrldtContextElementEName).getOrElse(
      sys.error(s"Missing attribute @xbrldt:contextElement on has-hypercube arc in $docUri"))

    ContextElement.fromString(attrValue)
  }

  final override def effectiveTargetRole: String = {
    arc.attrOption(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forHypercubeDimensionArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

final case class AllRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends HasHypercubeRelationship(arc, source, target) {

  def isAllRelationship: Boolean = true
}

final case class NotAllRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends HasHypercubeRelationship(arc, source, target) {

  def isAllRelationship: Boolean = false
}

final case class HypercubeDimensionRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DimensionalRelationship(arc, source, target) {

  def hypercube: EName = sourceConcept

  def dimension: EName = targetConcept

  override def effectiveTargetRole: String = {
    arc.attrOption(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDimensionDomainArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

sealed abstract class DomainAwareRelationship(
  arc: dom.DefinitionArc,
  source: dom.ConceptKey,
  target: dom.ConceptKey) extends DimensionalRelationship(arc, source, target) {

  final def usable: Boolean = {
    arc.attrOption(ENames.XbrldtUsableEName).map(v => XsBooleans.parseBoolean(v)).getOrElse(true)
  }

  final override def effectiveTargetRole: String = {
    arc.attrOption(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDomainMemberArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

final case class DimensionDomainRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DomainAwareRelationship(arc, source, target) {

  def dimension: EName = sourceConcept

  def domain: EName = targetConcept
}

final case class DomainMemberRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DomainAwareRelationship(arc, source, target) {

  def domain: EName = sourceConcept

  def member: EName = targetConcept
}

final case class DimensionDefaultRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DimensionalRelationship(arc, source, target) {

  def dimension: EName = sourceConcept

  def defaultOfDimension: EName = targetConcept
}

/**
 * Definition relationship that is not one of the known standard or dimensional ones.
 */
final case class OtherDefinitionRelationship(
  override val arc: dom.DefinitionArc,
  override val source: dom.ConceptKey,
  override val target: dom.ConceptKey) extends DefinitionRelationship(arc, source, target)
