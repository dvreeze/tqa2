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

package eu.cdevreeze.tqa2.locfreetaxonomy.relationship

import java.net.URI

import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Linkbase
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.node.saxon
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Test of extracting and querying relationhips.
 *
 * @author Chris de Vreeze
 */
class RelationshipTest extends AnyFunSuite {

  test("TQA should be able to extract and query standard label relationships") {
    val linkbase = Linkbase(getTaxonomyElement(
      URI.create("testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-lab-fr.xml")).underlyingElem)

    val relationshipFactory = DefaultRelationshipFactory

    val relationships = relationshipFactory.extractRelationships(Map(linkbase.docUri -> linkbase), RelationshipFactory.AnyArc)

    relationships.forall(_.isInstanceOf[ConceptLabelRelationship]) should equal(true)

    val venjBw2DimNs = "http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes"

    (relationships.collect { case rel: ConceptLabelRelationship => rel }.filter { rel =>
      rel.sourceConcept == EName(venjBw2DimNs, "ClassesOfDirectorsAndPersonnelAxis") &&
      rel.labelText == "Classes des administrateurs et du personnel [axe]"
    } should have).size(1)
  }

  private val processor = new Processor(false)

  private def getTaxonomyElement(relativeFilePath: URI): TaxonomyElem = {
    val doc: saxon.Document = TestResourceUtil.buildSaxonDocumentFromClasspathResource(relativeFilePath, processor)

    TaxonomyElem(doc.documentElement)
  }
}
