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

package eu.cdevreeze.tqa2.common.namespaceutils

import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.QName
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.utils.namespaces.TextENameExtractor

/**
 * Yaidom2 TextENameExtractor for text of schema type var:QName, which ignores the default namespace, if any.
 *
 * @author Chris de Vreeze
 */
object QNameTextENameExtractorIgnoringDefaultNamespace extends TextENameExtractor {

  def extractENames(scope: Scope, text: String): Set[EName] = {
    Set(scope.withoutDefaultNamespace.resolveQName(QName.parse(text.trim)))
  }
}
