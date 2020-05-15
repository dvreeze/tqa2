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
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyDocument
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElems
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.yaidom2.node.saxon
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Test of creating and querying "taxonomy bases".
 *
 * @author Chris de Vreeze
 */
class TaxonomyBaseTest extends AnyFunSuite {

  test("TQA should be able to create a single-document TaxonomyBase") {
    val schema =
      XsSchema(getTaxonomyElement(URI.create("testfiles/www.nltaxonomie.nl/nt12/kvk/20170714.a/dictionary/kvk-data.xsd")).underlyingElem)

    val taxonomyBase: TaxonomyBase = TaxonomyBase.build(Seq(TaxonomyDocument(schema)), SubstitutionGroupMap.Empty)

    val globalElemDecls = taxonomyBase.findAllGlobalElementDeclarations

    (globalElemDecls should have).size(8)

    taxonomyBase.findAllItemDeclarations.map(_.globalElementDeclaration) should equal(globalElemDecls)

    taxonomyBase.findAllPrimaryItemDeclarations.map(_.globalElementDeclaration) should equal(globalElemDecls)

    taxonomyBase.findAllPrimaryItemDeclarations.map(_.targetEName.namespaceUriOption).toSet should equal {
      Set(Some("http://www.nltaxonomie.nl/nt12/kvk/20170714.a/dictionary/kvk-data"))
    }

    taxonomyBase.findAllItemDeclarations.map(_.substitutionGroupOption).toSet should equal {
      Set(Some(ENames.XbrliItemEName))
    }
  }

  private val processor = new Processor(false)

  private def getTaxonomyElement(relativeFilePath: URI): TaxonomyElem = {
    val doc: saxon.Document = TestResourceUtil.buildSaxonDocumentFromClasspathResource(relativeFilePath, processor)

    TaxonomyElems.of(doc.documentElement)
  }
}
