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

package eu.cdevreeze.tqa2.common.xpath

import scala.reflect.ClassTag

/**
 * Typed value provider. Either the typed value itself, or an XPath expression (as ScopedXPathString) that must resolve
 * to a typed value of the given type.
 *
 * @author Chris de Vreeze
 */
sealed trait TypedValueProvider[T]

final case class TypedValue[T](value: T) extends TypedValueProvider[T]

final case class TypedValueExpr[T](tpe: ClassTag[T], scopedXPathString: ScopedXPathString) extends TypedValueProvider[T]
