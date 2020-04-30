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

import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.common.BaseSetKey
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ConceptLabelRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ElementLabelRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.InterConceptRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyFilteringTest.InterConceptRelKey
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult
import eu.cdevreeze.tqa2.validate.Validator
import eu.cdevreeze.tqa2.validate.rules._
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite

/**
 * Test of filtering taxonomies by filtering entrypoints.
 *
 * @author Chris de Vreeze
 */
class TaxonomyFilteringTest extends AnyFunSuite {

  test("TQA should be able to successfully filter an entrypoint") {
    val entrypointUri1 = URI.create("http://www.nltaxonomie.nl/nt12/venj/20170714.a/custom-entrypoints/custom-entrypoint1.xsd")

    val taxo1: BasicTaxonomy = TestResourceUtil.buildTaxonomyFromClasspath(entrypointUri1, URI.create("testfiles/"), processor)

    assertResult(true) {
      taxo1.relationships.sizeIs >= 100
    }

    assertResult(Nil) {
      validate(taxo1)
    }

    // Take another entrypoint, which is like the one before, but discarding (standard and generic) label linkbases.

    val entrypointUri2 = URI.create("http://www.nltaxonomie.nl/nt12/venj/20170714.a/custom-entrypoints/custom-entrypoint1-no-labels.xsd")

    val taxo2: BasicTaxonomy = TestResourceUtil.buildTaxonomyFromClasspath(entrypointUri2, URI.create("testfiles/"), processor)

    assertResult(true) {
      taxo2.relationships.sizeIs >= 50 && taxo2.relationships.size < 100
    }

    assertResult(Nil) {
      validate(taxo2)
    }

    assertResult(
      taxo1.relationships.filterNot(_.isInstanceOf[ConceptLabelRelationship]).filterNot(_.isInstanceOf[ElementLabelRelationship]).size) {
      taxo2.relationships.size
    }

    assertResult(true) {
      taxo2.findAllDimensionalRelationships.sizeIs >= 5 && taxo2.findAllDimensionalRelationships.size < 10
    }

    assertResult(taxo1.findAllDimensionalRelationships.map(InterConceptRelKey.from).toSet) {
      taxo2.findAllDimensionalRelationships.map(InterConceptRelKey.from).toSet
    }

    assertResult(true) {
      taxo2.findAllParentChildRelationships.sizeIs >= 20 && taxo2.findAllParentChildRelationships.size < 40
    }

    assertResult(taxo1.findAllParentChildRelationships.map(InterConceptRelKey.from).toSet) {
      taxo2.findAllParentChildRelationships.map(InterConceptRelKey.from).toSet
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

  private val processor = new Processor(false)
}

object TaxonomyFilteringTest {

  private final case class InterConceptRelKey(source: EName, target: EName, baseSet: BaseSetKey)

  private object InterConceptRelKey {

    def from(relationship: InterConceptRelationship): InterConceptRelKey = {
      InterConceptRelKey(relationship.sourceConcept, relationship.targetConcept, relationship.baseSetKey)
    }
  }
}
