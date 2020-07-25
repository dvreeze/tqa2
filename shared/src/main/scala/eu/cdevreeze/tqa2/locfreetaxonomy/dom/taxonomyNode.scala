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

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.common.datatypes.XsBooleans
import eu.cdevreeze.tqa2.common.datatypes.XsDoubles
import eu.cdevreeze.tqa2.common.FragmentKey
import eu.cdevreeze.tqa2.common.locfreexlink
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.common.xmlschema.XmlSchemaDialect
import eu.cdevreeze.tqa2.common.xpointer.XPointer
import eu.cdevreeze.tqa2.locfreetaxonomy.common._
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.dialect.AbstractDialectBackingElem
import eu.cdevreeze.yaidom2.queryapi.internal.AbstractSubtypeAwareElem
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import eu.cdevreeze.yaidom2.queryapi.ElemStep
import eu.cdevreeze.yaidom2.queryapi.anyElem
import eu.cdevreeze.yaidom2.queryapi.named

import scala.collection.immutable.ArraySeq
import scala.reflect.classTag

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

final case class TaxonomyProcessingInstructionNode(target: String, data: String)
    extends CanBeTaxonomyDocumentChild
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
sealed trait TaxonomyElem extends AbstractDialectBackingElem with AbstractSubtypeAwareElem with CanBeTaxonomyDocumentChild {

  type ThisElem = TaxonomyElem

  type ThisNode = TaxonomyNode

  /**
   * Customizable factory function of TaxonomyElem objects. Must be fast. It is called by method wrapElem.
   */
  def taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem

  final def wrapElem(underlyingElem: BackingNodes.Elem): ThisElem = taxonomyElemFactory(underlyingElem)

  // ClarkNodes.Elem

  final def children: ArraySeq[TaxonomyNode] = {
    underlyingElem.children.flatMap(ch => TaxonomyNode.opt(ch, taxonomyElemFactory)).to(ArraySeq)
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

  /**
   * Returns the key pointing to this element as taxonomy element key. If this element itself represents a key, an "any element key" is returned.
   * Otherwise, the "canonical" key pointing to this element is returned.
   */
  final def ownKey: TaxonomyElemKeys.TaxonomyElemKey = {
    this match {
      case e: GlobalElementDeclaration =>
        TaxonomyElemKeys.ElementKey(e.targetEName)
      case e: NamedTypeDefinition =>
        TaxonomyElemKeys.TypeKey(e.targetEName)
      case e: RoleType =>
        TaxonomyElemKeys.RoleKey(e.roleUri)
      case e: ArcroleType =>
        TaxonomyElemKeys.ArcroleKey(e.arcroleUri)
      case e =>
        ownAnyElementKey
    }
  }

  /**
   * Returns the key pointing to this element as "any element key", even if this element is a global element declaration, named type definition,
   * role type or arcrole type.
   */
  final def ownAnyElementKey: TaxonomyElemKeys.AnyElementKey = {
    // TODO Improve. What if the element is a root element, for example? Do we need a URI fragment then?
    val docUri: URI = this.docUri
    val xpointer: XPointer = XPointer.toXPointer(this.underlyingElem)
    val ownUri: URI = new URI(docUri.getScheme, docUri.getSchemeSpecificPart, xpointer.toString)

    TaxonomyElemKeys.AnyElementKey(ownUri)
  }

  final def fragmentKey: FragmentKey = {
    FragmentKey(underlyingElem.docUri, underlyingElem.ownNavigationPathRelativeToRootElem)
  }

  final def isRootElement: Boolean = this match {
    case _: RootElement => true
    case _              => false
  }

  final def idOption: Option[String] = attrOption(ENames.IdEName)

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
    findAllChildElemsOfType(classTag[ChildXLink])
  }

  final def xlinkResourceChildren: Seq[XLinkResource] = {
    findAllChildElemsOfType(classTag[XLinkResource])
  }

