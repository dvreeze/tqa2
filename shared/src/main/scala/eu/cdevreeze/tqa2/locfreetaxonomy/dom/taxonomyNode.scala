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
import eu.cdevreeze.tqa2.common.xmlschema.ElemInXsNamespace
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.dialect.AbstractDialectBackingElem
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import eu.cdevreeze.yaidom2.queryapi.ElemStep

/**
 * Node in a locator-free taxonomy.
 *
 * @author Chris de Vreeze
 */
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
 * Note that the underlying element can be of any element implementatioon that offers the `BackingNodes.Elem` API.
 *
 * Taxonomy elements are instantiated without knowing any context, such as substitution groups or sibling extended link
 * child elements. Only an entire taxonomy has enough context to turn global element declarations into concept declarations,
 * for example.
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

// Schema root element

// TODO Attributes like use on arcs

final case class XsSchema(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.XsSchema with TaxonomyRootElem {

  requireName(ENames.XsSchemaEName)

  def isSchema: Boolean = true

  def isLinkbase: Boolean = false

  // TODO Queries for schema content, such as element declarations
}

// Linkbase root element

final case class Linkbase(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with TaxonomyRootElem {

  requireName(ENames.CLinkLinkbaseEName)

  def isSchema: Boolean = false

  def isLinkbase: Boolean = true

  // TODO Queries for extended links
}

// Schema content.

/**
 * Global element declaration. Often a concept declaration, although in general the DOM element has not enough context
 * to determine that in isolation.
 */
final case class GlobalElementDeclaration(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.GlobalElementDeclaration {

  requireName(ENames.XsElementEName)

  // TODO Other query methods
}

final case class LocalElementDeclaration(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.LocalElementDeclaration {

  requireName(ENames.XsElementEName)

  // TODO Other query methods
}

final case class ElementReference(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.ElementReference {

  requireName(ENames.XsElementEName)

  // TODO Other query methods
}

final case class GlobalAttributeDeclaration(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.GlobalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)

  // TODO Other query methods
}

final case class LocalAttributeDeclaration(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.LocalAttributeDeclaration {

  requireName(ENames.XsAttributeEName)

  // TODO Other query methods
}

final case class AttributeReference(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.AttributeReference {

  requireName(ENames.XsAttributeEName)

  // TODO Other query methods
}

final case class NamedSimpleTypeDefinition(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.NamedSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)

  // TODO Other query methods
}

final case class AnonymousSimpleTypeDefinition(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.AnonymousSimpleTypeDefinition {

  requireName(ENames.XsSimpleTypeEName)

  // TODO Other query methods
}

final case class NamedComplexTypeDefinition(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.NamedComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)

  // TODO Other query methods
}

final case class AnonymousComplexTypeDefinition(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.AnonymousComplexTypeDefinition {

  requireName(ENames.XsComplexTypeEName)

  // TODO Other query methods
}

final case class AttributeGroupDefinition(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.AttributeGroupDefinition {

  requireName(ENames.XsAttributeGroupEName)

  // TODO Other query methods
}

final case class AttributeGroupReference(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.AttributeGroupReference {

  requireName(ENames.XsAttributeGroupEName)

  // TODO Other query methods
}

final case class ModelGroupDefinition(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.ModelGroupDefinition {

  requireName(ENames.XsGroupEName)

  // TODO Other query methods
}

final case class ModelGroupReference(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.ModelGroupReference {

  requireName(ENames.XsGroupEName)

  // TODO Other query methods
}

final case class SequenceModelGroup(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.SequenceModelGroup {

  requireName(ENames.XsSequenceEName)

  // TODO Other query methods
}

final case class ChoiceModelGroup(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.ChoiceModelGroup {

  requireName(ENames.XsChoiceEName)

  // TODO Other query methods
}

final case class AllModelGroup(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.AllModelGroup {

  requireName(ENames.XsAllEName)

  // TODO Other query methods
}

final case class Restriction(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.Restriction {

  requireName(ENames.XsRestrictionEName)

  // TODO Other query methods
}

final case class Extension(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.Extension {

  requireName(ENames.XsExtensionEName)

  // TODO Other query methods
}

final case class SimpleContent(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.SimpleContent {

  requireName(ENames.XsSimpleContentEName)

  // TODO Other query methods
}

final case class ComplexContent(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.ComplexContent {

  requireName(ENames.XsComplexContentEName)

  // TODO Other query methods
}

final case class Annotation(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.Annotation {

  requireName(ENames.XsAnnotationEName)

  // TODO Other query methods
}

final case class Appinfo(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.Appinfo {

  requireName(ENames.XsAppinfoEName)

  // TODO Other query methods
}

final case class Import(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace.Import {

  requireName(ENames.XsImportEName)

  // TODO Other query methods
}

/**
 * Other element in the XML Schema namespace. Either valid other schema content, or invalid content, such as an xs:element
 * that has both a name and a ref attribute.
 */
final case class OtherElemInXsNamespace(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInXsNamespace {

  // TODO Other query methods
}

// Linkbase content.

final case class DefinitionLink(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.DefinitionLink {

  requireName(ENames.CLinkDefinitionLinkEName)

  // TODO Other query methods
}

final case class PresentationLink(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.PresentationLink {

  requireName(ENames.CLinkPresentationLinkEName)

  // TODO Other query methods
}

final case class CalculationLink(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.CalculationLink {

  requireName(ENames.CLinkCalculationLinkEName)

  // TODO Other query methods
}

final case class LabelLink(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.LabelLink {

  requireName(ENames.CLinkLabelLinkEName)

  // TODO Other query methods
}

final case class ReferenceLink(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.ReferenceLink {

  requireName(ENames.CLinkReferenceLinkEName)

  // TODO Other query methods
}

final case class DefinitionArc(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.DefinitionArc {

  requireName(ENames.CLinkDefinitionArcEName)

  // TODO Other query methods
}

final case class PresentationArc(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.PresentationArc {

  requireName(ENames.CLinkPresentationArcEName)

  // TODO Other query methods
}

final case class CalculationArc(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.CalculationArc {

  requireName(ENames.CLinkCalculationArcEName)

  // TODO Other query methods
}

final case class LabelArc(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.LabelArc {

  requireName(ENames.CLinkLabelArcEName)

  // TODO Other query methods
}

final case class ReferenceArc(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.ReferenceArc {

  requireName(ENames.CLinkReferenceArcEName)

  // TODO Other query methods
}

final case class ConceptLabelResource(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.ConceptLabelResource {

  requireName(ENames.CLinkLabelEName)

  // TODO Other query methods
}

final case class ConceptReferenceResource(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace.ConceptReferenceResource {

  requireName(ENames.CLinkReferenceEName)

  // TODO Other query methods
}

/**
 * Other element in the CLink namespace. Either valid other CLink content, or invalid content.
 */
final case class OtherElemInCLinkNamespace(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInCLinkNamespace {

  // TODO Other query methods
}

final case class RoleType(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInLinkNamespace.RoleType {

  requireName(ENames.LinkRoleTypeEName)

  // TODO Other query methods
}

final case class ArcroleType(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInLinkNamespace.ArcroleType {

  requireName(ENames.LinkArcroleTypeEName)

  // TODO Other query methods
}

final case class Definition(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInLinkNamespace.Definition {

  requireName(ENames.LinkDefinitionEName)

  // TODO Other query methods
}

final case class UsedOn(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInLinkNamespace.UsedOn {

  requireName(ENames.LinkUsedOnEName)

  // TODO Other query methods
}

/**
 * Other element in the Link namespace. Either valid other Link content, or invalid content.
 */
final case class OtherElemInLinkNamespace(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with ElemInLinkNamespace {

  // TODO Other query methods
}

final case class ConceptKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with TaxonomyElementKey.ConceptKey {

  requireName(ENames.CKeyConceptKeyEName)

  // TODO Other query methods
}

final case class ElementKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with TaxonomyElementKey.ElementKey {

  requireName(ENames.CKeyElementKeyEName)

  // TODO Other query methods
}

final case class TypeKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with TaxonomyElementKey.TypeKey {

  requireName(ENames.CKeyTypeKeyEName)

  // TODO Other query methods
}

final case class RoleKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with TaxonomyElementKey.RoleKey {

  requireName(ENames.CKeyRoleKeyEName)

  // TODO Other query methods
}

final case class ArcroleKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with TaxonomyElementKey.ArcroleKey {

  requireName(ENames.CKeyArcroleKeyEName)

  // TODO Other query methods
}

final case class AnyElementKey(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with TaxonomyElementKey.AnyElementKey {

  requireName(ENames.CKeyAnyElemKeyEName)

  // TODO Other query methods
}

/**
 * Non-standard extended link
 */
final case class NonStandardLink(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with locfreexlink.ExtendedLink {

  // TODO Other query methods
}

/**
 * Non-standard arc
 */
final case class NonStandardArc(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with locfreexlink.XLinkArc {

  // TODO Other query methods
}

/**
 * Non-standard resource, which is also not a taxonomy element key
 */
final case class NonStandardResource(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) with locfreexlink.XLinkResource {

  // TODO Other query methods
}

/**
 * Any other non-XLink element
 */
final case class OtherNonXLinkElem(
  override val underlyingElem: BackingNodes.Elem) extends TaxonomyElem(underlyingElem) {

  // TODO Other query methods
}

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
          ENames.CLinkReferenceEName -> (e => new ConceptReferenceResource(e))
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
