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

import eu.cdevreeze.tqa2.locfreetaxonomy.dom.AnyElementKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NonKeyResource
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkResource

/**
 * A taxonomy element key, or a local or remote non-key XLink resource. It supports prohibition of non-key XLink resources
 * via taxonomy element keys pointing to these remote non-key XLink resources. For remote non-key XLink resources, the holder
 * contains the direct key resource (the "from" or "to" of the arc) as well as the remote resource pointed to. Otherwise
 * the holder only contains the resource (key or local resource).
 *
 * Due to the notion of a "resource holder", and in particular of a "local/remote non-key resource", we can support equivalent
 * concept label relationships where one points to a local label resource and the other the same remote label resource.
 *
 * @author Chris de Vreeze
 */
sealed trait ResourceHolder[+E <: XLinkResource] {

  /**
   * The direct resource which is either a key (possibly to a remote resource) or local resource.
   * It is directly referred to via the XLink label as source or target of the containing relationship.
   */
  def directResource: XLinkResource

  /**
   * The local or remote resource, or key if it does not point to a remote resource. In the case of a remote resource,
   * it is not referred to directly via the XLink label as source or target of the containing relationship.
   */
  def effectiveResource: E

  final def elr: String = directResource.elr
}

object ResourceHolder {

  def key[E <: TaxonomyElemKey](key: E): Key[E] = Key(key)

  def localResource[E <: NonKeyResource](resource: E): LocalNonKeyResource[E] = LocalNonKeyResource(resource)

  def remoteResource[E <: NonKeyResource](
    directResource: AnyElementKey,
    effectiveResource: E
  ): RemoteNonKeyResource[E] = {
    RemoteNonKeyResource(directResource, effectiveResource)
  }

  /**
   * A taxonomy key XLink resource (but not to a remote resource). The direct resource and effective resource are the same.
   */
  final case class Key[+E <: TaxonomyElemKey](key: E) extends ResourceHolder[E] {

    def directResource: E = key

    def effectiveResource: E = key
  }

  /**
   * A local non-key XLink resource. The direct resource and effective resource are the same.
   */
  final case class LocalNonKeyResource[+E <: NonKeyResource](resource: E) extends ResourceHolder[E] {

    def directResource: E = resource

    def effectiveResource: E = resource
  }

  /**
   * A remote non-key XLink resource, containing a taxonomy element key pointing to the remote non-key resource.
   */
  final case class RemoteNonKeyResource[+E <: NonKeyResource](
    directResource: AnyElementKey,
    effectiveResource: E
  ) extends ResourceHolder[E]
}
