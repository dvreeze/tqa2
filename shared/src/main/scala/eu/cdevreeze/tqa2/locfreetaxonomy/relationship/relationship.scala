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
import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys
import eu.cdevreeze.tqa2.locfreetaxonomy.common.Use
import eu.cdevreeze.tqa2.locfreetaxonomy.dom
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NonStandardResource
import eu.cdevreeze.yaidom2.core.EName

/**
 * Relationship in a locator-free taxonomy. The relationship source and target are Endpoint objects.
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
sealed trait Relationship {

  def arc: dom.XLinkArc

  def source: Endpoint

  def target: Endpoint

  final def validated: this.type = {
    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in $docUri")
    require(arc.findParentElem().nonEmpty, s"Missing parent (exended link) element of an arc in $docUri")
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
sealed trait StandardRelationship extends Relationship {

  def arc: dom.StandardArc

  def source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]

  final def sourceConcept: EName = source.taxonomyElemKey.key
}

/**
 * Non-standard relationship in the locator-free model, so typically a generic relationship.
 */
sealed trait NonStandardRelationship extends Relationship {

  def arc: dom.NonStandardArc
}

/**
 * Either an element-label relationship or element-reference relationship.
 */
sealed trait ElementResourceRelationship extends NonStandardRelationship {

  def target: Endpoint.RegularResource[NonStandardResource]
}

/**
 * Element-label relationship, with arcrole "http://xbrl.org/arcrole/2008/element-label".
 */
final case class ElementLabelRelationship(
  arc: dom.NonStandardArc,
  source: Endpoint,
  target: Endpoint.RegularResource[NonStandardResource]) extends ElementResourceRelationship

/**
 * Element-reference relationship, with arcrole "http://xbrl.org/arcrole/2008/element-reference".
 */
final case class ElementReferenceRelationship(
  arc: dom.NonStandardArc,
  source: Endpoint,
  target: Endpoint.RegularResource[NonStandardResource]) extends ElementResourceRelationship

/**
 * Element-message relationship, with a msg:message as target.
 */
final case class ElementMessageRelationship(
  arc: dom.NonStandardArc,
  source: Endpoint,
  target: Endpoint) extends NonStandardRelationship

/**
 * Other non-standard relationship in the locator-free model, so typically some generic relationship.
 *
 * TODO Open up this class for extension
 */
final case class OtherNonStandardRelationship(
  arc: dom.NonStandardArc,
  source: Endpoint,
  target: Endpoint) extends NonStandardRelationship

/**
 * Unknown relationship. Possibly an invalid relationship.
 */
final case class UnknownRelationship(
  arc: dom.XLinkArc,
  source: Endpoint,
  target: Endpoint) extends Relationship

/**
 * Inter-concept relationship in the locator-free model.
 */
sealed trait InterConceptRelationship extends StandardRelationship {

  def arc: dom.InterConceptArc

  def target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]

  final def targetConcept: EName = target.taxonomyElemKey.key

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
 * Hence the use of a RegularResource object for local or remote standard resources.
 */
sealed trait ConceptResourceRelationship extends StandardRelationship {

  def arc: dom.ConceptResourceArc

  def target: Endpoint.RegularResource[dom.StandardResource]
}

final case class ConceptLabelRelationship(
  arc: dom.LabelArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.RegularResource[dom.ConceptLabelResource])
  extends ConceptResourceRelationship {

  def resourceRole: String = target.resource.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    target.resource.attrOption(ENames.XmlLangEName)
      .getOrElse(sys.error(s"Missing xml:lang in ${target.resource.name} in ${target.resource.docUri}"))
  }

  def labelText: String = target.resource.text
}

final case class ConceptReferenceRelationship(
  arc: dom.ReferenceArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.RegularResource[dom.ConceptReferenceResource])
  extends ConceptResourceRelationship {

  def resourceRole: String = target.resource.roleOption.getOrElse(StandardReferenceRoles.StandardReference)

  def referenceElems: Seq[dom.TaxonomyElem] = target.resource.findAllChildElems
}

