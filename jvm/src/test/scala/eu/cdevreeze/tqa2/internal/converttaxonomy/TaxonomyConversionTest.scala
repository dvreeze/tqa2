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

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.common.namespaceutils.XbrlDocumentENameExtractor
import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.DefaultRelationshipFactory
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.queryapi.ScopedElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Test of converting taxonomies to the locator-free model.
 *
 * @author Chris de Vreeze
 */
class TaxonomyConversionTest extends AnyFunSuite {

  test("TQA should be able to convert a small taxonomy") {
    val docUris: Set[URI] = Set(
      "standard-xbrl-testfiles/www.nltaxonomie.nl/2013/xbrl/sbr-dimensional-concepts.xsd",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-lab-fr.xml",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-ref.xml",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes.xsd",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-data.xsd",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-domains.xsd",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-linkroles.xsd",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-linkroles-generic-lab-en.xml",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-abstracts.xsd",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-decree-on-additional-regulations-for-the-management-report-pre.xml",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-generic-linkrole-order.xml",
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/validation/venj-bw2-uniform-fiscal-valuation-principles-decree-def.xml",
    ).map(URI.create)

    val inputDocs = docUris.toSeq.map(uri => getStandardTaxonomyElement(uri))

    val sgMap: SubstitutionGroupMap = SubstitutionGroupMap.from(
      Map(
        EName.parse("{http://www.nltaxonomie.nl/2011/xbrl/xbrl-syntax-extension}presentationItem") -> ENames.XbrliItemEName,
        EName.parse("{http://www.nltaxonomie.nl/2011/xbrl/xbrl-syntax-extension}primaryDomainItem") -> ENames.XbrliItemEName
      ))

    val inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase =
      standardtaxonomy.taxonomy.TaxonomyBase.build(inputDocs, sgMap)

    val scope: PrefixedScope = PrefixedScope
      .ignoringDefaultNamespace(ScopedElemApi.unionScope(inputTaxonomyBase.rootElems))
      .append(PrefixedScope
        .from("clink" -> Namespaces.CLinkNamespace, "ckey" -> Namespaces.CKeyNamespace, "cxbrldt" -> Namespaces.CXbrldtNamespace))

    implicit val namespacePrefixMapper: NamespacePrefixMapper =
      NamespacePrefixMapper.fromPrefixToNamespaceMapWithFallback(scope.scope.prefixNamespaceMap)

    implicit val documentENameExtractor: DocumentENameExtractor = XbrlDocumentENameExtractor.defaultInstance

    val xlinkResourceConverter = new DefaultXLinkResourceConverter(namespacePrefixMapper)
    val taxoBaseConverter: TaxonomyBaseConverter =
      new TaxonomyBaseConverter(xlinkResourceConverter, namespacePrefixMapper, documentENameExtractor)

    val locFreeTaxoBase: TaxonomyBase = taxoBaseConverter.convertTaxonomyBaseIgnoringEntrypoints(inputTaxonomyBase, _ => false)

    (locFreeTaxoBase.rootElems should have).size(inputTaxonomyBase.rootElems.size)

    val locFreeTaxo: BasicTaxonomy = BasicTaxonomy.build(locFreeTaxoBase, DefaultRelationshipFactory, _ => true)

    // Perform some queries on the locator-free taxonomy

    locFreeTaxo.relationships.size should be > 50
  }

  private val processor = new Processor(false)

  private def getStandardTaxonomyElement(relativeFilePath: URI): standardtaxonomy.dom.TaxonomyElem = {
    val doc: saxon.Document = TestResourceUtil.buildSaxonDocumentFromClasspathResource(relativeFilePath, processor)

    standardtaxonomy.dom.TaxonomyElem(doc.documentElement)
  }
}
