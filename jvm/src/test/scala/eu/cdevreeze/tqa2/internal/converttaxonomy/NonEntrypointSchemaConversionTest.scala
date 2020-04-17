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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.resolved
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.queryapi.ScopedElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor
import net.sf.saxon.s9api.Processor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Test of converting non-entrypoint schemas to the locator-free model.
 *
 * @author Chris de Vreeze
 */
class NonEntrypointSchemaConversionTest extends AnyFunSuite {

  test("TQA should be able to convert a regular taxonomy schema") {
    val inputSchema1 =
      getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes.xsd"))
        .asInstanceOf[standardtaxonomy.dom.XsSchema]
    val inputSchema2 =
      getStandardTaxonomyElement(
        URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-domains.xsd"))
        .asInstanceOf[standardtaxonomy.dom.XsSchema]

    val inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase =
      standardtaxonomy.taxonomy.TaxonomyBase.build(Seq(inputSchema1, inputSchema2), SubstitutionGroupMap.Empty)

    // TODO Scope uses VectorMap, which is broken. See https://github.com/scala/scala/pull/8854 and https://github.com/scala/bug/issues/11933.

    val scope: PrefixedScope = PrefixedScope
      .ignoringDefaultNamespace(ScopedElemApi.unionScope(Seq(inputSchema1, inputSchema2)))
      .append(PrefixedScope.from("cxbrldt" -> Namespaces.CXbrldtNamespace))

    implicit val namespacePrefixMapper: NamespacePrefixMapper =
      NamespacePrefixMapper.fromPrefixToNamespaceMapWithFallback(scope.scope.prefixNamespaceMap)

    implicit val documentENameExtractor: DocumentENameExtractor = XbrlDocumentENameExtractor.defaultInstance

    val schemaConverter: NonEntrypointSchemaConverter = new NonEntrypointSchemaConverter(namespacePrefixMapper, documentENameExtractor)

    val locFreeSchema: XsSchema = schemaConverter.convertSchema(inputSchema1, inputTaxonomyBase)

    (locFreeSchema.findAllGlobalElementDeclarations should have).size(26)

    val typedDimData: Seq[(EName, EName)] =
      locFreeSchema
        .filterGlobalElementDeclarations(_.attrOption(ENames.CXbrldtTypedDomainKeyEName).nonEmpty)
        .map(e => (e.targetEName, e.attrAsResolvedQName(ENames.CXbrldtTypedDomainKeyEName)))

    typedDimData should contain(
      (
        EName.parse("{http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes}AverageNumberEmployeesSegmentsAxis"),
        EName.parse(
          "{http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-domains}SegmentForAverageNumberEmployeesTypedMember")
      ))

    // Unused namespace declarations have been pruned, so we can test for used namespaces

    locFreeSchema.scope.filterNamespaces(Set(Namespaces.LinkNamespace)) should be(empty)
    locFreeSchema.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should be(empty)
    locFreeSchema.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should be(empty)
    locFreeSchema.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should be(empty)

    locFreeSchema.scope.filterNamespaces(Set(Namespaces.XsNamespace)) should not be empty
    locFreeSchema.scope.filterNamespaces(Set(Namespaces.CXbrldtNamespace)) should not be empty
    locFreeSchema.scope.filterNamespaces(Set(Namespaces.XbrliNamespace)) should not be empty
    locFreeSchema.scope.filterNamespaces(Set(Namespaces.XbrldtNamespace)) should not be empty

    locFreeSchema.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-domains")) should not be empty

    // Compare with expected schema

    val expectedSchema = getLocatorFreeTaxonomyElement(
      URI.create("testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes.xsd")).asInstanceOf[XsSchema]

    val globalElemDecls = locFreeSchema.findAllGlobalElementDeclarations
    val expectedGlobalElemDecls = expectedSchema.findAllGlobalElementDeclarations

    (globalElemDecls should have).size(26)

    globalElemDecls.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedGlobalElemDecls.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    // TODO Compare 'skeletons'
  }

  private val processor = new Processor(false)

  private def getStandardTaxonomyElement(relativeFilePath: URI): standardtaxonomy.dom.TaxonomyElem = {
    val doc: saxon.Document = TestResourceUtil.buildSaxonDocumentFromClasspathResource(relativeFilePath, processor)

    standardtaxonomy.dom.TaxonomyElem(doc.documentElement)
  }

  private def getLocatorFreeTaxonomyElement(relativeFilePath: URI): TaxonomyElem = {
    val doc: saxon.Document = TestResourceUtil.buildSaxonDocumentFromClasspathResource(relativeFilePath, processor)

    TaxonomyElem(doc.documentElement)
  }
}
