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
import eu.cdevreeze.tqa2.aspect.Aspect
import eu.cdevreeze.tqa2.aspect.AspectModel
import eu.cdevreeze.tqa2.common.datatypes.XsBooleans
import eu.cdevreeze.tqa2.common.xpath.ScopedXPathString
import eu.cdevreeze.tqa2.common.xpath.TypedValue
import eu.cdevreeze.tqa2.common.xpath.TypedValueExpr
import eu.cdevreeze.tqa2.common.xpath.TypedValueProvider
import eu.cdevreeze.tqa2.locfreetaxonomy.common.AspectCoverFilters
import eu.cdevreeze.tqa2.locfreetaxonomy.common.ConceptRelationFilters
import eu.cdevreeze.tqa2.locfreetaxonomy.common.Occ
import eu.cdevreeze.tqa2.locfreetaxonomy.common.PeriodType
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

import scala.reflect.classTag

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
sealed trait VariableSet extends FormulaResource {

  /**
   * Returns the mandatory implicitFiltering attribute as boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def implicitFiltering: Boolean = {
    val attrValue = attr(ENames.ImplicitFilteringEName)
    XsBooleans.parseBoolean(attrValue)
  }

  /**
   * Returns the mandatory aspectModel attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def aspectModel: AspectModel = {
    val aspectModelString = attr(ENames.AspectModelEName)

    aspectModelString match {
      case "dimensional" => AspectModel.DimensionalAspectModel
      case _             => AspectModel.NonDimensionalAspectModel
    }
  }
}

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

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.TestEName), scope)
  }
}

/**
 * A formula:formula.
 */
final case class Formula(underlyingElem: BackingNodes.Elem) extends VariableSet {
  requireName(ENames.FormulaFormulaEName)

  def precisionElemOption: Option[PrecisionElem] = {
    findFirstChildElemOfType(classTag[PrecisionElem])
  }

  def decimalsElemOption: Option[DecimalsElem] = {
    findFirstChildElemOfType(classTag[DecimalsElem])
  }

  def formulaAspectsElems: Seq[FormulaAspectsElem] = {
    findAllChildElemsOfType(classTag[FormulaAspectsElem])
  }

  /**
   * Returns the mandatory value attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def valueExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.ValueEName), scope)
  }

  /**
   * Returns the source attribute as optional EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    val qnameOption = attrAsQNameOption(ENames.SourceEName)
    qnameOption.map(qn => scope.withoutDefaultNamespace.resolveQName(qn))
  }
}

/**
 * An ea:existenceAssertion.
 */
final case class ExistenceAssertion(underlyingElem: BackingNodes.Elem) extends VariableSetAssertion {
  requireName(ENames.EaExistenceAssertionEName)

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TestEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A ca:consistencyAssertion.
 */
final case class ConsistencyAssertion(underlyingElem: BackingNodes.Elem) extends Assertion {
  requireName(ENames.CaConsistencyAssertionEName)

  /**
   * Returns the mandatory strict attribute as Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def strict: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.StrictEName))
  }

  def absoluteAcceptanceRadiusOption: Option[ScopedXPathString] = {
    attrOption(ENames.AbsoluteAcceptanceRadiusEName).map(v => ScopedXPathString(v, scope))
  }

  def proportionalAcceptanceRadiusOption: Option[ScopedXPathString] = {
    attrOption(ENames.ProportionalAcceptanceRadiusEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A variable:precondition.
 */
final case class Precondition(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.VariablePreconditionEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.TestEName), scope)
  }
}

/**
 * A variable:parameter. Not final, because an instance:instance is also a parameter
 */
sealed trait Parameter extends VariableOrParameter {

  /**
   * Returns the mandatory name attribute as EName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def nameAttrValue: EName = {
    attrAsResolvedQName(ENames.NameEName)
  }

  /**
   * Returns the optional select attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def selectExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.SelectEName).map(v => ScopedXPathString(v, scope))
  }

  /**
   * Returns the optional "required" attribute as optional Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def requiredOption: Option[Boolean] = {
    attrOption(ENames.RequiredEName).map(v => XsBooleans.parseBoolean(v))
  }

  /**
   * Returns the "as" attribute as optional EName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def asOption: Option[EName] = {
    attrAsResolvedQNameOption(ENames.AsEName)
  }
}

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

  /**
   * Returns the optional nils attribute as optional Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nilsOption: Option[Boolean] = {
    attrOption(ENames.NilsEName).map(v => XsBooleans.parseBoolean(v))
  }

  /**
   * Returns the optional matches attribute as optional Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def matchesOption: Option[Boolean] = {
    attrOption(ENames.MatchesEName).map(v => XsBooleans.parseBoolean(v))
  }

  /**
   * Returns the optional fallbackValue attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def fallbackValueExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.FallbackValueEName).map(v => ScopedXPathString(v, scope))
  }

  /**
   * Returns the mandatory bindAsSequence attribute as Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def bindAsSequence: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.BindAsSequenceEName))
  }
}

/**
 * A variable:generalVariable.
 */
final case class GeneralVariable(underlyingElem: BackingNodes.Elem) extends Variable {
  requireName(ENames.VariableGeneralVariableEName)

  /**
   * Returns the mandatory select attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def selectExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.SelectEName), scope)
  }

  /**
   * Returns the mandatory bindAsSequence attribute as Boolean.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def bindAsSequence: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.BindAsSequenceEName))
  }
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

  /**
   * Returns the mandatory name attribute as EName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nameAttrValue: EName = {
    attrAsResolvedQName(ENames.NameEName)
  }

  def functionInputs: Seq[FunctionInput] = {
    findAllChildElemsOfType(classTag[FunctionInput])
  }

  /**
   * Returns the mandatory output attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def output: String = {
    attr(ENames.OutputEName)
  }
}

/**
 * A variable:equalityDefinition.
 */
final case class EqualityDefinition(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.VariableEqualityDefinitionEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.TestEName), scope)
  }
}

