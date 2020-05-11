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
final case class AssertionSet(underlyingElem: BackingNodes.Elem) extends FormulaResource

/**
 * A variable set assertion. See validation.xsd.
 */
sealed trait VariableSetAssertion extends VariableSet with Assertion

/**
 * A va:valueAssertion.
 */
final case class ValueAssertion(underlyingElem: BackingNodes.Elem) extends VariableSetAssertion

/**
 * A formula:formula.
 */
final case class Formula(underlyingElem: BackingNodes.Elem) extends VariableSet

/**
 * An ea:existenceAssertion.
 */
final case class ExistenceAssertion(underlyingElem: BackingNodes.Elem) extends VariableSetAssertion

/**
 * A ca:consistencyAssertion.
 */
final case class ConsistencyAssertion(underlyingElem: BackingNodes.Elem) extends FormulaResource with Assertion

/**
 * A variable:precondition.
 */
final case class Precondition(underlyingElem: BackingNodes.Elem) extends FormulaResource

/**
 * A variable:parameter. Not final, because an instance:instance is also a parameter
 */
sealed trait Parameter extends VariableOrParameter

/**
 * A variable:parameter, that is not of a sub-type.
 */
final case class RegularParameter(underlyingElem: BackingNodes.Elem) extends Parameter

/**
 * A variable:factVariable.
 */
final case class FactVariable(underlyingElem: BackingNodes.Elem) extends Variable

/**
 * A variable:generalVariable.
 */
final case class GeneralVariable(underlyingElem: BackingNodes.Elem) extends Variable

/**
 * An instance:instance.
 */
final case class Instance(underlyingElem: BackingNodes.Elem) extends Parameter

/**
 * A variable:function.
 */
final case class Function(underlyingElem: BackingNodes.Elem) extends FormulaResource

/**
 * A variable:equalityDefinition.
 */
final case class EqualityDefinition(underlyingElem: BackingNodes.Elem) extends FormulaResource

/**
 * A cfi:implementation.
 */
final case class FunctionImplementation(underlyingElem: BackingNodes.Elem) extends FormulaResource

/**
 * A msg:message, as used in a formula-related context. Strictly speaking messages are not just related
 * to formulas, but they are introduced here to avoid sub-classing the core DOM type NonStandardResource.
 */
final case class Message(underlyingElem: BackingNodes.Elem) extends FormulaResource

sealed trait Severity extends FormulaResource

/**
 * A sev:ok.
 */
final case class OkSeverity(underlyingElem: BackingNodes.Elem) extends Severity

/**
 * A sev:warning.
 */
final case class WarningSeverity(underlyingElem: BackingNodes.Elem) extends Severity

/**
 * A sev:error.
 */
final case class ErrorSeverity(underlyingElem: BackingNodes.Elem) extends Severity

/**
 * A filter.
 */
sealed trait Filter extends FormulaResource
