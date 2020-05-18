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
import eu.cdevreeze.tqa2.locfreetaxonomy.common._
import eu.cdevreeze.tqa2.locfreetaxonomy.dom
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Endpoint.ConceptKeyEndpoint
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Endpoint.RegularResource
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
 * Prohibition/overriding resolution and computation of networks of relationships are possible without needing any context other
 * than the relationships themselves. For caveats, see the documentation of class Endpoint. Note that base sets are found
 * by grouping relationships on the base set keys of the relationships. For finding equivalent relationships and resolving
 * prohibition/overriding, note that each relationship contains the arc, with its ancestry (like the parent extended link).
 * Along with the endpoints (which can be compared for identity of XML fragments) this is enough data to resolve prohibition/overriding.
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
    require(arc.findParentElem.nonEmpty, s"Missing parent (extended link) element of an arc in $docUri")
    this
  }

  final def docUri: URI = arc.docUri

  final def baseUri: URI = arc.baseUri

  final def elr: String = arc.elr

  final def arcName: EName = arc.name

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

  protected[relationship] def requireArcrole(requiredArcrole: String): Unit = {
    require(arcrole == requiredArcrole, s"Required arcrole: $requiredArcrole. Found arcrole $arcrole instead, in document $docUri")
  }
}

/**
 * Standard relationship in the locator-free model, so either an inter-concept relationship or a concept-resource relationship.
 */
sealed trait StandardRelationship extends Relationship {

  def arc: dom.StandardArc

  def source: ConceptKeyEndpoint

  final def sourceConcept: EName = source.taxonomyElemKey.key
}

/**
 * Non-standard relationship in the locator-free model, so typically a generic relationship.
 * This trait is not sealed, so may be extended for custom taxonomy "dialects".
 */
trait NonStandardRelationship extends Relationship {

  def arc: dom.AnyNonStandardArc
}

/**
 * Either an element-label relationship or element-reference relationship.
 */
sealed trait ElementResourceRelationship extends NonStandardRelationship {

  def target: RegularResource[dom.NonStandardResource]
}

/**
 * Element-label relationship, with arcrole "http://xbrl.org/arcrole/2008/element-label".
 */
final case class ElementLabelRelationship(arc: dom.AnyNonStandardArc, source: Endpoint, target: RegularResource[dom.NonStandardResource])
    extends ElementResourceRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/element-label")
}

/**
 * Element-reference relationship, with arcrole "http://xbrl.org/arcrole/2008/element-reference".
 */
final case class ElementReferenceRelationship(
    arc: dom.AnyNonStandardArc,
    source: Endpoint,
    target: RegularResource[dom.NonStandardResource])
    extends ElementResourceRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/element-reference")
}

/**
 * Element-message relationship, with a msg:message as target.
 */
sealed trait ElementMessageRelationship extends NonStandardRelationship {

  def target: RegularResource[_ <: dom.Message]

  final def message: dom.Message = target.resource
}

sealed trait AssertionMessageRelationship extends ElementMessageRelationship {

  def source: RegularResource[_ <: dom.Assertion]

  def assertion: dom.Assertion = source.resource
}

/**
 * An assertion-satisfied-message relationship.
 */
final case class AssertionSatisfiedMessageRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.Assertion],
    target: RegularResource[_ <: dom.Message])
    extends AssertionMessageRelationship {
  requireArcrole("http://xbrl.org/arcrole/2010/assertion-satisfied-message")
}

/**
 * An assertion-unsatisfied-message relationship.
 */
final case class AssertionUnsatisfiedMessageRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.Assertion],
    target: RegularResource[_ <: dom.Message])
    extends AssertionMessageRelationship {
  requireArcrole("http://xbrl.org/arcrole/2010/assertion-unsatisfied-message")
}

/**
 * Other (typically custom) element-message relationship in the locator-free model.
 */
