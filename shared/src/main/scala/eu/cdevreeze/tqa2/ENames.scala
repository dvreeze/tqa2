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

package eu.cdevreeze.tqa2

import eu.cdevreeze.yaidom2.core.EName

/**
 * Expanded names, for taxonomies and taxonomies in the locator-free model.
 *
 * @author Chris de Vreeze
 */
object ENames {

  import Namespaces._

  val XbrliItemEName = EName(XbrliNamespace, "item")
  val XbrliTupleEName = EName(XbrliNamespace, "tuple")
  val XbrliPeriodTypeEName = EName(XbrliNamespace, "periodType")
  val XbrliBalanceEName = EName(XbrliNamespace, "balance")

  val XbrldtTargetRoleEName = EName(XbrldtNamespace, "targetRole")
  val XbrldtUsableEName = EName(XbrldtNamespace, "usable")
  val XbrldtClosedEName = EName(XbrldtNamespace, "closed")
  val XbrldtDimensionItemEName = EName(XbrldtNamespace, "dimensionItem")
  val XbrldtHypercubeItemEName = EName(XbrldtNamespace, "hypercubeItem")
  val XbrldtTypedDomainRefEName = EName(XbrldtNamespace, "typedDomainRef")
  val XbrldtContextElementEName = EName(XbrldtNamespace, "contextElement")

  val XLinkTypeEName = EName(XLinkNamespace, "type")
  val XLinkHrefEName = EName(XLinkNamespace, "href")
  val XLinkLabelEName = EName(XLinkNamespace, "label")
  val XLinkRoleEName = EName(XLinkNamespace, "role")
  val XLinkArcroleEName = EName(XLinkNamespace, "arcrole")
  val XLinkFromEName = EName(XLinkNamespace, "from")
  val XLinkToEName = EName(XLinkNamespace, "to")

  val XsSchemaEName = EName(XsNamespace, "schema")
  val XsElementEName = EName(XsNamespace, "element")
  val XsAttributeEName = EName(XsNamespace, "attribute")
  val XsComplexTypeEName = EName(XsNamespace, "complexType")
  val XsSimpleTypeEName = EName(XsNamespace, "simpleType")
  val XsAnnotationEName = EName(XsNamespace, "annotation")
  val XsComplexContentEName = EName(XsNamespace, "complexContent")
  val XsSimpleContentEName = EName(XsNamespace, "simpleContent")
  val XsGroupEName = EName(XsNamespace, "group")
  val XsAllEName = EName(XsNamespace, "all")
  val XsChoiceEName = EName(XsNamespace, "choice")
  val XsSequenceEName = EName(XsNamespace, "sequence")
  val XsAttributeGroupEName = EName(XsNamespace, "attributeGroup")
  val XsAnyAttributeEName = EName(XsNamespace, "anyAttribute")
  val XsUniqueEName = EName(XsNamespace, "unique")
  val XsKeyEName = EName(XsNamespace, "key")
  val XsKeyrefEName = EName(XsNamespace, "keyref")
  val XsNotationEName = EName(XsNamespace, "notation")
  val XsImportEName = EName(XsNamespace, "import")
  val XsIncludeEName = EName(XsNamespace, "include")
  val XsRedefineEName = EName(XsNamespace, "redefine")
  val XsRestrictionEName = EName(XsNamespace, "restriction")
  val XsExtensionEName = EName(XsNamespace, "extension")
  val XsListEName = EName(XsNamespace, "list")
  val XsUnionEName = EName(XsNamespace, "union")
  val XsAppinfoEName = EName(XsNamespace, "appinfo")
  val XsDocumentationEName = EName(XsNamespace, "documentation")
  val XsSelectorEName = EName(XsNamespace, "selector")
  val XsFieldEName = EName(XsNamespace, "field")
  val XsAnyEName = EName(XsNamespace, "any")

