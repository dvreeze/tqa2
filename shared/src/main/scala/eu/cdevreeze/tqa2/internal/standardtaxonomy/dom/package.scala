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

package eu.cdevreeze.tqa2.internal.standardtaxonomy

/**
 * In order to create an in-memory locator-free taxonomy model from a standard taxonomy, all we have to do is create a
 * (locator-free) TaxonomyBase from the standard taxonomy. For that we only have to create a collection of root elements
 * as (locator-free) TaxonomyElem instances. For that we need a DOM model of standard taxonomies, and not a whole lot more
 * than that. This package contains that DOM model.
 *
 * Dependencies on the common package of the locator-free model are allowed.
 *
 * @author Chris de Vreeze
 */
package object dom