final case class OtherElementMessageRelationship(arc: dom.AnyNonStandardArc, source: Endpoint, target: RegularResource[_ <: dom.Message])
    extends ElementMessageRelationship

/**
 * Other (typically custom) non-standard relationship in the locator-free model, so typically some generic relationship.
 */
final case class OtherNonStandardRelationship(arc: dom.AnyNonStandardArc, source: Endpoint, target: Endpoint)
    extends NonStandardRelationship

/**
 * Unknown relationship. Possibly an invalid relationship.
 */
final case class UnknownRelationship(arc: dom.XLinkArc, source: Endpoint, target: Endpoint) extends Relationship

/**
 * Inter-concept relationship in the locator-free model.
 */
sealed trait InterConceptRelationship extends StandardRelationship {

  def arc: dom.InterConceptArc

  def target: ConceptKeyEndpoint

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

  def target: RegularResource[dom.StandardResource]
}

final case class ConceptLabelRelationship(arc: dom.LabelArc, source: ConceptKeyEndpoint, target: RegularResource[dom.ConceptLabelResource])
    extends ConceptResourceRelationship {

  def resourceRole: String = target.resource.roleOption.getOrElse(StandardLabelRoles.StandardLabel)

  def language: String = {
    target.resource
      .attrOption(ENames.XmlLangEName)
      .getOrElse(sys.error(s"Missing xml:lang in ${target.resource.name} in ${target.resource.docUri}"))
  }

  def labelText: String = target.resource.text
}

final case class ConceptReferenceRelationship(
    arc: dom.ReferenceArc,
    source: ConceptKeyEndpoint,
    target: RegularResource[dom.ConceptReferenceResource])
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

