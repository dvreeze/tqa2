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

import java.io.File

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElemTest
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor
import org.scalatest.FunSuite

/**
 * Test of creating and querying "taxonomy bases".
 *
 * @author Chris de Vreeze
 */
class TaxonomyBaseTest extends FunSuite {

  test("testCreateSingleDocumentTaxonomyBase") {
    val schema = XsSchema(getTaxonomyElement("/testfiles/kvk-data.xsd").underlyingElem)

    val taxonomyBase: TaxonomyBase = TaxonomyBase.build(Seq(schema), SubstitutionGroupMap.Empty)

    val globalElemDecls = taxonomyBase.findAllGlobalElementDeclarations

    assertResult(8) {
      globalElemDecls.size
    }

    assertResult(globalElemDecls) {
      taxonomyBase.findAllItemDeclarations.map(_.globalElementDeclaration)
    }

    assertResult(globalElemDecls) {
      taxonomyBase.findAllPrimaryItemDeclarations.map(_.globalElementDeclaration)
    }
  }

  private val processor = new Processor(false)

  private def getTaxonomyElement(relativeFilePath: String): TaxonomyElem = {
    val docBuilder = processor.newDocumentBuilder()
    val file = new File(classOf[TaxonomyElemTest].getResource("/" + relativeFilePath.stripPrefix("/")).toURI)
    val doc = docBuilder.build(file)

    val docElem: saxon.Elem = SaxonDocument(doc).documentElement

    TaxonomyElem(docElem)
  }
}
