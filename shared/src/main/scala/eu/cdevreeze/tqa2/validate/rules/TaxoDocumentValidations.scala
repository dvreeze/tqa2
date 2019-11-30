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

package eu.cdevreeze.tqa2.validate.rules

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult

/**
 * Taxonomy document validations, whether the document is a schema or linkbase.
 *
 * @author Chris de Vreeze
 */
object TaxoDocumentValidations {

  val xsiSchemaLocationNotAllowedRule: Rule = "Attribute xsi:schemaLocation not allowed"

  object XsiSchemaLocationNotAllowed extends Validation {

    def rule: Rule = xsiSchemaLocationNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val violatingElems = taxo.rootElems.filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.attrOption(ENames.XsiSchemaLocationEName).nonEmpty))

      violatingElems.map(_.docUri).distinct.map(uri => ValidationResult(rule, "xsi:schemaLocation attributes found but not allowed", uri))
    }
  }

  val all: Seq[Validation] = Seq(XsiSchemaLocationNotAllowed)
}
