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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.StandardResource
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.yaidom2.core.EName

/**
 * Relationship in a locator-free taxonomy. The relationship source and target is always an XLink resource, and more
 * often than not a taxonomy element key in particular. The source and target is always the direct source and target
 * pointed to by the underlying arc, even if the source or target is a key pointing to a remote resource.
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
  val source: ResourceHolder[dom.XLinkResource],
  val target: ResourceHolder[dom.XLinkResource]) {

  require(arc.from == source.directResource.xlinkLabel, s"Arc and 'source' not matching on XLink label in $docUri")
  require(arc.to == target.directResource.xlinkLabel, s"Arc and 'target' not matching on XLink label in $docUri")
  require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in $docUri")

  final def validated: this.type = {
    require(arc.findParentElem().nonEmpty, s"Missing parent (exended link) element of an arc in $docUri")
    require(
      source.directResource.findParentElem() == arc.findParentElem(),
      s"An arc and its source are not in the same extended link in $docUri")
    require(
      target.directResource.findParentElem() == arc.findParentElem(),
      s"An arc and its target are not in the same extended link in $docUri")
    this
  }

  /**
   * If the source points to a remote resource, returns the remote resource, and otherwise returns the source itself
   * (which is a taxonomy element key or local resource).
   */
  def effectiveSource: dom.XLinkResource

  /**
   * If the target points to a remote resource, returns the remote resource, and otherwise returns the target itself
   * (which is a taxonomy element key or local resource).
   */
  def effectiveTarget: dom.XLinkResource

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
  override val source: ResourceHolder.Key[dom.ConceptKey],
  target: ResourceHolder[dom.XLinkResource]) extends Relationship(arc, source, target) {

  final def effectiveSource: dom.ConceptKey = source.effectiveResource

  final def sourceConcept: EName = source.effectiveResource.key
}

/**
 * Non-standard relationship in the locator-free model, so typically a generic relationship.
 *
 * TODO Open up this class for extension
 */
final case class NonStandardRelationship(
  override val arc: dom.XLinkArc,
  override val source: ResourceHolder[dom.XLinkResource],
  override val target: ResourceHolder[dom.XLinkResource]) extends Relationship(arc, source, target) {

  def effectiveSource: dom.XLinkResource = source.effectiveResource

  def effectiveTarget: dom.XLinkResource = target.effectiveResource
}

/**
 * Unknown relationship. Possibly an invalid relationship.
 */
final case class UnknownRelationship(
  override val arc: dom.XLinkArc,
  override val source: ResourceHolder[dom.XLinkResource],
  override val target: ResourceHolder[dom.XLinkResource]) extends Relationship(arc, source, target) {

  def effectiveSource: dom.XLinkResource = source.effectiveResource

  def effectiveTarget: dom.XLinkResource = target.effectiveResource
}

/**
 * Inter-concept relationship in the locator-free model.
 */
