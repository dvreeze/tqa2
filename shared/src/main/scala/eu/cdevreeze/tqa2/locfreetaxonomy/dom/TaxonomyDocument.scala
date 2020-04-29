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

import eu.cdevreeze.yaidom2.creationapi.BackingDocumentFactory
import eu.cdevreeze.yaidom2.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

/**
 * Taxonomy document, holding a TaxonomyElem as root element (but possibly also top level comments and processing
 * instructions).
 *
 * @author Chris de Vreeze
 */
final case class TaxonomyDocument(children: Seq[CanBeTaxonomyDocumentChild]) extends BackingDocumentApi {
  require(
    children.collect { case e: TaxonomyElem => e }.size == 1,
    s"A document must have precisely 1 document element but found ${children.collect { case e: TaxonomyElem => e }.size} ones"
  )

  type NodeType = TaxonomyNode

  type CanBeDocumentChildType = CanBeTaxonomyDocumentChild

  type ElemType = TaxonomyElem

  type ThisDoc = TaxonomyDocument

  def docUriOption: Option[URI] = documentElement.docUriOption

  def documentElement: ElemType = children.collectFirst { case e: TaxonomyElem => e }.get
}

object TaxonomyDocument extends BackingDocumentFactory {

  type TargetDocumentType = TaxonomyDocument

  def apply(documentElement: TaxonomyElem): TaxonomyDocument = {
    apply(Seq(documentElement))
  }

  def from(document: BackingDocumentApi): TaxonomyDocument = {
    val targetChildren: Seq[CanBeTaxonomyDocumentChild] = document.children
      .map {
        case e: BackingNodes.Elem                   => TaxonomyElem(e)
        case c: BackingNodes.Comment                => TaxonomyCommentNode(c.text)
        case pi: BackingNodes.ProcessingInstruction => TaxonomyProcessingInstructionNode(pi.target, pi.data)
      }

    TaxonomyDocument(targetChildren)
  }
}
