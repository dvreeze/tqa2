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

package eu.cdevreeze.tqa2.internal.standardtaxonomy.queryapi.internal

import eu.cdevreeze.tqa2.internal.standardtaxonomy.dom._
import eu.cdevreeze.tqa2.internal.standardtaxonomy.queryapi.TaxonomySchemaQueryApi
import eu.cdevreeze.yaidom2.core.EName

/**
 * Partial implementation of TaxonomySchemaQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultTaxonomySchemaQueryApi extends TaxonomySchemaQueryApi {

  // Abstract methods

  def conceptDeclarations: Seq[ConceptDeclaration]

  def conceptDeclarationsByEName: Map[EName, ConceptDeclaration]

  // Concept declarations, across documents

  def findAllConceptDeclarations: Seq[ConceptDeclaration] = {
    conceptDeclarations
  }

  def filterConceptDeclarations(p: ConceptDeclaration => Boolean): Seq[ConceptDeclaration] = {
    findAllConceptDeclarations.filter(p)
  }

  def findConceptDeclaration(p: ConceptDeclaration => Boolean): Option[ConceptDeclaration] = {
    findAllConceptDeclarations.find(p)
  }

  def findConceptDeclaration(ename: EName): Option[ConceptDeclaration] = {
    conceptDeclarationsByEName.get(ename)
  }

  def getConceptDeclaration(ename: EName): ConceptDeclaration = {
    findConceptDeclaration(ename).getOrElse(sys.error(s"Missing concept declaration for expanded name $ename"))
  }

  // Item declarations, across documents

  def findAllItemDeclarations: Seq[ItemDeclaration] = {
    findAllConceptDeclarations.collect { case decl: ItemDeclaration => decl }
  }

  def filterItemDeclarations(p: ItemDeclaration => Boolean): Seq[ItemDeclaration] = {
    findAllConceptDeclarations.collect { case decl: ItemDeclaration if p(decl) => decl }
  }

  def findItemDeclaration(p: ItemDeclaration => Boolean): Option[ItemDeclaration] = {
    filterItemDeclarations(p).headOption
  }

  def findItemDeclaration(ename: EName): Option[ItemDeclaration] = {
    findConceptDeclaration(ename).collect { case e: ItemDeclaration => e }
  }

  def getItemDeclaration(ename: EName): ItemDeclaration = {
    findItemDeclaration(ename).getOrElse(sys.error(s"Missing item declaration for expanded name $ename"))
  }

  // Tuple declarations, across documents

  def findAllTupleDeclarations: Seq[TupleDeclaration] = {
    findAllConceptDeclarations.collect { case decl: TupleDeclaration => decl }
  }

  def filterTupleDeclarations(p: TupleDeclaration => Boolean): Seq[TupleDeclaration] = {
    findAllConceptDeclarations.collect { case decl: TupleDeclaration if p(decl) => decl }
  }

  def findTupleDeclaration(p: TupleDeclaration => Boolean): Option[TupleDeclaration] = {
    filterTupleDeclarations(p).headOption
  }

  def findTupleDeclaration(ename: EName): Option[TupleDeclaration] = {
    findConceptDeclaration(ename).collect { case e: TupleDeclaration => e }
  }

  def getTupleDeclaration(ename: EName): TupleDeclaration = {
    findTupleDeclaration(ename).getOrElse(sys.error(s"Missing tuple declaration for expanded name $ename"))
  }

  // Primary item declarations, across documents

  def findAllPrimaryItemDeclarations: Seq[PrimaryItemDeclaration] = {
    findAllConceptDeclarations.collect { case decl: PrimaryItemDeclaration => decl }
  }

  def filterPrimaryItemDeclarations(p: PrimaryItemDeclaration => Boolean): Seq[PrimaryItemDeclaration] = {
    findAllConceptDeclarations.collect { case decl: PrimaryItemDeclaration if p(decl) => decl }
  }

  def findPrimaryItemDeclaration(p: PrimaryItemDeclaration => Boolean): Option[PrimaryItemDeclaration] = {
    filterPrimaryItemDeclarations(p).headOption
  }

  def findPrimaryItemDeclaration(ename: EName): Option[PrimaryItemDeclaration] = {
    findConceptDeclaration(ename).collect { case e: PrimaryItemDeclaration => e }
  }

  def getPrimaryItemDeclaration(ename: EName): PrimaryItemDeclaration = {
    findPrimaryItemDeclaration(ename).getOrElse(sys.error(s"Missing primary item declaration for expanded name $ename"))
  }

  // Hypercube declarations, across documents

  def findAllHypercubeDeclarations: Seq[HypercubeDeclaration] = {
    findAllConceptDeclarations.collect { case decl: HypercubeDeclaration => decl }
  }

  def filterHypercubeDeclarations(p: HypercubeDeclaration => Boolean): Seq[HypercubeDeclaration] = {
    findAllConceptDeclarations.collect { case decl: HypercubeDeclaration if p(decl) => decl }
  }

  def findHypercubeDeclaration(p: HypercubeDeclaration => Boolean): Option[HypercubeDeclaration] = {
    filterHypercubeDeclarations(p).headOption
  }

  def findHypercubeDeclaration(ename: EName): Option[HypercubeDeclaration] = {
    findConceptDeclaration(ename).collect { case e: HypercubeDeclaration => e }
  }

  def getHypercubeDeclaration(ename: EName): HypercubeDeclaration = {
    findHypercubeDeclaration(ename).getOrElse(sys.error(s"Missing hypercube declaration for expanded name $ename"))
  }

  // Dimension declarations, across documents

  def findAllDimensionDeclarations: Seq[DimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: DimensionDeclaration => decl }
  }

  def filterDimensionDeclarations(p: DimensionDeclaration => Boolean): Seq[DimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: DimensionDeclaration if p(decl) => decl }
  }

  def findDimensionDeclaration(p: DimensionDeclaration => Boolean): Option[DimensionDeclaration] = {
    filterDimensionDeclarations(p).headOption
  }

  def findDimensionDeclaration(ename: EName): Option[DimensionDeclaration] = {
    findConceptDeclaration(ename).collect { case e: DimensionDeclaration => e }
  }

  def getDimensionDeclaration(ename: EName): DimensionDeclaration = {
    findDimensionDeclaration(ename).getOrElse(sys.error(s"Missing dimension declaration for expanded name $ename"))
  }

  // Explicit dimension declarations, across documents

  def findAllExplicitDimensionDeclarations: Seq[ExplicitDimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: ExplicitDimensionDeclaration => decl }
  }

  def filterExplicitDimensionDeclarations(p: ExplicitDimensionDeclaration => Boolean): Seq[ExplicitDimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: ExplicitDimensionDeclaration if p(decl) => decl }
  }

  def findExplicitDimensionDeclaration(p: ExplicitDimensionDeclaration => Boolean): Option[ExplicitDimensionDeclaration] = {
    filterExplicitDimensionDeclarations(p).headOption
  }

  def findExplicitDimensionDeclaration(ename: EName): Option[ExplicitDimensionDeclaration] = {
    findConceptDeclaration(ename).collect { case e: ExplicitDimensionDeclaration => e }
  }

  def getExplicitDimensionDeclaration(ename: EName): ExplicitDimensionDeclaration = {
    findExplicitDimensionDeclaration(ename).getOrElse(sys.error(s"Missing explicit dimension declaration for expanded name $ename"))
  }

  // Typed dimension declarations, across documents

  def findAllTypedDimensionDeclarations: Seq[TypedDimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: TypedDimensionDeclaration => decl }
  }

  def filterTypedDimensionDeclarations(p: TypedDimensionDeclaration => Boolean): Seq[TypedDimensionDeclaration] = {
    findAllConceptDeclarations.collect { case decl: TypedDimensionDeclaration if p(decl) => decl }
  }

  def findTypedDimensionDeclaration(p: TypedDimensionDeclaration => Boolean): Option[TypedDimensionDeclaration] = {
    filterTypedDimensionDeclarations(p).headOption
  }

  def findTypedDimensionDeclaration(ename: EName): Option[TypedDimensionDeclaration] = {
    findConceptDeclaration(ename).collect { case e: TypedDimensionDeclaration => e }
  }

  def getTypedDimensionDeclaration(ename: EName): TypedDimensionDeclaration = {
    findTypedDimensionDeclaration(ename).getOrElse(sys.error(s"Missing typed dimension declaration for expanded name $ename"))
  }

  // Typed dimension member declarations

  def findMemberDeclarationOfTypedDimension(typedDimension: EName): Option[GlobalElementDeclaration] = {
    val typedDimensionDeclOption = findTypedDimensionDeclaration(typedDimension)

    val typedDomainENameOption = typedDimensionDeclOption.map(_.typedDomainKey)

    typedDomainENameOption.flatMap(en => findGlobalElementDeclaration(en))
  }

  def getMemberDeclarationOfTypedDimension(typedDimension: EName): GlobalElementDeclaration = {
    findMemberDeclarationOfTypedDimension(typedDimension)
      .getOrElse(sys.error(s"Missing member declaration for typed dimension $typedDimension"))
  }
}