  val XsMinExclusiveEName = EName(XsNamespace, "minExclusive")
  val XsMinInclusiveEName = EName(XsNamespace, "minInclusive")
  val XsMaxExclusiveEName = EName(XsNamespace, "maxExclusive")
  val XsMaxInclusiveEName = EName(XsNamespace, "maxInclusive")
  val XsTotalDigitsEName = EName(XsNamespace, "totalDigits")
  val XsFractionDigitsEName = EName(XsNamespace, "fractionDigits")
  val XsLengthEName = EName(XsNamespace, "length")
  val XsMinLengthEName = EName(XsNamespace, "minLength")
  val XsMaxLengthEName = EName(XsNamespace, "maxLength")
  val XsEnumerationEName = EName(XsNamespace, "enumeration")
  val XsWhiteSpaceEName = EName(XsNamespace, "whiteSpace")
  val XsPatternEName = EName(XsNamespace, "pattern")

  val XsAnyURI_EName = EName(XsNamespace, "anyURI")
  val XsBase64BinaryEName = EName(XsNamespace, "base64Binary")
  val XsBooleanEName = EName(XsNamespace, "boolean")
  val XsByteEName = EName(XsNamespace, "byte")
  val XsDateEName = EName(XsNamespace, "date")
  val XsDateTimeEName = EName(XsNamespace, "dateTime")
  val XsDecimalEName = EName(XsNamespace, "decimal")
  val XsDerivationControlEName = EName(XsNamespace, "derivationControl")
  val XsDoubleEName = EName(XsNamespace, "double")
  val XsDurationEName = EName(XsNamespace, "duration")
  val XsENTITIES_EName = EName(XsNamespace, "ENTITIES")
  val XsENTITY_EName = EName(XsNamespace, "ENTITY")
  val XsFloatEName = EName(XsNamespace, "float")
  val XsGDayEName = EName(XsNamespace, "gDay")
  val XsGMonthEName = EName(XsNamespace, "gMonth")
  val XsGMonthDayEName = EName(XsNamespace, "gMonthDay")
  val XsGYearEName = EName(XsNamespace, "gYear")
  val XsGYearMonthEName = EName(XsNamespace, "gYearMonth")
  val XsHexBinaryEName = EName(XsNamespace, "hexBinary")
  val XsID_EName = EName(XsNamespace, "ID")
  val XsIDREF_EName = EName(XsNamespace, "IDREF")
  val XsIDREFS_EName = EName(XsNamespace, "IDREFS")
  val XsIntEName = EName(XsNamespace, "int")
  val XsIntegerEName = EName(XsNamespace, "integer")
  val XsLanguageEName = EName(XsNamespace, "language")
  val XsLongEName = EName(XsNamespace, "long")
  val XsNameEName = EName(XsNamespace, "Name")
  val XsNCNameEName = EName(XsNamespace, "NCName")
  val XsNegativeIntegerEName = EName(XsNamespace, "negativeInteger")
  val XsNMTOKEN_EName = EName(XsNamespace, "NMTOKEN")
  val XsNMTOKENS_EName = EName(XsNamespace, "NMTOKENS")
  val XsNonNegativeIntegerEName = EName(XsNamespace, "nonNegativeInteger")
  val XsNonPositiveIntegerEName = EName(XsNamespace, "nonPositiveInteger")
  val XsNormalizedStringEName = EName(XsNamespace, "normalizedString")
  val XsNOTATION_EName = EName(XsNamespace, "NOTATION")
  val XsPositiveIntegerEName = EName(XsNamespace, "positiveInteger")
  val XsQNameEName = EName(XsNamespace, "QName")
  val XsShortEName = EName(XsNamespace, "short")
  val XsSimpleDerivationSetEName = EName(XsNamespace, "simpleDerivationSet")
  val XsStringEName = EName(XsNamespace, "string")
  val XsTimeEName = EName(XsNamespace, "time")
  val XsTokenEName = EName(XsNamespace, "token")
  val XsUnsignedByteEName = EName(XsNamespace, "unsignedByte")
  val XsUnsignedIntEName = EName(XsNamespace, "unsignedInt")
  val XsUnsignedLongEName = EName(XsNamespace, "unsignedLong")
  val XsUnsignedShortEName = EName(XsNamespace, "unsignedShort")
  val XsAnyAtomicTypeEName = EName(XsNamespace, "anyAtomicType")
  val XsAnySimpleTypeEName = EName(XsNamespace, "anySimpleType")
  val XsAnyTypeEName = EName(XsNamespace, "anyType")

