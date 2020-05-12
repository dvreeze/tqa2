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

package eu.cdevreeze.tqa2.locfreetaxonomy.dom

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.datatypes.XsBooleans
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

/**
 * Standardized but non-standard taxonomy element in a locator-free taxonomy. Typically but not necessarily
 * formula or table content. Common super-trait of StandardizedNonStandardLink, StandardizedNonStandardArc,
 * StandardizedNonStandardResource and StandardizedOtherNonXLinkElem.
 *
 * In this context, the word "standard" means: defined in the XBRL Core or Dimensions specifications,
 * or defined in the locator-free model as key resource.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.types
// scalastyle:off file.size.limit
sealed trait StandardizedNonStandardTaxonomyElem extends AnyNonStandardElem

/**
 * An XLink extended link that is not a standard link, but that is defined in some XBRL specification.
 * In this context, the word "standard" means: defined in the XBRL Core or Dimensions specifications.
 */
sealed trait StandardizedNonStandardLink extends StandardizedNonStandardTaxonomyElem with AnyNonStandardLink

/**
 * An XLink arc that is not a standard arc, but that is defined in some XBRL specification.
 * In this context, the word "standard" means: defined in the XBRL Core or Dimensions specifications.
 */
sealed trait StandardizedNonStandardArc extends StandardizedNonStandardTaxonomyElem with AnyNonStandardArc

/**
 * An XLink resource that is neither a standard resource nor a key resource, but that is defined in some XBRL specification.
 * In this context, the word "standard" means: defined in the XBRL Core or Dimensions specifications,
 * or defined in the locator-free model as key resource.
 */
sealed trait StandardizedNonStandardResource extends StandardizedNonStandardTaxonomyElem with AnyNonStandardResource

/**
 * An non-XLink element that is not a "standard element", but that is defined in some XBRL specification.
 * In this context, the word "standard" means: defined in the XBRL Core or Dimensions specifications.
 */
sealed trait StandardizedOtherNonXLinkElem extends StandardizedNonStandardTaxonomyElem with AnyOtherNonXLinkElem

/**
 * StandardizedNonStandardArc defined by one of the formula-related specifications.
 */
sealed trait FormulaArc extends StandardizedNonStandardArc

/**
 * StandardizedNonStandardResource defined by one of the formula-related specifications.
 */
sealed trait FormulaResource extends StandardizedNonStandardResource

/**
 * StandardizedOtherNonXLinkElem defined by one of the formula-related specifications.
 */
sealed trait FormulaNonXLinkElem extends StandardizedOtherNonXLinkElem

/**
 * StandardizedNonStandardArc defined by one of the table-related specifications.
 */
sealed trait TableArc extends StandardizedNonStandardArc

/**
 * StandardizedNonStandardResource defined by one of the table-related specifications.
 */
sealed trait TableResource extends StandardizedNonStandardResource

/**
 * StandardizedOtherNonXLinkElem defined by one of the table-related specifications.
 */
sealed trait TableNonXLinkElem extends StandardizedOtherNonXLinkElem

// Formula arcs

/**
 * A variable:variableArc
 */
