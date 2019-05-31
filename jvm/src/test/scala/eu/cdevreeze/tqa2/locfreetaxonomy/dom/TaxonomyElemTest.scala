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

import java.io.File

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.node.resolved
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.queryapi.named
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite

/**
 * Test of parsing and querying taxonomy elements.
 *
 * @author Chris de Vreeze
 */
class TaxonomyElemTest extends AnyFunSuite {

  test("testParseAndQuerySchema") {
    val schema = XsSchema(getTaxonomyElement("/testfiles/kvk-data.xsd").underlyingElem)

    val elemClasses = schema.findAllDescendantElemsOrSelf().map(_.getClass).toSet

    assertResult(Set(classOf[XsSchema], classOf[GlobalElementDeclaration], classOf[Import])) {
      elemClasses
    }

    val globalElemDecls = schema.findAllGlobalElementDeclarations()

    assertResult(8) {
      globalElemDecls.size
    }

    assertResult(schema.filterChildElems(named(ENames.XsElementEName)).map(e => resolved.Elem.from(e))) {
      globalElemDecls.map(e => resolved.Elem.from(e))
    }

    val imports = schema.findAllImports

    assertResult(6) {
      imports.size
    }

    assertResult(schema.filterChildElems(named(ENames.XsImportEName)).map(e => resolved.Elem.from(e))) {
      imports.map(e => resolved.Elem.from(e))
    }
  }

  test("testParseAndQueryStandardLabelLinkbase") {
    val linkbase = Linkbase(getTaxonomyElement("/testfiles/venj-bw2-axes-lab-fr.xml").underlyingElem)

    val elemClasses = linkbase.findAllDescendantElemsOrSelf().map(_.getClass).toSet

    assertResult(Set(classOf[Linkbase], classOf[LabelLink], classOf[ConceptLabelResource], classOf[LabelArc], classOf[ConceptKey])) {
      elemClasses
    }

    val extendedLinks = linkbase.findAllExtendedLinks

    assertResult(1) {
      extendedLinks.size
    }

    val labeledResourceMap: Map[String, Seq[XLinkResource]] = extendedLinks.head.labeledXlinkResourceMap

    val venjBw2DimNs = "http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes"

    assertResult(true) {
      extendedLinks.head.arcs.exists { arc =>
        labeledResourceMap.getOrElse(arc.from, Seq.empty)
          .collect { case k: ConceptKey if k.key == EName(venjBw2DimNs, "ClassesOfDirectorsAndPersonnelAxis") => k }.nonEmpty &&
          labeledResourceMap.getOrElse(arc.to, Seq.empty)
            .collect { case r: ConceptLabelResource if r.text == "Classes des administrateurs et du personnel [axe]" => r }.nonEmpty
      }
    }

    assertResult(extendedLinks.head.xlinkResourceChildren.collect { case k: ConceptKey => k }) {
      extendedLinks.head.xlinkResourceChildren.collect { case k: ConceptKey if k.key.localPart.endsWith("Axis") => k }
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
