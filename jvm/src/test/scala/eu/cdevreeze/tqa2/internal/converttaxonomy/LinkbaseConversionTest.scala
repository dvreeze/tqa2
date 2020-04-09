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

package eu.cdevreeze.tqa2.internal.converttaxonomy

import java.net.URI

import eu.cdevreeze.tqa2.common.namespaceutils.XbrlDocumentENameExtractor
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.tqa2.internal.xmlutil.NodeBuilderUtil
import eu.cdevreeze.tqa2.internal.xmlutil.ScopeUtil._
import eu.cdevreeze.tqa2.internal.xmlutil.jvm.JvmNodeBuilderUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ConceptKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Linkbase
import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Test of converting linkbases to the locator-free model.
 *
 * @author Chris de Vreeze
 */
class LinkbaseConversionTest extends AnyFunSuite {

  test("TQA should be able to convert a label linkbase") {
    val inputLinkbase = getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/venj-bw2-axes-lab-fr.xml"))
      .asInstanceOf[standardtaxonomy.dom.Linkbase]
    val inputSchema = getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/venj-bw2-axes.xsd"))
      .asInstanceOf[standardtaxonomy.dom.XsSchema]

    val inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase =
      standardtaxonomy.taxonomy.TaxonomyBase.build(Seq(inputLinkbase, inputSchema), SubstitutionGroupMap.Empty)

    // TODO Scope uses VectorMap, which is broken. See https://github.com/scala/scala/pull/8854 and https://github.com/scala/bug/issues/11933.

    val scope: PrefixedScope = PrefixedScope
      .ignoringDefaultNamespace(inputLinkbase.scope)
      .usingListMap
      .append(PrefixedScope.ignoringDefaultNamespace(inputSchema.scope))
      .usingListMap
      .append(PrefixedScope.from("clink" -> Namespaces.CLinkNamespace, "ckey" -> Namespaces.CKeyNamespace))

    implicit val namespacePrefixMapper: NamespacePrefixMapper =
      NamespacePrefixMapper.fromMapWithFallback(scope.scope.inverse.view.mapValues(_.head).toMap)

    implicit val documentENameExtractor: DocumentENameExtractor = XbrlDocumentENameExtractor.defaultInstance

    implicit val nodeBuilderUtil: NodeBuilderUtil = JvmNodeBuilderUtil(namespacePrefixMapper, documentENameExtractor)

    val xlinkResourceConverter = new DefaultXLinkResourceConverter(namespacePrefixMapper)
    val linkbaseConverter: LinkbaseConverter = new LinkbaseConverter(xlinkResourceConverter)

    val locFreeLinkbase: Linkbase = linkbaseConverter.convertLinkbase(inputLinkbase, inputTaxonomyBase)

    (locFreeLinkbase.findAllExtendedLinks should have).size(1)

    val conceptKeyData: Seq[(EName, String, String)] =
      locFreeLinkbase
        .filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)
        .collect { case e: ConceptKey => e }
        .map(e => (e.key, e.xlinkLabel, e.xlinkType))

    conceptKeyData should contain(
      (
        EName.parse("{http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes}ClassesOfEquityAxis"),
        "venj-bw2-dim_ClassesOfEquityAxis_loc",
        "resource"
      ))

    // Unused namespace declarations have been pruned, so we can test for used namespaces

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.LinkNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XsNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrliNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrldtNamespace)) should be(empty)

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should not be (empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should not be (empty)

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should not be (empty)

    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes")) should not be (empty)
  }

  private val processor = new Processor(false)

  private def getStandardTaxonomyElement(relativeFilePath: URI): standardtaxonomy.dom.TaxonomyElem = {
    val doc: saxon.Document = TestResourceUtil.buildSaxonDocumentFromClasspathResource(relativeFilePath, processor)

    standardtaxonomy.dom.TaxonomyElem(doc.documentElement)
  }
}