final case class VariableArc(underlyingElem: BackingNodes.Elem) extends FormulaArc {
  requireName(ENames.VariableVariableArcEName)

  /**
   * Returns the name attribute as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nameAttrValue: EName = {
    val qname = attrAsQName(ENames.NameEName)
    scope.withoutDefaultNamespace.resolveQName(qname)
  }
}

/**
 * A variable:variableFilterArc
 */
final case class VariableFilterArc(underlyingElem: BackingNodes.Elem) extends FormulaArc {
  requireName(ENames.VariableVariableFilterArcEName)

  /**
   * Returns the boolean complement attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def complement: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.ComplementEName))
  }

  /**
   * Returns the boolean cover attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def cover: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.CoverEName))
  }
}

/**
 * A variable:variableSetFilterArc
 */
final case class VariableSetFilterArc(underlyingElem: BackingNodes.Elem) extends FormulaArc {
  requireName(ENames.VariableVariableSetFilterArcEName)

  /**
   * Returns the boolean complement attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def complement: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.ComplementEName))
  }
}

/**
 * Another FormulaArc, with unknown arc name but with a known formula-related arcrole.
 */
final case class OtherFormulaArc(underlyingElem: BackingNodes.Elem) extends FormulaArc

// Formula resources

/**
 * A variable set. See variable.xsd.
 */
sealed trait VariableSet extends FormulaResource

/**
 * A variable or parameter. See variable.xsd.
 */
sealed trait VariableOrParameter extends FormulaResource

/**
 * A variable. See variable.xsd.
 */
sealed trait Variable extends VariableOrParameter

/**
 * An assertion. Either in substitution group validation:assertion or validation:variableSetAssertion. See validation.xsd.
 */
sealed trait Assertion extends FormulaResource

/**
 * A validation:assertionSet.
 */
final case class AssertionSet(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.ValidationAssertionSetEName)
}

/**
 * A variable set assertion. See validation.xsd.
 */
sealed trait VariableSetAssertion extends VariableSet with Assertion

/**
 * A va:valueAssertion.
 */
final case class ValueAssertion(underlyingElem: BackingNodes.Elem) extends VariableSetAssertion {
  requireName(ENames.VaValueAssertionEName)
}

/**
 * A formula:formula.
 */
final case class Formula(underlyingElem: BackingNodes.Elem) extends VariableSet {
  requireName(ENames.FormulaFormulaEName)
}

/**
 * An ea:existenceAssertion.
 */
final case class ExistenceAssertion(underlyingElem: BackingNodes.Elem) extends VariableSetAssertion {
  requireName(ENames.EaExistenceAssertionEName)
}

/**
 * A ca:consistencyAssertion.
 */
final case class ConsistencyAssertion(underlyingElem: BackingNodes.Elem) extends Assertion {
  requireName(ENames.CaConsistencyAssertionEName)
}

/**
 * A variable:precondition.
 */
final case class Precondition(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.VariablePreconditionEName)
}

/**
 * A variable:parameter. Not final, because an instance:instance is also a parameter
 */
sealed trait Parameter extends VariableOrParameter

/**
 * A variable:parameter, that is not of a sub-type.
 */
final case class RegularParameter(underlyingElem: BackingNodes.Elem) extends Parameter {
  requireName(ENames.VariableParameterEName)
}

/**
 * A variable:factVariable.
 */
final case class FactVariable(underlyingElem: BackingNodes.Elem) extends Variable {
  requireName(ENames.VariableFactVariableEName)
}

/**
 * A variable:generalVariable.
 */
final case class GeneralVariable(underlyingElem: BackingNodes.Elem) extends Variable {
  requireName(ENames.VariableGeneralVariableEName)
}

/**
 * An instance:instance.
 */
final case class Instance(underlyingElem: BackingNodes.Elem) extends Parameter {
  requireName(ENames.InstancesInstanceEName)
}

/**
 * A variable:function.
 */
final case class Function(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.VariableFunctionEName)
}

/**
 * A variable:equalityDefinition.
 */
final case class EqualityDefinition(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.VariableEqualityDefinitionEName)
}

/**
 * A cfi:implementation.
 */
final case class FunctionImplementation(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.CfiImplementationEName)
}

/**
 * A msg:message, as used in a formula-related context. Strictly speaking messages are not just related
 * to formulas, but they are introduced here to avoid sub-classing the core DOM type NonStandardResource.
 */
final case class Message(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.MsgMessageEName)
}

sealed trait Severity extends FormulaResource

/**
 * A sev:ok.
 */
final case class OkSeverity(underlyingElem: BackingNodes.Elem) extends Severity {
  requireName(ENames.SevOkEName)
}

/**
 * A sev:warning.
 */
final case class WarningSeverity(underlyingElem: BackingNodes.Elem) extends Severity {
  requireName(ENames.SevWarningEName)
}

/**
 * A sev:error.
 */
final case class ErrorSeverity(underlyingElem: BackingNodes.Elem) extends Severity {
  requireName(ENames.SevErrorEName)
}

/**
 * A filter.
 */
sealed trait Filter extends FormulaResource

/**
 * A concept filter.
 */
sealed trait ConceptFilter extends Filter

/**
 * A cf:conceptName filter.
 */
final case class ConceptNameFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptNameEName)
}

/**
 * A cf:conceptPeriodType filter.
 */
final case class ConceptPeriodTypeFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptPeriodTypeEName)
}

/**
 * A cf:conceptBalance filter.
 */
final case class ConceptBalanceFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptBalanceEName)
}

/**
 * A cf:conceptCustomAttribute filter.
 */
final case class ConceptCustomAttributeFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptCustomAttributeEName)
}

/**
 * A cf:conceptDataType filter.
 */
final case class ConceptDataTypeFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptDataTypeEName)
}