  val LinkLinkbaseEName = EName(LinkNamespace, "linkbase")

  val LinkSchemaRefEName = EName(LinkNamespace, "schemaRef")
  val LinkLinkbaseRefEName = EName(LinkNamespace, "linkbaseRef")
  val LinkRoleRefEName = EName(LinkNamespace, "roleRef")
  val LinkArcroleRefEName = EName(LinkNamespace, "arcroleRef")

  val LinkLabelLinkEName = EName(LinkNamespace, "labelLink")
  val LinkLocEName = EName(LinkNamespace, "loc")
  val LinkLabelEName = EName(LinkNamespace, "label")
  val LinkLabelArcEName = EName(LinkNamespace, "labelArc")

  val LinkPresentationLinkEName = EName(LinkNamespace, "presentationLink")
  val LinkPresentationArcEName = EName(LinkNamespace, "presentationArc")

  val LinkReferenceLinkEName = EName(LinkNamespace, "referenceLink")
  val LinkReferenceEName = EName(LinkNamespace, "reference")
  val LinkReferenceArcEName = EName(LinkNamespace, "referenceArc")

  val LinkDefinitionLinkEName = EName(LinkNamespace, "definitionLink")
  val LinkDefinitionArcEName = EName(LinkNamespace, "definitionArc")

  val LinkCalculationLinkEName = EName(LinkNamespace, "calculationLink")
  val LinkCalculationArcEName = EName(LinkNamespace, "calculationArc")

  val LinkRoleTypeEName = EName(LinkNamespace, "roleType")
  val LinkArcroleTypeEName = EName(LinkNamespace, "arcroleType")
  val LinkDefinitionEName = EName(LinkNamespace, "definition")
  val LinkUsedOnEName = EName(LinkNamespace, "usedOn")

  val LinkPartEName = EName(LinkNamespace, "part")

  val GenLinkEName = EName(GenNamespace, "link")
  val GenArcEName = EName(GenNamespace, "arc")

  val LabelLabelEName = EName(LabelNamespace, "label")
  val ReferenceReferenceEName = EName(ReferenceNamespace, "reference")

  val RefPublisherEName = EName(RefNamespace, "Publisher")
  val RefNameEName = EName(RefNamespace, "Name")
  val RefNumberEName = EName(RefNamespace, "Number")
  val RefIssueDateEName = EName(RefNamespace, "IssueDate")
  val RefChapterEName = EName(RefNamespace, "Chapter")
  val RefArticleEName = EName(RefNamespace, "Article")
  val RefNoteEName = EName(RefNamespace, "Note")
  val RefSectionEName = EName(RefNamespace, "Section")
  val RefSubsectionEName = EName(RefNamespace, "Subsection")
  val RefParagraphEName = EName(RefNamespace, "Paragraph")
  val RefSubparagraphEName = EName(RefNamespace, "Subparagraph")
  val RefClauseEName = EName(RefNamespace, "Clause")
  val RefSubclauseEName = EName(RefNamespace, "Subclause")
  val RefAppendixEName = EName(RefNamespace, "Appendix")
  val RefExampleEName = EName(RefNamespace, "Example")
  val RefPageEName = EName(RefNamespace, "Page")
  val RefExhibitEName = EName(RefNamespace, "Exhibit")
  val RefFootnoteEName = EName(RefNamespace, "Footnote")
  val RefSentenceEName = EName(RefNamespace, "Sentence")
  val RefURIEName = EName(RefNamespace, "URI")
  val RefURIDateEName = EName(RefNamespace, "URIDate")

  val VariableVariableArcEName = EName(VariableNamespace, "variableArc")
  val VariableVariableFilterArcEName = EName(VariableNamespace, "variableFilterArc")
  val VariableVariableSetFilterArcEName = EName(VariableNamespace, "variableSetFilterArc")
  val VariablePreconditionEName = EName(VariableNamespace, "precondition")
  val VariableParameterEName = EName(VariableNamespace, "parameter")
  val VariableFactVariableEName = EName(VariableNamespace, "factVariable")
  val VariableGeneralVariableEName = EName(VariableNamespace, "generalVariable")
  val VariableFunctionEName = EName(VariableNamespace, "function")
  val VariableEqualityDefinitionEName = EName(VariableNamespace, "equalityDefinition")
  val VariableInputEName = EName(VariableNamespace, "input")

