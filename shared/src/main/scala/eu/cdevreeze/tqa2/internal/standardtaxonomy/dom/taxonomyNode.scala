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

package eu.cdevreeze.tqa2.internal.standardtaxonomy.dom

import scala.collection.immutable.ArraySeq

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.common.FragmentKey
import eu.cdevreeze.tqa2.common.datatypes.XsBooleans
import eu.cdevreeze.tqa2.common.xlink
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.common.xmlschema.XmlSchemaDialect
import eu.cdevreeze.tqa2.locfreetaxonomy.common._
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.dialect.AbstractDialectBackingElem
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import eu.cdevreeze.yaidom2.queryapi.ElemStep
import eu.cdevreeze.yaidom2.queryapi.anyElem
import eu.cdevreeze.yaidom2.queryapi.named

/**
 * Node in a standard XBRL taxonomy.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.types
// scalastyle:off file.size.limit
sealed trait TaxonomyNode extends BackingNodes.Node

sealed trait CanBeTaxonomyDocumentChild extends TaxonomyNode with BackingNodes.CanBeDocumentChild

final case class TaxonomyTextNode(text: String) extends TaxonomyNode with BackingNodes.Text

final case class TaxonomyCommentNode(text: String) extends CanBeTaxonomyDocumentChild with BackingNodes.Comment

final case class TaxonomyProcessingInstructionNode(target: String, data: String)
    extends CanBeTaxonomyDocumentChild
    with BackingNodes.ProcessingInstruction

/**
 * Standard XBRL taxonomy dialect element node, offering the `BackingNodes.Elem` element query API and additional
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

  // Overridden methods, to "fix" the method signatures (setting ThisElem to TaxonomyElem)

  final override def filterChildElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterChildElems(p)
  }

  final override def findAllChildElems: Seq[ThisElem] = {
    super.findAllChildElems
  }

  final override def findChildElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findChildElem(p)
  }

  final override def filterDescendantElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterDescendantElems(p)
  }

  final override def findAllDescendantElems: Seq[ThisElem] = {
    super.findAllDescendantElems
  }

  final override def findDescendantElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findDescendantElem(p)
  }

  final override def filterDescendantElemsOrSelf(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterDescendantElemsOrSelf(p)
  }

  final override def findAllDescendantElemsOrSelf: Seq[ThisElem] = {
    super.findAllDescendantElemsOrSelf
  }

  final override def findDescendantElemOrSelf(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findDescendantElemOrSelf(p)
  }

  final override def findTopmostElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.findTopmostElems(p)
  }

  final override def findTopmostElemsOrSelf(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.findTopmostElemsOrSelf(p)
  }

  final override def findDescendantElemOrSelf(navigationPath: Seq[Int]): Option[ThisElem] = {
    super.findDescendantElemOrSelf(navigationPath)
  }

  final override def getDescendantElemOrSelf(navigationPath: Seq[Int]): ThisElem = {
    super.getDescendantElemOrSelf(navigationPath)
  }

  final override def findParentElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findParentElem(p)
  }

  final override def findParentElem: Option[ThisElem] = {
    super.findParentElem
  }

  final override def filterAncestorElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterAncestorElems(p)
  }

  final override def findAllAncestorElems: Seq[ThisElem] = {
    super.findAllAncestorElems
  }

  final override def findAncestorElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findAncestorElem(p)
  }

  final override def filterAncestorElemsOrSelf(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterAncestorElemsOrSelf(p)
  }

  final override def findAllAncestorElemsOrSelf: Seq[ThisElem] = {
    super.findAllAncestorElemsOrSelf
  }

  final override def findAncestorElemOrSelf(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findAncestorElemOrSelf(p)
  }

  final override def findAllPrecedingSiblingElems: Seq[ThisElem] = {
    super.findAllPrecedingSiblingElems
  }

  final override def rootElem: ThisElem = {
    super.rootElem
  }

  // Other methods

  final def fragmentKey: FragmentKey = {
    FragmentKey(underlyingElem.docUri, underlyingElem.ownNavigationPathRelativeToRootElem)
  }

  final def isRootElement: Boolean = this match {
    case _: RootElement => true
    case _              => false
  }

  protected[dom] def requireName(elemName: EName): Unit = {
    require(name == elemName, s"Required name: $elemName. Found name $name instead, in document $docUri")
  }
}

// XLink

sealed trait XLinkElem extends TaxonomyElem with xlink.XLinkElem {

  type ChildXLinkType = ChildXLink

  type LabeledXLinkType = LabeledXLink

  type XLinkResourceType = XLinkResource

  type XLinkLocatorType = XLinkLocator

  type XLinkArcType = XLinkArc
}

/**
 * Simple or extended link
 */
