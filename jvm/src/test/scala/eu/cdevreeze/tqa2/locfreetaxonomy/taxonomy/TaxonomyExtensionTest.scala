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

package eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.common.BaseSetKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.InterConceptRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyExtensionTest.InterConceptRelKey
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult
import eu.cdevreeze.tqa2.validate.Validator
import eu.cdevreeze.tqa2.validate.rules._
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite

/**
 * Test of extending taxonomies by "extending" entrypoints. Note that the same mechanism can be used for layering
 * taxonomies, because layering and extending are technically the same.
 *
 * @author Chris de Vreeze
 */
class TaxonomyExtensionTest extends AnyFunSuite {

  // TODO Efficiently (and safely) extend a BasicTaxonomy

  test("TQA should be able to successfully extend an entrypoint") {
    val entrypointUri1 = URI.create("http://www.nltaxonomie.nl/nt12/venj/20170714.a/custom-entrypoints/custom-entrypoint1.xsd")

    val taxo1: BasicTaxonomy = TestResourceUtil.buildTaxonomyFromClasspath(entrypointUri1, URI.create("testfiles/"), processor)

    assertResult(true) {
      taxo1.relationships.sizeIs >= 100
    }

    assertResult(Nil) {
      validate(taxo1)
    }

    // Extending the entrypoint above by "extending" it with an extension taxonomy entrypoint.

    val entrypointUri2 = URI.create("http://www.nltaxonomie.nl/nt12/rj/20170714.a/custom-entrypoints/custom-extension-entrypoint1.xsd")

    val entrypoint: Set[URI] = Set(entrypointUri1, entrypointUri2)

    val taxo2: BasicTaxonomy = TestResourceUtil.buildTaxonomyFromClasspath(entrypoint, URI.create("testfiles/"), processor)

    assertResult(true) {
      checkEntrypoint(entrypoint, taxo2)
    }

    assertResult(true) {
      taxo2.relationships.size > taxo1.relationships.size
    }

    def isRjPresentationLinkbaseUri(uri: URI): Boolean = {
      uri.toString.contains("rj-annual-reporting-guidelines")
    }

    assertResult(taxo1.relationships.size) {
      taxo2.relationships.filterNot(rel => isRjPresentationLinkbaseUri(rel.docUri)).size
    }

    assertResult(Nil) {
      validate(taxo2)
    }

    assertResult(taxo1.findAllDimensionalRelationships.map(InterConceptRelKey.from).toSet) {
      taxo2.findAllDimensionalRelationships.map(InterConceptRelKey.from).toSet
    }

    assertResult(taxo1.findAllParentChildRelationships.map(InterConceptRelKey.from).toSet) {
      taxo2.filterParentChildRelationships(rel => !isRjPresentationLinkbaseUri(rel.docUri)).map(InterConceptRelKey.from).toSet
    }
  }

  private def validate(taxo: BasicTaxonomy): Seq[ValidationResult] = {
    val validations: Seq[Validation] = XLinkValidations.all
      .appendedAll(SchemaValidations.all)
      .appendedAll(TaxoDocumentValidations.all)
      .appendedAll(TaxoElemKeyValidations.all)
      .appendedAll(NamespaceValidations.all)
      .appendedAll(EntrypointSchemaValidations.all.filterNot(Set(EntrypointSchemaValidations.NotAClosedSchemaSet)))

    val validationResults: Seq[ValidationResult] = Validator.validate(taxo, validations)
    validationResults
  }

  /**
   * Checks that in a multi-document entrypoint an xs:import without schemaLocation has a namespace that is the
   * target namespace of another schema document in that entrypoint.
   */
  private def checkEntrypoint(entrypoint: Set[URI], taxo: BasicTaxonomy): Boolean = {
    val entrypointSchemas: Seq[XsSchema] =
      taxo.taxonomyBase.rootElemMap.view.filterKeys(entrypoint).values.toSeq.collect { case e: XsSchema => e }

    val unconnectedImportedNamespaces: Set[String] = entrypointSchemas.flatMap { e =>
      e.findAllImports.filter(_.attrOption(ENames.SchemaLocationEName).isEmpty).map(_.attr(ENames.NamespaceEName))
    }.toSet

    val targetNamespaces: Set[String] = entrypointSchemas.flatMap(_.targetNamespaceOption).toSet

    unconnectedImportedNamespaces.diff(targetNamespaces).isEmpty
  }

  private val processor = new Processor(false)
}

object TaxonomyExtensionTest {

  private final case class InterConceptRelKey(source: EName, target: EName, baseSet: BaseSetKey)

  private object InterConceptRelKey {

    def from(relationship: InterConceptRelationship): InterConceptRelKey = {
      InterConceptRelKey(relationship.sourceConcept, relationship.targetConcept, relationship.baseSetKey)
    }
  }
}