  val ValidationAssertionSetEName = EName(ValidationNamespace, "assertionSet")

  val InstancesInstanceEName = EName(InstancesNamespace, "instance")

  val MsgMessageEName = EName(MsgNamespace, "message")

  val XmlLangEName = EName(XmlNamespace, "lang")
  val XmlBaseEName = EName(XmlNamespace, "base")

  val XsiSchemaLocationEName = EName(XsiNamespace, "schemaLocation")

  val FormulaFormulaEName = EName(FormulaNamespace, "formula")
  val FormulaAspectsEName = EName(FormulaNamespace, "aspects")
  val FormulaPrecisionEName = EName(FormulaNamespace, "precision")
  val FormulaDecimalsEName = EName(FormulaNamespace, "decimals")
  val FormulaConceptEName = EName(FormulaNamespace, "concept")
  val FormulaEntityIdentifierEName = EName(FormulaNamespace, "entityIdentifier")
  val FormulaPeriodEName = EName(FormulaNamespace, "period")
  val FormulaUnitEName = EName(FormulaNamespace, "unit")
  val FormulaOccEmptyEName = EName(FormulaNamespace, "occEmpty")
  val FormulaOccFragmentsEName = EName(FormulaNamespace, "occFragments")
  val FormulaOccXpathEName = EName(FormulaNamespace, "occXpath")
  val FormulaExplicitDimensionEName = EName(FormulaNamespace, "explicitDimension")
  val FormulaTypedDimensionEName = EName(FormulaNamespace, "typedDimension")
  val FormulaMemberEName = EName(FormulaNamespace, "member")
  val FormulaQNameEName = EName(FormulaNamespace, "qname")
  val FormulaQNameExpressionEName = EName(FormulaNamespace, "qnameExpression")
  val FormulaOmitEName = EName(FormulaNamespace, "omit")
  val FormulaValueEName = EName(FormulaNamespace, "value")
  val FormulaXpathEName = EName(FormulaNamespace, "xpath")
  val FormulaForeverEName = EName(FormulaNamespace, "forever")
  val FormulaInstantEName = EName(FormulaNamespace, "instant")
  val FormulaDurationEName = EName(FormulaNamespace, "duration")
  val FormulaMultiplyByEName = EName(FormulaNamespace, "multiplyBy")
  val FormulaDivideByEName = EName(FormulaNamespace, "divideBy")

  val VaValueAssertionEName = EName(VaNamespace, "valueAssertion")

  val EaExistenceAssertionEName = EName(EaNamespace, "existenceAssertion")

  val CaConsistencyAssertionEName = EName(CaNamespace, "consistencyAssertion")