  final def arcs: Seq[XLinkArc] = {
    findAllChildElemsOfType(classTag[XLinkArc])
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

final case class XsSchema(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.XsSchema
    with RootElement {
  requireName(ENames.XsSchemaEName)

  def isSchema: Boolean = true

  def isLinkbase: Boolean = false

  def findAllImports: Seq[Import] = {
    findAllChildElemsOfType(classTag[Import])
  }

  def filterGlobalElementDeclarations(p: GlobalElementDeclaration => Boolean): Seq[GlobalElementDeclaration] = {
    filterChildElemsOfType(classTag[GlobalElementDeclaration])(p)
  }

  def findAllGlobalElementDeclarations: Seq[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(anyElem)
  }

  def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclaration => Boolean): Seq[GlobalAttributeDeclaration] = {
    filterChildElemsOfType(classTag[GlobalAttributeDeclaration])(p)
  }

  def findAllGlobalAttributeDeclarations: Seq[GlobalAttributeDeclaration] = {
    filterGlobalAttributeDeclarations(anyElem)
  }

  def filterNamedTypeDefinitions(p: NamedTypeDefinition => Boolean): Seq[NamedTypeDefinition] = {
    filterChildElemsOfType(classTag[NamedTypeDefinition])(p)
  }

  def findAllNamedTypeDefinitions: Seq[NamedTypeDefinition] = {
    filterNamedTypeDefinitions(anyElem)
  }
}

// Linkbase root element

final case class Linkbase(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with CLinkDialect.Linkbase
    with RootElement {
  requireName(ENames.CLinkLinkbaseEName)

  def isSchema: Boolean = false

  def isLinkbase: Boolean = true

  /**
   * Finds all ("taxonomy DOM") extended links
   */
  def findAllExtendedLinks: Seq[ExtendedLink] = {
    findAllChildElemsOfType(classTag[ExtendedLink])
  }

  def findAllRoleRefs: Seq[RoleRef] = {
    findAllChildElemsOfType(classTag[RoleRef])
  }

  def findAllArcroleRefs: Seq[ArcroleRef] = {
    findAllChildElemsOfType(classTag[ArcroleRef])
  }
}

// Schema content.

sealed trait ElementDeclarationOrReference extends ElemInXsNamespace with XmlSchemaDialect.ElementDeclarationOrReference

sealed trait ElementDeclaration extends ElementDeclarationOrReference with XmlSchemaDialect.ElementDeclaration

/**
 * Global element declaration. Often a concept declaration, although in general the DOM element has not enough context
 * to determine that in isolation.
 */
final case class GlobalElementDeclaration(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
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

final case class LocalElementDeclaration(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with ElementDeclaration
    with XmlSchemaDialect.LocalElementDeclaration {

  requireName(ENames.XsElementEName)
}

final case class ElementReference(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with ElementDeclarationOrReference
    with XmlSchemaDialect.ElementReference {

  requireName(ENames.XsElementEName)
}

sealed trait AttributeDeclarationOrReference extends ElemInXsNamespace with XmlSchemaDialect.AttributeDeclarationOrReference

sealed trait AttributeDeclaration extends AttributeDeclarationOrReference with XmlSchemaDialect.AttributeDeclaration

final case class GlobalAttributeDeclaration(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends NamedGlobalSchemaComponent
    with AttributeDeclaration
    with XmlSchemaDialect.GlobalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class LocalAttributeDeclaration(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with AttributeDeclaration
    with XmlSchemaDialect.LocalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)
}

final case class AttributeReference(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
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
      findFirstChildElemOfType(classTag[Restriction]).flatMap(_.baseTypeOption)
    case _ => None
  }
}

sealed trait ComplexTypeDefinition extends TypeDefinition with XmlSchemaDialect.ComplexTypeDefinition {

  final def contentType: ContentType = {
    val isMixed: Boolean = attrOption(ENames.MixedEName).exists(v => XsBooleans.parseBoolean(v))

    contentElemOption match {
      case Some(ComplexContent(_, _)) =>
        if (isMixed) ContentType.Mixed else ContentType.ElementOnly
      case Some(SimpleContent(_, _)) =>
        ContentType.Simple
      case _ =>
        if (findFirstChildElemOfType(classTag[ModelGroup]).isDefined) {
          if (isMixed) ContentType.Mixed else ContentType.ElementOnly
        } else if (findFirstChildElemOfType(classTag[ModelGroupReference]).isDefined) {
          if (isMixed) ContentType.Mixed else ContentType.ElementOnly
        } else {
          ContentType.Empty
        }
    }
  }
}

final case class NamedSimpleTypeDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with NamedTypeDefinition
    with SimpleTypeDefinition
    with XmlSchemaDialect.NamedSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class AnonymousSimpleTypeDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with AnonymousTypeDefinition
    with SimpleTypeDefinition
    with XmlSchemaDialect.AnonymousSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)
}

final case class NamedComplexTypeDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with NamedTypeDefinition
    with ComplexTypeDefinition
    with XmlSchemaDialect.NamedComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AnonymousComplexTypeDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with AnonymousTypeDefinition
    with ComplexTypeDefinition
    with XmlSchemaDialect.AnonymousComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)
}

final case class AttributeGroupDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.AttributeGroupDefinition {

  requireName(ENames.XsAttributeGroupEName)
}

final case class AttributeGroupReference(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.AttributeGroupReference {

  requireName(ENames.XsAttributeGroupEName)
}

final case class ModelGroupDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.ModelGroupDefinition {

  requireName(ENames.XsGroupEName)
}

final case class ModelGroupReference(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.ModelGroupReference {

  requireName(ENames.XsGroupEName)
}

sealed trait ModelGroup extends ElemInXsNamespace with XmlSchemaDialect.ModelGroup

final case class SequenceModelGroup(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with ModelGroup
    with XmlSchemaDialect.SequenceModelGroup {

  requireName(ENames.XsSequenceEName)
}

final case class ChoiceModelGroup(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with ModelGroup
    with XmlSchemaDialect.ChoiceModelGroup {

  requireName(ENames.XsChoiceEName)
}

final case class AllModelGroup(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with ModelGroup
    with XmlSchemaDialect.AllModelGroup {

  requireName(ENames.XsAllEName)
}

final case class Restriction(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.Restriction {
  requireName(ENames.XsRestrictionEName)
}

final case class Extension(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.Extension {
  requireName(ENames.XsExtensionEName)
}

final case class SimpleContent(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.SimpleContent {
  requireName(ENames.XsSimpleContentEName)
}

final case class ComplexContent(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.ComplexContent {
  requireName(ENames.XsComplexContentEName)
}

final case class Annotation(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.Annotation {
  requireName(ENames.XsAnnotationEName)
}

final case class Appinfo(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.Appinfo {
  requireName(ENames.XsAppinfoEName)
}

final case class Import(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace
    with XmlSchemaDialect.Import {
  requireName(ENames.XsImportEName)
}

/**
 * Other element in the XML Schema namespace. Either valid other schema content, or invalid content, such as an xs:element
 * that has both a name and a ref attribute.
 */
final case class OtherElemInXsNamespace(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInXsNamespace

// Linkbase content.

sealed trait StandardLink extends ElemInCLinkNamespace with ExtendedLink with CLinkDialect.StandardLink

final case class DefinitionLink(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with StandardLink
    with CLinkDialect.DefinitionLink {

  requireName(ENames.CLinkDefinitionLinkEName)
}

final case class PresentationLink(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with StandardLink
    with CLinkDialect.PresentationLink {

  requireName(ENames.CLinkPresentationLinkEName)
}

final case class CalculationLink(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with StandardLink
    with CLinkDialect.CalculationLink {

  requireName(ENames.CLinkCalculationLinkEName)
}

final case class LabelLink(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with StandardLink
    with CLinkDialect.LabelLink {
  requireName(ENames.CLinkLabelLinkEName)
}

final case class ReferenceLink(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with StandardLink
    with CLinkDialect.ReferenceLink {

  requireName(ENames.CLinkReferenceLinkEName)
}

sealed trait StandardArc extends ElemInCLinkNamespace with XLinkArc with CLinkDialect.StandardArc

sealed trait InterConceptArc extends StandardArc with CLinkDialect.InterConceptArc

sealed trait ConceptResourceArc extends StandardArc with CLinkDialect.ConceptResourceArc

final case class DefinitionArc(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with InterConceptArc
    with CLinkDialect.DefinitionArc {

  requireName(ENames.CLinkDefinitionArcEName)
}

final case class PresentationArc(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with InterConceptArc
    with CLinkDialect.PresentationArc {

  requireName(ENames.CLinkPresentationArcEName)
}

final case class CalculationArc(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with InterConceptArc
    with CLinkDialect.CalculationArc {

  requireName(ENames.CLinkCalculationArcEName)

  def weight: Double = {
    XsDoubles.parseDouble(attr(ENames.WeightEName))
  }
}

final case class LabelArc(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with ConceptResourceArc
    with CLinkDialect.LabelArc {

  requireName(ENames.CLinkLabelArcEName)
}

final case class ReferenceArc(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with ConceptResourceArc
    with CLinkDialect.ReferenceArc {

  requireName(ENames.CLinkReferenceArcEName)
}

/**
 * XLink resource that is not a TaxonomyElemKey. It has direct sub-types StandardResource and NonStandardResource.
 */
sealed trait NonKeyResource extends XLinkResource

sealed trait StandardResource extends ElemInCLinkNamespace with NonKeyResource with CLinkDialect.StandardResource

final case class ConceptLabelResource(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with StandardResource
    with CLinkDialect.ConceptLabelResource {

  requireName(ENames.CLinkLabelEName)
}

final case class ConceptReferenceResource(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with StandardResource
    with CLinkDialect.ConceptReferenceResource {

  requireName(ENames.CLinkReferenceEName)
}

final case class RoleRef(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with CLinkDialect.RoleRef {

  requireName(ENames.CLinkRoleRefEName)
}

final case class ArcroleRef(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with CLinkDialect.ArcroleRef {

  requireName(ENames.CLinkArcroleRefEName)
}

// In entrypoint schemas

final case class LinkbaseRef(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace
    with CLinkDialect.LinkbaseRef {

  requireName(ENames.CLinkLinkbaseRefEName)
}

/**
 * Other element in the CLink namespace. Either valid other CLink content, or invalid content.
 */
final case class OtherElemInCLinkNamespace(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInCLinkNamespace

final case class RoleType(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInLinkNamespace
    with LinkDialect.RoleType {
  requireName(ENames.LinkRoleTypeEName)

  def definitionOption: Option[Definition] = {
    findFirstChildElemOfType(classTag[Definition])
  }

  def usedOn: Seq[UsedOn] = {
    findAllChildElemsOfType(classTag[UsedOn])
  }
}

final case class ArcroleType(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInLinkNamespace
    with LinkDialect.ArcroleType {
  requireName(ENames.LinkArcroleTypeEName)

  /**
   * Returns the cyclesAllowed attribute.
   */
  def cyclesAllowed: CyclesAllowed = {
    CyclesAllowed.fromString(attr(ENames.CyclesAllowedEName))
  }

  def definitionOption: Option[Definition] = {
    findFirstChildElemOfType(classTag[Definition])
  }

  def usedOn: Seq[UsedOn] = {
    findAllChildElemsOfType(classTag[UsedOn])
  }
}

final case class Definition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInLinkNamespace
    with LinkDialect.Definition {
  requireName(ENames.LinkDefinitionEName)
}

final case class UsedOn(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInLinkNamespace
    with LinkDialect.UsedOn {
  requireName(ENames.LinkUsedOnEName)
}

/**
 * Other element in the Link namespace. Either valid other Link content, or invalid content.
 */
final case class OtherElemInLinkNamespace(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends ElemInLinkNamespace

sealed trait TaxonomyElemKey extends TaxonomyElem with XLinkResource with TaxonomyElemKeyDialect.TaxonomyElemKey

final case class ConceptKey(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends TaxonomyElemKey
    with TaxonomyElemKeyDialect.ConceptKey {
  requireName(ENames.CKeyConceptKeyEName)
}

final case class ElementKey(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends TaxonomyElemKey
    with TaxonomyElemKeyDialect.ElementKey {
  requireName(ENames.CKeyElementKeyEName)
}

final case class TypeKey(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends TaxonomyElemKey
    with TaxonomyElemKeyDialect.TypeKey {
  requireName(ENames.CKeyTypeKeyEName)
}

final case class RoleKey(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends TaxonomyElemKey
    with TaxonomyElemKeyDialect.RoleKey {
  requireName(ENames.CKeyRoleKeyEName)
}

final case class ArcroleKey(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends TaxonomyElemKey
    with TaxonomyElemKeyDialect.ArcroleKey {
  requireName(ENames.CKeyArcroleKeyEName)
}

final case class AnyElementKey(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends TaxonomyElemKey
    with TaxonomyElemKeyDialect.AnyElementKey {
  requireName(ENames.CKeyAnyElemKeyEName)
}

/**
 * Common non-sealed super-trait of AnyNonStandardLink, AnyNonStandardArc, AnyNonStandardResource and AnyOtherNonXLinkElem.
 */
trait AnyNonStandardElem extends TaxonomyElem

/**
 * Non-standard extended link, as extensible non-sealed trait
 */
trait AnyNonStandardLink extends AnyNonStandardElem with ExtendedLink

/**
 * Non-standard extended link
 */
final case class NonStandardLink(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends AnyNonStandardLink

/**
 * Non-standard arc, as extensible non-sealed trait. This enables us to "replace" a NonStandardArc
 * by an arc in a formula or table context (or in any custom XBRL context).
 */
trait AnyNonStandardArc extends AnyNonStandardElem with XLinkArc

/**
 * Non-standard arc
 */
final case class NonStandardArc(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends AnyNonStandardArc

/**
 * Non-standard resource, which is also not a taxonomy element key, as extensible non-sealed trait.
 * This enables us to "replace" a NonStandardResource by an XLink resource in a formula or table context
 * (or in any custom XBRL context).
 */
trait AnyNonStandardResource extends AnyNonStandardElem with NonKeyResource

/**
 * Non-standard resource, which is also not a taxonomy element key
 */
final case class NonStandardResource(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends AnyNonStandardResource

/**
 * Any other non-XLink element, not in the "xs", "clink" or "link" namespaces, as extensible non-sealed trait.
 * This enables us to "replace" an OtherNonXLinkElem by a non-XLink element in a formula or table context
 * (or in any custom XBRL context).
 */
trait AnyOtherNonXLinkElem extends AnyNonStandardElem

/**
 * Any other non-XLink element, not in the "xs", "clink" or "link" namespaces.
 */
final case class OtherNonXLinkElem(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: BackingNodes.Elem => TaxonomyElem)
    extends AnyOtherNonXLinkElem

// Companion objects

object TaxonomyNode {

  /**
   * Returns an optiona TaxonomyNode from the given underlying node and TaxonomyElem factory.
   */
  def opt(underlyingNode: BackingNodes.Node, elemFactory: BackingNodes.Elem => TaxonomyElem): Option[TaxonomyNode] = {
    underlyingNode match {
      case e: BackingNodes.Elem                   => Some(elemFactory(e))
      case t: BackingNodes.Text                   => Some(TaxonomyTextNode(t.text))
      case c: BackingNodes.Comment                => Some(TaxonomyCommentNode(c.text))
      case pi: BackingNodes.ProcessingInstruction => Some(TaxonomyProcessingInstructionNode(pi.target, pi.data))
    }
  }
}

object TaxonomyElem {

  /**
   * Returns `of(underlyingElem, DefaultElemFactory)`. That means that the returned TaxonomyElem is not known to be formula or table
   * content, from the specific TaxonomyElem sub-type.
   */
  def apply(underlyingElem: BackingNodes.Elem): TaxonomyElem = {
    of(underlyingElem, DefaultElemFactory)
  }

  /**
   * Creates a TaxonomyElem from the given underlying element, using the given ElemFactory.
   * That is, returns `elemFactory(underlyingElem)`.
   */
  def of(underlyingElem: BackingNodes.Elem, elemFactory: ElemFactory): TaxonomyElem = {
    elemFactory(underlyingElem)
  }

  /**
   * Factory turning a "backing element" into a TaxonomyElem that wraps it.
   */
  type ElemFactory = BackingNodes.Elem => TaxonomyElem

  /**
   * API for constructors of TaxonomyElem instances, taking a "backing element" and ElemFactory as input parameters.
   */
  type ElemConstructor = (BackingNodes.Elem, ElemFactory) => TaxonomyElem

  /**
   * API for optional constructors of TaxonomyElem instances, taking a "backing element" and ElemFactory as input parameters.
   */
  type OptionalElemConstructor = (BackingNodes.Elem, ElemFactory) => Option[TaxonomyElem]

  /**
   * Functions from element ENames to the corresponding ElemConstructors.
   */
  type ElemConstructorGetterByEName = EName => ElemConstructor

  /**
   * Functions from element ENames to the corresponding optional ElemConstructors.
   */
  type ElemConstructorFinderByEName = EName => Option[ElemConstructor]

  /**
   * The default ElemConstructorGetterByEName.
   */
  final class DefaultElemConstructorGetter(
      val elemConstructorMap: Map[EName, ElemConstructor],
      val fallbackElemConstructor: ElemConstructor)
      extends ElemConstructorGetterByEName {

    def apply(name: EName): ElemConstructor = {
      elemConstructorMap.getOrElse(name, fallbackElemConstructor)
    }
  }

  private def optElementDeclarationOrReference(
      underlyingElem: BackingNodes.Elem,
      taxonomyElemFactory: ElemFactory): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(GlobalElementDeclaration(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(LocalElementDeclaration(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(ElementReference(underlyingElem, taxonomyElemFactory))
    } else {
      None
    }
  }

  private def optAttributeDeclarationOrReference(
      underlyingElem: BackingNodes.Elem,
      taxonomyElemFactory: ElemFactory): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(GlobalAttributeDeclaration(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && hasName && !hasRef) {
      Some(LocalAttributeDeclaration(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(AttributeReference(underlyingElem, taxonomyElemFactory))
    } else {
      None
    }
  }

  private def optSimpleTypeDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: ElemFactory): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty

    if (parentIsSchema && hasName) {
      Some(NamedSimpleTypeDefinition(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && !hasName) {
      Some(AnonymousSimpleTypeDefinition(underlyingElem, taxonomyElemFactory))
    } else {
      None
    }
  }

  private def optComplexTypeDefinition(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: ElemFactory): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty

    if (parentIsSchema && hasName) {
      Some(NamedComplexTypeDefinition(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && !hasName) {
      Some(AnonymousComplexTypeDefinition(underlyingElem, taxonomyElemFactory))
    } else {
      None
    }
  }

  private def optAttributeGroupDefinitionOrReference(
      underlyingElem: BackingNodes.Elem,
      taxonomyElemFactory: ElemFactory): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(AttributeGroupDefinition(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(AttributeGroupReference(underlyingElem, taxonomyElemFactory))
    } else {
      None
    }
  }

  private def optModelGroupDefinitionOrReference(
      underlyingElem: BackingNodes.Elem,
      taxonomyElemFactory: ElemFactory): Option[TaxonomyElem] = {
    val parentIsSchema = underlyingElem.findParentElem.exists(_.name == ENames.XsSchemaEName)
    val hasName = underlyingElem.attrOption(ENames.NameEName).nonEmpty
    val hasRef = underlyingElem.attrOption(ENames.RefEName).nonEmpty

    if (parentIsSchema && hasName && !hasRef) {
      Some(ModelGroupDefinition(underlyingElem, taxonomyElemFactory))
    } else if (!parentIsSchema && !hasName && hasRef) {
      Some(ModelGroupReference(underlyingElem, taxonomyElemFactory))
    } else {
      None
    }
  }

  def fallbackElem(underlyingElem: BackingNodes.Elem, taxonomyElemFactory: ElemFactory): TaxonomyElem = {
    // Not an OtherElemInXsNamespace, OtherElemInCLinkNamespace or OtherElemInLinkNamespace.
    underlyingElem.attrOption(ENames.XLinkTypeEName) match {
      case Some("extended") => NonStandardLink(underlyingElem, taxonomyElemFactory)
      case Some("arc")      => NonStandardArc(underlyingElem, taxonomyElemFactory)
      case Some("resource") => NonStandardResource(underlyingElem, taxonomyElemFactory)
      case _                => OtherNonXLinkElem(underlyingElem, taxonomyElemFactory)
    }
  }

  private val namespaceToElemConstructorGetterMap: Map[String, ElemConstructorGetterByEName] = {
    Map(
      Namespaces.XsNamespace -> new DefaultElemConstructorGetter(
        Map[EName, ElemConstructor](
          ENames.XsSchemaEName -> XsSchema.apply,
          ENames.XsElementEName -> { (e, ef) =>
            optElementDeclarationOrReference(e, ef).getOrElse(OtherElemInXsNamespace(e, ef))
          },
          ENames.XsAttributeEName -> { (e, ef) =>
            optAttributeDeclarationOrReference(e, ef).getOrElse(OtherElemInXsNamespace(e, ef))
          },
          ENames.XsSimpleTypeEName -> { (e, ef) =>
            optSimpleTypeDefinition(e, ef).getOrElse(OtherElemInXsNamespace(e, ef))
          },
          ENames.XsComplexTypeEName -> { (e, ef) =>
            optComplexTypeDefinition(e, ef).getOrElse(OtherElemInXsNamespace(e, ef))
          },
          ENames.XsAttributeGroupEName -> { (e, ef) =>
            optAttributeGroupDefinitionOrReference(e, ef).getOrElse(OtherElemInXsNamespace(e, ef))
          },
          ENames.XsGroupEName -> { (e, ef) =>
            optModelGroupDefinitionOrReference(e, ef).getOrElse(OtherElemInXsNamespace(e, ef))
          },
          ENames.XsSequenceEName -> SequenceModelGroup.apply,
          ENames.XsChoiceEName -> ChoiceModelGroup.apply,
          ENames.XsAllEName -> AllModelGroup.apply,
          ENames.XsRestrictionEName -> Restriction.apply,
          ENames.XsExtensionEName -> Extension.apply,
          ENames.XsSimpleContentEName -> SimpleContent.apply,
          ENames.XsComplexContentEName -> ComplexContent.apply,
          ENames.XsAnnotationEName -> Annotation.apply,
          ENames.XsAppinfoEName -> Appinfo.apply,
          ENames.XsImportEName -> Import.apply
        ),
        OtherElemInXsNamespace.apply
      ),
      Namespaces.CLinkNamespace -> new DefaultElemConstructorGetter(
        Map[EName, ElemConstructor](
          ENames.CLinkLinkbaseEName -> Linkbase.apply,
          ENames.CLinkDefinitionLinkEName -> DefinitionLink.apply,
          ENames.CLinkPresentationLinkEName -> PresentationLink.apply,
          ENames.CLinkCalculationLinkEName -> CalculationLink.apply,
          ENames.CLinkLabelLinkEName -> LabelLink.apply,
          ENames.CLinkReferenceLinkEName -> ReferenceLink.apply,
          ENames.CLinkDefinitionArcEName -> DefinitionArc.apply,
          ENames.CLinkPresentationArcEName -> PresentationArc.apply,
          ENames.CLinkCalculationArcEName -> CalculationArc.apply,
          ENames.CLinkLabelArcEName -> LabelArc.apply,
          ENames.CLinkReferenceArcEName -> ReferenceArc.apply,
          ENames.CLinkLabelEName -> ConceptLabelResource.apply,
          ENames.CLinkReferenceEName -> ConceptReferenceResource.apply,
          ENames.CLinkRoleRefEName -> RoleRef.apply,
          ENames.CLinkArcroleRefEName -> ArcroleRef.apply,
          ENames.CLinkLinkbaseRefEName -> LinkbaseRef.apply,
        ),
        OtherElemInCLinkNamespace.apply
      ),
      Namespaces.LinkNamespace -> new DefaultElemConstructorGetter(
        Map[EName, ElemConstructor](
          ENames.LinkRoleTypeEName -> RoleType.apply,
          ENames.LinkArcroleTypeEName -> ArcroleType.apply,
          ENames.LinkDefinitionEName -> Definition.apply,
          ENames.LinkUsedOnEName -> UsedOn.apply,
        ),
        OtherElemInLinkNamespace.apply
      ),
      Namespaces.CKeyNamespace -> new DefaultElemConstructorGetter(
        Map[EName, ElemConstructor](
          ENames.CKeyConceptKeyEName -> ConceptKey.apply,
          ENames.CKeyElementKeyEName -> ElementKey.apply,
          ENames.CKeyTypeKeyEName -> TypeKey.apply,
          ENames.CKeyRoleKeyEName -> RoleKey.apply,
          ENames.CKeyArcroleKeyEName -> ArcroleKey.apply,
          ENames.CKeyAnyElemKeyEName -> AnyElementKey.apply,
        ),
        fallbackElem
      ),
      Namespaces.CGenNamespace -> new DefaultElemConstructorGetter(Map.empty, fallbackElem),
      Namespaces.GenNamespace -> new DefaultElemConstructorGetter(Map.empty, fallbackElem),
    )
  }

  /**
   * The default ElemFactory implementation. It returns TaxonomyElem instances whose types do not know about formula or
   * table content.
   */
  object DefaultElemFactory extends ElemFactory {

    /**
     * Returns `of(underlyingElem, DefaultElemFactory)`. That is, creates a TaxonomyElem using this element factory, which
     * is used for this element and all its descendants.
     */
    def apply(underlyingElem: BackingNodes.Elem): TaxonomyElem = {
      of(underlyingElem, DefaultElemFactory)
    }

    /**
     * Creates a TaxonomyElem wrapping the parameter underlying element. This DefaultElemFactory determines the type of
     * taxonomy element wrapping the underlying element, whereas the parameter element factory is used to create descendant
     * taxonomy elements (when querying for them). The parameter element factory could be this DefaultElemFactory, or
     * it could typically be StandardizedNonStandardTaxonomyElem.DefaultElemFactory, which adds formula/table knowledge
     * to the taxonomy DOM.
     */
    def of(underlyingElem: BackingNodes.Elem, elemFactory: ElemFactory): TaxonomyElem = {
      val name = underlyingElem.name

      namespaceToElemConstructorGetterMap
        .get(name.namespaceUriOption.getOrElse(""))
        .map(_.apply(name))
        .map(f => f(underlyingElem, elemFactory))
        .getOrElse(fallbackElem(underlyingElem, elemFactory))
    }
  }
}

object XsSchema {

  /**
   * Returns `of(underlyingElem, DefaultElemFactory)`.
   */
  def apply(underlyingElem: BackingNodes.Elem): XsSchema = {
    of(underlyingElem, TaxonomyElem.DefaultElemFactory)
  }

  /**
   * Creates an XsSchema from the given underlying element, using the given ElemFactory.
   * That is, returns `XsSchema(underlyingElem, elemFactory)`.
   */
  def of(underlyingElem: BackingNodes.Elem, elemFactory: TaxonomyElem.ElemFactory): XsSchema = {
    XsSchema(underlyingElem, elemFactory)
  }
}

object Linkbase {

  /**
   * Returns `of(underlyingElem, DefaultElemFactory)`.
   */
  def apply(underlyingElem: BackingNodes.Elem): Linkbase = {
    of(underlyingElem, TaxonomyElem.DefaultElemFactory)
  }

  /**
   * Creates a Linkbase from the given underlying element, using the given ElemFactory.
   * That is, returns `Linkbase(underlyingElem, elemFactory)`.
   */
  def of(underlyingElem: BackingNodes.Elem, elemFactory: TaxonomyElem.ElemFactory): Linkbase = {
    Linkbase(underlyingElem, elemFactory)
  }
}
