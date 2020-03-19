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

import scala.collection.immutable.ArraySeq

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.common.FragmentKey
import eu.cdevreeze.tqa2.common.locfreexlink
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.common.xmlschema.XmlSchemaDialect
import eu.cdevreeze.tqa2.locfreetaxonomy.common.BaseSetKey
import eu.cdevreeze.tqa2.locfreetaxonomy.common.CyclesAllowed
import eu.cdevreeze.tqa2.locfreetaxonomy.common.PeriodType
import eu.cdevreeze.tqa2.locfreetaxonomy.common.Use
import eu.cdevreeze.tqa2.locfreetaxonomy.common.Variety
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.dialect.AbstractDialectBackingElem
import eu.cdevreeze.yaidom2.queryapi.anyElem
import eu.cdevreeze.yaidom2.queryapi.named
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import eu.cdevreeze.yaidom2.queryapi.ElemStep

/**
 * Node in a locator-free taxonomy.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.types
// scalastyle:off file.size.limit
sealed trait TaxonomyNode extends BackingNodes.Node

sealed trait CanBeTaxonomyDocumentChild extends TaxonomyNode with BackingNodes.CanBeDocumentChild

final case class TaxonomyTextNode(text: String) extends TaxonomyNode with BackingNodes.Text

final case class TaxonomyCommentNode(text: String) extends CanBeTaxonomyDocumentChild with BackingNodes.Comment

final case class TaxonomyProcessingInstructionNode(target: String, data: String) extends CanBeTaxonomyDocumentChild
  with BackingNodes.ProcessingInstruction

/**
 * Locator-free taxonomy dialect element node, offering the `BackingNodes.Elem` element query API and additional
 * type-safe query methods.
 *
 * Note that the underlying element can be of any element implementation that offers the `BackingNodes.Elem` API.
 *
 * Taxonomy elements are instantiated without knowing any context of the containing document, such as substitution groups or
 * sibling extended link child elements. Only an entire taxonomy has enough context to turn global element declarations into
 * concept declarations, for example.
 *
 * Creating taxonomy elements should hardly, if ever, fail. After creation, type-safe query methods can fail if the taxonomy
 * content is not valid against the schema, however.
 */
sealed trait TaxonomyElem extends AbstractDialectBackingElem with CanBeTaxonomyDocumentChild {

  type ThisElem = TaxonomyElem

  type ThisNode = TaxonomyNode

  final def wrapElem(underlyingElem: BackingNodes.Elem): ThisElem = TaxonomyElem(underlyingElem)

  // ClarkNodes.Elem

  final def children: ArraySeq[TaxonomyNode] = {
    underlyingElem.children.flatMap(TaxonomyNode.opt).to(ArraySeq)
  }

  final def select(step: ElemStep[TaxonomyElem]): Seq[TaxonomyElem] = {
    step(this)
  }

  // Other methods

  final def fragmentKey: FragmentKey = {
    FragmentKey(underlyingElem.docUri, underlyingElem.ownNavigationPathRelativeToRootElem)
  }

  final def isRootElement: Boolean = this match {
    case _: RootElement => true
    case _ => false
  }

  protected[dom] def requireName(elemName: EName): Unit = {
    require(name == elemName, s"Required name: $elemName. Found name $name instead, in document $docUri")
  }
}

// XLink

sealed trait XLinkElem extends TaxonomyElem with locfreexlink.XLinkElem {

  type ChildXLinkType = ChildXLink

  type XLinkResourceType = XLinkResource

  type XLinkArcType = XLinkArc
}

sealed trait ChildXLink extends XLinkElem with locfreexlink.ChildXLink

sealed trait XLinkResource extends ChildXLink with locfreexlink.XLinkResource

sealed trait ExtendedLink extends XLinkElem with locfreexlink.ExtendedLink {

  final def xlinkChildren: Seq[ChildXLink] = {
    findAllChildElems.collect { case e: ChildXLink => e }
  }

  final def xlinkResourceChildren: Seq[XLinkResource] = {
    findAllChildElems.collect { case e: XLinkResource => e }
  }

  final def arcs: Seq[XLinkArc] = {
    findAllChildElems.collect { case e: XLinkArc => e }
  }

