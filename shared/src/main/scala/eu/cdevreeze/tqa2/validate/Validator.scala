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

package eu.cdevreeze.tqa2.validate

import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy

/**
 * Validator, running a collection of validations.
 *
 * @author Chris de Vreeze
 */
object Validator {

  def validate(taxonomy: BasicTaxonomy, validations: Seq[Validation], resultFilter: ValidationResult => Boolean): Seq[ValidationResult] = {
    validations.flatMap(_.validationFunction(taxonomy)).filter(resultFilter)
  }

  def validate(taxonomy: BasicTaxonomy, validations: Seq[Validation]): Seq[ValidationResult] = {
    validate(taxonomy, validations, _ => true)
  }
}
