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

package eu.cdevreeze.tqa2.locfreetaxonomy.dom

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.node.resolved
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.queryapi.named
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Test of parsing and querying taxonomy elements.
 *
 * @author Chris de Vreeze
 */
class TaxonomyElemTest extends AnyFunSuite {

  test("TQA should be able to parse and query a schema") {
    val schema = XsSchema(getTaxonomyElement(URI.create("testfiles/kvk-data.xsd")).underlyingElem)

    val elemClasses = schema.findAllDescendantElemsOrSelf.map(_.getClass).toSet

    elemClasses should equal(Set(classOf[XsSchema], classOf[GlobalElementDeclaration], classOf[Import]))

    val globalElemDecls = schema.findAllGlobalElementDeclarations()

    globalElemDecls should have size 8

    globalElemDecls.map(e => resolved.Elem.from(e)) should equal {
      schema.filterChildElems(named(ENames.XsElementEName)).map(e => resolved.Elem.from(e))
    }

    val imports = schema.findAllImports

    imports should have size 6

    imports.map(e => resolved.Elem.from(e)) should equal {
      schema.filterChildElems(named(ENames.XsImportEName)).map(e => resolved.Elem.from(e))
    }
  }

  test("TQA should be able to parse and query a standard label linkbase") {
    val linkbase = Linkbase(getTaxonomyElement(URI.create("testfiles/venj-bw2-axes-lab-fr.xml")).underlyingElem)

    val elemClasses = linkbase.findAllDescendantElemsOrSelf.map(_.getClass).toSet

    elemClasses should be {
      Set(classOf[Linkbase], classOf[LabelLink], classOf[ConceptLabelResource], classOf[LabelArc], classOf[ConceptKey])
    }

    val extendedLinks = linkbase.findAllExtendedLinks

    extendedLinks should have size 1

    val labeledResourceMap: Map[String, Seq[XLinkResource]] = extendedLinks.head.labeledXlinkResourceMap

    val venjBw2DimNs = "http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes"

    extendedLinks.head.arcs.filter { arc =>
      labeledResourceMap
        .getOrElse(arc.from, Seq.empty)
        .collect { case k: ConceptKey if k.key == EName(venjBw2DimNs, "ClassesOfDirectorsAndPersonnelAxis") => k }
        .nonEmpty &&
      labeledResourceMap
        .getOrElse(arc.to, Seq.empty)
        .collect { case r: ConceptLabelResource if r.text == "Classes des administrateurs et du personnel [axe]" => r }
        .nonEmpty
    } should have size 1

    extendedLinks.head.xlinkResourceChildren.collect { case k: ConceptKey if k.key.localPart.endsWith("Axis") => k } should equal {
      extendedLinks.head.xlinkResourceChildren.collect { case k: ConceptKey => k }
    }
  }

  private val processor = new Processor(false)

  private def getTaxonomyElement(relativeFilePath: URI): TaxonomyElem = {
    val doc: saxon.Document = TestResourceUtil.buildSaxonDocumentFromClasspathResource(relativeFilePath, processor)

    TaxonomyElem(doc.documentElement)
  }
}