final case class ParentChildRelationship(arc: dom.PresentationArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends PresentationRelationship {
  requireArcrole(StandardArcroles.StandardPresentationArcrole)
}

final case class OtherPresentationRelationship(arc: dom.PresentationArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends PresentationRelationship

/**
 * Calculation relationship in the locator-free model.
 */
sealed trait CalculationRelationship extends InterConceptRelationship {

  def arc: dom.CalculationArc

  def weight: Double = arc.weight
}

final case class SummationItemRelationship(arc: dom.CalculationArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends CalculationRelationship {
  requireArcrole(StandardArcroles.StandardCalculationArcrole)
}

final case class OtherCalculationRelationship(arc: dom.CalculationArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends CalculationRelationship

/**
 * Definition relationship in the locator-free model.
 */
sealed trait DefinitionRelationship extends InterConceptRelationship {

  def arc: dom.DefinitionArc
}

final case class GeneralSpecialRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DefinitionRelationship {
  requireArcrole(StandardArcroles.GeneralSpecialArcrole)
}

final case class EssenceAliasRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DefinitionRelationship {
  requireArcrole(StandardArcroles.EssenceAliasArcrole)
}

final case class SimilarTuplesRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DefinitionRelationship {
  requireArcrole(StandardArcroles.SimilarTuplesArcrole)
}

final case class RequiresElementRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DefinitionRelationship {
  requireArcrole(StandardArcroles.RequiresElementArcrole)
}

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
    val attrValue = arc
      .attrOption(ENames.XbrldtContextElementEName)
      .getOrElse(sys.error(s"Missing attribute @xbrldt:contextElement on has-hypercube arc in $docUri"))

    ContextElement.fromString(attrValue)
  }

  final override def effectiveTargetRole: String = {
    arc.attrOption(ENames.XbrldtTargetRoleEName).getOrElse(elr)
  }

  final override def effectiveTargetBaseSetKey: BaseSetKey = {
    BaseSetKey.forHypercubeDimensionArc(effectiveTargetRole).ensuring(_.extLinkRole == effectiveTargetRole)
  }
}

final case class AllRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends HasHypercubeRelationship {
  requireArcrole("http://xbrl.org/int/dim/arcrole/all")

  def isAllRelationship: Boolean = true
}

final case class NotAllRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends HasHypercubeRelationship {
  requireArcrole("http://xbrl.org/int/dim/arcrole/notAll")

  def isAllRelationship: Boolean = false
}

final case class HypercubeDimensionRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DimensionalRelationship {
  requireArcrole("http://xbrl.org/int/dim/arcrole/hypercube-dimension")

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

final case class DimensionDomainRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DomainAwareRelationship {
  requireArcrole("http://xbrl.org/int/dim/arcrole/dimension-domain")

  def dimension: EName = sourceConcept

  def domain: EName = targetConcept
}

final case class DomainMemberRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DomainAwareRelationship {
  requireArcrole("http://xbrl.org/int/dim/arcrole/domain-member")

  def domain: EName = sourceConcept

  def member: EName = targetConcept
}

final case class DimensionDefaultRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DimensionalRelationship {
  requireArcrole("http://xbrl.org/int/dim/arcrole/dimension-default")

  def dimension: EName = sourceConcept

  def defaultOfDimension: EName = targetConcept
}

/**
 * Definition relationship that is not one of the known standard or dimensional ones.
 */
final case class OtherDefinitionRelationship(arc: dom.DefinitionArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint)
    extends DefinitionRelationship

// Formula and table relationships

/**
 * Non-standard formula-related relationship in the locator-free model, so typically a generic formula-related relationship.
 */
sealed trait FormulaRelationship extends NonStandardRelationship {

  def arc: dom.AnyNonStandardArc

  def source: RegularResource[_ <: dom.FormulaResource]

  def target: RegularResource[_ <: dom.FormulaResource]
}

/**
 * Non-standard table-related relationship in the locator-free model, so typically a generic table-related relationship.
 */
sealed trait TableRelationship extends NonStandardRelationship {

  def arc: dom.TableArc

  def source: RegularResource[_ <: dom.TableResource]

  def target: RegularResource[_ <: dom.FormulaOrTableResource]
}

/**
 * Variable-set relationship.
 */
final case class VariableSetRelationship(
    arc: dom.VariableArc,
    source: RegularResource[_ <: dom.VariableSet],
    target: RegularResource[_ <: dom.VariableOrParameter])
    extends FormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/variable-set")

  def variableSet: dom.VariableSet = source.resource

  def variableOrParameter: dom.VariableOrParameter = target.resource

  def arcNameAttrValue: EName = arc.nameAttrValue
}

/**
 * Variable-filter relationship.
 */
final case class VariableFilterRelationship(
    arc: dom.VariableFilterArc,
    source: RegularResource[_ <: dom.FactVariable],
    target: RegularResource[_ <: dom.Filter])
    extends FormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/variable-filter")

  def factVariable: dom.FactVariable = source.resource

  def filter: dom.Filter = target.resource

  def complement: Boolean = arc.complement

  def cover: Boolean = arc.cover
}

/**
 * Variable-set-filter relationship.
 */
final case class VariableSetFilterRelationship(
    arc: dom.VariableSetFilterArc,
    source: RegularResource[_ <: dom.VariableSet],
    target: RegularResource[_ <: dom.Filter])
    extends FormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/variable-set-filter")

  def variableSet: dom.VariableSet = source.resource

  def filter: dom.Filter = target.resource

  def complement: Boolean = arc.complement
}

/**
 * Boolean-filter relationship, backed by a VariableFilterArc.
 */
final case class BooleanFilterRelationship(
    arc: dom.VariableFilterArc,
    source: RegularResource[_ <: dom.BooleanFilter],
    target: RegularResource[_ <: dom.Filter])
    extends FormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/boolean-filter")

  def booleanFilter: dom.BooleanFilter = source.resource

  def subFilter: dom.Filter = target.resource

  def complement: Boolean = arc.complement

  def cover: Boolean = arc.cover
}

/**
 * A consistency-assertion-parameter relationship, backed by a VariableArc.
 */