/**
 * A cf:conceptSubstitutionGroup filter.
 */
final case class ConceptSubstitutionGroupFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptSubstitutionGroupEName)
}

/**
 * A boolean filter.
 */
sealed trait BooleanFilter extends Filter

/**
 * A bf:andFilter filter.
 */
final case class AndFilter(underlyingElem: BackingNodes.Elem) extends BooleanFilter {
  requireName(ENames.BfAndFilterEName)
}

/**
 * A bf:orFilter filter.
 */
final case class OrFilter(underlyingElem: BackingNodes.Elem) extends BooleanFilter {
  requireName(ENames.BfOrFilterEName)
}

/**
 * A dimension filter.
 */
sealed trait DimensionFilter extends Filter

/**
 * A df:explicitDimension filter.
 */
final case class ExplicitDimensionFilter(underlyingElem: BackingNodes.Elem) extends DimensionFilter {
  requireName(ENames.DfExplicitDimensionEName)
}

/**
 * A df:typedDimension filter.
 */
final case class TypedDimensionFilter(underlyingElem: BackingNodes.Elem) extends DimensionFilter {
  requireName(ENames.DfTypedDimensionEName)
}

/**
 * An entity filter.
 */
sealed trait EntityFilter extends Filter

/**
 * An ef:identifier filter.
 */
final case class IdentifierFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfIdentifierEName)
}

/**
 * An ef:specificScheme filter.
 */
final case class SpecificSchemeFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfSpecificSchemeEName)
}

/**
 * An ef:regexpScheme filter.
 */
final case class RegexpSchemeFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfRegexpSchemeEName)
}

/**
 * An ef:specificIdentifier filter.
 */
final case class SpecificIdentifierFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfSpecificIdentifierEName)
}

/**
 * An ef:regexpIdentifier filter.
 */
final case class RegexpIdentifierFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfRegexpIdentifierEName)
}

/**
 * A general filter (gf:general).
 */
final case class GeneralFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.GfGeneralEName)
}

/**
 * A match filter.
 */
sealed trait MatchFilter extends Filter

/**
 * An mf:matchConcept filter.
 */
final case class MatchConceptFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchConceptEName)
}

/**
 * An mf:matchLocation filter.
 */
final case class MatchLocationFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchLocationEName)
}

/**
 * An mf:matchUnit filter.
 */
final case class MatchUnitFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchUnitEName)
}

/**
 * An mf:matchEntityIdentifier filter.
 */
final case class MatchEntityIdentifierFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchEntityIdentifierEName)
}

/**
 * An mf:matchPeriod filter.
 */
final case class MatchPeriodFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchPeriodEName)
}

/**
 * An mf:matchSegment filter.
 */
final case class MatchSegmentFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchSegmentEName)
}

/**
 * An mf:matchScenario filter.
 */
final case class MatchScenarioFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchScenarioEName)
}

/**
 * An mf:matchNonXDTSegment filter.
 */
final case class MatchNonXDTSegmentFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchNonXDTSegmentEName)
}

/**
 * An mf:matchNonXDTScenario filter.
 */
final case class MatchNonXDTScenarioFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchNonXDTScenarioEName)
}

/**
 * An mf:matchDimension filter.
 */
final case class MatchDimensionFilter(underlyingElem: BackingNodes.Elem) extends MatchFilter {
  requireName(ENames.MfMatchDimensionEName)
}

/**
 * A period aspect filter.
 */
sealed trait PeriodAspectFilter extends Filter

/**
 * A pf:period filter.
 */
final case class PeriodFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfPeriodEName)
}

/**
 * A pf:periodStart filter.
 */
final case class PeriodStartFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfPeriodStartEName)
}

/**
 * A pf:periodEnd filter.
 */
final case class PeriodEndFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfPeriodEndEName)
}

/**
 * A pf:periodInstant filter.
 */
final case class PeriodInstantFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfPeriodInstantEName)
}

/**
 * A pf:forever filter.
 */
final case class ForeverFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfForeverEName)
}

/**
 * A pf:instantDuration filter.
 */
final case class InstantDurationFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfInstantDurationEName)
}

/**
 * A relative filter (rf:relativeFilter).
 */
final case class RelativeFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.RfRelativeFilterEName)
}

/**
 * A segment scenario filter.
 */
sealed trait SegmentScenarioFilter extends Filter

/**
 * An ssf:segment filter.
 */
final case class SegmentFilter(underlyingElem: BackingNodes.Elem) extends SegmentScenarioFilter {
  requireName(ENames.SsfSegmentEName)
}