sealed trait XLinkLink extends XLinkElem with xlink.XLinkLink

// TODO XLink title and documentation (abstract) elements have not been modeled (yet).

/**
 * XLink child element of an extended link, so an XLink resource, locator or arc
 */
sealed trait ChildXLink extends XLinkElem with xlink.ChildXLink

/**
 * XLink resource or locator
 */
sealed trait LabeledXLink extends ChildXLink with xlink.LabeledXLink

sealed trait XLinkResource extends LabeledXLink with xlink.XLinkResource

sealed trait XLinkLocator extends LabeledXLink with xlink.XLinkLocator

sealed trait SimpleLink extends XLinkLink with xlink.SimpleLink

sealed trait ExtendedLink extends XLinkLink with xlink.ExtendedLink {

  final def xlinkChildren: Seq[ChildXLink] = {
    findAllChildElems.collect { case e: ChildXLink => e }
  }

  final def labeledXLinkChildren: Seq[LabeledXLink] = {
    findAllChildElems.collect { case e: LabeledXLink => e }
  }

  final def arcs: Seq[XLinkArc] = {
    findAllChildElems.collect { case e: XLinkArc => e }
  }

  /**
   * Returns the XLink resources/locators grouped by XLink label.
   * This is an expensive method, so when processing an extended link, this method should
   * be called only once per extended link.
   */
  final def labeledXlinkMap: Map[String, Seq[LabeledXLink]] = {
    labeledXLinkChildren.groupBy(_.xlinkLabel)
  }
}

