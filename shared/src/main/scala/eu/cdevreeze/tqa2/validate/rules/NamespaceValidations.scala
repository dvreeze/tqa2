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

import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.queryapi.ScopedElemApi

/**
 * Namespace prefix consistency validations.
 *
 * @author Chris de Vreeze
 */
object NamespaceValidations {

  val conflictingNamespaceScopesNotAllowedRule: Rule = "Conflicting namespace scopes not allowed"

  object ConflictingNamespaceScopesNotAllowed extends Validation {

    def rule: Rule = conflictingNamespaceScopesNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val noNamespaceScopeConflicts = ScopedElemApi.containsNoConflictingScopes(taxo.rootElems)

      if (noNamespaceScopeConflicts) {
        Seq.empty
      } else {
        val scopes: Seq[Scope] =
          taxo.rootElems.flatMap(_.findAllDescendantElemsOrSelf.map(_.scope.withoutDefaultNamespace).distinct).distinct

        val prefixNamespaces: Seq[(String, String)] = scopes.flatMap(_.prefixNamespaceMap).distinct

        val prefixNamespaceMap: Map[String, Set[String]] = prefixNamespaces.groupMap(_._1)(_._2).view.mapValues(_.toSet).toMap

        val violatingPrefixNamespaceMap: Map[String, Set[String]] = prefixNamespaceMap.filter(_._2.sizeIs >= 2)

        violatingPrefixNamespaceMap.toSeq.map {
          case (prefix, _) =>
            ValidationResult(rule, "violating prefix bound to multiple namespaces not allowed", prefix)
        }
      }
    }
  }

  val all: Seq[Validation] = Seq(ConflictingNamespaceScopesNotAllowed)
}