/**
 * An ssf:scenario filter.
 */
final case class ScenarioFilter(underlyingElem: BackingNodes.Elem) extends SegmentScenarioFilter {
  requireName(ENames.SsfScenarioEName)
}

/**
 * A tuple filter.
 */
sealed trait TupleFilter extends Filter

/**
 * A tf:parentFilter filter.
 */
final case class ParentFilter(underlyingElem: BackingNodes.Elem) extends TupleFilter {
  requireName(ENames.TfParentFilterEName)
}

/**
 * A tf:ancestorFilter filter.
 */
final case class AncestorFilter(underlyingElem: BackingNodes.Elem) extends TupleFilter {
  requireName(ENames.TfAncestorFilterEName)
}

/**
 * A tf:siblingFilter filter.
 */
final case class SiblingFilter(underlyingElem: BackingNodes.Elem) extends TupleFilter {
  requireName(ENames.TfSiblingFilterEName)
}

/**
 * A tf:locationFilter filter.
 */
final case class LocationFilter(underlyingElem: BackingNodes.Elem) extends TupleFilter {
  requireName(ENames.TfLocationFilterEName)
}

/**
 * A unit filter.
 */
sealed trait UnitFilter extends Filter

/**
 * An uf:singleMeasure filter.
 */
final case class SingleMeasureFilter(underlyingElem: BackingNodes.Elem) extends UnitFilter {
  requireName(ENames.UfSingleMeasureEName)
}

/**
 * An uf:generalMeasure filter.
 */
final case class GeneralMeasureFilter(underlyingElem: BackingNodes.Elem) extends UnitFilter {
  requireName(ENames.UfGeneralMeasuresEName)
}

/**
 * A value filter.
 */
sealed trait ValueFilter extends Filter

/**
 * A vf:nil filter.
 */
final case class NilFilter(underlyingElem: BackingNodes.Elem) extends ValueFilter {
  requireName(ENames.VfNilEName)
}

/**
 * A vf:precision filter.
 */
final case class PrecisionFilter(underlyingElem: BackingNodes.Elem) extends ValueFilter {
  requireName(ENames.VfPrecisionEName)
}

/**
 * An aspect cover filter (acf:aspectCover).
 */
final case class AspectCoverFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.AcfAspectCoverEName)
}

/**
 * A concept relation filter (crf:conceptRelation).
 */
final case class ConceptRelationFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.CrfConceptRelationEName)
}

// Formula non-XLink elements

/**
 * A child element of a variable:function.
 */
sealed trait FunctionContentElem extends FormulaNonXLinkElem

/**
 * A variable:input child element of a variable:function.
 */
final case class FunctionInput(underlyingElem: BackingNodes.Elem) extends FunctionContentElem {
  requireName(ENames.VariableInputEName)
}

/**
 * A child element of a cfi:implementation.
 */
sealed trait FunctionImplementationContentElem extends FormulaNonXLinkElem

/**
 * A cfi:input child element of a cfi:implementation.
 */
final case class FunctionImplementationInput(underlyingElem: BackingNodes.Elem) extends FunctionImplementationContentElem {
  requireName(ENames.CfiInputEName)
}

/**
 * A cfi:step child element of a cfi:implementation.
 */
final case class FunctionImplementationStep(underlyingElem: BackingNodes.Elem) extends FunctionImplementationContentElem {
  requireName(ENames.CfiStepEName)
}

/**
 * A cfi:output child element of a cfi:implementation.
 */
final case class FunctionImplementationOutput(underlyingElem: BackingNodes.Elem) extends FunctionImplementationContentElem {
  requireName(ENames.CfiOutputEName)
}

/**
 * A descendant element of a concept filter.
 */
sealed trait ConceptFilterContentElem extends FormulaNonXLinkElem

/**
 * A cf:concept child element of a concept filter.
 */
final case class ConceptFilterConcept(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfConceptEName)
}

/**
 * A cf:attribute child element of a concept filter.
 */
final case class ConceptFilterAttribute(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfAttributeEName)
}

/**
 * A cf:type child element of a concept filter.
 */
final case class ConceptFilterType(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfTypeEName)
}

/**
 * A cf:substitutionGroup child element of a concept filter.
 */
final case class ConceptFilterSubstitutionGroup(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfSubstitutionGroupEName)
}

/**
 * A cf:qname descendant element of a concept filter.
 */
final case class ConceptFilterQName(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfQnameEName)
}