final case class ConsistencyAssertionParameterRelationship(
    arc: dom.VariableArc,
    source: RegularResource[_ <: dom.ConsistencyAssertion],
    target: RegularResource[_ <: dom.Parameter])
    extends FormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/consistency-assertion-parameter")

  def consistencyAssertion: dom.ConsistencyAssertion = source.resource

  def parameter: dom.Parameter = target.resource

  def arcNameAttrValue: EName = arc.nameAttrValue
}

/**
 * Another formula-related relationship (with "unknown" arc element name).
 */
sealed trait OtherFormulaRelationship extends FormulaRelationship {

  def arc: dom.AnyNonStandardArc
}

/**
 * A variable-set-precondition relationship.
 */
final case class VariableSetPreconditionRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.VariableSet],
    target: RegularResource[_ <: dom.Precondition])
    extends OtherFormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/variable-set-precondition")

  def variableSet: dom.VariableSet = source.resource

  def precondition: dom.Precondition = target.resource
}

/**
 * A consistency-assertion-formula relationship.
 */
final case class ConsistencyAssertionFormulaRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.ConsistencyAssertion],
    target: RegularResource[_ <: dom.Formula])
    extends OtherFormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/consistency-assertion-formula")

  def consistencyAssertion: dom.ConsistencyAssertion = source.resource

  def formula: dom.Formula = target.resource
}

/**
 * An assertion-set relationship.
 */
final case class AssertionSetRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.AssertionSet],
    target: RegularResource[_ <: dom.Assertion])
    extends OtherFormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2008/assertion-set")

  def assertionSet: dom.AssertionSet = source.resource

  def assertion: dom.Assertion = target.resource
}

/**
 * An assertion-set relationship.
 */
final case class InstanceVariableRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.Instance],
    target: RegularResource[_ <: dom.Variable])
    extends OtherFormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2010/instance-variable")

  def instance: dom.Instance = source.resource

  def variable: dom.Variable = target.resource
}

/**
 * A formula-instance relationship.
 */
final case class FormulaInstanceRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.Formula],
    target: RegularResource[_ <: dom.Instance])
    extends OtherFormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2010/formula-instance")

  def formula: dom.Formula = source.resource

  def instance: dom.Instance = target.resource
}

/**
 * An assertion-unsatisfied-severity relationship.
 */
final case class AssertionUnsatisfiedSeverityRelationship(
    arc: dom.AnyNonStandardArc,
    source: RegularResource[_ <: dom.Assertion],
    target: RegularResource[_ <: dom.Severity])
    extends OtherFormulaRelationship {
  requireArcrole("http://xbrl.org/arcrole/2016/assertion-unsatisfied-severity")

  def assertion: dom.Assertion = source.resource

  def severity: dom.Severity = target.resource
}

/**
 * A table-breakdown relationship.
 */
final case class TableBreakdownRelationship(
    arc: dom.TableBreakdownArc,
    source: RegularResource[_ <: dom.Table],
    target: RegularResource[_ <: dom.TableBreakdown])
    extends TableRelationship {
  requireArcrole("http://xbrl.org/arcrole/2014/table-breakdown")

  def table: dom.Table = source.resource

  def breakdown: dom.TableBreakdown = target.resource

  def axis: TableAxis = arc.axis
}

/**
 * A breakdown-tree relationship.
 */
final case class BreakdownTreeRelationship(
    arc: dom.BreakdownTreeArc,
    source: RegularResource[_ <: dom.TableBreakdown],
    target: RegularResource[_ <: dom.DefinitionNode])
    extends TableRelationship {
  requireArcrole("http://xbrl.org/arcrole/2014/breakdown-tree")

  def breakdown: dom.TableBreakdown = source.resource

  def definitionNode: dom.DefinitionNode = target.resource
}

/**
 * A definition-node-subtree relationship.
 */
