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
import eu.cdevreeze.tqa2.internal.xmlutil.ScopeUtil._
import eu.cdevreeze.tqa2.locfreetaxonomy.TestResourceUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ConceptKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Linkbase
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.RoleKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.resolved
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
    val inputLinkbase = getStandardTaxonomyElement(
      URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-lab-fr.xml"))
      .asInstanceOf[standardtaxonomy.dom.Linkbase]
    val inputSchema =
      getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes.xsd"))
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

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes")) should not be empty

    // Compare with expected linkbase

    val expectedLinkbase = getLocatorFreeTaxonomyElement(
      URI.create("testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-lab-fr.xml")).asInstanceOf[Linkbase]

    val arcs = locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkLabelArcEName)
    val expectedArcs = expectedLinkbase.filterDescendantElems(_.name == ENames.CLinkLabelArcEName)

    (arcs should have).size(26)

    arcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedArcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val taxoElemKeys = locFreeLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)
    val expectedTaxoElemKeys = expectedLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)

    (taxoElemKeys should have).size(26)

    taxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedTaxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val labels = locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkLabelEName)
    val expectedLabels = expectedLinkbase.filterDescendantElems(_.name == ENames.CLinkLabelEName)

    (labels should have).size(26)

    labels.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedLabels.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val envelope = resolved.Elem.from(locFreeLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkLabelLinkEName => Nil
      case e                                         => Seq(e)
    }

    val expectedEnvelope = resolved.Elem.from(expectedLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkLabelLinkEName => Nil
      case e                                         => Seq(e)
    }

    // TODO Is resolved.Elem.removeAllInterElementWhitespace broken?
    envelope.removeAllInterElementWhitespace should be(expectedEnvelope.removeAllInterElementWhitespace)
  }

  test("TQA should be able to convert a reference linkbase") {
    val inputLinkbase = getStandardTaxonomyElement(
      URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-ref.xml"))
      .asInstanceOf[standardtaxonomy.dom.Linkbase]
    val inputSchema =
      getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes.xsd"))
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
        EName.parse("{http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes}LoansAdvancesGuaranteesAxis"),
        "venj-bw2-dim_LoansAdvancesGuaranteesAxis_loc",
        "resource"
      ))

    // Unused namespace declarations have been pruned, so we can test for used namespaces

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.LinkNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XsNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrliNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrldtNamespace)) should be(empty)

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes")) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.RefNamespace)) should not be empty

    // Compare with expected linkbase

    val expectedLinkbase = getLocatorFreeTaxonomyElement(
      URI.create("testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-axes-ref.xml")).asInstanceOf[Linkbase]

    val arcs = locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkReferenceArcEName)
    val expectedArcs = expectedLinkbase.filterDescendantElems(_.name == ENames.CLinkReferenceArcEName)

    (arcs should have).size(1)

    arcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedArcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val taxoElemKeys = locFreeLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)
    val expectedTaxoElemKeys = expectedLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)

    (taxoElemKeys should have).size(1)

    taxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedTaxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val refs = locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkReferenceEName)
    val expectedRefs = expectedLinkbase.filterDescendantElems(_.name == ENames.CLinkReferenceEName)

    (refs should have).size(1)

    refs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedRefs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val envelope = resolved.Elem.from(locFreeLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkReferenceLinkEName => Nil
      case e                                             => Seq(e)
    }

    val expectedEnvelope = resolved.Elem.from(expectedLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkReferenceLinkEName => Nil
      case e                                             => Seq(e)
    }

    // TODO Is resolved.Elem.removeAllInterElementWhitespace broken?
    envelope.removeAllInterElementWhitespace should be(expectedEnvelope.removeAllInterElementWhitespace)
  }

  test("TQA should be able to convert a presentation linkbase") {
    val inputLinkbase = getStandardTaxonomyElement(URI.create(
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-decree-on-additional-regulations-for-the-management-report-pre.xml"))
      .asInstanceOf[standardtaxonomy.dom.Linkbase]
    val inputSchema1 =
      getStandardTaxonomyElement(
        URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-abstracts.xsd"))
        .asInstanceOf[standardtaxonomy.dom.XsSchema]
    val inputSchema2 =
      getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-data.xsd"))
        .asInstanceOf[standardtaxonomy.dom.XsSchema]

    val inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase =
      standardtaxonomy.taxonomy.TaxonomyBase.build(
        Seq(inputLinkbase, inputSchema1, inputSchema2),
        SubstitutionGroupMap.from(
          Map(EName.parse("{http://www.nltaxonomie.nl/2011/xbrl/xbrl-syntax-extension}presentationItem") -> ENames.XbrliItemEName))
      )

    // TODO Scope uses VectorMap, which is broken. See https://github.com/scala/scala/pull/8854 and https://github.com/scala/bug/issues/11933.

    val scope: PrefixedScope = PrefixedScope
      .ignoringDefaultNamespace(inputLinkbase.scope)
      .usingListMap
      .append(PrefixedScope.ignoringDefaultNamespace(inputSchema1.scope))
      .usingListMap
      .append(PrefixedScope.ignoringDefaultNamespace(inputSchema2.scope))
      .usingListMap
      .append(PrefixedScope.from("clink" -> Namespaces.CLinkNamespace, "ckey" -> Namespaces.CKeyNamespace))

    implicit val namespacePrefixMapper: NamespacePrefixMapper =
      NamespacePrefixMapper.fromMapWithFallback(scope.scope.inverse.view.mapValues(_.head).toMap)

    implicit val documentENameExtractor: DocumentENameExtractor = XbrlDocumentENameExtractor.defaultInstance

    val xlinkResourceConverter = new DefaultXLinkResourceConverter(namespacePrefixMapper)
    val linkbaseConverter: LinkbaseConverter = new LinkbaseConverter(xlinkResourceConverter)

    val locFreeLinkbase: Linkbase = linkbaseConverter.convertLinkbase(inputLinkbase, inputTaxonomyBase)

    (locFreeLinkbase.findAllExtendedLinks should have).size(1)

    val conceptKeyENames: Seq[EName] =
      locFreeLinkbase
        .filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)
        .collect { case e: ConceptKey => e }
        .map(e => e.key)

    conceptKeyENames should contain(
      EName.parse("{http://www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-abstracts}Article3bTitle"))

    locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkRoleRefEName).map(_.attr(ENames.RoleURIEName)) should be(
      Seq("urn:venj:linkrole:decree-on-additional-regulations-for-the-management-report"))

    // Unused namespace declarations have been pruned, so we can test for used namespaces

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.LinkNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XsNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrliNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrldtNamespace)) should be(empty)

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-abstracts")) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-data")) should not be empty

    // Compare with expected linkbase

    val expectedLinkbase = getLocatorFreeTaxonomyElement(URI.create(
      "testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-decree-on-additional-regulations-for-the-management-report-pre.xml"))
      .asInstanceOf[Linkbase]

    val arcs = locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkPresentationArcEName)
    val expectedArcs = expectedLinkbase.filterDescendantElems(_.name == ENames.CLinkPresentationArcEName)

    (arcs should have).size(22)

    arcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedArcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val taxoElemKeys = locFreeLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)
    val expectedTaxoElemKeys = expectedLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)

    (taxoElemKeys should have).size(23)

    taxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedTaxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val envelope = resolved.Elem.from(locFreeLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkPresentationLinkEName => Nil
      case e                                                => Seq(e)
    }

    val expectedEnvelope = resolved.Elem.from(expectedLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkPresentationLinkEName => Nil
      case e                                                => Seq(e)
    }

    // TODO Is resolved.Elem.removeAllInterElementWhitespace broken?
    envelope.removeAllInterElementWhitespace should be(expectedEnvelope.removeAllInterElementWhitespace)
  }

  test("TQA should be able to convert a definition linkbase") {
    val inputLinkbase = getStandardTaxonomyElement(URI.create(
      "standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/validation/venj-bw2-uniform-fiscal-valuation-principles-decree-def.xml"))
      .asInstanceOf[standardtaxonomy.dom.Linkbase]
    val inputSchema1 =
      getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/2013/xbrl/sbr-dimensional-concepts.xsd"))
        .asInstanceOf[standardtaxonomy.dom.XsSchema]
    val inputSchema2 =
      getStandardTaxonomyElement(URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-data.xsd"))
        .asInstanceOf[standardtaxonomy.dom.XsSchema]

    val inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase =
      standardtaxonomy.taxonomy.TaxonomyBase.build(
        Seq(inputLinkbase, inputSchema1, inputSchema2),
        SubstitutionGroupMap.from(
          Map(EName.parse("{http://www.nltaxonomie.nl/2011/xbrl/xbrl-syntax-extension}primaryDomainItem") -> ENames.XbrliItemEName))
      )

    // TODO Scope uses VectorMap, which is broken. See https://github.com/scala/scala/pull/8854 and https://github.com/scala/bug/issues/11933.

    val scope: PrefixedScope = PrefixedScope
      .ignoringDefaultNamespace(inputLinkbase.scope)
      .usingListMap
      .append(PrefixedScope.ignoringDefaultNamespace(inputSchema1.scope))
      .usingListMap
      .append(PrefixedScope.ignoringDefaultNamespace(inputSchema2.scope))
      .usingListMap
      .append(PrefixedScope.from("clink" -> Namespaces.CLinkNamespace, "ckey" -> Namespaces.CKeyNamespace))

    implicit val namespacePrefixMapper: NamespacePrefixMapper =
      NamespacePrefixMapper.fromMapWithFallback(scope.scope.inverse.view.mapValues(_.head).toMap)

    implicit val documentENameExtractor: DocumentENameExtractor = XbrlDocumentENameExtractor.defaultInstance

    val xlinkResourceConverter = new DefaultXLinkResourceConverter(namespacePrefixMapper)
    val linkbaseConverter: LinkbaseConverter = new LinkbaseConverter(xlinkResourceConverter)

    val locFreeLinkbase: Linkbase = linkbaseConverter.convertLinkbase(inputLinkbase, inputTaxonomyBase)

    (locFreeLinkbase.findAllExtendedLinks should have).size(1)

    val conceptKeyENames: Seq[EName] =
      locFreeLinkbase
        .filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)
        .collect { case e: ConceptKey => e }
        .map(e => e.key)

    conceptKeyENames should contain(
      EName.parse("{http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-data}IncomeTaxExpense"))

    locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkRoleRefEName).map(_.attr(ENames.RoleURIEName)) should be(
      Seq("urn:venj:linkrole:decree-on-uniform-fiscal-valuation-principles"))

    locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkArcroleRefEName).map(_.attr(ENames.ArcroleURIEName)) should be(
      Seq("http://xbrl.org/int/dim/arcrole/all", "http://xbrl.org/int/dim/arcrole/domain-member"))

    // Unused namespace declarations have been pruned, so we can test for used namespaces

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.LinkNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XsNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrliNamespace)) should be(empty)

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrldtNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/2013/xbrl/sbr-dimensional-concepts")) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-data")) should not be empty

    // Compare with expected linkbase

    val expectedLinkbase = getLocatorFreeTaxonomyElement(
      URI.create(
        "testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/validation/venj-bw2-uniform-fiscal-valuation-principles-decree-def.xml"))
      .asInstanceOf[Linkbase]

    val arcs = locFreeLinkbase.filterDescendantElems(_.name == ENames.CLinkDefinitionArcEName)
    val expectedArcs = expectedLinkbase.filterDescendantElems(_.name == ENames.CLinkDefinitionArcEName)

    (arcs should have).size(9)

    arcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedArcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val taxoElemKeys = locFreeLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)
    val expectedTaxoElemKeys = expectedLinkbase.filterDescendantElems(_.name == ENames.CKeyConceptKeyEName)

    (taxoElemKeys should have).size(10)

    taxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedTaxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val envelope = resolved.Elem.from(locFreeLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkDefinitionLinkEName => Nil
      case e                                              => Seq(e)
    }

    val expectedEnvelope = resolved.Elem.from(expectedLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CLinkDefinitionLinkEName => Nil
      case e                                              => Seq(e)
    }

    // TODO Is resolved.Elem.removeAllInterElementWhitespace broken?
    envelope.removeAllInterElementWhitespace should be(expectedEnvelope.removeAllInterElementWhitespace)
  }

  test("TQA should be able to convert a generic label linkbase") {
    val inputLinkbase = getStandardTaxonomyElement(
      URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-linkroles-generic-lab-en.xml"))
      .asInstanceOf[standardtaxonomy.dom.Linkbase]
    val inputSchema =
      getStandardTaxonomyElement(
        URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-linkroles.xsd"))
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

    val xlinkResourceConverter = new DefaultXLinkResourceConverter(namespacePrefixMapper)
    val linkbaseConverter: LinkbaseConverter = new LinkbaseConverter(xlinkResourceConverter)

    val locFreeLinkbase: Linkbase = linkbaseConverter.convertLinkbase(inputLinkbase, inputTaxonomyBase)

    (locFreeLinkbase.findAllExtendedLinks should have).size(1)

    val roleKeyData: Seq[(String, String, String)] =
      locFreeLinkbase
        .filterDescendantElems(_.name == ENames.CKeyRoleKeyEName)
        .collect { case e: RoleKey => e }
        .map(e => (e.key, e.xlinkLabel, e.xlinkType))

    roleKeyData should contain(
      (
        "urn:venj:linkrole:decree-on-uniform-fiscal-valuation-principles",
        "venj-bw2-lr_DecreeOnUniformFiscalValuationPrinciples_loc",
        "resource"
      ))

    // Unused namespace declarations have been pruned, so we can test for used namespaces

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.LinkNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XsNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrliNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrldtNamespace)) should be(empty)

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.GenNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CGenNamespace)) should not be empty

    // Compare with expected linkbase

    val expectedLinkbase = getLocatorFreeTaxonomyElement(
      URI.create("testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-linkroles-generic-lab-en.xml"))
      .asInstanceOf[Linkbase]

    val arcs = locFreeLinkbase.filterDescendantElems(_.name == ENames.GenArcEName)
    val expectedArcs = expectedLinkbase.filterDescendantElems(_.name == ENames.GenArcEName)

    (arcs should have).size(25)

    arcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedArcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val taxoElemKeys = locFreeLinkbase.filterDescendantElems(_.name == ENames.CKeyRoleKeyEName)
    val expectedTaxoElemKeys = expectedLinkbase.filterDescendantElems(_.name == ENames.CKeyRoleKeyEName)

    (taxoElemKeys should have).size(25)

    taxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedTaxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val labels = locFreeLinkbase.filterDescendantElems(_.name == ENames.LabelLabelEName)
    val expectedLabels = expectedLinkbase.filterDescendantElems(_.name == ENames.LabelLabelEName)

    (labels should have).size(25)

    labels.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedLabels.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val envelope = resolved.Elem.from(locFreeLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CGenLinkEName => Nil
      case e                                   => Seq(e)
    }

    val expectedEnvelope = resolved.Elem.from(expectedLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CGenLinkEName => Nil
      case e                                   => Seq(e)
    }

    // TODO Is resolved.Elem.removeAllInterElementWhitespace broken?
    envelope.removeAllInterElementWhitespace should be(expectedEnvelope.removeAllInterElementWhitespace)
  }

  test("TQA should be able to convert a custom generic linkbase") {
    val inputLinkbase = getStandardTaxonomyElement(
      URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-generic-linkrole-order.xml"))
      .asInstanceOf[standardtaxonomy.dom.Linkbase]
    val inputSchema =
      getStandardTaxonomyElement(
        URI.create("standard-xbrl-testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/dictionary/venj-bw2-linkroles.xsd"))
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

    val xlinkResourceConverter = new DefaultXLinkResourceConverter(namespacePrefixMapper)
    val linkbaseConverter: LinkbaseConverter = new LinkbaseConverter(xlinkResourceConverter)

    val locFreeLinkbase: Linkbase = linkbaseConverter.convertLinkbase(inputLinkbase, inputTaxonomyBase)

    (locFreeLinkbase.findAllExtendedLinks should have).size(1)

    val roleKeyData: Seq[(String, String, String)] =
      locFreeLinkbase
        .filterDescendantElems(_.name == ENames.CKeyRoleKeyEName)
        .collect { case e: RoleKey => e }
        .map(e => (e.key, e.xlinkLabel, e.xlinkType))

    roleKeyData should contain(
      (
        "urn:venj:linkrole:decree-on-uniform-fiscal-valuation-principles",
        "venj-bw2-lr_DecreeOnUniformFiscalValuationPrinciples_loc",
        "resource"
      ))

    // Unused namespace declarations have been pruned, so we can test for used namespaces

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.LinkNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XsNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrliNamespace)) should be(empty)
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XbrldtNamespace)) should be(empty)

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CLinkNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CKeyNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.XLinkNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.GenNamespace)) should not be empty
    locFreeLinkbase.scope.filterNamespaces(Set(Namespaces.CGenNamespace)) should not be empty

    locFreeLinkbase.scope.filterNamespaces(Set("http://www.nltaxonomie.nl/2011/xbrl/xbrl-syntax-extension")) should not be empty

    // Compare with expected linkbase

    val expectedLinkbase = getLocatorFreeTaxonomyElement(
      URI.create("testfiles/www.nltaxonomie.nl/nt12/venj/20170714.a/presentation/venj-bw2-generic-linkrole-order.xml"))
      .asInstanceOf[Linkbase]

    val arcs = locFreeLinkbase.filterDescendantElems(_.name == ENames.GenArcEName)
    val expectedArcs = expectedLinkbase.filterDescendantElems(_.name == ENames.GenArcEName)

    (arcs should have).size(25)

    arcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedArcs.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val taxoElemKeys = locFreeLinkbase.filterDescendantElems(_.name == ENames.CKeyRoleKeyEName)
    val expectedTaxoElemKeys = expectedLinkbase.filterDescendantElems(_.name == ENames.CKeyRoleKeyEName)

    (taxoElemKeys should have).size(25)

    taxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedTaxoElemKeys.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val ns = "http://www.nltaxonomie.nl/2011/xbrl/xbrl-syntax-extension"
    val linkroleOrders = locFreeLinkbase.filterDescendantElems(_.name == EName(ns, "linkroleOrder"))
    val expectedLinkroleOrders = expectedLinkbase.filterDescendantElems(_.name == EName(ns, "linkroleOrder"))

    (linkroleOrders should have).size(25)

    linkroleOrders.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet should be(
      expectedLinkroleOrders.map(resolved.Elem.from(_).removeAllInterElementWhitespace).toSet)

    val envelope = resolved.Elem.from(locFreeLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CGenLinkEName => Nil
      case e                                   => Seq(e)
    }

    val expectedEnvelope = resolved.Elem.from(expectedLinkbase).transformDescendantElemsToNodeSeq {
      case e if e.name == ENames.CGenLinkEName => Nil
      case e                                   => Seq(e)
    }

    // TODO Is resolved.Elem.removeAllInterElementWhitespace broken?
    envelope.removeAllInterElementWhitespace should be(expectedEnvelope.removeAllInterElementWhitespace)
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