/**
 * A cfi:implementation.
 */
final case class FunctionImplementation(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.CfiImplementationEName)

  def inputs: Seq[FunctionImplementationInput] = {
    findAllChildElemsOfType(classTag[FunctionImplementationInput])
  }

  def steps: Seq[FunctionImplementationStep] = {
    findAllChildElemsOfType(classTag[FunctionImplementationStep])
  }

  def output: FunctionImplementationOutput = {
    findFirstChildElemOfType(classTag[FunctionImplementationOutput]).get
  }
}

/**
 * A msg:message, as used in a formula-related context.
 */
final case class Message(underlyingElem: BackingNodes.Elem) extends FormulaResource {
  requireName(ENames.MsgMessageEName)

  /**
   * Returns the mandatory lang attribute.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def lang: String = {
    attr(ENames.XmlLangEName)
  }
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

  def concepts: Seq[ConceptFilterConcept] = {
    findAllChildElemsOfType(classTag[ConceptFilterConcept])
  }
}

/**
 * A cf:conceptPeriodType filter.
 */
final case class ConceptPeriodTypeFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptPeriodTypeEName)

  def periodType: String = {
    attr(ENames.PeriodTypeEName)
  }
}

/**
 * A cf:conceptBalance filter.
 */
final case class ConceptBalanceFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptBalanceEName)

  def balance: String = {
    attr(ENames.BalanceEName)
  }
}

/**
 * A cf:conceptCustomAttribute filter.
 */
final case class ConceptCustomAttributeFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptCustomAttributeEName)

  def customAttribute: ConceptFilterAttribute = {
    findFirstChildElemOfType(classTag[ConceptFilterAttribute]).get
  }

  /**
   * Returns the optional value attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def valueExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.ValueEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A cf:conceptDataType filter.
 */
final case class ConceptDataTypeFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptDataTypeEName)

  def conceptDataType: ConceptFilterType = {
    findFirstChildElemOfType(classTag[ConceptFilterType]).get
  }

  def strict: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.StrictEName))
  }
}

/**
 * A cf:conceptSubstitutionGroup filter.
 */
final case class ConceptSubstitutionGroupFilter(underlyingElem: BackingNodes.Elem) extends ConceptFilter {
  requireName(ENames.CfConceptSubstitutionGroupEName)

  def conceptSubstitutionGroup: ConceptFilterSubstitutionGroup = {
    findFirstChildElemOfType(classTag[ConceptFilterSubstitutionGroup]).get
  }

  def strict: Boolean = {
    XsBooleans.parseBoolean(attr(ENames.StrictEName))
  }
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
sealed trait DimensionFilter extends Filter {

  final def dimension: DimensionFilterDimension = {
    findFirstChildElemOfType(classTag[DimensionFilterDimension]).get
  }
}

/**
 * A df:explicitDimension filter.
 */
final case class ExplicitDimensionFilter(underlyingElem: BackingNodes.Elem) extends DimensionFilter {
  requireName(ENames.DfExplicitDimensionEName)

  def members: Seq[DimensionFilterMember] = {
    findAllChildElemsOfType(classTag[DimensionFilterMember])
  }
}

/**
 * A df:typedDimension filter.
 */
final case class TypedDimensionFilter(underlyingElem: BackingNodes.Elem) extends DimensionFilter {
  requireName(ENames.DfTypedDimensionEName)

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TestEName).map(v => ScopedXPathString(v, scope))
  }
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

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.TestEName), scope)
  }
}

/**
 * An ef:specificScheme filter.
 */
final case class SpecificSchemeFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfSpecificSchemeEName)

  /**
   * Returns the mandatory scheme attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def schemeExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.SchemeEName), scope)
  }
}

/**
 * An ef:regexpScheme filter.
 */