final case class DefinitionNodeSubtreeRelationship(
    arc: dom.DefinitionNodeSubtreeArc,
    source: RegularResource[_ <: dom.DefinitionNode],
    target: RegularResource[_ <: dom.DefinitionNode])
    extends TableRelationship {
  requireArcrole("http://xbrl.org/arcrole/2014/definition-node-subtree")

  def sourceNode: dom.DefinitionNode = source.resource

  def targetNode: dom.DefinitionNode = target.resource
}

/**
 * A table-filter relationship.
 */
final case class TableFilterRelationship(
    arc: dom.TableFilterArc,
    source: RegularResource[_ <: dom.Table],
    target: RegularResource[_ <: dom.Filter])
    extends TableRelationship {
  requireArcrole("http://xbrl.org/arcrole/2014/table-filter")

  def table: dom.Table = source.resource

  def filter: dom.Filter = target.resource

  def complement: Boolean = arc.complement
}

/**
 * A table-parameter relationship.
 */
final case class TableParameterRelationship(
    arc: dom.TableParameterArc,
    source: RegularResource[_ <: dom.Table],
    target: RegularResource[_ <: dom.Parameter])
    extends TableRelationship {
  requireArcrole("http://xbrl.org/arcrole/2014/table-parameter")

  def table: dom.Table = source.resource

  def parameter: dom.Parameter = target.resource

  /**
   * Returns the name attribute as EName. The default namespace is not used to resolve the QName.
   */
  def arcNameAttrValue: EName = arc.nameAttrValue
}

/**
 * An aspect-node-filter relationship.
 */
final case class AspectNodeFilterRelationship(
    arc: dom.AspectNodeFilterArc,
    source: RegularResource[_ <: dom.AspectNode],
    target: RegularResource[_ <: dom.Filter])
    extends TableRelationship {
  requireArcrole("http://xbrl.org/arcrole/2014/aspect-node-filter")

  def aspectNode: dom.AspectNode = source.resource

  def filter: dom.Filter = target.resource

  def complement: Boolean = arc.complement
}

// Companion objects

object Relationship {

  def apply(arc: dom.XLinkArc, source: Endpoint, target: Endpoint): Relationship = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, source) match {
      case (arc: dom.StandardArc, source: ConceptKeyEndpoint) =>
        StandardRelationship
          .opt(arc, source, target)
          .getOrElse(UnknownRelationship(arc, source, target))
      case (arc: dom.AnyNonStandardArc, _) =>
        NonStandardRelationship.opt(arc, source, target).getOrElse(UnknownRelationship(arc, source, target))
      case _ =>
        UnknownRelationship(arc, source, target)
    }
  }
}

object StandardRelationship {

  def opt(arc: dom.StandardArc, source: ConceptKeyEndpoint, target: Endpoint): Option[StandardRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, target, target.targetResourceOption) match {
      case (arc: dom.InterConceptArc, target: ConceptKeyEndpoint, None) =>
        InterConceptRelationship.opt(arc, source, target)
      case (arc: dom.ConceptResourceArc, target: RegularResource[_], Some(_: dom.StandardResource)) =>
        ConceptResourceRelationship.opt(arc, source, target.asInstanceOf[RegularResource[dom.StandardResource]])
      case _ =>
        None
    }
  }
}

object NonStandardRelationship {