  /**
   * Returns the XLink resources grouped by XLink label.
   * This is an expensive method, so when processing an extended link, this method should
   * be called only once per extended link.
   */
  final def labeledXlinkResourceMap: Map[String, Seq[XLinkResource]] = {
    xlinkResourceChildren.groupBy(_.xlinkLabel)
  }
}

sealed trait XLinkArc extends ChildXLink with locfreexlink.XLinkArc {

  /**
   * Returns the Base Set key.
   *
   * If the taxonomy is not known to be schema-valid, it may be impossible to create a Base Set key.
   */
  final def baseSetKey: BaseSetKey = {
    val parentElemOption = underlyingElem.findParentElem
    require(parentElemOption.nonEmpty, s"Missing parent element. Document $docUri. Element name: $name")
    BaseSetKey(name, arcrole, parentElemOption.get.name, parentElemOption.get.attr(ENames.XLinkRoleEName))
  }

  /**
   * Returns the "use" attribute (defaulting to "optional").
   */
  final def use: Use = {
    Use.fromString(attrOption(ENames.UseEName).getOrElse("optional"))
  }

  /**
   * Returns the "priority" integer attribute (defaulting to 0).
   */
  final def priority: Int = {
    attrOption(ENames.PriorityEName).getOrElse("0").toInt
  }

  /**
   * Returns the "order" decimal attribute (defaulting to 1).
   */
  final def order: BigDecimal = {
    BigDecimal(attrOption(ENames.OrderEName).getOrElse("1"))
  }
}

// Root element

/**
 * Taxonomy root element
 */
sealed trait RootElement extends TaxonomyElem with TaxonomyRootElem

// Elements in some known namespaces

sealed trait ElemInXsNamespace extends TaxonomyElem with XmlSchemaDialect.Elem {

  type GlobalElementDeclarationType = GlobalElementDeclaration

  type GlobalAttributeDeclarationType = GlobalAttributeDeclaration

  type NamedTypeDefinitionType = NamedTypeDefinition
}

sealed trait ElemInCLinkNamespace extends TaxonomyElem with CLinkDialect.Elem

sealed trait ElemInLinkNamespace extends TaxonomyElem with LinkDialect.Elem

// Named top-level schema component

/**
 * Named top-level schema component, such as a global element declaration, global attribute declaration or named type
 * definition. This trait extends `ElemInXsNamespace` and offers the `XmlSchemaDialect.NamedGlobalDeclOrDef` API.
 * Hence it offers method `targetEName`.
 */
sealed trait NamedGlobalSchemaComponent extends ElemInXsNamespace with XmlSchemaDialect.NamedGlobalDeclOrDef

// Schema root element

final case class XsSchema(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.XsSchema with RootElement {

  requireName(ENames.XsSchemaEName)

  def isSchema: Boolean = true

  def isLinkbase: Boolean = false

  def findAllImports: Seq[Import] = {
    filterChildElems(named(ENames.XsImportEName)).collect { case e: Import => e }
  }

  def filterGlobalElementDeclarations(p: GlobalElementDeclaration => Boolean): Seq[GlobalElementDeclaration] = {
    filterChildElems(named(ENames.XsElementEName)).collect { case e: GlobalElementDeclaration if p(e) => e }
  }

  def findAllGlobalElementDeclarations(): Seq[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(anyElem)
  }

  def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclaration => Boolean): Seq[GlobalAttributeDeclaration] = {
    filterChildElems(named(ENames.XsAttributeEName)).collect { case e: GlobalAttributeDeclaration if p(e) => e }
  }

  def findAllGlobalAttributeDeclarations(): Seq[GlobalAttributeDeclaration]  = {
    filterGlobalAttributeDeclarations(anyElem)
  }

  def filterNamedTypeDefinitions(p: NamedTypeDefinition => Boolean): Seq[NamedTypeDefinition] = {
    filterChildElems(e => e.name == ENames.XsComplexTypeEName || e.name == ENames.XsSimpleTypeEName)
      .collect { case e: NamedTypeDefinition if p(e) => e }
  }

  def findAllNamedTypeDefinitions(): Seq[NamedTypeDefinition] = {
    filterNamedTypeDefinitions(anyElem)
  }
}

// Linkbase root element

