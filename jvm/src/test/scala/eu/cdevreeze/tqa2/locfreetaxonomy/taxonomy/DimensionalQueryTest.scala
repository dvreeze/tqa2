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
import eu.cdevreeze.tqa2.locfreetaxonomy.DimensionalConformanceSuiteUtil
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.Validator
import eu.cdevreeze.tqa2.validate.rules.EntrypointSchemaValidations
import eu.cdevreeze.tqa2.validate.rules.SchemaValidations
import eu.cdevreeze.tqa2.validate.rules.TaxoDocumentValidations
import eu.cdevreeze.tqa2.validate.rules.TaxoElemKeyValidations
import eu.cdevreeze.tqa2.validate.rules.XLinkValidations
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Dimensional querying test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
class DimensionalQueryTest extends AnyFunSuite {

  test("TQA should be able to query for (abstract) hypercube declarations") {
    val taxo = makeDts("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeValid.xsd")

    val hypercubeName: EName = EName.parse("{http://www.xbrl.org/dim/conf/100/hypercubeValid}MyHypercube")
    val hypercube = taxo.getHypercubeDeclaration(hypercubeName)

    hypercube.targetEName should be(hypercubeName)

    hypercube.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtHypercubeItemEName, taxo.substitutionGroupMap) should be(true)

    hypercube.isAbstract should be(true)

    hypercube.globalElementDeclaration.isAbstract should be(true)
  }

  test("TQA should be able to query for (non-abstract) hypercube declarations") {
    val taxo = makeDts("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeNotAbstract.xsd")

    val hypercubeName: EName = EName.parse("{http://www.xbrl.org/dim/conf/100/hypercubeNotAbstract}MyHypercube")
    val hypercube = taxo.getHypercubeDeclaration(hypercubeName)

    hypercube.targetEName should be(hypercubeName)

    hypercube.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtHypercubeItemEName, taxo.substitutionGroupMap) should be(true)

    hypercube.isAbstract should be(false)

    hypercube.globalElementDeclaration.isAbstract should be(false)
  }

  test("TQA should be able to query for hypercube declarations with substitution group complexities") {
    val taxo = makeDts("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeNotAbstractWithSGComplexities.xsd")

    val hypercubeName: EName = EName.parse("{http://www.xbrl.org/dim/conf/100/hypercubeNotAbstract}MyHypercube")
    val otherHypercubeName: EName = EName.parse("{http://www.xbrl.org/dim/conf/100/hypercubeNotAbstract}MyOtherHypercube")

    val hypercube = taxo.getHypercubeDeclaration(hypercubeName)
    val otherHypercube = taxo.getHypercubeDeclaration(otherHypercubeName)

    hypercube.targetEName should be(hypercubeName)

    otherHypercube.targetEName should be(otherHypercubeName)

    otherHypercube.substitutionGroupOption should be(Some(hypercubeName))

    hypercube.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtHypercubeItemEName, taxo.substitutionGroupMap) should be(true)

    otherHypercube.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtHypercubeItemEName, taxo.substitutionGroupMap) should be(true)

    hypercube.isAbstract should be(true)
    otherHypercube.isAbstract should be(false)
    hypercube.globalElementDeclaration.isAbstract should be(true)
    otherHypercube.globalElementDeclaration.isAbstract should be(false)
  }

  private val processor = new Processor(false)

  private def makeDts(relativeFilePath: String): BasicTaxonomy = {
    DimensionalConformanceSuiteUtil
      .makeTestDts(Seq(URI.create(relativeFilePath)), processor)
      .ensuring(taxo => Validator.validate(taxo, getValidations(relativeFilePath)).isEmpty)
  }

  private def getValidations(relativeFilePath: String): Seq[Validation] =
    XLinkValidations.all
      .appendedAll(SchemaValidations.all)
      .appendedAll(TaxoDocumentValidations.all)
      .appendedAll(TaxoElemKeyValidations.all)
      .appendedAll(
        EntrypointSchemaValidations.all(DimensionalConformanceSuiteUtil.findAllUrisOfEntrypoint(Seq(URI.create(relativeFilePath)))))
}
