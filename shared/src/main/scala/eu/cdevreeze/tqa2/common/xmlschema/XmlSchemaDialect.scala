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
import eu.cdevreeze.yaidom2.queryapi.named
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * XML Schema dialect. All elements in this dialect are in the "xs" namespace.
 *
 * The elements in this dialect extend the yaidom `BackingElemApi` API and not the more specific `BackingNodes.Elem` API.
 * It is meant to be mixed in by more concrete XML Schema dialects that do extend the `BackingNodes.Elem` API.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.types
// scalastyle:off number.of.methods
object XmlSchemaDialect {

  /**
   * Element in the "XML schema" namespace in a locator-free taxonomy.
   * This type or a sub-type is mixed in by taxonomy elements that are indeed in the XML Schema namespace.
   *
   * It is assumed that the XML Schema content obeys the XML schema of XML Schema itself, or else the query methods below may throw an exception.
   */
  trait Elem extends BackingElemApi {

    type GlobalElementDeclarationType <: GlobalElementDeclaration

    type GlobalAttributeDeclarationType <: GlobalAttributeDeclaration

    type NamedTypeDefinitionType <: NamedTypeDefinition

    /**
     * Returns the optional target namespace of the surrounding schema root element (or self), ignoring the possibility that
     * this is an included chameleon schema.
     */
    final def schemaTargetNamespaceOption: Option[String] = {
      findAncestorElemOrSelf(_.name == ENames.XsSchemaEName).flatMap(_.attrOption(ENames.TargetNamespaceEName))
    }
  }

  // Traits that are more general than specific schema components

  /**
   * Super-type of schema components that can be abstract.
   */
  trait CanBeAbstract extends Elem {

    /**
     * Returns the boolean "abstract" attribute (defaulting to false).
     */
    final def isAbstract: Boolean = {
      attrOption(ENames.AbstractEName).exists(v => XsBooleans.parseBoolean(v))
    }

    final def isConcrete: Boolean = {
      !isAbstract
    }
  }

  /**
   * Super-type of schema components that have a name attribute.
   */
  trait NamedDeclOrDef extends Elem {

    /**
     * Returns the "name" attribute.
     */
    final def nameAttributeValue: String = {
      attr(ENames.NameEName)
    }
  }

  /**
   * Super-type of top-level schema components that have a name attribute and optional namespace (in the root element).
   */
  trait NamedGlobalDeclOrDef extends NamedDeclOrDef {

    def schemaTargetNamespaceOption: Option[String]

    /**
     * Returns the "target EName". That is, returns the EName composed of the optional target namespace and the
     * name attribute as local part.
     */
    final def targetEName: EName = EName(schemaTargetNamespaceOption, nameAttributeValue)
  }

  /**
   * Super-type of schema components that are references.
   */
  trait Reference extends Elem {

    /**
     * Returns the "ref" attribute as EName.
     */
    final def ref: EName = {
      attrAsResolvedQName(ENames.RefEName)
    }
  }

  trait Particle extends Elem {

    /**
     * The minOccurs attribute as integer, defaulting to 1.
     */
    final def minOccurs: Int = {
      attrOption(ENames.MinOccursEName).getOrElse("1").toInt
    }

    /**
     * The maxOccurs attribute as optional integer, defaulting to 1, but returning None if unbounded.
     */
    final def maxOccursOption: Option[Int] = {
      attrOption(ENames.MaxOccursEName) match {
        case Some("unbounded") => None
        case Some(i) => Some(i.toInt)
        case None => Some(1)
      }
    }
  }

  // The schema root element itself

  trait XsSchema extends Elem {

    /**
     * Returns the optional target namespace of this schema root element, ignoring the possibility that
     * this is an included chameleon schema.
     */
    final def targetNamespaceOption: Option[String] = schemaTargetNamespaceOption

    def findAllImports: Seq[Import]

    def filterGlobalElementDeclarations(p: GlobalElementDeclarationType => Boolean): Seq[GlobalElementDeclarationType]

    def findAllGlobalElementDeclarations(): Seq[GlobalElementDeclarationType]

    def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclarationType => Boolean): Seq[GlobalAttributeDeclarationType]

    def findAllGlobalAttributeDeclarations(): Seq[GlobalAttributeDeclarationType]

    def filterNamedTypeDefinitions(p: NamedTypeDefinitionType => Boolean): Seq[NamedTypeDefinitionType]