  val TableTableEName = EName(TableNamespace, "table")
  val TableBreakdownEName = EName(TableNamespace, "breakdown")
  val TableRuleNodeEName = EName(TableNamespace, "ruleNode")
  val TableConceptRelationshipNodeEName = EName(TableNamespace, "conceptRelationshipNode")
  val TableDimensionRelationshipNodeEName = EName(TableNamespace, "dimensionRelationshipNode")
  val TableAspectNodeEName = EName(TableNamespace, "aspectNode")
  val TableTableBreakdownArcEName = EName(TableNamespace, "tableBreakdownArc")
  val TableBreakdownTreeArcEName = EName(TableNamespace, "breakdownTreeArc")
  val TableDefinitionNodeSubtreeArcEName = EName(TableNamespace, "definitionNodeSubtreeArc")
  val TableTableFilterArcEName = EName(TableNamespace, "tableFilterArc")
  val TableTableParameterArcEName = EName(TableNamespace, "tableParameterArc")
  val TableAspectNodeFilterArcEName = EName(TableNamespace, "aspectNodeFilterArc")
  val TableConceptAspectEName = EName(TableNamespace, "conceptAspect")
  val TableUnitAspectEName = EName(TableNamespace, "unitAspect")
  val TableEntityIdentifierAspectEName = EName(TableNamespace, "entityIdentifierAspect")
  val TablePeriodAspectEName = EName(TableNamespace, "periodAspect")
  val TableDimensionAspectEName = EName(TableNamespace, "dimensionAspect")
  val TableRuleSetEName = EName(TableNamespace, "ruleSet")
  val TableDimensionEName = EName(TableNamespace, "dimension")
  val TableRelationshipSourceEName = EName(TableNamespace, "relationshipSource")
  val TableRelationshipSourceExpressionEName = EName(TableNamespace, "relationshipSourceExpression")
  val TableLinkroleEName = EName(TableNamespace, "linkrole")
  val TableLinkroleExpressionEName = EName(TableNamespace, "linkroleExpression")
  val TableArcroleEName = EName(TableNamespace, "arcrole")
  val TableArcroleExpressionEName = EName(TableNamespace, "arcroleExpression")
  val TableFormulaAxisEName = EName(TableNamespace, "formulaAxis")
  val TableFormulaAxisExpressionEName = EName(TableNamespace, "formulaAxisExpression")
  val TableGenerationsEName = EName(TableNamespace, "generations")
  val TableGenerationsExpressionEName = EName(TableNamespace, "generationsExpression")
  val TableLinknameEName = EName(TableNamespace, "linkname")
  val TableLinknameExpressionEName = EName(TableNamespace, "linknameExpression")
  val TableArcnameEName = EName(TableNamespace, "arcname")
  val TableArcnameExpressionEName = EName(TableNamespace, "arcnameExpression")

  val SevOkEName = EName(SevNamespace, "ok")
  val SevWarningEName = EName(SevNamespace, "warning")
  val SevErrorEName = EName(SevNamespace, "error")

  val GplPreferredLabelEName = EName(GplNamespace, "preferredLabel")

  val XfiRootEName = EName(XfiNamespace, "root")

  val CfiImplementationEName = EName(CfiNamespace, "implementation")
  val CfiInputEName = EName(CfiNamespace, "input")
  val CfiStepEName = EName(CfiNamespace, "step")
  val CfiOutputEName = EName(CfiNamespace, "output")

  val CfConceptNameEName = EName(CfNamespace, "conceptName")
  val CfConceptPeriodTypeEName = EName(CfNamespace, "conceptPeriodType")
  val CfConceptBalanceEName = EName(CfNamespace, "conceptBalance")
  val CfConceptCustomAttributeEName = EName(CfNamespace, "conceptCustomAttribute")
  val CfConceptDataTypeEName = EName(CfNamespace, "conceptDataType")
  val CfConceptSubstitutionGroupEName = EName(CfNamespace, "conceptSubstitutionGroup")

  val CfConceptEName = EName(CfNamespace, "concept")
  val CfAttributeEName = EName(CfNamespace, "attribute")
  val CfTypeEName = EName(CfNamespace, "type")
  val CfSubstitutionGroupEName = EName(CfNamespace, "substitutionGroup")
  val CfQnameEName = EName(CfNamespace, "qname")
  val CfQnameExpressionEName = EName(CfNamespace, "qnameExpression")

  val BfAndFilterEName = EName(BfNamespace, "andFilter")
  val BfOrFilterEName = EName(BfNamespace, "orFilter")

  val DfExplicitDimensionEName = EName(DfNamespace, "explicitDimension")
  val DfTypedDimensionEName = EName(DfNamespace, "typedDimension")

  val DfDimensionEName = EName(DfNamespace, "dimension")
  val DfMemberEName = EName(DfNamespace, "member")
  val DfLinkroleEName = EName(DfNamespace, "linkrole")
  val DfArcroleEName = EName(DfNamespace, "arcrole")
  val DfAxisEName = EName(DfNamespace, "axis")
  val DfVariableEName = EName(DfNamespace, "variable")
  val DfQnameEName = EName(DfNamespace, "qname")
  val DfQnameExpressionEName = EName(DfNamespace, "qnameExpression")

