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
import eu.cdevreeze.tqa2.common.locfreexlink
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
sealed abstract class TaxonomyElem(
  underlyingElem: BackingNodes.Elem) extends AbstractDialectBackingElem(underlyingElem) with CanBeTaxonomyDocumentChild {

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
    findAllChildElems().collect { case e: ChildXLink => e }
  }

  final def xlinkResourceChildren: Seq[XLinkResource] = {
    findAllChildElems().collect { case e: XLinkResource => e }
  }

  final def arcs: Seq[XLinkArc] = {
    findAllChildElems().collect { case e: XLinkArc => e }
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
    val parentElemOption = underlyingElem.findParentElem()
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

sealed abstract class ElemInXsNamespace(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with XmlSchemaDialect.Elem {

  type GlobalElementDeclarationType = GlobalElementDeclaration

  type GlobalAttributeDeclarationType = GlobalAttributeDeclaration

  type NamedTypeDefinitionType = NamedTypeDefinition
}

sealed abstract class ElemInCLinkNamespace(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with CLinkDialect.Elem

sealed abstract class ElemInLinkNamespace(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with LinkDialect.Elem

// Schema root element

final case class XsSchema(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.XsSchema with RootElement {

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
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with CLinkDialect.Linkbase with RootElement {

  requireName(ENames.CLinkLinkbaseEName)

  def isSchema: Boolean = false

  def isLinkbase: Boolean = true

  /**
   * Finds all ("taxonomy DOM") extended links
   */
  def findAllExtendedLinks: Seq[ExtendedLink] = {
    findAllChildElems().collect { case e: ExtendedLink => e }
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
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with ElementDeclaration with XmlSchemaDialect.GlobalElementDeclaration {

  requireName(ENames.XsElementEName)

  /**
   * Returns the optional xbrli:periodType attribute, as `PeriodType`.
   */
  final def periodTypeOption: Option[PeriodType] = {
    attrOption(ENames.XbrliPeriodTypeEName).map(v => PeriodType.fromString(v))
  }

  // TODO Methods hasSubstitutionGroup and findAllOwnOrTransitivelyInheritedSubstitutionGroups, taking a SubstitutionGroupMap
}

final case class LocalElementDeclaration(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with ElementDeclaration with XmlSchemaDialect.LocalElementDeclaration {

  requireName(ENames.XsElementEName)
}

final case class ElementReference(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with ElementDeclarationOrReference with XmlSchemaDialect.ElementReference {

  requireName(ENames.XsElementEName)
}

sealed trait AttributeDeclarationOrReference extends ElemInXsNamespace with XmlSchemaDialect.AttributeDeclarationOrReference

sealed trait AttributeDeclaration extends AttributeDeclarationOrReference with XmlSchemaDialect.AttributeDeclaration

final case class GlobalAttributeDeclaration(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with AttributeDeclaration with XmlSchemaDialect.GlobalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class LocalAttributeDeclaration(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with AttributeDeclaration with XmlSchemaDialect.LocalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class AttributeReference(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with AttributeDeclarationOrReference with XmlSchemaDialect.AttributeReference {

  requireName(ENames.XsAttributeEName)
}

sealed trait TypeDefinition extends ElemInXsNamespace with XmlSchemaDialect.TypeDefinition

sealed trait NamedTypeDefinition extends TypeDefinition with XmlSchemaDialect.NamedTypeDefinition

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
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with NamedTypeDefinition with SimpleTypeDefinition with XmlSchemaDialect.NamedSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class AnonymousSimpleTypeDefinition(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with AnonymousTypeDefinition with SimpleTypeDefinition with XmlSchemaDialect.AnonymousSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class NamedComplexTypeDefinition(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with NamedTypeDefinition with ComplexTypeDefinition with XmlSchemaDialect.NamedComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AnonymousComplexTypeDefinition(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)
  with AnonymousTypeDefinition with ComplexTypeDefinition with XmlSchemaDialect.AnonymousComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AttributeGroupDefinition(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.AttributeGroupDefinition {

  requireName(ENames.XsAttributeGroupEName)
}

final case class AttributeGroupReference(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.AttributeGroupReference {

  requireName(ENames.XsAttributeGroupEName)
}

final case class ModelGroupDefinition(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.ModelGroupDefinition {

  requireName(ENames.XsGroupEName)
}

final case class ModelGroupReference(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.ModelGroupReference {

  requireName(ENames.XsGroupEName)
}

sealed trait ModelGroup extends ElemInXsNamespace with XmlSchemaDialect.ModelGroup

final case class SequenceModelGroup(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with ModelGroup with XmlSchemaDialect.SequenceModelGroup {

  requireName(ENames.XsSequenceEName)
}

final case class ChoiceModelGroup(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with ModelGroup with XmlSchemaDialect.ChoiceModelGroup {

  requireName(ENames.XsChoiceEName)
}

final case class AllModelGroup(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with ModelGroup with XmlSchemaDialect.AllModelGroup {

  requireName(ENames.XsAllEName)
}

final case class Restriction(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.Restriction {

  requireName(ENames.XsRestrictionEName)
}

final case class Extension(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.Extension {

  requireName(ENames.XsExtensionEName)
}

final case class SimpleContent(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.SimpleContent {

  requireName(ENames.XsSimpleContentEName)
}

final case class ComplexContent(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.ComplexContent {

  requireName(ENames.XsComplexContentEName)
}

final case class Annotation(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.Annotation {

  requireName(ENames.XsAnnotationEName)
}

final case class Appinfo(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.Appinfo {

  requireName(ENames.XsAppinfoEName)
}

final case class Import(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem) with XmlSchemaDialect.Import {

  requireName(ENames.XsImportEName)
}

/**
 * Other element in the XML Schema namespace. Either valid other schema content, or invalid content, such as an xs:element
 * that has both a name and a ref attribute.
 */
final case class OtherElemInXsNamespace(
  override val underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace(underlyingElem)

// Linkbase content.

sealed trait StandardLink extends ElemInCLinkNamespace with ExtendedLink with CLinkDialect.StandardLink

final case class DefinitionLink(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardLink with CLinkDialect.DefinitionLink {

  requireName(ENames.CLinkDefinitionLinkEName)
}

final case class PresentationLink(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardLink with CLinkDialect.PresentationLink {

  requireName(ENames.CLinkPresentationLinkEName)
}

final case class CalculationLink(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardLink with CLinkDialect.CalculationLink {

  requireName(ENames.CLinkCalculationLinkEName)
}

final case class LabelLink(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardLink with CLinkDialect.LabelLink {

  requireName(ENames.CLinkLabelLinkEName)
}

final case class ReferenceLink(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardLink with CLinkDialect.ReferenceLink {

  requireName(ENames.CLinkReferenceLinkEName)
}

sealed trait StandardArc extends ElemInCLinkNamespace with XLinkArc with CLinkDialect.StandardArc

final case class DefinitionArc(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardArc with CLinkDialect.DefinitionArc {

  requireName(ENames.CLinkDefinitionArcEName)
}

final case class PresentationArc(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardArc with CLinkDialect.PresentationArc {

  requireName(ENames.CLinkPresentationArcEName)
}

final case class CalculationArc(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardArc with CLinkDialect.CalculationArc {

  requireName(ENames.CLinkCalculationArcEName)
}

final case class LabelArc(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardArc with CLinkDialect.LabelArc {

  requireName(ENames.CLinkLabelArcEName)
}

final case class ReferenceArc(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with StandardArc with CLinkDialect.ReferenceArc {

  requireName(ENames.CLinkReferenceArcEName)
}

sealed trait StandardResource extends ElemInCLinkNamespace with XLinkResource with CLinkDialect.StandardResource

final case class ConceptLabelResource(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem)
  with StandardResource with CLinkDialect.ConceptLabelResource {

  requireName(ENames.CLinkLabelEName)
}

final case class ConceptReferenceResource(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem)
  with StandardResource with CLinkDialect.ConceptReferenceResource {

  requireName(ENames.CLinkReferenceEName)
}

final case class RoleRef(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with CLinkDialect.RoleRef {

  requireName(ENames.CLinkRoleRefEName)
}

final case class ArcroleRef(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem) with CLinkDialect.ArcroleRef {

  requireName(ENames.CLinkArcroleRefEName)
}

/**
 * Other element in the CLink namespace. Either valid other CLink content, or invalid content.
 */
final case class OtherElemInCLinkNamespace(
  override val underlyingElem: BackingNodes.Elem) extends ElemInCLinkNamespace(underlyingElem)

final case class RoleType(
  override val underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace(underlyingElem) with LinkDialect.RoleType {

  requireName(ENames.LinkRoleTypeEName)

  def definitionOption: Option[Definition] = {
    filterChildElems(named(ENames.LinkDefinitionEName)).collectFirst { case e: Definition => e }
  }

  def usedOn: Seq[UsedOn] = {
    filterChildElems(named(ENames.LinkUsedOnEName)).collect { case e: UsedOn => e }
  }
}

final case class ArcroleType(
  override val underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace(underlyingElem) with LinkDialect.ArcroleType {

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
  override val underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace(underlyingElem) with LinkDialect.Definition {

  requireName(ENames.LinkDefinitionEName)
}

final case class UsedOn(
  override val underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace(underlyingElem) with LinkDialect.UsedOn {

  requireName(ENames.LinkUsedOnEName)
}

/**
 * Other element in the Link namespace. Either valid other Link content, or invalid content.
 */
final case class OtherElemInLinkNamespace(
  override val underlyingElem: BackingNodes.Elem) extends ElemInLinkNamespace(underlyingElem)

sealed abstract class TaxonomyElemKey(
  underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with XLinkResource with TaxonomyElementKey

final case class ConceptKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey(underlyingElem) with TaxonomyElementKey.ConceptKey {

  requireName(ENames.CKeyConceptKeyEName)
}

final case class ElementKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey(underlyingElem) with TaxonomyElementKey.ElementKey {

  requireName(ENames.CKeyElementKeyEName)
}

final case class TypeKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey(underlyingElem) with TaxonomyElementKey.TypeKey {

  requireName(ENames.CKeyTypeKeyEName)
}

final case class RoleKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey(underlyingElem) with TaxonomyElementKey.RoleKey {

  requireName(ENames.CKeyRoleKeyEName)
}

final case class ArcroleKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey(underlyingElem) with TaxonomyElementKey.ArcroleKey {

  requireName(ENames.CKeyArcroleKeyEName)
}

final case class AnyElementKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElemKey(underlyingElem) with TaxonomyElementKey.AnyElementKey {

  requireName(ENames.CKeyAnyElemKeyEName)
}

/**
 * Non-standard extended link
 */
final case class NonStandardLink(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ExtendedLink

/**
 * Non-standard arc
 */
final case class NonStandardArc(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with XLinkArc

/**
 * Non-standard resource, which is also not a taxonomy element key
 */
final case class NonStandardResource(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with XLinkResource

/**
 * Any other non-XLink element, not in the "xs", "clink" or "link" namespaces.
 */
final case class OtherNonXLinkElem(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem)

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
    val parentIsSchema = underlyingElem.findParentElem().exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(new GlobalElementDeclaration(underlyingElem))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(new LocalElementDeclaration(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new ElementReference(underlyingElem))
    } else {
      None
    }
  }

  private def optAttributeDeclarationOrReference(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem().exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(new GlobalAttributeDeclaration(underlyingElem))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(new LocalAttributeDeclaration(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new AttributeReference(underlyingElem))
    } else {
      None
    }
  }

  private def optSimpleTypeDefinition(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem().exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty

    if (parentIsSchema && hasName) {
      Some(new NamedSimpleTypeDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName) {
      Some(new AnonymousSimpleTypeDefinition(underlyingElem))
    } else {
      None
    }
  }

  private def optComplexTypeDefinition(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem().exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty

    if (parentIsSchema && hasName) {
      Some(new NamedComplexTypeDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName) {
      Some(new AnonymousComplexTypeDefinition(underlyingElem))
    } else {
      None
    }
  }

  private def optAttributeGroupDefinitionOrReference(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem().exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(new AttributeGroupDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new AttributeGroupReference(underlyingElem))
    } else {
      None
    }
  }

  private def optModelGroupDefinitionOrReference(underlyingElem: BackingNodes.Elem): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem().exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(new ModelGroupDefinition(underlyingElem))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(new ModelGroupReference(underlyingElem))
    } else {
      None
    }
  }

  private def fallbackElem(underlyingElem: BackingNodes.Elem): TaxonomyElem = {
    underlyingElem.attrOption(ENames.XLinkTypeEName) match {
      case Some("extended") => new NonStandardLink(underlyingElem)
      case Some("arc") => new NonStandardArc(underlyingElem)
      case Some("resource") => new NonStandardResource(underlyingElem)
      case _ => new OtherNonXLinkElem(underlyingElem)
    }
  }

  private val elemFactoryMap: Map[String, ElemFactoryWithFallback] =
    Map(
      Namespaces.XsNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.XsSchemaEName -> (e => new XsSchema(e)),
          ENames.XsElementEName -> (e => optElementDeclarationOrReference(e).getOrElse(new OtherElemInXsNamespace(e))),
          ENames.XsAttributeEName -> (e => optAttributeDeclarationOrReference(e).getOrElse(new OtherElemInXsNamespace(e))),
          ENames.XsSimpleTypeEName -> (e => optSimpleTypeDefinition(e).getOrElse(new OtherElemInXsNamespace(e))),
          ENames.XsComplexTypeEName -> (e => optComplexTypeDefinition(e).getOrElse(new OtherElemInXsNamespace(e))),
          ENames.XsAttributeGroupEName -> (e => optAttributeGroupDefinitionOrReference(e).getOrElse(new OtherElemInXsNamespace(e))),
          ENames.XsGroupEName -> (e => optModelGroupDefinitionOrReference(e).getOrElse(new OtherElemInXsNamespace(e))),
          ENames.XsSequenceEName -> (e => new SequenceModelGroup(e)),
          ENames.XsChoiceEName -> (e => new ChoiceModelGroup(e)),
          ENames.XsAllEName -> (e => new AllModelGroup(e)),
          ENames.XsRestrictionEName -> (e => new Restriction(e)),
          ENames.XsExtensionEName -> (e => new Extension(e)),
          ENames.XsSimpleContentEName -> (e => new SimpleContent(e)),
          ENames.XsComplexContentEName -> (e => new ComplexContent(e)),
          ENames.XsAnnotationEName -> (e => new Annotation(e)),
          ENames.XsAppinfoEName -> (e => new Appinfo(e)),
          ENames.XsImportEName -> (e => new Import(e))
        ),
        (e => new OtherElemInXsNamespace(e))),
      Namespaces.CLinkNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.CLinkLinkbaseEName -> (e => new Linkbase(e)),
          ENames.CLinkDefinitionLinkEName -> (e => new DefinitionLink(e)),
          ENames.CLinkPresentationLinkEName -> (e => new PresentationLink(e)),
          ENames.CLinkCalculationLinkEName -> (e => new CalculationLink(e)),
          ENames.CLinkLabelLinkEName -> (e => new LabelLink(e)),
          ENames.CLinkReferenceLinkEName -> (e => new ReferenceLink(e)),
          ENames.CLinkDefinitionArcEName -> (e => new DefinitionArc(e)),
          ENames.CLinkPresentationArcEName -> (e => new PresentationArc(e)),
          ENames.CLinkCalculationArcEName -> (e => new CalculationArc(e)),
          ENames.CLinkLabelArcEName -> (e => new LabelArc(e)),
          ENames.CLinkReferenceArcEName -> (e => new ReferenceArc(e)),
          ENames.CLinkLabelEName -> (e => new ConceptLabelResource(e)),
          ENames.CLinkReferenceEName -> (e => new ConceptReferenceResource(e)),
          ENames.CLinkRoleRefEName -> (e => new RoleRef(e)),
          ENames.CLinkArcroleRefEName -> (e => new ArcroleRef(e))
        ),
        (e => new OtherElemInCLinkNamespace(e))),
      Namespaces.LinkNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.LinkRoleTypeEName -> (e => new RoleType(e)),
          ENames.LinkArcroleTypeEName -> (e => new ArcroleType(e)),
          ENames.LinkDefinitionEName -> (e => new Definition(e)),
          ENames.LinkUsedOnEName -> (e => new UsedOn(e))
        ),
        (e => new OtherElemInLinkNamespace(e))),
      Namespaces.CKeyNamespace -> new ElemFactoryWithFallback(
        Map(
          ENames.CKeyConceptKeyEName -> (e => new ConceptKey(e)),
          ENames.CKeyElementKeyEName -> (e => new ElementKey(e)),
          ENames.CKeyTypeKeyEName -> (e => new TypeKey(e)),
          ENames.CKeyRoleKeyEName -> (e => new RoleKey(e)),
          ENames.CKeyArcroleKeyEName -> (e => new ArcroleKey(e)),
          ENames.CKeyAnyElemKeyEName -> (e => new AnyElementKey(e))
        ),
        fallbackElem _
      ),
      Namespaces.CGenNamespace -> new ElemFactoryWithFallback(fallbackElem _),
      Namespaces.GenNamespace -> new ElemFactoryWithFallback(fallbackElem _)
    )

  private[TaxonomyElem] final class ElemFactoryWithFallback(
    val elemFactory: Map[EName, BackingNodes.Elem => TaxonomyElem],
    val fallback: BackingNodes.Elem => TaxonomyElem) {

    def this(fallback: BackingNodes.Elem => TaxonomyElem) = this(Map.empty, fallback)

    def forName(name: EName): BackingNodes.Elem => TaxonomyElem = {
      elemFactory.get(name).getOrElse(fallback)
    }
  }

}