sealed trait XLinkArc extends ChildXLink with xlink.XLinkArc {

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

sealed trait ElemInLinkNamespace extends TaxonomyElem

// Named top-level schema component

/**
 * Named top-level schema component, such as a global element declaration, global attribute declaration or named type
 * definition. This trait extends `ElemInXsNamespace` and offers the `XmlSchemaDialect.NamedGlobalDeclOrDef` API.
 * Hence it offers method `targetEName`.
 */
sealed trait NamedGlobalSchemaComponent extends ElemInXsNamespace with XmlSchemaDialect.NamedGlobalDeclOrDef

// Schema root element

final case class XsSchema(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.XsSchema with RootElement {
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

  def findAllGlobalAttributeDeclarations(): Seq[GlobalAttributeDeclaration] = {
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

// TODO Linkbase root element

// Schema content.

sealed trait ElementDeclarationOrReference extends ElemInXsNamespace with XmlSchemaDialect.ElementDeclarationOrReference

sealed trait ElementDeclaration extends ElementDeclarationOrReference with XmlSchemaDialect.ElementDeclaration

/**
 * Global element declaration. Often a concept declaration, although in general the DOM element has not enough context
 * to determine that in isolation.
 */
final case class GlobalElementDeclaration(underlyingElem: BackingNodes.Elem)
    extends NamedGlobalSchemaComponent
    with ElementDeclaration
    with XmlSchemaDialect.GlobalElementDeclaration {

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

final case class LocalElementDeclaration(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with ElementDeclaration
    with XmlSchemaDialect.LocalElementDeclaration {

  requireName(ENames.XsElementEName)
}

final case class ElementReference(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with ElementDeclarationOrReference
    with XmlSchemaDialect.ElementReference {

  requireName(ENames.XsElementEName)
}

sealed trait AttributeDeclarationOrReference extends ElemInXsNamespace with XmlSchemaDialect.AttributeDeclarationOrReference

sealed trait AttributeDeclaration extends AttributeDeclarationOrReference with XmlSchemaDialect.AttributeDeclaration

final case class GlobalAttributeDeclaration(underlyingElem: BackingNodes.Elem)
    extends NamedGlobalSchemaComponent
    with AttributeDeclaration
    with XmlSchemaDialect.GlobalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class LocalAttributeDeclaration(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with AttributeDeclaration
    with XmlSchemaDialect.LocalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class AttributeReference(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with AttributeDeclarationOrReference
    with XmlSchemaDialect.AttributeReference {

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

sealed trait ComplexTypeDefinition extends TypeDefinition with XmlSchemaDialect.ComplexTypeDefinition {

  final def contentType: ContentType = {
    val isMixed: Boolean = attrOption(ENames.MixedEName).exists(v => XsBooleans.parseBoolean(v))

    contentElemOption match {
      case Some(ComplexContent(_)) =>
        if (isMixed) ContentType.Mixed else ContentType.ElementOnly
      case Some(SimpleContent(_)) =>
        ContentType.Simple
      case _ =>
        if (findAllChildElems.collectFirst { case e: ModelGroup => e }.isDefined) {
          if (isMixed) ContentType.Mixed else ContentType.ElementOnly
        } else if (findAllChildElems.collectFirst { case e: ModelGroupReference => e }.isDefined) {
          if (isMixed) ContentType.Mixed else ContentType.ElementOnly
        } else {
          ContentType.Empty
        }
    }
  }
}

final case class NamedSimpleTypeDefinition(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with NamedTypeDefinition
    with SimpleTypeDefinition
    with XmlSchemaDialect.NamedSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class AnonymousSimpleTypeDefinition(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with AnonymousTypeDefinition
    with SimpleTypeDefinition
    with XmlSchemaDialect.AnonymousSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class NamedComplexTypeDefinition(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with NamedTypeDefinition
    with ComplexTypeDefinition
    with XmlSchemaDialect.NamedComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AnonymousComplexTypeDefinition(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with AnonymousTypeDefinition
    with ComplexTypeDefinition
    with XmlSchemaDialect.AnonymousComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AttributeGroupDefinition(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.AttributeGroupDefinition {

  requireName(ENames.XsAttributeGroupEName)
}

final case class AttributeGroupReference(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.AttributeGroupReference {

  requireName(ENames.XsAttributeGroupEName)
}

final case class ModelGroupDefinition(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.ModelGroupDefinition {

  requireName(ENames.XsGroupEName)
}

final case class ModelGroupReference(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.ModelGroupReference {

  requireName(ENames.XsGroupEName)
}

sealed trait ModelGroup extends ElemInXsNamespace with XmlSchemaDialect.ModelGroup

final case class SequenceModelGroup(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with ModelGroup
    with XmlSchemaDialect.SequenceModelGroup {

  requireName(ENames.XsSequenceEName)
}

final case class ChoiceModelGroup(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with ModelGroup
    with XmlSchemaDialect.ChoiceModelGroup {

  requireName(ENames.XsChoiceEName)
}

final case class AllModelGroup(underlyingElem: BackingNodes.Elem)
    extends ElemInXsNamespace
    with ModelGroup
    with XmlSchemaDialect.AllModelGroup {

  requireName(ENames.XsAllEName)
}

final case class Restriction(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Restriction {
  requireName(ENames.XsRestrictionEName)
}

final case class Extension(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Extension {
  requireName(ENames.XsExtensionEName)
}

final case class SimpleContent(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.SimpleContent {
  requireName(ENames.XsSimpleContentEName)
}

final case class ComplexContent(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.ComplexContent {
  requireName(ENames.XsComplexContentEName)
}

final case class Annotation(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Annotation {
  requireName(ENames.XsAnnotationEName)
}

final case class Appinfo(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Appinfo {
  requireName(ENames.XsAppinfoEName)
}

final case class Import(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace with XmlSchemaDialect.Import {
  requireName(ENames.XsImportEName)
}

/**
 * Other element in the XML Schema namespace. Either valid other schema content, or invalid content, such as an xs:element
 * that has both a name and a ref attribute.
 */
final case class OtherElemInXsNamespace(underlyingElem: BackingNodes.Elem) extends ElemInXsNamespace

// Companion objects

object TaxonomyNode {

  def opt(underlyingNode: BackingNodes.Node): Option[TaxonomyNode] = {
    underlyingNode match {
      case e: BackingNodes.Elem                   => Some(TaxonomyElem(e))
      case t: BackingNodes.Text                   => Some(TaxonomyTextNode(t.text))
      case c: BackingNodes.Comment                => Some(TaxonomyCommentNode(c.text))
      case pi: BackingNodes.ProcessingInstruction => Some(TaxonomyProcessingInstructionNode(pi.target, pi.data))
    }
  }
}

object TaxonomyElem {

  def apply(underlyingElem: BackingNodes.Elem): TaxonomyElem = {
    val name = underlyingElem.name

    elemFactoryMap
      .get(name.namespaceUriOption.getOrElse(""))
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
    // TODO
    ???
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
        e => OtherElemInXsNamespace(e)
      ),
      Namespaces.LinkNamespace -> new ElemFactoryWithFallback( // TODO
        Map[EName, BackingNodes.Elem => TaxonomyElem](
          ),
        (e: BackingNodes.Elem) => ???),
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