  val EfIdentifierEName = EName(EfNamespace, "identifier")
  val EfSpecificSchemeEName = EName(EfNamespace, "specificScheme")
  val EfRegexpSchemeEName = EName(EfNamespace, "regexpScheme")
  val EfSpecificIdentifierEName = EName(EfNamespace, "specificIdentifier")
  val EfRegexpIdentifierEName = EName(EfNamespace, "regexpIdentifier")

  val GfGeneralEName = EName(GfNamespace, "general")

  val MfMatchConceptEName = EName(MfNamespace, "matchConcept")
  val MfMatchLocationEName = EName(MfNamespace, "matchLocation")
  val MfMatchUnitEName = EName(MfNamespace, "matchUnit")
  val MfMatchEntityIdentifierEName = EName(MfNamespace, "matchEntityIdentifier")
  val MfMatchPeriodEName = EName(MfNamespace, "matchPeriod")
  val MfMatchSegmentEName = EName(MfNamespace, "matchSegment")
  val MfMatchScenarioEName = EName(MfNamespace, "matchScenario")
  val MfMatchNonXDTSegmentEName = EName(MfNamespace, "matchNonXDTSegment")
  val MfMatchNonXDTScenarioEName = EName(MfNamespace, "matchNonXDTScenario")
  val MfMatchDimensionEName = EName(MfNamespace, "matchDimension")

  val PfPeriodEName = EName(PfNamespace, "period")
  val PfPeriodStartEName = EName(PfNamespace, "periodStart")
  val PfPeriodEndEName = EName(PfNamespace, "periodEnd")
  val PfPeriodInstantEName = EName(PfNamespace, "periodInstant")
  val PfForeverEName = EName(PfNamespace, "forever")
  val PfInstantDurationEName = EName(PfNamespace, "instantDuration")

  val RfRelativeFilterEName = EName(RfNamespace, "relativeFilter")

  val SsfSegmentEName = EName(SsfNamespace, "segment")
  val SsfScenarioEName = EName(SsfNamespace, "scenario")

  val TfParentFilterEName = EName(TfNamespace, "parentFilter")
  val TfAncestorFilterEName = EName(TfNamespace, "ancestorFilter")
  val TfSiblingFilterEName = EName(TfNamespace, "siblingFilter")
  val TfLocationFilterEName = EName(TfNamespace, "locationFilter")

  val TfParentEName = EName(TfNamespace, "parent")
  val TfAncestorEName = EName(TfNamespace, "ancestor")
  val TfQnameEName = EName(TfNamespace, "qname")
  val TfQnameExpressionEName = EName(TfNamespace, "qnameExpression")

  val UfSingleMeasureEName = EName(UfNamespace, "singleMeasure")
  val UfGeneralMeasuresEName = EName(UfNamespace, "generalMeasures")

  val UfMeasureEName = EName(UfNamespace, "measure")
  val UfQnameEName = EName(UfNamespace, "qname")
  val UfQnameExpressionEName = EName(UfNamespace, "qnameExpression")

  val VfNilEName = EName(VfNamespace, "nil")
  val VfPrecisionEName = EName(VfNamespace, "precision")

  val AcfAspectCoverEName = EName(AcfNamespace, "aspectCover")

  val AcfAspectEName = EName(AcfNamespace, "aspect")
  val AcfDimensionEName = EName(AcfNamespace, "dimension")
  val AcfExcludeDimensionEName = EName(AcfNamespace, "excludeDimension")
  val AcfQnameEName = EName(AcfNamespace, "qname")
  val AcfQnameExpressionEName = EName(AcfNamespace, "qnameExpression")

  val CrfConceptRelationEName = EName(CrfNamespace, "conceptRelation")

  val CrfAxisEName = EName(CrfNamespace, "axis")
  val CrfGenerationsEName = EName(CrfNamespace, "generations")
  val CrfVariableEName = EName(CrfNamespace, "variable")
  val CrfQnameEName = EName(CrfNamespace, "qname")
  val CrfQnameExpressionEName = EName(CrfNamespace, "qnameExpression")
  val CrfLinkroleEName = EName(CrfNamespace, "linkrole")
  val CrfLinkroleExpressionEName = EName(CrfNamespace, "linkroleExpression")
  val CrfLinknameEName = EName(CrfNamespace, "linkname")
  val CrfLinknameExpressionEName = EName(CrfNamespace, "linknameExpression")
  val CrfArcroleEName = EName(CrfNamespace, "arcrole")
  val CrfArcroleExpressionEName = EName(CrfNamespace, "arcroleExpression")
  val CrfArcnameEName = EName(CrfNamespace, "arcname")
  val CrfArcnameExpressionEName = EName(CrfNamespace, "arcnameExpression")