  def opt(arc: dom.AnyNonStandardArc, source: Endpoint, target: Endpoint): Option[NonStandardRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    val ElementLabelArcrole = "http://xbrl.org/arcrole/2008/element-label"
    val ElementReferenceArcrole = "http://xbrl.org/arcrole/2008/element-reference"

    (arc, arc.arcrole, source, source.targetResourceOption, target, target.targetResourceOption) match {
      case (arc: dom.AnyNonStandardArc, ElementLabelArcrole, _, _, target: RegularResource[_], Some(_: dom.NonStandardResource)) =>
        Some(ElementLabelRelationship(arc, source, target.asInstanceOf[RegularResource[dom.NonStandardResource]]))
      case (arc: dom.AnyNonStandardArc, ElementReferenceArcrole, _, _, target: RegularResource[_], Some(_: dom.NonStandardResource)) =>
        Some(ElementReferenceRelationship(arc, source, target.asInstanceOf[RegularResource[dom.NonStandardResource]]))
      case (
          arc: dom.AnyNonStandardArc,
          "http://xbrl.org/arcrole/2010/assertion-satisfied-message",
          source: RegularResource[_],
          Some(_: dom.Assertion),
          target: RegularResource[_],
          Some(_: dom.Message)) =>
        Some(
          AssertionSatisfiedMessageRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.Assertion]],
            target.asInstanceOf[RegularResource[dom.Message]]))
      case (
          arc: dom.AnyNonStandardArc,
          "http://xbrl.org/arcrole/2010/assertion-unsatisfied-message",
          source: RegularResource[_],
          Some(_: dom.Assertion),
          target: RegularResource[_],
          Some(_: dom.Message)) =>
        Some(
          AssertionUnsatisfiedMessageRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.Assertion]],
            target.asInstanceOf[RegularResource[dom.Message]]))
      case (
          arc: dom.AnyNonStandardArc,
          _,
          source: RegularResource[_],
          Some(_: dom.FormulaResource),
          target: RegularResource[_],
          Some(_: dom.FormulaResource)) =>
        FormulaRelationship.opt(
          arc,
          source.asInstanceOf[RegularResource[dom.FormulaResource]],
          target.asInstanceOf[RegularResource[dom.FormulaResource]])
      case (
          arc: dom.TableArc,
          _,
          source: RegularResource[_],
          Some(_: dom.TableResource),
          target: RegularResource[_],
          Some(_: dom.FormulaOrTableResource)) =>
        TableRelationship.opt(
          arc,
          source.asInstanceOf[RegularResource[dom.TableResource]],
          target.asInstanceOf[RegularResource[dom.FormulaOrTableResource]])
      case (arc: dom.AnyNonStandardArc, _, _, _, target: RegularResource[_], Some(_: dom.Message)) =>
        Some(OtherElementMessageRelationship(arc, source, target.asInstanceOf[RegularResource[dom.Message]]))
      case (arc: dom.AnyNonStandardArc, _, _, _, _, _) =>
        Some(OtherNonStandardRelationship(arc, source, target))
      case _ =>
        None
    }
  }
}

object InterConceptRelationship {