final case class RegexpSchemeFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfRegexpSchemeEName)

  def pattern: String = {
    attr(ENames.PatternEName)
  }
}

/**
 * An ef:specificIdentifier filter.
 */
final case class SpecificIdentifierFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfSpecificIdentifierEName)

  /**
   * Returns the mandatory scheme attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def schemeExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.SchemeEName), scope)
  }

  /**
   * Returns the mandatory value attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def valueExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.ValueEName), scope)
  }
}

/**
 * An ef:regexpIdentifier filter.
 */
final case class RegexpIdentifierFilter(underlyingElem: BackingNodes.Elem) extends EntityFilter {
  requireName(ENames.EfRegexpIdentifierEName)

  def pattern: String = {
    attr(ENames.PatternEName)
  }
}

/**
 * A general filter (gf:general).
 */
final case class GeneralFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.GfGeneralEName)

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TestEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A match filter.
 */
sealed trait MatchFilter extends Filter {

  /**
   * Returns the variable attribute, as expanded name.
   */
  final def variable: EName = {
    val qn = attrAsQName(ENames.VariableEName)
    scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }

  final def matchAny: Boolean = {
    attrOption(ENames.MatchAnyEName).exists(s => XsBooleans.parseBoolean(s))
  }
}

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

  def dimension: EName = {
    attrAsResolvedQName(ENames.DimensionEName)
  }
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

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.TestEName), scope)
  }
}

/**
 * A pf:periodStart filter.
 */
final case class PeriodStartFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfPeriodStartEName)

  /**
   * Returns the mandatory date attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dateExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.DateEName), scope)
  }

  /**
   * Returns the optional time attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def timeExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TimeEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A pf:periodEnd filter.
 */
final case class PeriodEndFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfPeriodEndEName)

  /**
   * Returns the mandatory date attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dateExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.DateEName), scope)
  }

  /**
   * Returns the optional time attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def timeExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TimeEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A pf:periodInstant filter.
 */
final case class PeriodInstantFilter(underlyingElem: BackingNodes.Elem) extends PeriodAspectFilter {
  requireName(ENames.PfPeriodInstantEName)

  /**
   * Returns the mandatory date attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def dateExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.DateEName), scope)
  }

  /**
   * Returns the optional time attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def timeExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TimeEName).map(v => ScopedXPathString(v, scope))
  }
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

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = attrAsQName(ENames.VariableEName)
    scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }

  def boundary: String = {
    attr(ENames.BoundaryEName)
  }
}

/**
 * A relative filter (rf:relativeFilter).
 */
final case class RelativeFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.RfRelativeFilterEName)

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = attrAsQName(ENames.VariableEName)
    scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }
}

/**
 * A segment scenario filter.
 */
sealed trait SegmentScenarioFilter extends Filter {

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def testExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TestEName).map(v => ScopedXPathString(v, scope))
  }
}

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

  def parent: TupleFilterParent = {
    findFirstChildElemOfType(classTag[TupleFilterParent]).get
  }
}

/**
 * A tf:ancestorFilter filter.
 */
final case class AncestorFilter(underlyingElem: BackingNodes.Elem) extends TupleFilter {
  requireName(ENames.TfAncestorFilterEName)

  def ancestor: TupleFilterAncestor = {
    findFirstChildElemOfType(classTag[TupleFilterAncestor]).get
  }
}

/**
 * A tf:siblingFilter filter.
 */
final case class SiblingFilter(underlyingElem: BackingNodes.Elem) extends TupleFilter {
  requireName(ENames.TfSiblingFilterEName)

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = attrAsQName(ENames.VariableEName)
    scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }
}

/**
 * A tf:locationFilter filter.
 */
final case class LocationFilter(underlyingElem: BackingNodes.Elem) extends TupleFilter {
  requireName(ENames.TfLocationFilterEName)

  /**
   * Returns the variable attribute, as expanded name.
   */
  def variable: EName = {
    val qn = attrAsQName(ENames.VariableEName)
    scope.withoutDefaultNamespace.resolveQNameOption(qn).get
  }

  /**
   * Returns the mandatory location attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def locationExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.LocationEName), scope)
  }
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

  def measure: UnitFilterMeasure = {
    findFirstChildElemOfType(classTag[UnitFilterMeasure]).get
  }
}

/**
 * An uf:generalMeasure filter.
 */
final case class GeneralMeasureFilter(underlyingElem: BackingNodes.Elem) extends UnitFilter {
  requireName(ENames.UfGeneralMeasuresEName)

  /**
   * Returns the mandatory test attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.TestEName), scope)
  }
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

  /**
   * Returns the mandatory minimum attribute as ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def minimumExpr: ScopedXPathString = {
    ScopedXPathString(attr(ENames.MinimumEName), scope)
  }
}

/**
 * An aspect cover filter (acf:aspectCover).
 */
final case class AspectCoverFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.AcfAspectCoverEName)

  def aspects: Seq[AspectCoverFilterAspect] = {
    findAllChildElemsOfType(classTag[AspectCoverFilterAspect])
  }

  def dimensions: Seq[AspectCoverFilterDimension] = {
    findAllChildElemsOfType(classTag[AspectCoverFilterDimension])
  }

  def excludeDimensions: Seq[AspectCoverFilterExcludeDimension] = {
    findAllChildElemsOfType(classTag[AspectCoverFilterExcludeDimension])
  }
}

