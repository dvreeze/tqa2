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

package eu.cdevreeze.tqa2.internal.xmlutil

import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.core.Scope

import scala.collection.immutable.ListMap

/**
 * Workaround for Scala bugs in VectorMap, which is used internally by Scope and therefore by PrefixedScope.
 * See https://github.com/scala/scala/pull/8854 and https://github.com/scala/bug/issues/11933.
 *
 * @author Chris de Vreeze
 */
object ScopeUtil {

  implicit class ToScopeUsingListMap(val scope: Scope) extends AnyVal {

    def usingListMap: AltScope = AltScope(scope)
  }

  implicit class ToPrefixedScopeUsingListMap(val prefixedScope: PrefixedScope) extends AnyVal {

    def usingListMap: AltPrefixedScope = AltPrefixedScope(prefixedScope)
  }

  final case class AltScope(scope: Scope) {

    def append(other: Scope): Scope = {
      Scope(scope.prefixNamespaceMap.to(ListMap) ++ other.prefixNamespaceMap)
    }

    def prepend(other: Scope): Scope = AltScope(other).append(this.scope)
  }

  final case class AltPrefixedScope(prefixedScope: PrefixedScope) {

    def append(other: PrefixedScope): PrefixedScope = {
      PrefixedScope.ignoringDefaultNamespace(AltScope(prefixedScope.scope).append(other.scope))
    }

    def prepend(other: PrefixedScope): PrefixedScope = AltPrefixedScope(other).append(this.prefixedScope)
  }
}
