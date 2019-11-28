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
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
 * Dimensional querying test case. It uses test data from the XBRL Dimensions conformance suite.
 *
 * @author Chris de Vreeze
 */
class DimensionalQueryTest extends FunSuite {

  test("TQA should be able to query for (abstract) hypercube declarations") {
    val taxo = makeDts("100-xbrldte/101-HypercubeElementIsNotAbstractError/hypercubeValid.xsd")

    val hypercubeName: EName = EName.parse("{http://www.xbrl.org/dim/conf/100/hypercubeValid}MyHypercube")
    val hypercube = taxo.getHypercubeDeclaration(hypercubeName)

    hypercube.targetEName should be(hypercubeName)

    hypercube.globalElementDeclaration.hasSubstitutionGroup(ENames.XbrldtHypercubeItemEName, taxo.substitutionGroupMap) should be(true)

    hypercube.isAbstract should be(true)

    hypercube.globalElementDeclaration.isAbstract should be(true)
  }

  private val processor = new Processor(false)

  private def makeDts(relativeFilePath: String): BasicTaxonomy = {
    DimensionalConformanceSuiteUtil.makeTestDts(Seq(URI.create(relativeFilePath)), processor)
  }
}
