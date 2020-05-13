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

package eu.cdevreeze.tqa2.locfreetaxonomy.dom

import eu.cdevreeze.tqa2.common.xpath.ScopedXPathString
import eu.cdevreeze.tqa2.common.xpath.TypedValue
import eu.cdevreeze.tqa2.common.xpath.TypedValueExpr
import eu.cdevreeze.tqa2.common.xpath.TypedValueProvider
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Small DOM query API traits for standardized non-standard taxonomy elements. They are used as mixin traits to reduce code duplication
 * in the type hierarchy for standardized non-standard taxonomy elements.
 *
 * @author Chris de Vreeze
 */
object NonStandardTaxonomyElemSupport {

  trait HasExprText extends BackingElemApi {

    final def expr: ScopedXPathString = {
      ScopedXPathString(text, scope)
    }
  }

  trait HasQNameValue extends BackingElemApi {

    /**
     * Returns the element text resolved as EName. This may fail with an exception if the taxonomy is not schema-valid.
     */
    final def qnameValue: EName = {
      textAsResolvedQName
    }
  }

  /**
   * Trait for a taxonomy element that provides a QName, either by a child element containing the QName or by a child element
   * containing an XPath expression that should evaluate to a QName. This trait cannot be used if the QName can also be provided
   * by an element acting as a "variable".
   */
  trait ProvidesQName[V <: HasQNameValue, E <: HasExprText] extends BackingElemApi {

    private implicit val clsTagV: ClassTag[V] = classTag[V]
    private implicit val clsTagE: ClassTag[E] = classTag[E]

    final def qnameElemOption: Option[V] = {
      findAllChildElems.collectFirst { case e: V => e }
    }

    final def qnameExpressionElemOption: Option[E] = {
      findAllChildElems.collectFirst { case e: E => e }
    }

    /**
     * Returns the qname as EName value provider. This may fail if this element is not schema-valid.
     */
    final def qnameValueOrExpr: TypedValueProvider[EName] = {
      qnameElemOption
        .map(_.qnameValue)
        .map(v => TypedValue(v))
        .orElse(qnameExpressionElemOption.map(_.expr).map(v => TypedValueExpr(classTag[EName], v)))
        .get
    }
  }
}
