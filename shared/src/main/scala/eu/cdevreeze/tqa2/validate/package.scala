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

package eu.cdevreeze.tqa2

/**
 * Validations on locator-free taxonomies. For example, locator-free taxonomies must have neither XLink locators nor XLink simple links.
 * As another example, all taxonomy element keys must refer to existing taxonomy elements in the taxonomy.
 *
 * @author Chris de Vreeze
 */
package object validate {

  type Rule = String
}