sealed abstract class InterConceptRelationship(
  override val arc: dom.InterConceptArc,
  source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends StandardRelationship(arc, source, target) {

  final def effectiveTarget: dom.ConceptKey = target.effectiveResource

  final def targetConcept: EName = target.effectiveResource.key

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
 * Hence the use of a ResourceHolder object for local or remote standard resources.
 */
sealed abstract class ConceptResourceRelationship(
  override val arc: dom.ConceptResourceArc,
  source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder[StandardResource])
  extends StandardRelationship(arc, source, target) {

  def effectiveTarget: dom.StandardResource = target.effectiveResource
}

final case class ConceptLabelRelationship(
  override val arc: dom.LabelArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder[dom.ConceptLabelResource])
  extends ConceptResourceRelationship(arc, source, target) {

  override def effectiveTarget: dom.ConceptLabelResource = target.effectiveResource

  def resourceRole: String = effectiveTarget.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    effectiveTarget.attrOption(ENames.XmlLangEName)
      .getOrElse(sys.error(s"Missing xml:lang in ${effectiveTarget.name} in ${effectiveTarget.docUri}"))
  }

  def labelText: String = effectiveTarget.text
}

final case class ConceptReferenceRelationship(
  override val arc: dom.ReferenceArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder[dom.ConceptReferenceResource])
  extends ConceptResourceRelationship(arc, source, target) {

  override def effectiveTarget: dom.ConceptReferenceResource = target.effectiveResource

  def resourceRole: String = effectiveTarget.roleOption.getOrElse(StandardReferenceRoles.StandardReference)

  def referenceElems: Seq[TaxonomyElem] = effectiveTarget.findAllChildElems
}

/**
 * Presentation relationship in the locator-free model.
 */
sealed abstract class PresentationRelationship(
  override val arc: dom.PresentationArc,
  source: ResourceHolder.Key[dom.ConceptKey],
  target: ResourceHolder.Key[dom.ConceptKey]) extends InterConceptRelationship(arc, source, target)

final case class ParentChildRelationship(
  override val arc: dom.PresentationArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends PresentationRelationship(arc, source, target)

final case class OtherPresentationRelationship(
  override val arc: dom.PresentationArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends PresentationRelationship(arc, source, target)

/**
 * Calculation relationship in the locator-free model.
 */
sealed abstract class CalculationRelationship(
  override val arc: dom.CalculationArc,
  source: ResourceHolder.Key[dom.ConceptKey],
  target: ResourceHolder.Key[dom.ConceptKey]) extends InterConceptRelationship(arc, source, target)

final case class SummationItemRelationship(
  override val arc: dom.CalculationArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends CalculationRelationship(arc, source, target)

final case class OtherCalculationRelationship(
  override val arc: dom.CalculationArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends CalculationRelationship(arc, source, target)

/**
 * Definition relationship in the locator-free model.
 */
sealed abstract class DefinitionRelationship(
  override val arc: dom.DefinitionArc,
  source: ResourceHolder.Key[dom.ConceptKey],
  target: ResourceHolder.Key[dom.ConceptKey]) extends InterConceptRelationship(arc, source, target)

final case class GeneralSpecialRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DefinitionRelationship(arc, source, target)

final case class EssenceAliasRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DefinitionRelationship(arc, source, target)

final case class SimilarTuplesRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DefinitionRelationship(arc, source, target)

final case class RequiresElementRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DefinitionRelationship(arc, source, target)

/**
 * Dimensional (definition) relationship in the locator-free model.
 */
sealed abstract class DimensionalRelationship(
  arc: dom.DefinitionArc,
  source: ResourceHolder.Key[dom.ConceptKey],
  target: ResourceHolder.Key[dom.ConceptKey]) extends DefinitionRelationship(arc, source, target)

sealed abstract class HasHypercubeRelationship(
  arc: dom.DefinitionArc,
  source: ResourceHolder.Key[dom.ConceptKey],
  target: ResourceHolder.Key[dom.ConceptKey]) extends DimensionalRelationship(arc, source, target) {

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
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends HasHypercubeRelationship(arc, source, target) {

  def isAllRelationship: Boolean = true
}

final case class NotAllRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends HasHypercubeRelationship(arc, source, target) {

  def isAllRelationship: Boolean = false
}

final case class HypercubeDimensionRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DimensionalRelationship(arc, source, target) {

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
  source: ResourceHolder.Key[dom.ConceptKey],
  target: ResourceHolder.Key[dom.ConceptKey]) extends DimensionalRelationship(arc, source, target) {

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
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DomainAwareRelationship(arc, source, target) {

  def dimension: EName = sourceConcept

  def domain: EName = targetConcept
}

final case class DomainMemberRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DomainAwareRelationship(arc, source, target) {

  def domain: EName = sourceConcept

  def member: EName = targetConcept
}

final case class DimensionDefaultRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DimensionalRelationship(arc, source, target) {

  def dimension: EName = sourceConcept

  def defaultOfDimension: EName = targetConcept
}

/**
 * Definition relationship that is not one of the known standard or dimensional ones.
 */
final case class OtherDefinitionRelationship(
  override val arc: dom.DefinitionArc,
  override val source: ResourceHolder.Key[dom.ConceptKey],
  override val target: ResourceHolder.Key[dom.ConceptKey]) extends DefinitionRelationship(arc, source, target)
