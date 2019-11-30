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

package eu.cdevreeze.tqa2.docbuilder

import java.net.URI

import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.core.SimpleScope
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.nodebuilder.NodeBuilders.ElemCreator
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.queryapi.ScopedNodes

/**
 * Simple catalog, as used in (and restricted by) XBRL taxonomy packages. It only supports rewriting of URIs, so it is typically
 * used for mapping URIs of published (or soon to be published) taxonomy documents to their local mirror URIs (using the "file"
 * scheme).
 *
 * Hence this catalog class is far more restricted than the Catalog class of Java 9+. It is also used in a different way,
 * in that "document builders" may used these catalogs for all URIs, not just to resolve URIs within documents that have
 * already been parsed.
 *
 * @author Chris de Vreeze
 */
final case class SimpleCatalog(xmlBaseAttrOption: Option[URI], uriRewrites: Seq[SimpleCatalog.UriRewrite]) {

  /**
   * Applies the best matching rewrite rule to the given URI, if any, and returns the optional
   * resulting URI. Matching is done after normalizing the URI, as well as the URI start strings.
   *
   * The best matching rewrite rule, if any, is the one with the longest matching URI start string.
   */
  def findMappedUri(uri: URI): Option[URI] = {
    val sortedRewrites = uriRewrites.sortBy(_.normalizedUriStartString.length).reverse
    val normalizedUri = uri.normalize
    val normalizedUriString = normalizedUri.toString

    val uriRewriteOption: Option[SimpleCatalog.UriRewrite] =
      sortedRewrites.find(rewrite => normalizedUriString.startsWith(rewrite.normalizedUriStartString))

    uriRewriteOption.map { rewrite =>
      val relativeUri: URI =
        URI.create(rewrite.normalizedUriStartString).relativize(normalizedUri).ensuring(u => !u.isAbsolute)

      val effectiveRewritePrefix: URI =
        xmlBaseAttrOption.map(_.resolve(rewrite.effectiveRewritePrefix)).getOrElse(URI.create(rewrite.effectiveRewritePrefix))

      effectiveRewritePrefix.resolve(relativeUri)
    }
  }

  /**
   * Returns the equivalent of `findMappedUri(uri).get`.
   */
  def getMappedUri(uri: URI): URI = {
    findMappedUri(uri).getOrElse(sys.error(s"Could not map URI '$uri'"))
  }

  /**
   * Returns the same simple catalog, but first resolving XML base attributes. Therefore the result has no XML base attributes anywhere.
   */
  def netSimpleCatalog: SimpleCatalog = {
    val netUriRewrites: Seq[SimpleCatalog.UriRewrite] = uriRewrites.map { rewrite =>
      val effectiveRewritePrefix: URI =
        xmlBaseAttrOption.map(_.resolve(rewrite.effectiveRewritePrefix)).getOrElse(URI.create(rewrite.effectiveRewritePrefix))

      SimpleCatalog.UriRewrite(None, rewrite.uriStartString, effectiveRewritePrefix.toString)
    }

    SimpleCatalog(None, netUriRewrites)
  }

  /**
   * Returns this simple catalog as the mapping of the net simple catalog.
   */
  def toMap: Map[String, String] = {
    netSimpleCatalog.uriRewrites.map { rewrite =>
      rewrite.uriStartString -> rewrite.rewritePrefix
    }.toMap
  }

  /**
   * Tries to reverse this simple catalog (after converting it to the net simple catalog), but if this simple catalog
   * is not invertible, the result is incorrect.
   */
  def reverse: SimpleCatalog = {
    val reverseMappings: Map[String, String] = toMap.toSeq.map { case (s, p) => p -> s }.toMap
    SimpleCatalog.from(reverseMappings)
  }

  def filter(p: SimpleCatalog.UriRewrite => Boolean): SimpleCatalog = {
    SimpleCatalog(xmlBaseAttrOption, uriRewrites.filter(p))
  }

  def toElem: simple.Elem = {
    import SimpleCatalog._

    val simpleScope: SimpleScope = SimpleScope.from("er" -> ErNamespace)

    val uriRewriteElems: Seq[nodebuilder.Elem] = uriRewrites.map(_.toElem).map(e => nodebuilder.Elem.from(e))

    val resultElem = ElemCreator(simpleScope)
      .emptyElem(ErCatalogEName)
      .plusAttributeOption(XmlBaseEName, xmlBaseAttrOption.map(_.toString))
      .plusChildren(uriRewriteElems)

    simple.Elem.from(resultElem) // TODO Prettify
  }
}

object SimpleCatalog {

  final case class UriRewrite(xmlBaseAttrOption: Option[URI], uriStartString: String, rewritePrefix: String) {

    /**
     * Returns the normalized URI start string, which is used for matching against normalized URIs.
     */
    def normalizedUriStartString: String = {
      URI.create(uriStartString).normalize.toString
    }

    /**
     * Returns the rewrite prefix, but if this rewrite element contains an XML Base attribute, first
     * resolves the rewrite prefix against that XML Base attribute.
     */
    def effectiveRewritePrefix: String = {
      xmlBaseAttrOption.map(_.resolve(rewritePrefix).toString).getOrElse(rewritePrefix)
    }

    def toElem: simple.Elem = {
      val simpleScope: SimpleScope = SimpleScope.from("er" -> ErNamespace)

      val resultElem = ElemCreator(simpleScope)
        .emptyElem(ErRewriteURIEName)
        .plusAttributeOption(XmlBaseEName, xmlBaseAttrOption.map(_.toString))
        .plusAttribute(UriStartStringEName, uriStartString)
        .plusAttribute(RewritePrefixEName, rewritePrefix)

      simple.Elem.from(resultElem)
    }
  }

  object UriRewrite {

    def fromElem(rewriteElem: ScopedNodes.Elem): UriRewrite = {
      require(rewriteElem.name == ErRewriteURIEName, s"Expected $ErRewriteURIEName but got ${rewriteElem.name}")

      UriRewrite(rewriteElem.attrOption(XmlBaseEName).map(URI.create),
                 rewriteElem.attr(UriStartStringEName),
                 rewriteElem.attr(RewritePrefixEName))
    }
  }

  def from(uriRewrites: Map[String, String]): SimpleCatalog = {
    SimpleCatalog(None, uriRewrites.toSeq.map { case (startString, rewritePrefix) => UriRewrite(None, startString, rewritePrefix) })
  }

  def fromElem(catalogElem: ScopedNodes.Elem): SimpleCatalog = {
    require(catalogElem.name == ErCatalogEName, s"Expected $ErCatalogEName but got ${catalogElem.name}")

    val uriRewrites: Seq[UriRewrite] = catalogElem.filterChildElems(_.name == ErRewriteURIEName).map(e => UriRewrite.fromElem(e))

    SimpleCatalog(catalogElem.attrOption(XmlBaseEName).map(URI.create), uriRewrites)
  }

  val ErNamespace = "urn:oasis:names:tc:entity:xmlns:xml:catalog"

  val ErCatalogEName = EName(ErNamespace, "catalog")
  val ErRewriteURIEName = EName(ErNamespace, "rewriteURI")

  val UriStartStringEName: EName = EName.fromLocalName("uriStartString")
  val RewritePrefixEName: EName = EName.fromLocalName("rewritePrefix")

  private val XmlBaseEName = EName(Scope.XmlNamespace, "base")
}
