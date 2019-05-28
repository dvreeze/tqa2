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

package eu.cdevreeze.tqa2.common.xmlschema

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.datatypes.XsBooleans
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * Element in the "XML schema" namespace in a locator-free taxonomy.
 * This type or a sub-type is mixed in by taxonomy elements that are indeed in the XML Schema namespace.
 *
 * @author Chris de Vreeze
 */
trait ElemInXsNamespace extends BackingElemApi {

  /**
   * Returns the optional target namespace of the surrounding schema root element (or self), ignoring the possibility that
   * this is an included chameleon schema.
   */
  final def schemaTargetNamespaceOption: Option[String] = {
    findAncestorElemOrSelf(_.name == ENames.XsSchemaEName).flatMap(_.attrOption(ENames.TargetNamespaceEName))
  }
}

object ElemInXsNamespace {

  // Traits that are more general than specific schema components

  /**
   * Super-type of schema components that can be abstract.
   */
  trait CanBeAbstract extends ElemInXsNamespace {

    /**
     * Returns the boolean "abstract" attribute (defaulting to false). This may fail with an exception if the taxonomy is not schema-valid.
     */
    final def isAbstract: Boolean = {
      attrOption(ENames.AbstractEName).map(v => XsBooleans.parseBoolean(v)).getOrElse(false)
    }

    final def isConcrete: Boolean = {
      !isAbstract
    }
  }

  /**
   * Super-type of schema components that have a name attribute.
   */
  trait NamedDeclOrDef extends ElemInXsNamespace {

    /**
     * Returns the "name" attribute. This may fail with an exception if the taxonomy is not schema-valid.
     */
    final def nameAttributeValue: String = {
      attr(ENames.NameEName)
    }
  }

  /**
   * Super-type of schema components that are references.
   */
  trait Reference extends ElemInXsNamespace {

    /**
     * Returns the "ref" attribute as EName. This may fail with an exception if the taxonomy is not schema-valid.
     */
    final def ref: EName = {
      attrAsResolvedQName(ENames.RefEName)
    }
  }

  trait Particle extends ElemInXsNamespace {
    // TODO
  }

  // The schema root element itself

  trait XsSchema extends ElemInXsNamespace {
    // TODO
  }

  // Traits that are specific to schema components or parts thereof

  trait ElementDeclarationOrReference extends ElemInXsNamespace {
    // TODO
  }

  trait ElementDeclaration extends ElementDeclarationOrReference with NamedDeclOrDef {
    // TODO

    /**
     * Returns the optional type attribute (as EName). This may fail with an exception if the taxonomy is not schema-valid.
     */
    final def typeOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.TypeEName)
    }
  }

  /**
   * Global element declaration. Often a concept declaration, although in general the DOM element has not enough context
   * to determine that in isolation.
   */
  trait GlobalElementDeclaration extends ElementDeclaration with CanBeAbstract {

    /**
     * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
     * name attribute as local part.
     */
    final def targetEName: EName = {
      val tnsOption = schemaTargetNamespaceOption
      EName(tnsOption, nameAttributeValue)
    }

    /**
     * Returns the optional substitution group (as EName).
     */
    final def substitutionGroupOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.SubstitutionGroupEName)
    }

    // TODO Other query methods
  }

  /**
   * Local element declaration.
   */
  trait LocalElementDeclaration extends ElementDeclaration with Particle {
    // TODO
  }

  /**
   * Element reference.
   */
  trait ElementReference extends ElementDeclarationOrReference with Reference {
    // TODO
  }

  trait AttributeDeclarationOrReference extends ElemInXsNamespace {
    // TODO
  }

  trait AttributeDeclaration extends AttributeDeclarationOrReference with NamedDeclOrDef {
    // TODO

    /**
     * Returns the optional type attribute (as EName). This may fail with an exception if the taxonomy is not schema-valid.
     */
    final def typeOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.TypeEName)
    }
  }

  /**
   * Global attribute declaration.
   */
  trait GlobalAttributeDeclaration extends AttributeDeclaration {

    /**
     * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
     * name attribute as local part.
     */
    final def targetEName: EName = {
      val tnsOption = schemaTargetNamespaceOption
      EName(tnsOption, nameAttributeValue)
    }

    // TODO Other query methods
  }

  /**
   * Local attribute declaration.
   */
  trait LocalAttributeDeclaration extends AttributeDeclaration {
    // TODO
  }

  /**
   * Attribute reference.
   */
  trait AttributeReference extends AttributeDeclarationOrReference with Reference {
    // TODO
  }

  trait TypeDefinition extends ElemInXsNamespace {
    // TODO
  }

  trait NamedTypeDefinition extends TypeDefinition with NamedDeclOrDef {
    // TODO
  }

  trait AnonymousTypeDefinition extends TypeDefinition {
    // TODO
  }

  trait SimpleTypeDefinition extends TypeDefinition {
    // TODO
  }

  trait ComplexTypeDefinition extends TypeDefinition {
    // TODO
  }

  trait NamedSimpleTypeDefinition extends SimpleTypeDefinition with NamedTypeDefinition {
    // TODO
  }

  trait AnonymousSimpleTypeDefinition extends SimpleTypeDefinition with AnonymousTypeDefinition {
    // TODO
  }

  trait NamedComplexTypeDefinition extends ComplexTypeDefinition with NamedTypeDefinition {
    // TODO
  }

  trait AnonymousComplexTypeDefinition extends ComplexTypeDefinition with AnonymousTypeDefinition {
    // TODO
  }

  trait AttributeGroupDefinitionOrReference extends ElemInXsNamespace {
    // TODO
  }

  trait AttributeGroupDefinition extends AttributeGroupDefinitionOrReference with NamedDeclOrDef {
    // TODO
  }

  trait AttributeGroupReference extends AttributeGroupDefinitionOrReference with Reference {
    // TODO
  }

  trait ModelGroupDefinitionOrReference extends ElemInXsNamespace {
    // TODO
  }

  trait ModelGroupDefinition extends ModelGroupDefinitionOrReference {
    // TODO
  }

  trait ModelGroupReference extends ModelGroupDefinitionOrReference with Reference {
    // TODO
  }

  trait ModelGroup extends ElemInXsNamespace {
    // TODO
  }

  trait SequenceModelGroup extends ModelGroup {
    // TODO
  }

  trait ChoiceModelGroup extends ModelGroup {
    // TODO
  }

  trait AllModelGroup extends ModelGroup {
    // TODO
  }

  trait RestrictionOrExtension extends ElemInXsNamespace {
    // TODO
  }

  trait Restriction extends RestrictionOrExtension {
    // TODO
  }

  trait Extension extends RestrictionOrExtension {
    // TODO
  }

  trait Content extends ElemInXsNamespace {
    // TODO
  }

  trait SimpleContent extends Content {
    // TODO
  }

  trait ComplexContent extends Content {
    // TODO
  }

  trait Annotation extends ElemInXsNamespace {
    // TODO
  }

  trait Appinfo extends ElemInXsNamespace {
    // TODO
  }

  trait Import extends ElemInXsNamespace {
    // TODO
  }

  // No redefines (and if possible no include either)
}