  val NameEName: EName = EName.fromLocalName("name")
  val IdEName: EName = EName.fromLocalName("id")
  val FormEName: EName = EName.fromLocalName("form")
  val AbstractEName: EName = EName.fromLocalName("abstract")
  val FinalEName: EName = EName.fromLocalName("final")
  val DefaultEName: EName = EName.fromLocalName("default")
  val FixedEName: EName = EName.fromLocalName("fixed")
  val TypeEName: EName = EName.fromLocalName("type")
  val NillableEName: EName = EName.fromLocalName("nillable")
  val BlockEName: EName = EName.fromLocalName("block")
  val UseEName: EName = EName.fromLocalName("use")
  val SubstitutionGroupEName: EName = EName.fromLocalName("substitutionGroup")
  val RefEName: EName = EName.fromLocalName("ref")
  val ReferEName: EName = EName.fromLocalName("refer")
  val SchemaLocationEName: EName = EName.fromLocalName("schemaLocation")
  val MinOccursEName: EName = EName.fromLocalName("minOccurs")
  val MaxOccursEName: EName = EName.fromLocalName("maxOccurs")
  val BaseEName: EName = EName.fromLocalName("base")
  val TargetNamespaceEName: EName = EName.fromLocalName("targetNamespace")
  val ElementFormDefaultEName: EName = EName.fromLocalName("elementFormDefault")
  val AttributeFormDefaultEName: EName = EName.fromLocalName("attributeFormDefault")
  val HrefEName: EName = EName.fromLocalName("href")
  val OrderEName: EName = EName.fromLocalName("order")
  val NamespaceEName: EName = EName.fromLocalName("namespace")
  val PreferredLabelEName: EName = EName.fromLocalName("preferredLabel")
  val PriorityEName: EName = EName.fromLocalName("priority")
  val MixedEName: EName = EName.fromLocalName("mixed")
  val ComplementEName: EName = EName.fromLocalName("complement")
  val CoverEName: EName = EName.fromLocalName("cover")
  val TestEName: EName = EName.fromLocalName("test")
  val AspectModelEName: EName = EName.fromLocalName("aspectModel")
  val ImplicitFilteringEName: EName = EName.fromLocalName("implicitFiltering")
  val BindAsSequenceEName: EName = EName.fromLocalName("bindAsSequence")
  val PrecisionEName: EName = EName.fromLocalName("precision")
  val DecimalsEName: EName = EName.fromLocalName("decimals")
  val SourceEName: EName = EName.fromLocalName("source")
  val ValueEName: EName = EName.fromLocalName("value")
  val StrictEName: EName = EName.fromLocalName("strict")
  val SelectEName: EName = EName.fromLocalName("select")
  val AbsoluteAcceptanceRadiusEName: EName = EName.fromLocalName("absoluteAcceptanceRadius")
  val ProportionalAcceptanceRadiusEName: EName = EName.fromLocalName("proportionalAcceptanceRadius")
  val RequiredEName: EName = EName.fromLocalName("required")
  val AsEName: EName = EName.fromLocalName("as")
  val NilsEName: EName = EName.fromLocalName("nils")
  val MatchesEName: EName = EName.fromLocalName("matches")
  val FallbackValueEName: EName = EName.fromLocalName("fallbackValue")
  val RoleURIEName: EName = EName.fromLocalName("roleURI")
  val ArcroleURIEName: EName = EName.fromLocalName("arcroleURI")
  val ParentChildOrderEName: EName = EName.fromLocalName("parentChildOrder")
  val TagSelectorEName: EName = EName.fromLocalName("tagSelector")
  val MergeEName: EName = EName.fromLocalName("merge")
  val AxisEName: EName = EName.fromLocalName("axis")
  val DimensionEName: EName = EName.fromLocalName("dimension")
  val IncludeUnreportedValueEName: EName = EName.fromLocalName("includeUnreportedValue")
  val SchemeEName: EName = EName.fromLocalName("scheme")
  val TagEName: EName = EName.fromLocalName("tag")
  val OccEName: EName = EName.fromLocalName("occ")
  val AugmentEName: EName = EName.fromLocalName("augment")
  val OutputEName: EName = EName.fromLocalName("output")
  val PatternEName: EName = EName.fromLocalName("pattern")
  val VariableEName: EName = EName.fromLocalName("variable")
  val MatchAnyEName: EName = EName.fromLocalName("matchAny")
  val DateEName: EName = EName.fromLocalName("date")
  val TimeEName: EName = EName.fromLocalName("time")
  val BoundaryEName: EName = EName.fromLocalName("boundary")
  val LocationEName: EName = EName.fromLocalName("location")
  val MinimumEName: EName = EName.fromLocalName("minimum")
  val WeightEName: EName = EName.fromLocalName("weight")
  val CyclesAllowedEName: EName = EName.fromLocalName("cyclesAllowed")
  val StartEName: EName = EName.fromLocalName("start")
  val EndEName: EName = EName.fromLocalName("end")
  val MeasureEName: EName = EName.fromLocalName("measure")
  val PeriodTypeEName: EName = EName.fromLocalName("periodType")
  val BalanceEName: EName = EName.fromLocalName("balance")

