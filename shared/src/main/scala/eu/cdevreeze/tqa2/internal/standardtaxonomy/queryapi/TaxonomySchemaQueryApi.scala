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

import eu.cdevreeze.tqa2.internal.standardtaxonomy.dom._
import eu.cdevreeze.yaidom2.core.EName

/**
 * Purely abstract trait offering an XBRL taxonomy schema query API.
 *
 * @author Chris de Vreeze
 */
trait TaxonomySchemaQueryApi extends SchemaQueryApi {

  // Concept declarations, across documents

  def findAllConceptDeclarations: Seq[ConceptDeclaration]

  def filterConceptDeclarations(p: ConceptDeclaration => Boolean): Seq[ConceptDeclaration]

  def findConceptDeclaration(p: ConceptDeclaration => Boolean): Option[ConceptDeclaration]

  def findConceptDeclaration(ename: EName): Option[ConceptDeclaration]

  def getConceptDeclaration(ename: EName): ConceptDeclaration

  // Item declarations, across documents

  def findAllItemDeclarations: Seq[ItemDeclaration]

  def filterItemDeclarations(p: ItemDeclaration => Boolean): Seq[ItemDeclaration]

  def findItemDeclaration(p: ItemDeclaration => Boolean): Option[ItemDeclaration]

  def findItemDeclaration(ename: EName): Option[ItemDeclaration]

  def getItemDeclaration(ename: EName): ItemDeclaration

  // Tuple declarations, across documents

  def findAllTupleDeclarations: Seq[TupleDeclaration]

  def filterTupleDeclarations(p: TupleDeclaration => Boolean): Seq[TupleDeclaration]

  def findTupleDeclaration(p: TupleDeclaration => Boolean): Option[TupleDeclaration]

  def findTupleDeclaration(ename: EName): Option[TupleDeclaration]

  def getTupleDeclaration(ename: EName): TupleDeclaration

  // Primary item declarations, across documents

  def findAllPrimaryItemDeclarations: Seq[PrimaryItemDeclaration]

  def filterPrimaryItemDeclarations(p: PrimaryItemDeclaration => Boolean): Seq[PrimaryItemDeclaration]

  def findPrimaryItemDeclaration(p: PrimaryItemDeclaration => Boolean): Option[PrimaryItemDeclaration]

  def findPrimaryItemDeclaration(ename: EName): Option[PrimaryItemDeclaration]

  def getPrimaryItemDeclaration(ename: EName): PrimaryItemDeclaration

  // Hypercube declarations, across documents

  def findAllHypercubeDeclarations: Seq[HypercubeDeclaration]

  def filterHypercubeDeclarations(p: HypercubeDeclaration => Boolean): Seq[HypercubeDeclaration]

  def findHypercubeDeclaration(p: HypercubeDeclaration => Boolean): Option[HypercubeDeclaration]

  def findHypercubeDeclaration(ename: EName): Option[HypercubeDeclaration]

  def getHypercubeDeclaration(ename: EName): HypercubeDeclaration

  // Dimension declarations, across documents

  def findAllDimensionDeclarations: Seq[DimensionDeclaration]

  def filterDimensionDeclarations(p: DimensionDeclaration => Boolean): Seq[DimensionDeclaration]

  def findDimensionDeclaration(p: DimensionDeclaration => Boolean): Option[DimensionDeclaration]

  def findDimensionDeclaration(ename: EName): Option[DimensionDeclaration]

  def getDimensionDeclaration(ename: EName): DimensionDeclaration

  // Explicit dimension declarations, across documents

  def findAllExplicitDimensionDeclarations: Seq[ExplicitDimensionDeclaration]

  def filterExplicitDimensionDeclarations(p: ExplicitDimensionDeclaration => Boolean): Seq[ExplicitDimensionDeclaration]

  def findExplicitDimensionDeclaration(p: ExplicitDimensionDeclaration => Boolean): Option[ExplicitDimensionDeclaration]

  def findExplicitDimensionDeclaration(ename: EName): Option[ExplicitDimensionDeclaration]

  def getExplicitDimensionDeclaration(ename: EName): ExplicitDimensionDeclaration

  // Typed dimension declarations, across documents

  def findAllTypedDimensionDeclarations: Seq[TypedDimensionDeclaration]

  def filterTypedDimensionDeclarations(p: TypedDimensionDeclaration => Boolean): Seq[TypedDimensionDeclaration]

  def findTypedDimensionDeclaration(p: TypedDimensionDeclaration => Boolean): Option[TypedDimensionDeclaration]

  def findTypedDimensionDeclaration(ename: EName): Option[TypedDimensionDeclaration]

  def getTypedDimensionDeclaration(ename: EName): TypedDimensionDeclaration

  // Typed dimension member declarations

  def findMemberDeclarationOfTypedDimension(typedDimension: EName): Option[GlobalElementDeclaration]

  def getMemberDeclarationOfTypedDimension(typedDimension: EName): GlobalElementDeclaration
}
