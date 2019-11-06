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

package eu.cdevreeze.tqa2.locfreetaxonomy

/**
 * Type-safe taxonomy element DOM content in the locator-free model.
 *
 * The "taxonomy DOM" is represented as a "yaidom2 dialect". It is type TaxonomyElem with its sub-types for taxonomy content,
 * like (locator-free) taxonomy schema content or (locator-free) linkbase content. Type TaxonomyElem extends yaidom2 type
 * AbstractDialectBackingElem, which is derived from yaidom2 type BackingElemApi. By extending type AbstractDialectBackingElem,
 * "dialect support" comes almost for free, and the underlying yaidom2 elements can be any BackingNodes.Elem implementation (Saxon
 * or native yaidom, for example).
 *
 * The TaxonomyElem sub-types mix in several other "dialects", implemented in this package and elsewhere. Those dialects
 * (for "clink" content, locator-free XLink content etc. etc.) are trait type hierarchies (possibly with partial implementations)
 * that all (also) extend yaidom2 type BackingElemApi, and as such can indeed be mixed in into the TaxonomyElem type
 * hierarchy.
 *
 * As an implementation detail, all these "taxonomy DOM" type hierarchies are based on traits rather than abstract classes.
 * Besides the multiple inheritance supported by deriving from traits, this has also the advantage that "state" only lives
 * in concrete classes (the leaves of the type hierarchies), which makes the concrete classes more predictable in terms
 * of value equality.
 *
 * Besides TaxonomyElem (and the "dialects" it uses) there is type ConceptDeclaration and its sub-types. Type ConceptDeclaration
 * wraps a GlobalElementDeclaration, adding substitution group knowledge to those global element declarations. After all,
 * a "typesafe DOM" type like GlobalElementDeclaration (as an XML element wrapper) has no knowledge about substitution group
 * hierarchies, which is only available in taxonomies spanning multiple documents.
 *
 * @author Chris de Vreeze
 */
package object dom
