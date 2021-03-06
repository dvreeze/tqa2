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

package eu.cdevreeze.tqa2.internal.standardtaxonomy.queryapi

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.internal.standardtaxonomy.dom._
import eu.cdevreeze.yaidom2.core.EName

/**
 * Purely abstract trait offering a schema query API.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.methods
trait SchemaQueryApi {

  // Schema root elements

  /**
   * Returns the schema root elements. To find certain taxonomy schema elements, the following pattern can be used:
   * {{{
   * findAllXsdSchemas.flatMap(_.filterElemsOrSelfOfType(classTag[E])(pred))
   * }}}
   */
  def findAllXsdSchemas: Seq[XsSchema]

  /**
   * Returns schema root elements obeying some predicate. To find certain taxonomy schema elements,
   * the following pattern can be used:
   * {{{
   * filterXsdSchemas(p).flatMap(_.filterElemsOrSelfOfType(classTag[E])(pred))
   * }}}
   */
  def filterXsdSchemas(p: XsSchema => Boolean): Seq[XsSchema]

  /**
   * Finds an optional schema root element obeying some predicate. To find certain taxonomy schema elements,
   * the following pattern can be used:
   * {{{
   * findXsdSchema(p).toIndexedSeq.flatMap(_.filterElemsOrSelfOfType(classTag[E])(pred))
   * }}}
   */
  def findXsdSchema(p: XsSchema => Boolean): Option[XsSchema]

  // Known substitution groups

  /**
   * Returns the known substitution groups as SubstitutionGroupMap. If the taxonomy is closed under
   * DTS discovery, these substitution groups are found within the taxonomy. Otherwise they may
   * partly be external.
   *
   * Implementations should store this as a field, in order to make substitution group lookups as
   * fast as possible.
   */
  def substitutionGroupMap: SubstitutionGroupMap

  // Global element declarations, across documents

  def findAllGlobalElementDeclarations: Seq[GlobalElementDeclaration]

  def filterGlobalElementDeclarations(p: GlobalElementDeclaration => Boolean): Seq[GlobalElementDeclaration]

  def filterGlobalElementDeclarationsOnOwnSubstitutionGroup(p: EName => Boolean): Seq[GlobalElementDeclaration]

  def filterGlobalElementDeclarationsOnOwnOrInheritedSubstitutionGroup(sg: EName): Seq[GlobalElementDeclaration]

  def findGlobalElementDeclaration(p: GlobalElementDeclaration => Boolean): Option[GlobalElementDeclaration]

  /**
   * Finds the optional global element declaration with the given target EName, if any. This must be a fast operation,
   * probably backed by a mapping from ENames to global element declarations.
   */
  def findGlobalElementDeclaration(ename: EName): Option[GlobalElementDeclaration]

  /**
   * Returns the equivalent of `findGlobalElementDeclaration(ename).get`, throwing an exception if not found.
   */
  def getGlobalElementDeclaration(ename: EName): GlobalElementDeclaration

  /**
   * Finds the named type of the global element declaration with the given target EName, recursively trying to obtain the
   * type via the substitution group ancestry chain, if needed.
   */
  def findNamedTypeOfGlobalElementDeclaration(ename: EName): Option[EName]

  // Global attribute declarations, across documents

  def findAllGlobalAttributeDeclarations: Seq[GlobalAttributeDeclaration]

  def filterGlobalAttributeDeclarations(p: GlobalAttributeDeclaration => Boolean): Seq[GlobalAttributeDeclaration]

  def findGlobalAttributeDeclaration(p: GlobalAttributeDeclaration => Boolean): Option[GlobalAttributeDeclaration]

  /**
   * Finds the optional global attribute declaration with the given target EName, if any. This must be a fast operation,
   * probably backed by a mapping from ENames to global attribute declarations.
   */
  def findGlobalAttributeDeclaration(ename: EName): Option[GlobalAttributeDeclaration]

  /**
   * Returns the equivalent of `findGlobalAttributeDeclaration(ename).get`, throwing an exception if not found.
   */
  def getGlobalAttributeDeclaration(ename: EName): GlobalAttributeDeclaration

  // Named type definitions, across documents

  def findAllNamedTypeDefinitions: Seq[NamedTypeDefinition]

  def filterNamedTypeDefinitions(p: NamedTypeDefinition => Boolean): Seq[NamedTypeDefinition]

  def findNamedTypeDefinition(p: NamedTypeDefinition => Boolean): Option[NamedTypeDefinition]

  /**
   * Finds the optional named type definition with the given target EName, if any. This must be a fast operation,
   * probably backed by a mapping from ENames to named type definitions.
   */
  def findNamedTypeDefinition(ename: EName): Option[NamedTypeDefinition]

  /**
   * Returns the equivalent of `findNamedTypeDefinition(ename).get`, throwing an exception if not found.
   */
  def getNamedTypeDefinition(ename: EName): NamedTypeDefinition

  // Named complex type definitions, across documents

  def findAllNamedComplexTypeDefinitions: Seq[NamedComplexTypeDefinition]

  def filterNamedComplexTypeDefinitions(p: NamedComplexTypeDefinition => Boolean): Seq[NamedComplexTypeDefinition]

  def findNamedComplexTypeDefinition(p: NamedComplexTypeDefinition => Boolean): Option[NamedComplexTypeDefinition]

  /**
   * Finds the optional named complex type definition with the given target EName, if any. This must be a fast operation,
   * probably backed by a mapping from ENames to named type definitions.
   */
  def findNamedComplexTypeDefinition(ename: EName): Option[NamedComplexTypeDefinition]

  /**
   * Returns the equivalent of `findNamedComplexTypeDefinition(ename).get`, throwing an exception if not found.
   */
  def getNamedComplexTypeDefinition(ename: EName): NamedComplexTypeDefinition

  // Named simple type definitions, across documents

  def findAllNamedSimpleTypeDefinitions: Seq[NamedSimpleTypeDefinition]

  def filterNamedSimpleTypeDefinitions(p: NamedSimpleTypeDefinition => Boolean): Seq[NamedSimpleTypeDefinition]

  def findNamedSimpleTypeDefinition(p: NamedSimpleTypeDefinition => Boolean): Option[NamedSimpleTypeDefinition]

  /**
   * Finds the optional named simple type definition with the given target EName, if any. This must be a fast operation,
   * probably backed by a mapping from ENames to named type definitions.
   */
  def findNamedSimpleTypeDefinition(ename: EName): Option[NamedSimpleTypeDefinition]

  /**
   * Returns the equivalent of `findNamedSimpleTypeDefinition(ename).get`, throwing an exception if not found.
   */
  def getNamedSimpleTypeDefinition(ename: EName): NamedSimpleTypeDefinition

  // Finding ancestry of types, across documents

  /**
   * If the given type obeys the type predicate, returns it, wrapped in an Option.
   * Otherwise, returns the optional base type if that type obeys the type predicate, and so on,
   * until either the predicate holds or no further base type can be found in the taxonomy.
   */
  def findBaseTypeOrSelfUntil(typeEName: EName, p: EName => Boolean): Option[EName]
}