  // scalastyle:off cyclomatic.complexity
  def opt(arc: dom.InterConceptArc, source: ConceptKeyEndpoint, target: ConceptKeyEndpoint): Option[InterConceptRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    arc match {
      case arc: dom.PresentationArc =>
        arc.arcrole match {
          case "http://www.xbrl.org/2003/arcrole/parent-child" => Some(ParentChildRelationship(arc, source, target))
          case _                                               => Some(OtherPresentationRelationship(arc, source, target))
        }
      case arc: dom.DefinitionArc =>
        arc.arcrole match {
          case "http://xbrl.org/int/dim/arcrole/hypercube-dimension" => Some(HypercubeDimensionRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/dimension-domain"    => Some(DimensionDomainRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/domain-member"       => Some(DomainMemberRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/dimension-default"   => Some(DimensionDefaultRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/all"                 => Some(AllRelationship(arc, source, target))
          case "http://xbrl.org/int/dim/arcrole/notAll"              => Some(NotAllRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/general-special"    => Some(GeneralSpecialRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/essence-alias"      => Some(EssenceAliasRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/similar-tuples"     => Some(SimilarTuplesRelationship(arc, source, target))
          case "http://www.xbrl.org/2003/arcrole/requires-element"   => Some(RequiresElementRelationship(arc, source, target))
          case _                                                     => Some(OtherDefinitionRelationship(arc, source, target))
        }
      case arc: dom.CalculationArc =>
        arc.arcrole match {
          case "http://www.xbrl.org/2003/arcrole/summation-item" => Some(SummationItemRelationship(arc, source, target))
          case _                                                 => Some(OtherCalculationRelationship(arc, source, target))
        }
      case _ =>
        None
    }
  }
}

object ConceptResourceRelationship {

  def opt(
      arc: dom.ConceptResourceArc,
      source: ConceptKeyEndpoint,
      target: RegularResource[dom.StandardResource]): Option[ConceptResourceRelationship] = {

    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, target.resource) match {
      case (arc: dom.LabelArc, _: dom.ConceptLabelResource) =>
        Some(ConceptLabelRelationship(arc, source, target.asInstanceOf[RegularResource[dom.ConceptLabelResource]]))
      case (arc: dom.ReferenceArc, _: dom.ConceptReferenceResource) =>
        Some(ConceptReferenceRelationship(arc, source, target.asInstanceOf[RegularResource[dom.ConceptReferenceResource]]))
      case _ =>
        None
    }
  }
}

object FormulaRelationship {

  def opt(
      arc: dom.AnyNonStandardArc,
      source: RegularResource[dom.FormulaResource],
      target: RegularResource[dom.FormulaResource]): Option[FormulaRelationship] = {
    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, arc.arcrole, source.targetResourceOption, target.targetResourceOption) match {
      case (
          arc: dom.VariableArc,
          "http://xbrl.org/arcrole/2008/variable-set",
          Some(_: dom.VariableSet),
          Some(_: dom.VariableOrParameter)) =>
        Some(
          VariableSetRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.VariableSet]],
            target.asInstanceOf[RegularResource[dom.VariableOrParameter]]))
      case (arc: dom.VariableFilterArc, "http://xbrl.org/arcrole/2008/variable-filter", Some(_: dom.FactVariable), Some(_: dom.Filter)) =>
        Some(
          VariableFilterRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.FactVariable]],
            target.asInstanceOf[RegularResource[dom.Filter]]))
      case (
          arc: dom.VariableSetFilterArc,
          "http://xbrl.org/arcrole/2008/variable-set-filter",
          Some(_: dom.VariableSet),
          Some(_: dom.Filter)) =>
        Some(
          VariableSetFilterRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.VariableSet]],
            target.asInstanceOf[RegularResource[dom.Filter]]))
      case (arc: dom.VariableFilterArc, "http://xbrl.org/arcrole/2008/boolean-filter", Some(_: dom.BooleanFilter), Some(_: dom.Filter)) =>
        Some(
          BooleanFilterRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.BooleanFilter]],
            target.asInstanceOf[RegularResource[dom.Filter]]))
      case (
          arc: dom.VariableArc,
          "http://xbrl.org/arcrole/2008/consistency-assertion-parameter",
          Some(_: dom.ConsistencyAssertion),
          Some(_: dom.Parameter)) =>
        Some(
          ConsistencyAssertionParameterRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.ConsistencyAssertion]],
            target.asInstanceOf[RegularResource[dom.Parameter]]))
      case (
          arc: dom.AnyNonStandardArc,
          "http://xbrl.org/arcrole/2008/variable-set-precondition",
          Some(_: dom.VariableSet),
          Some(_: dom.Precondition)) =>
        Some(
          VariableSetPreconditionRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.VariableSet]],
            target.asInstanceOf[RegularResource[dom.Precondition]]))
      case (
          arc: dom.AnyNonStandardArc,
          "http://xbrl.org/arcrole/2008/consistency-assertion-formula",
          Some(_: dom.ConsistencyAssertion),
          Some(_: dom.Formula)) =>
        Some(
          ConsistencyAssertionFormulaRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.ConsistencyAssertion]],
            target.asInstanceOf[RegularResource[dom.Formula]]))
      case (arc: dom.AnyNonStandardArc, "http://xbrl.org/arcrole/2008/assertion-set", Some(_: dom.AssertionSet), Some(_: dom.Assertion)) =>
        Some(
          AssertionSetRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.AssertionSet]],
            target.asInstanceOf[RegularResource[dom.Assertion]]))
      case (arc: dom.AnyNonStandardArc, "http://xbrl.org/arcrole/2010/instance-variable", Some(_: dom.Instance), Some(_: dom.Variable)) =>
        Some(
          InstanceVariableRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.Instance]],
            target.asInstanceOf[RegularResource[dom.Variable]]))
      case (arc: dom.AnyNonStandardArc, "http://xbrl.org/arcrole/2010/formula-instance", Some(_: dom.Formula), Some(_: dom.Instance)) =>
        Some(
          FormulaInstanceRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.Formula]],
            target.asInstanceOf[RegularResource[dom.Instance]]))
      case (
          arc: dom.AnyNonStandardArc,
          "http://xbrl.org/arcrole/2016/assertion-unsatisfied-severity",
          Some(_: dom.Assertion),
          Some(_: dom.Severity)) =>
        Some(
          AssertionUnsatisfiedSeverityRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.Assertion]],
            target.asInstanceOf[RegularResource[dom.Severity]]))
      case _ =>
        None
    }
  }
}