/**
 * A cf:qnameExpression descendant element of a concept filter.
 */
final case class ConceptFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfQnameExpressionEName)
}

/**
 * A descendant element of a tuple filter.
 */
sealed trait TupleFilterContentElem extends FormulaNonXLinkElem

/**
 * A tf:parent child element of a concept filter.
 */
final case class TupleFilterParent(underlyingElem: BackingNodes.Elem) extends TupleFilterContentElem {
  requireName(ENames.TfParentEName)
}

/**
 * A tf:ancestor child element of a concept filter.
 */
final case class TupleFilterAncestor(underlyingElem: BackingNodes.Elem) extends TupleFilterContentElem {
  requireName(ENames.TfAncestorEName)
}

/**
 * A tf:qname descendant element of a tuple filter.
 */
final case class TupleFilterQName(underlyingElem: BackingNodes.Elem) extends TupleFilterContentElem {
  requireName(ENames.TfQnameEName)
}

/**
 * A tf:qnameExpression descendant element of a tuple filter.
 */
final case class TupleFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends TupleFilterContentElem {
  requireName(ENames.TfQnameExpressionEName)
}

/**
 * A descendant element of a dimension filter.
 */
sealed trait DimensionFilterContentElem extends FormulaNonXLinkElem

/**
 * A df:dimension child element of a dimension filter.
 */
final case class DimensionFilterDimension(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfDimensionEName)
}

/**
 * A df:member child element of a dimension filter.
 */
final case class DimensionFilterMember(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfMemberEName)
}

/**
 * A df:variable descendant element of a dimension filter.
 */
final case class DimensionFilterVariable(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfVariableEName)
}

/**
 * A df:linkrole descendant element of a dimension filter.
 */
final case class DimensionFilterLinkrole(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfLinkroleEName)
}

/**
 * A df:arcrole descendant element of a dimension filter.
 */
final case class DimensionFilterArcrole(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfArcroleEName)
}

/**
 * A df:axis descendant element of a dimension filter.
 */
final case class DimensionFilterAxis(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfAxisEName)
}

/**
 * A df:qname descendant element of a dimension filter.
 */
final case class DimensionFilterQName(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfQnameEName)
}

/**
 * A df:qnameExpression descendant element of a dimension filter.
 */
final case class DimensionFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfQnameExpressionEName)
}

/**
 * A descendant element of a unit filter.
 */
sealed trait UnitFilterContentElem extends FormulaNonXLinkElem

/**
 * A uf:measure child element of a dimension filter.
 */
final case class UnitFilterMeasure(underlyingElem: BackingNodes.Elem) extends UnitFilterContentElem {
  requireName(ENames.UfMeasureEName)
}

/**
 * A uf:qname descendant element of a unit filter.
 */
final case class UnitFilterQName(underlyingElem: BackingNodes.Elem) extends UnitFilterContentElem {
  requireName(ENames.UfQnameEName)
}

/**
 * A uf:qnameExpression descendant element of a unit filter.
 */
final case class UnitFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends UnitFilterContentElem {
  requireName(ENames.UfQnameExpressionEName)
}

/**
 * A descendant element of an aspect cover filter.
 */
sealed trait AspectCoverFilterContentElem extends FormulaNonXLinkElem

/**
 * An acf:aspect descendant element of a dimension filter.
 */
final case class AspectCoverFilterAspect(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfAspectEName)
}

/**
 * An acf:dimension descendant element of a dimension filter.
 */
final case class AspectCoverFilterDimension(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfDimensionEName)
}

/**
 * An acf:excludeDimension descendant element of a dimension filter.
 */
final case class AspectCoverFilterExcludeDimension(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfExcludeDimensionEName)
}

/**
 * An acf:qname descendant element of an aspect cover filter.
 */
final case class AspectCoverFilterQName(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfQnameEName)
}

/**
 * An acf:qnameExpression descendant element of an aspect cover filter.
 */
final case class AspectCoverFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfQnameExpressionEName)
}

/**
 * A descendant element of a concept relation filter.
 */
sealed trait ConceptRelationFilterContentElem extends FormulaNonXLinkElem