/**
 * Presentation relationship in the locator-free model.
 */
sealed trait PresentationRelationship extends InterConceptRelationship {

  def arc: dom.PresentationArc
}

final case class ParentChildRelationship(
  arc: dom.PresentationArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends PresentationRelationship

final case class OtherPresentationRelationship(
  arc: dom.PresentationArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends PresentationRelationship

/**
 * Calculation relationship in the locator-free model.
 */
sealed trait CalculationRelationship extends InterConceptRelationship {

  def arc: dom.CalculationArc
}

final case class SummationItemRelationship(
  arc: dom.CalculationArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends CalculationRelationship

final case class OtherCalculationRelationship(
  arc: dom.CalculationArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends CalculationRelationship

/**
 * Definition relationship in the locator-free model.
 */
sealed trait DefinitionRelationship extends InterConceptRelationship {

  def arc: dom.DefinitionArc
}

final case class GeneralSpecialRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DefinitionRelationship

final case class EssenceAliasRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DefinitionRelationship

final case class SimilarTuplesRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DefinitionRelationship

final case class RequiresElementRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DefinitionRelationship

/**
 * Dimensional (definition) relationship in the locator-free model.
 */
sealed trait DimensionalRelationship extends DefinitionRelationship

sealed trait HasHypercubeRelationship extends DimensionalRelationship {

  final def primary: EName = sourceConcept

  final def hypercube: EName = targetConcept

  def isAllRelationship: Boolean

  final def isNotAllRelationship: Boolean = !isAllRelationship

  final def closed: Boolean = {
    arc.attrOption(ENames.XbrldtClosedEName).exists(v => XsBooleans.parseBoolean(v))
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
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = true
}

final case class NotAllRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends HasHypercubeRelationship {

  def isAllRelationship: Boolean = false
}

final case class HypercubeDimensionRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DimensionalRelationship {

  def hypercube: EName = sourceConcept

  def dimension: EName = targetConcept

  override def effectiveTargetRole: String = {
    arc.attrOption(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDimensionDomainArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

sealed trait DomainAwareRelationship extends DimensionalRelationship {

  final def usable: Boolean = {
    arc.attrOption(ENames.XbrldtUsableEName).forall(v => XsBooleans.parseBoolean(v))
  }

  final override def effectiveTargetRole: String = {
    arc.attrOption(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forDomainMemberArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

final case class DimensionDomainRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DomainAwareRelationship {

  def dimension: EName = sourceConcept

  def domain: EName = targetConcept
}

final case class DomainMemberRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DomainAwareRelationship {

  def domain: EName = sourceConcept

  def member: EName = targetConcept
}

final case class DimensionDefaultRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DimensionalRelationship {

  def dimension: EName = sourceConcept

  def defaultOfDimension: EName = targetConcept
}

/**
 * Definition relationship that is not one of the known standard or dimensional ones.
 */
final case class OtherDefinitionRelationship(
  arc: dom.DefinitionArc,
  source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
  target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]) extends DefinitionRelationship

// Companion objects

object Relationship {

  def apply(
    arc: dom.XLinkArc,
    source: Endpoint,
    target: Endpoint): Relationship = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, source.taxonomyElemKey) match {
      case (arc: dom.StandardArc, _: TaxonomyElemKeys.ConceptKey) =>
        StandardRelationship.opt(arc, source.asInstanceOf[Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]], target)
          .getOrElse(new UnknownRelationship(arc, source, target))
      case (arc: dom.NonStandardArc, _) =>
        NonStandardRelationship.opt(arc, source, target).getOrElse(new UnknownRelationship(arc, source, target))
      case _ =>
        new UnknownRelationship(arc, source, target)
    }
  }
}

object StandardRelationship {

  def opt(
    arc: dom.StandardArc,
    source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
    target: Endpoint): Option[StandardRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, target.taxonomyElemKey, target.targetResourceOption) match {
      case (arc: dom.InterConceptArc, _: TaxonomyElemKeys.ConceptKey, None) =>
        InterConceptRelationship.opt(arc, source, target.asInstanceOf[Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]])
      case (arc: dom.ConceptResourceArc, _, Some(_: dom.StandardResource)) =>
        ConceptResourceRelationship.opt(arc, source, target.asInstanceOf[Endpoint.RegularResource[dom.StandardResource]])
      case _ =>
        None
    }
  }
}

object NonStandardRelationship {

  def opt(
    arc: dom.NonStandardArc,
    source: Endpoint,
    target: Endpoint): Option[NonStandardRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, arc.arcrole, target.targetResourceOption) match {
      case (arc: dom.NonStandardArc, "http://xbrl.org/arcrole/2008/element-label", Some(_: NonStandardResource)) =>
        Some(ElementLabelRelationship(arc, source, target.asInstanceOf[Endpoint.RegularResource[NonStandardResource]]))
      case (arc: dom.NonStandardArc, "http://xbrl.org/arcrole/2008/element-reference", Some(_: NonStandardResource)) =>
        Some(ElementReferenceRelationship(arc, source, target.asInstanceOf[Endpoint.RegularResource[NonStandardResource]]))
      case (arc: dom.NonStandardArc, _, Some(e: dom.XLinkResource)) if e.name == ENames.MsgMessageEName =>
        Some(ElementMessageRelationship(arc, source, target))
      case (arc: dom.NonStandardArc, _, _) =>
        Some(OtherNonStandardRelationship(arc, source, target))
      case _ =>
        None
    }
  }
}

object UnknownRelationship {

  def opt(
    arc: dom.XLinkArc,
    source: Endpoint,
    target: Endpoint): Option[UnknownRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    Some(UnknownRelationship(arc, source, target))
  }
}

object InterConceptRelationship {

  def opt(
    arc: dom.InterConceptArc,
    source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
    target: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey]): Option[InterConceptRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    arc match {
      case arc: dom.PresentationArc =>
        arc.arcrole match {
          case "http://www.xbrl.org/2003/arcrole/parent-child" => Some(ParentChildRelationship(arc, source, target))
          case _ => Some(OtherPresentationRelationship(arc, source, target))
        }
      case arc: dom.DefinitionArc =>
        arc.arcrole match {
          case "http://xbrl.org/int/dim/arcrole/hypercube-dimension" => Some(HypercubeDimensionRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/dimension-domain" => Some(DimensionDomainRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/domain-member" => Some(DomainMemberRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/dimension-default" => Some(DimensionDefaultRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/all" => Some(AllRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/notAll" => Some(NotAllRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/general-special" => Some(GeneralSpecialRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/essence-alias" => Some(EssenceAliasRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/similar-tuples" => Some(SimilarTuplesRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/requires-element" => Some(RequiresElementRelationship(arc, source, target))
          case _ => Some(OtherDefinitionRelationship(arc, source, target))
        }
      case arc: dom.CalculationArc =>
        arc.arcrole match {
          case "http://www.xbrl.org/2003/arcrole/summation-item" => Some(SummationItemRelationship(arc, source, target))
          case _ => Some(OtherCalculationRelationship(arc, source, target))
        }
      case _ =>
        None
    }
  }
}

object ConceptResourceRelationship {

  def opt(
    arc: dom.ConceptResourceArc,
    source: Endpoint.KeyEndpoint[TaxonomyElemKeys.ConceptKey],
    target: Endpoint.RegularResource[dom.StandardResource]): Option[ConceptResourceRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, target.resource) match {
      case (arc: dom.LabelArc, _: dom.ConceptLabelResource) =>
        Some(ConceptLabelRelationship(arc, source, target.asInstanceOf[Endpoint.RegularResource[dom.ConceptLabelResource]]))
      case (arc: dom.ReferenceArc, _: dom.ConceptReferenceResource) =>
        Some(ConceptReferenceRelationship(arc, source, target.asInstanceOf[Endpoint.RegularResource[dom.ConceptReferenceResource]]))
      case _ =>
        None
    }
  }
}