final case class Linkbase(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with CLinkDialect.Linkbase with RootElement {

  requireName(ENames.CLinkLinkbaseEName)

  def isSchema: Boolean = false

  def isLinkbase: Boolean = true

  /**
   * Finds all ("taxonomy DOM") extended links
   */
  def findAllExtendedLinks: Seq[ExtendedLink] = {
    findAllChildElems.collect { case e: ExtendedLink => e }
  }

  def findAllRoleRefs(): Seq[RoleRef] = {
    filterChildElems(named(ENames.CLinkRoleRefEName)).collect { case e: RoleRef => e }
  }

  def findAllArcroleRefs(): Seq[ArcroleRef] = {
    filterChildElems(named(ENames.CLinkArcroleRefEName)).collect { case e: ArcroleRef => e }
  }
}

// Schema content.

sealed trait ElementDeclarationOrReference extends ElemInXsNamespace with XmlSchemaDialect.ElementDeclarationOrReference

sealed trait ElementDeclaration extends ElementDeclarationOrReference with XmlSchemaDialect.ElementDeclaration

/**
 * Global element declaration. Often a concept declaration, although in general the DOM element has not enough context
 * to determine that in isolation.
 */
final case class GlobalElementDeclaration(
  underlyingElem: BackingNodes.Elem) extends NamedGlobalSchemaComponent
  with ElementDeclaration with XmlSchemaDialect.GlobalElementDeclaration {

  requireName(ENames.XsElementEName)

  /**
   * Returns true if this global element declaration has the given substitution group, either
   * directly or indirectly. The given mappings are used as the necessary context, but are not needed if the element
   * declaration directly has the substitution group itself.
   *
   * This method may fail with an exception if the taxonomy is not schema-valid.
   */
  def hasSubstitutionGroup(substGroup: EName, substitutionGroupMap: SubstitutionGroupMap): Boolean = {
    substitutionGroupOption.contains(substGroup) || {
      val derivedSubstGroups = substitutionGroupMap.substitutionGroupDerivations.getOrElse(substGroup, Set.empty)

      // Recursive calls

      derivedSubstGroups.exists(substGrp => hasSubstitutionGroup(substGrp, substitutionGroupMap))
    }
  }

  /**
   * Returns all own or transitively inherited substitution groups. The given mappings are used as the necessary context.
   *
   * This method may fail with an exception if the taxonomy is not schema-valid.
   */
  def findAllOwnOrTransitivelyInheritedSubstitutionGroups(substitutionGroupMap: SubstitutionGroupMap): Set[EName] = {
    substitutionGroupOption.toSeq.flatMap { sg =>
      substitutionGroupMap.transitivelyInheritedSubstitutionGroupsIncludingSelf(sg)
    }.toSet
  }

  /**
   * Returns the optional xbrli:periodType attribute, as `PeriodType`.
   */
  def periodTypeOption: Option[PeriodType] = {
    attrOption(ENames.XbrliPeriodTypeEName).map(v => PeriodType.fromString(v))
  }
}

final case class LocalElementDeclaration(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with ElementDeclaration with XmlSchemaDialect.LocalElementDeclaration {

  requireName(ENames.XsElementEName)
}

final case class ElementReference(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with ElementDeclarationOrReference with XmlSchemaDialect.ElementReference {

  requireName(ENames.XsElementEName)
}

sealed trait AttributeDeclarationOrReference extends ElemInXsNamespace with XmlSchemaDialect.AttributeDeclarationOrReference

sealed trait AttributeDeclaration extends AttributeDeclarationOrReference with XmlSchemaDialect.AttributeDeclaration

final case class GlobalAttributeDeclaration(
  underlyingElem: BackingNodes.Elem) extends NamedGlobalSchemaComponent
  with AttributeDeclaration with XmlSchemaDialect.GlobalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class LocalAttributeDeclaration(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with AttributeDeclaration with XmlSchemaDialect.LocalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class AttributeReference(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with AttributeDeclarationOrReference with XmlSchemaDialect.AttributeReference {

  requireName(ENames.XsAttributeEName)
}

sealed trait TypeDefinition extends ElemInXsNamespace with XmlSchemaDialect.TypeDefinition

sealed trait NamedTypeDefinition extends TypeDefinition with NamedGlobalSchemaComponent with XmlSchemaDialect.NamedTypeDefinition

sealed trait AnonymousTypeDefinition extends TypeDefinition with XmlSchemaDialect.AnonymousTypeDefinition

sealed trait SimpleTypeDefinition extends TypeDefinition with XmlSchemaDialect.SimpleTypeDefinition {

  /**
   * Returns the variety.
   */
  final def variety: Variety = {
    if (findChildElem(named(ENames.XsListEName)).isDefined) {
      Variety.List
    } else if (findChildElem(named(ENames.XsUnionEName)).isDefined) {
      Variety.Union
    } else if (findChildElem(named(ENames.XsRestrictionEName)).isDefined) {
      Variety.Atomic
    } else {
      sys.error(s"Could not determine variety. Document: $docUri. Element: $name")
    }
  }

  /**
   * Returns the optional base type.
   */
  final def baseTypeOption: Option[EName] = variety match {
    case Variety.Atomic =>
      filterChildElems(named(ENames.XsRestrictionEName)).collectFirst { case e: Restriction => e }.flatMap(_.baseTypeOption)
    case _ => None
  }
}

sealed trait ComplexTypeDefinition extends TypeDefinition with XmlSchemaDialect.ComplexTypeDefinition

final case class NamedSimpleTypeDefinition(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with NamedTypeDefinition with SimpleTypeDefinition with XmlSchemaDialect.NamedSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class AnonymousSimpleTypeDefinition(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with AnonymousTypeDefinition with SimpleTypeDefinition with XmlSchemaDialect.AnonymousSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class NamedComplexTypeDefinition(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with NamedTypeDefinition with ComplexTypeDefinition with XmlSchemaDialect.NamedComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AnonymousComplexTypeDefinition(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace
  with AnonymousTypeDefinition with ComplexTypeDefinition with XmlSchemaDialect.AnonymousComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AttributeGroupDefinition(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.AttributeGroupDefinition {

  requireName(ENames.XsAttributeGroupEName)
}

final case class AttributeGroupReference(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.AttributeGroupReference {

  requireName(ENames.XsAttributeGroupEName)
}

final case class ModelGroupDefinition(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.ModelGroupDefinition {

  requireName(ENames.XsGroupEName)
}

final case class ModelGroupReference(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.ModelGroupReference {

  requireName(ENames.XsGroupEName)
}

sealed trait ModelGroup extends ElemInXsNamespace with XmlSchemaDialect.ModelGroup

final case class SequenceModelGroup(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with ModelGroup with XmlSchemaDialect.SequenceModelGroup {

  requireName(ENames.XsSequenceEName)
}

final case class ChoiceModelGroup(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with ModelGroup with XmlSchemaDialect.ChoiceModelGroup {

  requireName(ENames.XsChoiceEName)
}

final case class AllModelGroup(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with ModelGroup with XmlSchemaDialect.AllModelGroup {

  requireName(ENames.XsAllEName)
}

final case class Restriction(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Restriction {

  requireName(ENames.XsRestrictionEName)
}

final case class Extension(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Extension {

  requireName(ENames.XsExtensionEName)
}

final case class SimpleContent(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.SimpleContent {

  requireName(ENames.XsSimpleContentEName)
}

final case class ComplexContent(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.ComplexContent {

  requireName(ENames.XsComplexContentEName)
}

final case class Annotation(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Annotation {

  requireName(ENames.XsAnnotationEName)
}

final case class Appinfo(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Appinfo {

  requireName(ENames.XsAppinfoEName)
}

final case class Import(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Import {

  requireName(ENames.XsImportEName)
}

/**
 * Other element in the XML Schema namespace. Either valid other schema content, or invalid content, such as an xs:element
 * that has both a name and a ref attribute.
 */
final case class OtherElemInXsNamespace(
  underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace

// Linkbase content.

sealed trait StandardLink extends ElemInCLinkNamespace with ExtendedLink with CLinkDialect.StandardLink

final case class DefinitionLink(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with StandardLink with CLinkDialect.DefinitionLink {

  requireName(ENames.CLinkDefinitionLinkEName)
}

final case class PresentationLink(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with StandardLink with CLinkDialect.PresentationLink {

  requireName(ENames.CLinkPresentationLinkEName)
}

final case class CalculationLink(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with StandardLink with CLinkDialect.CalculationLink {

  requireName(ENames.CLinkCalculationLinkEName)
}

final case class LabelLink(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with StandardLink with CLinkDialect.LabelLink {

  requireName(ENames.CLinkLabelLinkEName)
}

final case class ReferenceLink(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with StandardLink with CLinkDialect.ReferenceLink {

  requireName(ENames.CLinkReferenceLinkEName)
}

sealed trait StandardArc extends ElemInCLinkNamespace with XLinkArc with CLinkDialect.StandardArc

sealed trait InterConceptArc extends StandardArc with CLinkDialect.InterConceptArc

sealed trait ConceptResourceArc extends StandardArc with CLinkDialect.ConceptResourceArc

final case class DefinitionArc(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with InterConceptArc with CLinkDialect.DefinitionArc {

  requireName(ENames.CLinkDefinitionArcEName)
}

final case class PresentationArc(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with InterConceptArc with CLinkDialect.PresentationArc {

  requireName(ENames.CLinkPresentationArcEName)
}

final case class CalculationArc(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with InterConceptArc with CLinkDialect.CalculationArc {

  requireName(ENames.CLinkCalculationArcEName)
}

final case class LabelArc(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with ConceptResourceArc with CLinkDialect.LabelArc {

  requireName(ENames.CLinkLabelArcEName)
}

final case class ReferenceArc(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with ConceptResourceArc with CLinkDialect.ReferenceArc {

  requireName(ENames.CLinkReferenceArcEName)
}

/**
 * XLink resource that is not a TaxonomyElemKey. It has direct sub-types StandardResource and NonStandardResource.
 */
sealed trait NonKeyResource extends XLinkResource

sealed trait StandardResource extends ElemInCLinkNamespace with NonKeyResource with CLinkDialect.StandardResource

final case class ConceptLabelResource(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace
  with StandardResource with CLinkDialect.ConceptLabelResource {

  requireName(ENames.CLinkLabelEName)
}

final case class ConceptReferenceResource(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace
  with StandardResource with CLinkDialect.ConceptReferenceResource {

  requireName(ENames.CLinkReferenceEName)
}

final case class RoleRef(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with CLinkDialect.RoleRef {

  requireName(ENames.CLinkRoleRefEName)
}

final case class ArcroleRef(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with CLinkDialect.ArcroleRef {

  requireName(ENames.CLinkArcroleRefEName)
}

// In entrypoint schemas

final case class LinkbaseRef(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace with CLinkDialect.LinkbaseRef {

  requireName(ENames.CLinkLinkbaseRefEName)
}

/**
 * Other element in the CLink namespace. Either valid other CLink content, or invalid content.
 */
final case class OtherElemInCLinkNamespace(
  underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace

final case class RoleType(
  underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace with LinkDialect.RoleType {

  requireName(ENames.LinkRoleTypeEName)

  def definitionOption: Option[Definition] = {
    filterChildElems(named(ENames.LinkDefinitionEName)).collectFirst { case e: Definition => e }
  }

  def usedOn: Seq[UsedOn] = {
    filterChildElems(named(ENames.LinkUsedOnEName)).collect { case e: UsedOn => e }
  }
}

final case class ArcroleType(
  underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace with LinkDialect.ArcroleType {

  requireName(ENames.LinkArcroleTypeEName)

  /**
   * Returns the cyclesAllowed attribute.
   */
  def cyclesAllowed: CyclesAllowed = {
    CyclesAllowed.fromString(attr(ENames.CyclesAllowedEName))
  }

  def definitionOption: Option[Definition] = {
    filterChildElems(named(ENames.LinkDefinitionEName)).collectFirst { case e: Definition => e }
  }

  def usedOn: Seq[UsedOn] = {
    filterChildElems(named(ENames.LinkUsedOnEName)).collect { case e: UsedOn => e }
  }
}

final case class Definition(
  underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace with LinkDialect.Definition {

  requireName(ENames.LinkDefinitionEName)
}

final case class UsedOn(
  underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace with LinkDialect.UsedOn {

  requireName(ENames.LinkUsedOnEName)
}

/**
 * Other element in the Link namespace. Either valid other Link content, or invalid content.
 */
final case class OtherElemInLinkNamespace(
  underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace

sealed trait TaxonomyElemKey extends TaxonomyElem with XLinkResource with TaxonomyElemKeyDialect.TaxonomyElemKey

final case class ConceptKey(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey with TaxonomyElemKeyDialect.ConceptKey {

  requireName(ENames.CKeyConceptKeyEName)
}

final case class ElementKey(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey with TaxonomyElemKeyDialect.ElementKey {

  requireName(ENames.CKeyElementKeyEName)
}

final case class TypeKey(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey with TaxonomyElemKeyDialect.TypeKey {

  requireName(ENames.CKeyTypeKeyEName)
}

final case class RoleKey(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey with TaxonomyElemKeyDialect.RoleKey {

  requireName(ENames.CKeyRoleKeyEName)
}

final case class ArcroleKey(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey with TaxonomyElemKeyDialect.ArcroleKey {

  requireName(ENames.CKeyArcroleKeyEName)
}

final case class AnyElementKey(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey with TaxonomyElemKeyDialect.AnyElementKey {

  requireName(ENames.CKeyAnyElemKeyEName)
}

/**
 * Non-standard extended link
 */
final case class NonStandardLink(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem with ExtendedLink

/**
 * Non-standard arc
 */
final case class NonStandardArc(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem with XLinkArc

/**
 * Non-standard resource, which is also not a taxonomy element key
 */
final case class NonStandardResource(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem with NonKeyResource

/**
 * Any other non-XLink element, not in the "xs", "clink" or "link" namespaces.
 */
final case class OtherNonXLinkElem(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem

// Companion objects

object TaxonomyNode {

  def opt(underlyingNode: BackingNodes.Node): Option[TaxonomyNode] = {
    underlyingNode match {
      case e: BackingNodes.Elem => Some(TaxonomyElem(e))
      case t: BackingNodes.Text => Some(TaxonomyTextNode(t.text))
      case c: BackingNodes.Comment => Some(TaxonomyCommentNode(c.text))
      case pi: BackingNodes.ProcessingInstruction => Some(TaxonomyProcessingInstructionNode(pi.target, pi.data))
    }
  }
}

object TaxonomyElem {

  def apply(underlyingElem: BackingNodes.Elem): TaxonomyElem = {
    val name = underlyingElem.name

    elemFactoryMap.get(name.namespaceUriOption.getOrElse(""))
      .map(_.forName(name))
      .map(f => f(underlyingElem))
      .getOrElse(fallbackElem(underlyingElem))
  }

  private def optElementDeclarationOrReference(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(GlobalElementDeclaration(underlyingElem))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(LocalElementDeclaration(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(ElementReference(underlyingElem))
    } else {
      None
    }
  }

  private def optAttributeDeclarationOrReference(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(GlobalAttributeDeclaration(underlyingElem))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(LocalAttributeDeclaration(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(AttributeReference(underlyingElem))
    } else {
      None
    }
  }

  private def optSimpleTypeDefinition(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty

    if (parentIsSchema && hasName) {
      Some(NamedSimpleTypeDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName) {
      Some(AnonymousSimpleTypeDefinition(underlyingElem))
    } else {
      None
    }
  }

  private def optComplexTypeDefinition(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty

    if (parentIsSchema && hasName) {
      Some(NamedComplexTypeDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName) {
      Some(AnonymousComplexTypeDefinition(underlyingElem))
    } else {
      None
    }
  }

  private def optAttributeGroupDefinitionOrReference(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(AttributeGroupDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(AttributeGroupReference(underlyingElem))
    } else {
      None
    }
  }

  private def optModelGroupDefinitionOrReference(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(ModelGroupDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(ModelGroupReference(underlyingElem))
    } else {
      None
    }
  }

  private def fallbackElem(underlyingElem: BackingNodes.Elem): TaxonomyElem = {
    underlyingElem.attrOption(ENames.XLinkTypeEName) match {
      case Some("extended") => NonStandardLink(underlyingElem)
      case Some("arc") => NonStandardArc(underlyingElem)
      case Some("resource") => NonStandardResource(underlyingElem)
      case _ => OtherNonXLinkElem(underlyingElem)
    }
  }

  private val elemFactoryMap: Map[String, ElemFactoryWithFallback] =
    Map(
      Namespaces.XsNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.XsSchemaEName -> (e => XsSchema(e)),
          ENames.XsElementEName -> (e => optElementDeclarationOrReference(e).getOrElse(OtherElemInXsNamespace(e))),
          ENames.XsAttributeEName -> (e => optAttributeDeclarationOrReference(e).getOrElse(OtherElemInXsNamespace(e))),
          ENames.XsSimpleTypeEName -> (e => optSimpleTypeDefinition(e).getOrElse(OtherElemInXsNamespace(e))),
          ENames.XsComplexTypeEName -> (e => optComplexTypeDefinition(e).getOrElse(OtherElemInXsNamespace(e))),
          ENames.XsAttributeGroupEName -> (e => optAttributeGroupDefinitionOrReference(e).getOrElse(OtherElemInXsNamespace(e))),
          ENames.XsGroupEName -> (e => optModelGroupDefinitionOrReference(e).getOrElse(OtherElemInXsNamespace(e))),
          ENames.XsSequenceEName -> (e => SequenceModelGroup(e)),
          ENames.XsChoiceEName -> (e => ChoiceModelGroup(e)),
          ENames.XsAllEName -> (e => AllModelGroup(e)),
          ENames.XsRestrictionEName -> (e => Restriction(e)),
          ENames.XsExtensionEName -> (e => Extension(e)),
          ENames.XsSimpleContentEName -> (e => SimpleContent(e)),
          ENames.XsComplexContentEName -> (e => ComplexContent(e)),
          ENames.XsAnnotationEName -> (e => Annotation(e)),
          ENames.XsAppinfoEName -> (e => Appinfo(e)),
          ENames.XsImportEName -> (e => Import(e))
        ),
        e => OtherElemInXsNamespace(e)),
      Namespaces.CLinkNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.CLinkLinkbaseEName -> (e => Linkbase(e)),
          ENames.CLinkDefinitionLinkEName -> (e => DefinitionLink(e)),
          ENames.CLinkPresentationLinkEName -> (e => PresentationLink(e)),
          ENames.CLinkCalculationLinkEName -> (e => CalculationLink(e)),
          ENames.CLinkLabelLinkEName -> (e => LabelLink(e)),
          ENames.CLinkReferenceLinkEName -> (e => ReferenceLink(e)),
          ENames.CLinkDefinitionArcEName -> (e => DefinitionArc(e)),
          ENames.CLinkPresentationArcEName -> (e => PresentationArc(e)),
          ENames.CLinkCalculationArcEName -> (e => CalculationArc(e)),
          ENames.CLinkLabelArcEName -> (e => LabelArc(e)),
          ENames.CLinkReferenceArcEName -> (e => ReferenceArc(e)),
          ENames.CLinkLabelEName -> (e => ConceptLabelResource(e)),
          ENames.CLinkReferenceEName -> (e => ConceptReferenceResource(e)),
          ENames.CLinkRoleRefEName -> (e => RoleRef(e)),
          ENames.CLinkArcroleRefEName -> (e => ArcroleRef(e)),
          ENames.CLinkLinkbaseRefEName -> (e => LinkbaseRef(e))
        ),
        e => OtherElemInCLinkNamespace(e)),
      Namespaces.LinkNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.LinkRoleTypeEName -> (e => RoleType(e)),
          ENames.LinkArcroleTypeEName -> (e => ArcroleType(e)),
          ENames.LinkDefinitionEName -> (e => Definition(e)),
          ENames.LinkUsedOnEName -> (e => UsedOn(e))
        ),
        e => OtherElemInLinkNamespace(e)),
      Namespaces.CKeyNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.CKeyConceptKeyEName -> (e => ConceptKey(e)),
          ENames.CKeyElementKeyEName -> (e => ElementKey(e)),
          ENames.CKeyTypeKeyEName -> (e => TypeKey(e)),
          ENames.CKeyRoleKeyEName -> (e => RoleKey(e)),
          ENames.CKeyArcroleKeyEName -> (e => ArcroleKey(e)),
          ENames.CKeyAnyElemKeyEName -> (e => AnyElementKey(e))
        ),
        fallbackElem
      ),
      Namespaces.CGenNamespace -> new ElemFactoryWithFallback(fallbackElem),
      Namespaces.GenNamespace -> new ElemFactoryWithFallback(fallbackElem)
    )

  private[TaxonomyElem] final class ElemFactoryWithFallback(
    val elemFactory: Map[EName, BackingNodes.Elem => TaxonomyElem],
    val fallback: BackingNodes.Elem => TaxonomyElem) {

    def this(fallback: BackingNodes.Elem => TaxonomyElem) = this(Map.empty, fallback)

    def forName(name: EName): BackingNodes.Elem => TaxonomyElem = {
      elemFactory.getOrElse(name, fallback)
    }
  }
}
