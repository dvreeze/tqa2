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

package eu.cdevreeze.tqa2.internal

/**
 * Conversions from standard taxonomies to locator-free taxonomies as TaxonomyBase instances.
 *
 * Before converting taxonomies, we need an entrypoint as non-empty URI set (typically singleton) and a mapping from URIs
 * to yaidom2 root elements (BackingNodes.Elem) that is big enough to hold the DTS of the entrypoint.
 * Then conversion is done in multiple steps:
 * <p><ul>
 * <li>Determine the DTS, given an entrypoint (as non-empty set of document URIs, typically a singleton set)
 * <li>Validate if this DTS can lead to a locator-free taxonomy (so xs:include is not allowed, among other things)
 * <li>Create the typesafe DOM models of the documents in the standard input taxonomy (corresponding to the DTS)
 * <li>Create the (in-memory) locator-free equivalent of the entrypoint (sometimes user input is needed to create a non-singleton entrypoint)
 * <li>For all URIs in the DTS, other than the entrypoint itself, convert the DOM model of the root element to its locator-free equivalent
 * <li>Combine the locator-free entrypoint documents with the locator-free non-entrypoint documents, yielding a locator-free TaxonomyBase instance
 * </ul></p>
 * @author Chris de Vreeze
 */
package object converttaxonomy