/**
 * A concept relation filter (crf:conceptRelation).
 */
final case class ConceptRelationFilter(underlyingElem: BackingNodes.Elem) extends Filter {
  requireName(ENames.CrfConceptRelationEName)

  /**
   * Returns `variableOption.orElse(qnameOption).orElse(qnameExpressionOption).get`.
   */
  def source: ConceptRelationFilterContentElem = {
    variableOption.orElse(qnameOption).orElse(qnameExpressionOption).getOrElse(sys.error(s"Missing variable, qname and qnameExpression"))
  }

  def variableOption: Option[ConceptRelationFilterVariable] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterVariable])
  }

  def qnameOption: Option[ConceptRelationFilterQName] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterQName])
  }

  def qnameExpressionOption: Option[ConceptRelationFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterQNameExpression])
  }

  /**
   * Returns the source as EName value provider. This may fail if this element is not schema-valid.
   */
  def sourceValueOrExpr: TypedValueProvider[EName] = {
    variableOption
      .map(_.name)
      .map(v => TypedValue(v))
      .orElse(
        qnameOption
          .map(_.qnameValue)
          .map(v => TypedValue(v))
          .orElse(qnameExpressionOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v))))
      .get
  }

  /**
   * Returns `linkroleOption.orElse(linkroleExpressionOption).get`.
   */
  def linkroleOrLinkroleExpression: ConceptRelationFilterContentElem = {
    linkroleOption.orElse(linkroleExpressionOption).getOrElse(sys.error(s"Missing linkrole or linkroleExpression"))
  }

  def linkroleOption: Option[ConceptRelationFilterLinkrole] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterLinkrole])
  }

  def linkroleExpressionOption: Option[ConceptRelationFilterLinkroleExpression] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterLinkroleExpression])
  }

  /**
   * Returns the linkrole as String value provider. This may fail if this element is not schema-valid.
   */
  def linkroleValueOrExpr: TypedValueProvider[String] = {
    linkroleOption
      .map(_.linkrole)
      .map(v => TypedValue(v))
      .orElse(linkroleExpressionOption.map(_.expr).map(v => TypedValueExpr(classTag[String], v)))
      .get
  }

  /**
   * Returns `linknameOption.orElse(linknameExpressionOption)`.
   */
  def linknameOrLinknameExpressionOption: Option[ConceptRelationFilterContentElem] = {
    linknameOption.orElse(linknameExpressionOption)
  }

  def linknameOption: Option[ConceptRelationFilterLinkname] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterLinkname])
  }

  def linknameExpressionOption: Option[ConceptRelationFilterLinknameExpression] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterLinknameExpression])
  }

  /**
   * Returns the linkname as optional EName value provider. This may fail if this element is not schema-valid.
   */
  def linknameValueOrExprOption: Option[TypedValueProvider[EName]] = {
    linknameOption
      .map(_.linknameValue)
      .map(v => TypedValue(v))
      .orElse(linknameExpressionOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
  }

  /**
   * Returns `arcroleOption.orElse(arcroleExpressionOption).get`.
   */
  def arcroleOrArcroleExpression: ConceptRelationFilterContentElem = {
    arcroleOption.orElse(arcroleExpressionOption).getOrElse(sys.error(s"Missing arcrole or arcroleExpression"))
  }

  def arcroleOption: Option[ConceptRelationFilterArcrole] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterArcrole])
  }

  def arcroleExpressionOption: Option[ConceptRelationFilterArcroleExpression] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterArcroleExpression])
  }

  /**
   * Returns the arcrole as String value provider. This may fail if this element is not schema-valid.
   */
  def arcroleValueOrExpr: TypedValueProvider[String] = {
    arcroleOption
      .map(_.arcrole)
      .map(v => TypedValue(v))
      .orElse(arcroleExpressionOption.map(_.expr).map(v => TypedValueExpr(classTag[String], v)))
      .get
  }

  /**
   * Returns `arcnameOption.orElse(arcnameExpressionOption)`.
   */
  def arcnameOrArcnameExpressionOption: Option[ConceptRelationFilterContentElem] = {
    arcnameOption.orElse(arcnameExpressionOption)
  }

  def arcnameOption: Option[ConceptRelationFilterArcname] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterArcname])
  }

  def arcnameExpressionOption: Option[ConceptRelationFilterArcnameExpression] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterArcnameExpression])
  }

  /**
   * Returns the arcname as optional EName value provider. This may fail if this element is not schema-valid.
   */
  def arcnameValueOrExprOption: Option[TypedValueProvider[EName]] = {
    arcnameOption
      .map(_.arcnameValue)
      .map(v => TypedValue(v))
      .orElse(arcnameExpressionOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
  }

  def axis: ConceptRelationFilterAxis = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterAxis]).get
  }

  def generationsOption: Option[ConceptRelationFilterGenerations] = {
    findFirstChildElemOfType(classTag[ConceptRelationFilterGenerations])
  }

  /**
   * Returns the optional test attribute as optional ScopedXPathString.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def testExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.TestEName).map(v => ScopedXPathString(v, scope))
  }
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

  /**
   * Returns the type attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def inputType: String = underlyingElem.attr(ENames.TypeEName)
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

  /**
   * Returns the mandatory name attribute as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nameAttrValue: EName = {
    val qn = attrAsQName(ENames.NameEName)
    scope.withoutDefaultNamespace.resolveQName(qn)
  }
}

/**
 * A cfi:step child element of a cfi:implementation.
 */
