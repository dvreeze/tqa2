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

package eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.GlobalAttributeDeclaration
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NamedComplexTypeDefinition
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NamedGlobalSchemaComponent
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NamedSimpleTypeDefinition
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NamedTypeDefinition
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.SchemaQueryApi
import eu.cdevreeze.yaidom2.core.EName

/**
 * Partial implementation of SchemaQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.methods
trait DefaultSchemaQueryApi extends SchemaQueryApi {

  // The following abstract methods must be implemented as val fields

  /**
   * All document elements in the taxonomy
   */
  def rootElems: Seq[TaxonomyElem]

  /**
   * Mapping from target ENames to the corresponding named global schema components
   */
  def namedGlobalSchemaComponentMap: Map[EName, Seq[NamedGlobalSchemaComponent]]

  // Schema root elements

  def findAllXsdSchemas: Seq[XsSchema] = {
    rootElems.flatMap(_.findTopmostElemsOrSelf(_.isRootElement)).flatMap {
      case e: XsSchema => Seq(e)
      case _ => Seq.empty
    }
  }

  def filterXsdSchemas(p: XsSchema => Boolean): Seq[XsSchema] = {
    findAllXsdSchemas.filter(p)
  }

  def findXsdSchema(p: XsSchema => Boolean): Option[XsSchema] = {
    filterXsdSchemas(p).headOption
  }

  // Known substitution groups

  def substitutionGroupMap: SubstitutionGroupMap

  // Global element declarations, across documents

  def findAllGlobalElementDeclarations: Seq[GlobalElementDeclaration] = {
    findAllXsdSchemas.flatMap(_.findAllGlobalElementDeclarations())
  }

  def filterGlobalElementDeclarations(p: GlobalElementDeclaration => Boolean): Seq[GlobalElementDeclaration] = {
    findAllXsdSchemas.flatMap(_.filterGlobalElementDeclarations(p))
  }

  def filterGlobalElementDeclarationsOnOwnSubstitutionGroup(p: EName => Boolean): Seq[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(e => e.substitutionGroupOption.exists(sg => p(sg)))
  }

  def filterGlobalElementDeclarationsOnOwnOrInheritedSubstitutionGroup(sg: EName): Seq[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(e => e.hasSubstitutionGroup(sg, substitutionGroupMap))
  }

  def findGlobalElementDeclaration(p: GlobalElementDeclaration => Boolean): Option[GlobalElementDeclaration] = {
    filterGlobalElementDeclarations(p).headOption
  }

  def findGlobalElementDeclaration(ename: EName): Option[GlobalElementDeclaration] = {
    namedGlobalSchemaComponentMap.getOrElse(ename, Seq.empty).collectFirst { case e: GlobalElementDeclaration => e }
  }

  def getGlobalElementDeclaration(ename: EName): GlobalElementDeclaration = {
    findGlobalElementDeclaration(ename).getOrElse(sys.error(s"Missing global element declaration for expanded name $ename"))
  }

  def findNamedTypeOfGlobalElementDeclaration(ename: EName): Option[EName] = {
    val elemDeclOption: Option[GlobalElementDeclaration] = findGlobalElementDeclaration(ename)

    elemDeclOption.flatMap(_.typeOption).orElse {
      elemDeclOption.flatMap(_.substitutionGroupOption).flatMap { sg =>
        // Recursive call
        findNamedTypeOfGlobalElementDeclaration(sg)
      }
    }
  }

  // Global attribute declarations, across documents

  def findAllGlobalAttributeDeclarations: Seq[GlobalAttributeDeclaration] = {
    findAllXsdSchemas.flatMap(_.findAllGlobalAttributeDeclarations())
  }

  def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclaration => Boolean): Seq[GlobalAttributeDeclaration] = {
    findAllXsdSchemas.flatMap(_.filterGlobalAttributeDeclarations(p))
  }

  def findGlobalAttributeDeclaration(p: GlobalAttributeDeclaration => Boolean): Option[GlobalAttributeDeclaration] = {
    filterGlobalAttributeDeclarations(p).headOption
  }

  def findGlobalAttributeDeclaration(ename: EName): Option[GlobalAttributeDeclaration] = {
    namedGlobalSchemaComponentMap.getOrElse(ename, Seq.empty).collectFirst { case e: GlobalAttributeDeclaration => e }
  }

  def getGlobalAttributeDeclaration(ename: EName): GlobalAttributeDeclaration = {
    findGlobalAttributeDeclaration(ename).getOrElse(sys.error(s"Missing global attribute declaration for expanded name $ename"))
  }

  // Named type definitions, across documents

  def findAllNamedTypeDefinitions: Seq[NamedTypeDefinition] = {
    findAllXsdSchemas.flatMap(_.findAllNamedTypeDefinitions())
  }

  def filterNamedTypeDefinitions(p: NamedTypeDefinition => Boolean): Seq[NamedTypeDefinition] = {
    findAllXsdSchemas.flatMap(_.filterNamedTypeDefinitions(p))
  }

  def findNamedTypeDefinition(p: NamedTypeDefinition => Boolean): Option[NamedTypeDefinition] = {
    filterNamedTypeDefinitions(p).headOption
  }

  def findNamedTypeDefinition(ename: EName): Option[NamedTypeDefinition] = {
    namedGlobalSchemaComponentMap.getOrElse(ename, Seq.empty).collectFirst { case e: NamedTypeDefinition => e }
  }

  def getNamedTypeDefinition(ename: EName): NamedTypeDefinition = {
    findNamedTypeDefinition(ename).getOrElse(sys.error(s"Missing named type definition for expanded name $ename"))
  }

  // Named complex type definitions, across documents

  def findAllNamedComplexTypeDefinitions: Seq[NamedComplexTypeDefinition] = {
    findAllNamedTypeDefinitions.collect { case e: NamedComplexTypeDefinition => e }
  }

  def filterNamedComplexTypeDefinitions(p: NamedComplexTypeDefinition => Boolean): Seq[NamedComplexTypeDefinition] = {
    findAllNamedTypeDefinitions.collect { case e: NamedComplexTypeDefinition if p(e) => e }
  }

  def findNamedComplexTypeDefinition(p: NamedComplexTypeDefinition => Boolean): Option[NamedComplexTypeDefinition] = {
    findAllNamedComplexTypeDefinitions.find(p)
  }

  def findNamedComplexTypeDefinition(ename: EName): Option[NamedComplexTypeDefinition] = {
    namedGlobalSchemaComponentMap.getOrElse(ename, Seq.empty).collectFirst { case e: NamedComplexTypeDefinition => e }
  }

  def getNamedComplexTypeDefinition(ename: EName): NamedComplexTypeDefinition = {
    findNamedComplexTypeDefinition(ename).getOrElse(sys.error(s"Missing named complex type definition for expanded name $ename"))
  }

  // Named simple type definitions, across documents

  def findAllNamedSimpleTypeDefinitions: Seq[NamedSimpleTypeDefinition] = {
    findAllNamedTypeDefinitions.collect { case e: NamedSimpleTypeDefinition => e }
  }

  def filterNamedSimpleTypeDefinitions(p: NamedSimpleTypeDefinition => Boolean): Seq[NamedSimpleTypeDefinition] = {
    findAllNamedTypeDefinitions.collect { case e: NamedSimpleTypeDefinition if p(e) => e }
  }

  def findNamedSimpleTypeDefinition(p: NamedSimpleTypeDefinition => Boolean): Option[NamedSimpleTypeDefinition] = {
    findAllNamedSimpleTypeDefinitions.find(p)
  }

  def findNamedSimpleTypeDefinition(ename: EName): Option[NamedSimpleTypeDefinition] = {
    namedGlobalSchemaComponentMap.getOrElse(ename, Seq.empty).collectFirst { case e: NamedSimpleTypeDefinition => e }
  }

  def getNamedSimpleTypeDefinition(ename: EName): NamedSimpleTypeDefinition = {
    findNamedSimpleTypeDefinition(ename).getOrElse(sys.error(s"Missing named simple type definition for expanded name $ename"))
  }

  // Finding ancestry of types, across documents

  def findBaseTypeOrSelfUntil(typeEName: EName, p: EName => Boolean): Option[EName] = {
    if (p(typeEName)) {
      Some(typeEName)
    } else {
      val typeDefinitionOption = findNamedTypeDefinition(typeEName)

      val baseTypeOption = typeDefinitionOption.flatMap(_.baseTypeOption)

      // Recursive call
      baseTypeOption.flatMap(baseType => findBaseTypeOrSelfUntil(baseType, p))
    }
  }
}