  // ENames in the locator-free model.

  // The "clink" namespace. It has no counterparts for the simple links (like schemaRef), nor for roleTypes etc. (which
  // are re-used from the "link" namespace, like link:part). Foremost of all, it has no counterpart for the link:loc name.

  val CLinkLinkbaseEName = EName(CLinkNamespace, "linkbase")

  val CLinkLabelLinkEName = EName(CLinkNamespace, "labelLink")
  val CLinkLabelEName = EName(CLinkNamespace, "label")
  val CLinkLabelArcEName = EName(CLinkNamespace, "labelArc")

  val CLinkPresentationLinkEName = EName(CLinkNamespace, "presentationLink")
  val CLinkPresentationArcEName = EName(CLinkNamespace, "presentationArc")

  val CLinkReferenceLinkEName = EName(CLinkNamespace, "referenceLink")
  val CLinkReferenceEName = EName(CLinkNamespace, "reference")
  val CLinkReferenceArcEName = EName(CLinkNamespace, "referenceArc")

  val CLinkDefinitionLinkEName = EName(CLinkNamespace, "definitionLink")
  val CLinkDefinitionArcEName = EName(CLinkNamespace, "definitionArc")

  val CLinkCalculationLinkEName = EName(CLinkNamespace, "calculationLink")
  val CLinkCalculationArcEName = EName(CLinkNamespace, "calculationArc")

  val CLinkRoleRefEName = EName(CLinkNamespace, "roleRef")
  val CLinkArcroleRefEName = EName(CLinkNamespace, "arcroleRef")

  val CLinkLinkbaseRefEName = EName(CLinkNamespace, "linkbaseRef")
  val CLinkEntrypointEName = EName(CLinkNamespace, "entrypoint")

  // The "ckey" namespace.

  val CKeyConceptKeyEName = EName(CKeyNamespace, "conceptKey")
  val CKeyElementKeyEName = EName(CKeyNamespace, "elementKey")
  val CKeyTypeKeyEName = EName(CKeyNamespace, "typeKey")
  val CKeyRoleKeyEName = EName(CKeyNamespace, "roleKey")
  val CKeyArcroleKeyEName = EName(CKeyNamespace, "arcroleKey")
  val CKeyAnyElemKeyEName = EName(CKeyNamespace, "anyElemKey")

  // The "cxbrldt" namespace.

  val CXbrldtTypedDomainKeyEName = EName(CXbrldtNamespace, "typedDomainKey")

  // The "cgen" namespace.

  val CGenLinkEName = EName(CGenNamespace, "link")

  // Key attribute name, etc.

  val KeyEName: EName = EName.fromLocalName("key")
  val ElementNameEName: EName = EName.fromLocalName("elementName")
}