final case class FunctionImplementationStep(underlyingElem: BackingNodes.Elem) extends FunctionImplementationContentElem {
  requireName(ENames.CfiStepEName)

  /**
   * Returns the mandatory name attribute as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nameAttrValue: EName = {
    val qn = attrAsQName(ENames.NameEName)
    scope.withoutDefaultNamespace.resolveQName(qn)
  }

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}

/**
 * A cfi:output child element of a cfi:implementation.
 */
final case class FunctionImplementationOutput(underlyingElem: BackingNodes.Elem) extends FunctionImplementationContentElem {
  requireName(ENames.CfiOutputEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  def qnameElemOption: Option[ConceptFilterQName] = {
    findFirstChildElemOfType(classTag[ConceptFilterQName])
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[ConceptFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A cf:attribute child element of a concept filter.
 */
final case class ConceptFilterAttribute(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfAttributeEName)

  def qnameElemOption: Option[ConceptFilterQName] = {
    findFirstChildElemOfType(classTag[ConceptFilterQName])
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[ConceptFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A cf:type child element of a concept filter.
 */
final case class ConceptFilterType(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfTypeEName)

  def qnameElemOption: Option[ConceptFilterQName] = {
    findFirstChildElemOfType(classTag[ConceptFilterQName])
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[ConceptFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A cf:substitutionGroup child element of a concept filter.
 */
final case class ConceptFilterSubstitutionGroup(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfSubstitutionGroupEName)

  def qnameElemOption: Option[ConceptFilterQName] = {
    findFirstChildElemOfType(classTag[ConceptFilterQName])
  }

  def qnameExpressionElemOption: Option[ConceptFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[ConceptFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A cf:qname descendant element of a concept filter.
 */
final case class ConceptFilterQName(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A cf:qnameExpression descendant element of a concept filter.
 */
final case class ConceptFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends ConceptFilterContentElem {
  requireName(ENames.CfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  def qnameElemOption: Option[TupleFilterQName] = {
    findFirstChildElemOfType(classTag[TupleFilterQName])
  }

  def qnameExpressionElemOption: Option[TupleFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[TupleFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A tf:ancestor child element of a concept filter.
 */
final case class TupleFilterAncestor(underlyingElem: BackingNodes.Elem) extends TupleFilterContentElem {
  requireName(ENames.TfAncestorEName)

  def qnameElemOption: Option[TupleFilterQName] = {
    findFirstChildElemOfType(classTag[TupleFilterQName])
  }

  def qnameExpressionElemOption: Option[TupleFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[TupleFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A tf:qname descendant element of a tuple filter.
 */
final case class TupleFilterQName(underlyingElem: BackingNodes.Elem) extends TupleFilterContentElem {
  requireName(ENames.TfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A tf:qnameExpression descendant element of a tuple filter.
 */
final case class TupleFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends TupleFilterContentElem {
  requireName(ENames.TfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  def qnameElemOption: Option[DimensionFilterQName] = {
    findFirstChildElemOfType(classTag[DimensionFilterQName])
  }

  def qnameExpressionElemOption: Option[DimensionFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[DimensionFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A df:member child element of a dimension filter.
 */
final case class DimensionFilterMember(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfMemberEName)

  def variableElemOption: Option[DimensionFilterVariable] = {
    findFirstChildElemOfType(classTag[DimensionFilterVariable])
  }

  def qnameElemOption: Option[DimensionFilterQName] = {
    findFirstChildElemOfType(classTag[DimensionFilterQName])
  }

  def qnameExpressionElemOption: Option[DimensionFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[DimensionFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    variableElemOption
      .map(_.name)
      .map(v => TypedValue(v))
      .orElse(
        qnameElemOption
          .map(_.qnameValue)
          .map(v => TypedValue(v))
          .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v))))
      .get
  }

  def linkroleElemOption: Option[DimensionFilterLinkrole] = {
    findFirstChildElemOfType(classTag[DimensionFilterLinkrole])
  }

  def arcroleElemOption: Option[DimensionFilterArcrole] = {
    findFirstChildElemOfType(classTag[DimensionFilterArcrole])
  }

  def axisElemOption: Option[DimensionFilterAxis] = {
    findFirstChildElemOfType(classTag[DimensionFilterAxis])
  }
}

/**
 * A df:variable descendant element of a dimension filter.
 */
final case class DimensionFilterVariable(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfVariableEName)

  /**
   * Returns the text as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nameAttrValue: EName = {
    val qname = textAsQName
    scope.withoutDefaultNamespace.resolveQName(qname)
  }
}

/**
 * A df:linkrole descendant element of a dimension filter.
 */
final case class DimensionFilterLinkrole(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfLinkroleEName)

  def linkrole: String = text
}

/**
 * A df:arcrole descendant element of a dimension filter.
 */
final case class DimensionFilterArcrole(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfArcroleEName)

  def arcrole: String = text
}

/**
 * A df:axis descendant element of a dimension filter.
 */
final case class DimensionFilterAxis(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfAxisEName)

  def axis: String = text
}

/**
 * A df:qname descendant element of a dimension filter.
 */
final case class DimensionFilterQName(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A df:qnameExpression descendant element of a dimension filter.
 */
final case class DimensionFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends DimensionFilterContentElem {
  requireName(ENames.DfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  def qnameElemOption: Option[UnitFilterQName] = {
    findFirstChildElemOfType(classTag[UnitFilterQName])
  }

  def qnameExpressionElemOption: Option[UnitFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[UnitFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A uf:qname descendant element of a unit filter.
 */
final case class UnitFilterQName(underlyingElem: BackingNodes.Elem) extends UnitFilterContentElem {
  requireName(ENames.UfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A uf:qnameExpression descendant element of a unit filter.
 */
final case class UnitFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends UnitFilterContentElem {
  requireName(ENames.UfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  def aspectValue: AspectCoverFilters.Aspect = {
    AspectCoverFilters.Aspect.fromString(underlyingElem.text)
  }
}

/**
 * An acf:dimension descendant element of a dimension filter.
 */
final case class AspectCoverFilterDimension(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfDimensionEName)

  def qnameElemOption: Option[AspectCoverFilterQName] = {
    findFirstChildElemOfType(classTag[AspectCoverFilterQName])
  }

  def qnameExpressionElemOption: Option[AspectCoverFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[AspectCoverFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * An acf:excludeDimension descendant element of a dimension filter.
 */
final case class AspectCoverFilterExcludeDimension(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfExcludeDimensionEName)

  def qnameElemOption: Option[AspectCoverFilterQName] = {
    findFirstChildElemOfType(classTag[AspectCoverFilterQName])
  }

  def qnameExpressionElemOption: Option[AspectCoverFilterQNameExpression] = {
    findFirstChildElemOfType(classTag[AspectCoverFilterQNameExpression])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * An acf:qname descendant element of an aspect cover filter.
 */
final case class AspectCoverFilterQName(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * An acf:qnameExpression descendant element of an aspect cover filter.
 */
final case class AspectCoverFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends AspectCoverFilterContentElem {
  requireName(ENames.AcfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  def axisValue: ConceptRelationFilters.Axis = {
    ConceptRelationFilters.Axis.fromString(text)
  }
}

/**
 * A crf:generations descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterGenerations(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfGenerationsEName)

  def intValue: Int = {
    text.toInt
  }
}

/**
 * A crf:variable descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterVariable(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfVariableEName)

  /**
   * Returns the text as EName. The default namespace is not used to resolve the QName.
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def nameAttrValue: EName = {
    val qname = textAsQName
    scope.withoutDefaultNamespace.resolveQName(qname)
  }
}

/**
 * A crf:linkrole descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinkrole(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinkroleEName)

  def linkrole: String = text
}

/**
 * A crf:linkroleExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinkroleExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinkroleExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}

/**
 * A crf:linkname descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinkname(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinknameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def linknameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A crf:linknameExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterLinknameExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfLinknameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}

/**
 * A crf:arcrole descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcrole(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcroleEName)

  def arcrole: String = text
}

/**
 * A crf:arcroleExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcroleExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcroleExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}

/**
 * A crf:arcname descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcname(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def arcnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A crf:arcnameExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterArcnameExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfArcnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}

/**
 * A crf:qname descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterQName(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfQnameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A crf:qnameExpression descendant element of a concept relation filter.
 */
final case class ConceptRelationFilterQNameExpression(underlyingElem: BackingNodes.Elem) extends ConceptRelationFilterContentElem {
  requireName(ENames.CrfQnameExpressionEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    val sc = scope.withoutDefaultNamespace
    attrAsQNameOption(ENames.SourceEName).map(qn => sc.resolveQName(qn))
  }

  /**
   * Returns the aspects themselves.
   */
  def formulaAspects: Seq[FormulaAspect] = {
    findAllChildElemsOfType(classTag[FormulaAspect])
  }
}

/**
 * An aspect.
 */
sealed trait FormulaAspect extends FormulaAspectOrAspectsElem {

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def sourceOption: Option[EName] = {
    val sc = scope.withoutDefaultNamespace
    attrAsQNameOption(ENames.SourceEName).map(qn => sc.resolveQName(qn))
  }

  /**
   * Returns the aspect value, depending on the aspect model used.
   */
  def aspect(aspectModel: AspectModel): Aspect
}

/**
 * A formula:concept.
 */
final case class ConceptAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaConceptEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.ConceptAspect

  def qnameElemOption: Option[QNameElem] = {
    findFirstChildElemOfType(classTag[QNameElem])
  }

  def qnameExpressionElemOption: Option[QNameExpressionElem] = {
    findFirstChildElemOfType(classTag[QNameExpressionElem])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.qnameExpr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
}

/**
 * A formula:entityIdentifier.
 */
final case class EntityIdentifierAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaEntityIdentifierEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.EntityIdentifierAspect

  def schemeExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.SchemeEName).map(v => ScopedXPathString(v, scope))
  }

  def valueExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.ValueEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A formula:period.
 */
final case class PeriodAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaPeriodEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.PeriodAspect

  def foreverElemOption: Option[ForeverElem] = {
    findFirstChildElemOfType(classTag[ForeverElem])
  }

  def instantElemOption: Option[InstantElem] = {
    findFirstChildElemOfType(classTag[InstantElem])
  }

  def durationElemOption: Option[DurationElem] = {
    findFirstChildElemOfType(classTag[DurationElem])
  }

  def periodElems: Seq[PeriodElem] = {
    findAllChildElemsOfType(classTag[PeriodElem])
  }
}

/**
 * A formula:unit.
 */
final case class UnitAspect(underlyingElem: BackingNodes.Elem) extends FormulaAspect {
  requireName(ENames.FormulaUnitEName)

  def aspect(aspectModel: AspectModel): Aspect = Aspect.UnitAspect

  def multiplyByElems: Seq[MultiplyByElem] = {
    findAllChildElemsOfType(classTag[MultiplyByElem])
  }

  def divideByElems: Seq[DivideByElem] = {
    findAllChildElemsOfType(classTag[DivideByElem])
  }

  /**
   * Returns the optional boolean augment attribute. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def augmentOption: Option[Boolean] = {
    attrOption(ENames.AugmentEName).map(v => XsBooleans.parseBoolean(v))
  }
}

/**
 * An OCC aspect.
 */
sealed trait OccAspect extends FormulaAspect {

  final def aspect(aspectModel: AspectModel): Aspect.OccAspect = (occ, aspectModel) match {
    case (Occ.Segment, AspectModel.DimensionalAspectModel)     => Aspect.NonXDTSegmentAspect
    case (Occ.Segment, AspectModel.NonDimensionalAspectModel)  => Aspect.CompleteSegmentAspect
    case (Occ.Scenario, AspectModel.DimensionalAspectModel)    => Aspect.NonXDTScenarioAspect
    case (Occ.Scenario, AspectModel.NonDimensionalAspectModel) => Aspect.CompleteScenarioAspect
  }

  /**
   * Returns the occ attribute as Occ. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def occ: Occ = {
    Occ.fromString(attr(ENames.OccEName))
  }
}

/**
 * A formula:occEmpty.
 */
final case class OccEmptyAspect(underlyingElem: BackingNodes.Elem) extends OccAspect {
  requireName(ENames.FormulaOccEmptyEName)
}

/**
 * A formula:occFragments.
 */
final case class OccFragmentsAspect(underlyingElem: BackingNodes.Elem) extends OccAspect {
  requireName(ENames.FormulaOccFragmentsEName)
}

/**
 * A formula:occXpath.
 */
final case class OccXpathAspect(underlyingElem: BackingNodes.Elem) extends OccAspect {
  requireName(ENames.FormulaOccXpathEName)

  def selectExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.SelectEName).map(v => ScopedXPathString(v, scope))
  }
}

/**
 * A dimension aspect.
 */
sealed trait DimensionAspect extends FormulaAspect {

  final def aspect(aspectModel: AspectModel): Aspect.DimensionAspect = {
    require(aspectModel == AspectModel.DimensionalAspectModel, s"Only the dimensional aspect model supports dimension aspects")

    Aspect.DimensionAspect(dimension)
  }

  /**
   * Returns the dimension attribute as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  final def dimension: EName = {
    attrAsResolvedQName(ENames.DimensionEName)
  }
}

/**
 * A formula:explicitDimension.
 */
final case class ExplicitDimensionAspect(underlyingElem: BackingNodes.Elem) extends DimensionAspect {
  requireName(ENames.FormulaExplicitDimensionEName)

  def memberElemOption: Option[MemberElem] = {
    findFirstChildElemOfType(classTag[MemberElem])
  }

  def omitElemOption: Option[OmitElem] = {
    findFirstChildElemOfType(classTag[OmitElem])
  }
}

/**
 * A formula:typedDimension.
 */
final case class TypedDimensionAspect(underlyingElem: BackingNodes.Elem) extends DimensionAspect {
  requireName(ENames.FormulaTypedDimensionEName)

  def xpathElemOption: Option[XpathElem] = {
    findFirstChildElemOfType(classTag[XpathElem])
  }

  def valueElemOption: Option[ValueElem] = {
    findFirstChildElemOfType(classTag[ValueElem])
  }

  def omitElemOption: Option[OmitElem] = {
    findFirstChildElemOfType(classTag[OmitElem])
  }
}

/**
 * A formula:qname.
 */
final case class QNameElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaQNameEName)

  /**
   * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
   */
  def qnameValue: EName = {
    textAsResolvedQName
  }
}

/**
 * A formula:qnameExpression.
 */
final case class QNameExpressionElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaQNameExpressionEName)

  def qnameExpr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}

/**
 * A child element of a PeriodAspect.
 */
sealed trait PeriodElem extends FormulaNonXLinkElem {

  def periodType: PeriodType
}

/**
 * A formula:forever.
 */
final case class ForeverElem(underlyingElem: BackingNodes.Elem) extends PeriodElem {
  requireName(ENames.FormulaForeverEName)

  def periodType: PeriodType = PeriodType.Duration
}

/**
 * A formula:instant.
 */
final case class InstantElem(underlyingElem: BackingNodes.Elem) extends PeriodElem {
  requireName(ENames.FormulaInstantEName)

  def valueExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.ValueEName).map(v => ScopedXPathString(v, scope))
  }

  def periodType: PeriodType = PeriodType.Instant
}

/**
 * A formula:duration.
 */
final case class DurationElem(underlyingElem: BackingNodes.Elem) extends PeriodElem {
  requireName(ENames.FormulaDurationEName)

  def startExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.StartEName).map(v => ScopedXPathString(v, scope))
  }

  def endExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.EndEName).map(v => ScopedXPathString(v, scope))
  }

  def periodType: PeriodType = PeriodType.Duration
}

/**
 * A formula:multiplyBy.
 */
final case class MultiplyByElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaMultiplyByEName)

  def measureExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.MeasureEName).map(v => ScopedXPathString(v, scope))
  }

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    attrAsQNameOption(ENames.SourceEName).map(qn => scope.withoutDefaultNamespace.resolveQName(qn))
  }
}

/**
 * A formula:divideBy.
 */
final case class DivideByElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaDivideByEName)

  def measureExprOption: Option[ScopedXPathString] = {
    attrOption(ENames.MeasureEName).map(v => ScopedXPathString(v, scope))
  }

  /**
   * Returns the optional source as EName. The default namespace is not used to resolve the QName.
   *
   * This may fail with an exception if the taxonomy is not schema-valid.
   */
  def sourceOption: Option[EName] = {
    attrAsQNameOption(ENames.SourceEName).map(qn => scope.withoutDefaultNamespace.resolveQName(qn))
  }
}

/**
 * A formula:member.
 */
final case class MemberElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaMemberEName)

  def qnameElemOption: Option[QNameElem] = {
    findFirstChildElemOfType(classTag[QNameElem])
  }

  def qnameExpressionElemOption: Option[QNameExpressionElem] = {
    findFirstChildElemOfType(classTag[QNameExpressionElem])
  }

  /**
   * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
   */
  def qnameValueOrExpr: TypedValueProvider[EName] = {
    qnameElemOption
      .map(_.qnameValue)
      .map(v => TypedValue(v))
      .orElse(qnameExpressionElemOption.map(_.qnameExpr).map(v => TypedValueExpr(classTag[EName], v)))
      .get
  }
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

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
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

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}

/**
 * A formula:decimals.
 */
final case class DecimalsElem(underlyingElem: BackingNodes.Elem) extends FormulaNonXLinkElem {
  requireName(ENames.FormulaDecimalsEName)

  def expr: ScopedXPathString = {
    ScopedXPathString(text, scope)
  }
}
