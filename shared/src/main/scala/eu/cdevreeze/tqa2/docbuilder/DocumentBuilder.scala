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

import eu.cdevreeze.yaidom2.queryapi.BackingDocumentApi

/**
 * Any builder of a ("backing") document.
 *
 * Typical document builders convert the document URI to a local URI (maybe by using a SimpleCatalog), use the local URI
 * for finding the document to parse, and store the original URI as "system ID" in the parsed document. Some document builders
 * are capable of parsing documents inside ZIP files. Document builders can also be stacked, for example to perform some
 * post-processing (fix a broken link, or cache the document, etc.).
 *
 * @author Chris de Vreeze
 */
trait DocumentBuilder {

  type BackingDoc <: BackingDocumentApi

  /**
   * Returns the document with the given URI. This typically but not necessarily involves parsing of the document.
   */
  def build(uri: URI): BackingDoc
}

object DocumentBuilder {

  type Aux[A] = DocumentBuilder { type BackingDoc = A }

  /**
   * Document builder that uses an URI resolver internally, typically to load the document from a local mirror.
   * The "original" URI is stored with the document as its document URI, even if the document is loaded from a local mirror.
   */
  trait UsingUriResolver extends DocumentBuilder {

    def uriResolver: UriResolver
  }

  object UsingUriResolver {

    type Aux[A] = UsingUriResolver { type BackingDoc = A }
  }

  /**
   * DocumentBuilder promising that it can safely be used by multiple threads simultaneously.
   */
  trait ThreadSafeDocumentBuilder extends DocumentBuilder

  object ThreadSafeDocumentBuilder {

    type Aux[A] = ThreadSafeDocumentBuilder { type BackingDoc = A }
  }
}