object TableRelationship {

  def opt(
      arc: dom.TableArc,
      source: RegularResource[dom.TableResource],
      target: RegularResource[dom.FormulaOrTableResource]): Option[TableRelationship] = {
    require(arc.attrOption(ENames.XLinkArcroleEName).nonEmpty, s"Missing arcrole on arc in ${arc.docUri}")

    (arc, arc.arcrole, source.targetResourceOption, target.targetResourceOption) match {
      case (arc: dom.TableBreakdownArc, "http://xbrl.org/arcrole/2014/table-breakdown", Some(_: dom.Table), Some(_: dom.TableBreakdown)) =>
        Some(
          TableBreakdownRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.Table]],
            target.asInstanceOf[RegularResource[dom.TableBreakdown]]))
      case (
          arc: dom.BreakdownTreeArc,
          "http://xbrl.org/arcrole/2014/breakdown-tree",
          Some(_: dom.TableBreakdown),
          Some(_: dom.DefinitionNode)) =>
        Some(
          BreakdownTreeRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.TableBreakdown]],
            target.asInstanceOf[RegularResource[dom.DefinitionNode]]))
      case (
          arc: dom.DefinitionNodeSubtreeArc,
          "http://xbrl.org/arcrole/2014/definition-node-subtree",
          Some(_: dom.DefinitionNode),
          Some(_: dom.DefinitionNode)) =>
        Some(
          DefinitionNodeSubtreeRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.DefinitionNode]],
            target.asInstanceOf[RegularResource[dom.DefinitionNode]]))
      case (arc: dom.TableFilterArc, "http://xbrl.org/arcrole/2014/table-filter", Some(_: dom.Table), Some(_: dom.Filter)) =>
        Some(
          TableFilterRelationship(arc, source.asInstanceOf[RegularResource[dom.Table]], target.asInstanceOf[RegularResource[dom.Filter]]))
      case (arc: dom.TableParameterArc, "http://xbrl.org/arcrole/2014/table-parameter", Some(_: dom.Table), Some(_: dom.Parameter)) =>
        Some(
          TableParameterRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.Table]],
            target.asInstanceOf[RegularResource[dom.Parameter]]))
      case (
          arc: dom.AspectNodeFilterArc,
          "http://xbrl.org/arcrole/2014/aspect-node-filter",
          Some(_: dom.AspectNode),
          Some(_: dom.Filter)) =>
        Some(
          AspectNodeFilterRelationship(
            arc,
            source.asInstanceOf[RegularResource[dom.AspectNode]],
            target.asInstanceOf[RegularResource[dom.Filter]]))
      case _ =>
        None
    }
  }
}