/**
 * A crf:axis descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterAxis(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfAxisEName)
}

/**
 * A crf:generations descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterGenerations(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfGenerationsEName)
}

/**
 * A crf:variable descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterVariable(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfVariableEName)
}

/**
 * A crf:linkrole descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinkrole(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinkroleEName)
}

/**
 * A crf:linkroleExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinkroleExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinkroleExpressionEName)
}

/**
 * A crf:linkname descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinkname(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinknameEName)
}

/**
 * A crf:linknameExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinknameExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinknameExpressionEName)
}

/**
 * A crf:arcrole descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcrole(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcroleEName)
}

/**
 * A crf:arcroleExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcroleExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcroleExpressionEName)
}

/**
 * A crf:arcname descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcname(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcnameEName)
}

/**
 * A crf:arcnameExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcnameExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcnameExpressionEName)
}

/**
 * A crf:qname descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterQName(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfQnameEName)
}

/**
 * A crf:qnameExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfQnameExpressionEName)
}

/**
 * An aspect or aspects element.
 */
sealed trait FormulaAspectOrAspectsElem extends FormulaNonXLinkElem

/**
 * An aspects element.
 */
final case class FormulaAspectsElem(underlyingElem: BackingNodes.Elem) extends FormulaAspectOrAspectsElem {
  requireName(ENames.FormulaAspectsEName)
}

/**
 * An aspect.
 */
sealed trait FormulaAspect extends FormulaAspectOrAspectsElem

/**
 * A formula:concept.
 */
final case class ConceptAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaConceptEName)
}

/**
 * A formula:entityIdentifier.
 */
final case class EntityIdentifierAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaEntityIdentifierEName)
}

/**
 * A formula:period.
 */
final case class PeriodAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaPeriodEName)
}

/**
 * A formula:unit.
 */
final case class UnitAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaUnitEName)
}

/**
 * An OCC aspect.
 */
sealed trait OccAspect extends FormulaAspect

/**
 * A formula:occEmpty.
 */
final case class OccEmptyAspect(underlyingElem: BackingNodes.Elem) extends OccAspect {
  requireName(ENames.FormulaOccEmptyEName)
}

/**
 * A formula:fragments.
 */
final case class OccFragmentsAspect(underlyingElem: BackingNodes.Elem) extends OccAspect {
  requireName(ENames.FormulaOccFragmentsEName)
}

/**
 * A formula:occXpath.
 */
final case class OccXpathAspect(underlyingElem: BackingNodes.Elem) extends OccAspect {
  requireName(ENames.FormulaOccXpathEName)
}

/**
 * A dimension aspect.
 */
sealed trait DimensionAspect extends FormulaAspect

/**
 * A formula:explicitDimension.
 */
final case class ExplicitDimensionAspect(underlyingElem: BackingNodes.Elem) extends DimensionAspect {
  requireName(ENames.FormulaExplicitDimensionEName)
}

/**
 * A formula:typedDimension.
 */
final case class TypedDimensionAspect(underlyingElem: BackingNodes.Elem) extends DimensionAspect {
  requireName(ENames.FormulaTypedDimensionEName)
}

/**
 * A formula:qname.
 */
final case class QNameElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaQNameEName)
}

/**
 * A formula:qnameExpression.
 */
final case class QNameExpressionElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaQNameExpressionEName)
}

/**
 * A child element of a PeriodAspect.
 */
sealed trait PeriodElem extends FormulaNonXLinkElem

/**
 * A formula:forever.
 */
final case class ForeverElem(underlyingElem: BackingNodes.Elem) extends PeriodElem {
  requireName(ENames.FormulaForeverEName)
}

/**
 * A formula:instant.
 */
final case class InstantElem(underlyingElem: BackingNodes.Elem) extends PeriodElem {
  requireName(ENames.FormulaInstantEName)
}

/**
 * A formula:duration.
 */
final case class DurationElem(underlyingElem: BackingNodes.Elem) extends PeriodElem {
  requireName(ENames.FormulaDurationEName)
}

/**
 * A formula:multiplyBy.
 */
final case class MultiplyByElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaMultiplyByEName)
}

/**
 * A formula:divideBy.
 */
final case class DivideByElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaDivideByEName)
}

/**
 * A formula:member.
 */
final case class MemberElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaMemberEName)
}

/**
 * A formula:omit.
 */
final case class OmitElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaOmitEName)
}

/**
 * A formula:xpath.
 */
final case class XpathElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaXpathEName)
}

/**
 * A formula:value.
 */
final case class ValueElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaValueEName)
}

/**
 * A formula:precision.
 */
final case class PrecisionElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaPrecisionEName)
}

/**
 * A formula:decimals.
 */
final case class DecimalsElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaDecimalsEName)
}