    def findAllNamedTypeDefinitions(): Seq[NamedTypeDefinitionType]
  }

  // Traits that are specific to schema components or parts thereof

  trait ElementDeclarationOrReference extends Elem

  trait ElementDeclaration extends ElementDeclarationOrReference with NamedDeclOrDef {

    /**
     * Returns the optional type attribute (as EName).
     */
    final def typeOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.TypeEName)
    }
  }

  /**
   * Global element declaration. Often a concept declaration, although in general the DOM element has not enough context
   * to determine that in isolation.
   */
  trait GlobalElementDeclaration extends ElementDeclaration with NamedGlobalDeclOrDef with CanBeAbstract {

    /**
     * Returns the optional substitution group (as EName).
     */
    final def substitutionGroupOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.SubstitutionGroupEName)
    }
  }

  /**
   * Local element declaration.
   */
  trait LocalElementDeclaration extends ElementDeclaration with Particle

  /**
   * Element reference.
   */
  trait ElementReference extends ElementDeclarationOrReference with Reference

  trait AttributeDeclarationOrReference extends Elem

  trait AttributeDeclaration extends AttributeDeclarationOrReference with NamedDeclOrDef {

    /**
     * Returns the optional type attribute (as EName).
     */
    final def typeOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.TypeEName)
    }
  }

  /**
   * Global attribute declaration.
   */
  trait GlobalAttributeDeclaration extends AttributeDeclaration with NamedGlobalDeclOrDef

  /**
   * Local attribute declaration.
   */
  trait LocalAttributeDeclaration extends AttributeDeclaration

  /**
   * Attribute reference.
   */
  trait AttributeReference extends AttributeDeclarationOrReference with Reference

  trait TypeDefinition extends Elem {

    def isSimpleType: Boolean

    final def isComplexType: Boolean = !isSimpleType

    /**
     * Returns the base type of this type, as EName, if any, wrapped in an Option.
     * If defined, this type is then a restriction or extension of that base type.
     *
     * For type xs:anyType, None is returned. For union and list types, None is returned as well.
     *
     * For simple types, derivation (from the base type) is always by restriction.
     */
    def baseTypeOption: Option[EName]
  }

  trait NamedTypeDefinition extends TypeDefinition with NamedGlobalDeclOrDef

  trait AnonymousTypeDefinition extends TypeDefinition

  trait SimpleTypeDefinition extends TypeDefinition {

    final def isSimpleType: Boolean = true
  }

  trait ComplexTypeDefinition extends TypeDefinition {

    final def isSimpleType: Boolean = false

    final def contentElemOption: Option[Content] = {
      val complexContentOption = filterChildElems(named(ENames.XsComplexContentEName)).collectFirst { case e: ComplexContent => e }
      val simpleContentOption = filterChildElems(named(ENames.XsSimpleContentEName)).collectFirst { case e: SimpleContent => e }

      complexContentOption.orElse(simpleContentOption)
    }

    /**
     * Returns the optional base type.
     */
    final def baseTypeOption: Option[EName] = {
      contentElemOption.flatMap(_.baseTypeOption).orElse(Some(ENames.XsAnyTypeEName))
    }
  }

  trait NamedSimpleTypeDefinition extends SimpleTypeDefinition with NamedTypeDefinition

  trait AnonymousSimpleTypeDefinition extends SimpleTypeDefinition with AnonymousTypeDefinition

  trait NamedComplexTypeDefinition extends ComplexTypeDefinition with NamedTypeDefinition

  trait AnonymousComplexTypeDefinition extends ComplexTypeDefinition with AnonymousTypeDefinition

  trait AttributeGroupDefinitionOrReference extends Elem

  trait AttributeGroupDefinition extends AttributeGroupDefinitionOrReference with NamedDeclOrDef

  trait AttributeGroupReference extends AttributeGroupDefinitionOrReference with Reference

  trait ModelGroupDefinitionOrReference extends Elem

  trait ModelGroupDefinition extends ModelGroupDefinitionOrReference

  trait ModelGroupReference extends ModelGroupDefinitionOrReference with Reference

  trait ModelGroup extends Elem

  trait SequenceModelGroup extends ModelGroup

  trait ChoiceModelGroup extends ModelGroup

  trait AllModelGroup extends ModelGroup

  trait RestrictionOrExtension extends Elem {

    /**
     * Returns the optional base type.
     */
    def baseTypeOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.BaseEName)
    }
  }

  trait Restriction extends RestrictionOrExtension

  trait Extension extends RestrictionOrExtension

  trait Content extends Elem {

    /**
     * Returns the derivation. This may fail with an exception if the taxonomy is not schema-valid.
     */
    final def derivation: RestrictionOrExtension = {
      val restrictionOption = filterChildElems(named(ENames.XsRestrictionEName)).collectFirst { case e: Restriction => e }
      val extensionOption = filterChildElems(named(ENames.XsExtensionEName)).collectFirst { case e: Extension => e }

      restrictionOption.
        orElse(extensionOption).
        getOrElse(sys.error(s"Expected xs:restriction or xs:extension child element. Document: $docUri. Element: $name"))
    }

    /**
     * Convenience method to get the base type of the child restriction or extension element.
     */
    final def baseTypeOption: Option[EName] = derivation.baseTypeOption
  }

  trait SimpleContent extends Content

  trait ComplexContent extends Content

  trait Annotation extends Elem

  trait Appinfo extends Elem

  trait Import extends Elem

  // No redefines (and if possible no include either)
}
