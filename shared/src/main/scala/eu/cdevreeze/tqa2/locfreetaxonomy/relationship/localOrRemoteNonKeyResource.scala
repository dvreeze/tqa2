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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkResource

/**
 * Local or remote non-key XLink resource. It supports prohibition of non-key XLink resources via taxonomy element keys
 * pointing to remote non-key XLink resources.
 *
 * @author Chris de Vreeze
 */
sealed trait LocalOrRemoteNonKeyResource[+E <: NonKeyResource] {

  /**
   * The direct resource which is either a local resource or a taxonomy element key to a remote resource.
   */
  def directResource: XLinkResource

  /**
   * The local or remote resource.
   */
  def effectiveResource: E

  final def elr: String = directResource.elr
}

/**
 * A local non-key XLink resource. The direct resource and target resource are the same.
 */
final case class LocalNonKeyResource[+E <: NonKeyResource](
  effectiveResource: E) extends LocalOrRemoteNonKeyResource[E] {

  def directResource: E = effectiveResource
}

/**
 * A remote non-key XLink resource, containing a taxonomy element key pointing to the remote non-key resource.
 */
final case class RemoteNonKeyResource[+E <: NonKeyResource](
  directResource: AnyElementKey,
  effectiveResource: E
) extends LocalOrRemoteNonKeyResource[E]
