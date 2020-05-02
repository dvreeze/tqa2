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
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor
import eu.cdevreeze.yaidom2.utils.namespaces.TextENameExtractor

/**
 * DocumentENameExtractor for XBRL taxonomy schema documents, including locator-free ones, determining which namespaces
 * are used (in an "XML Schema sense") for which an xs:import element is needed.
 *
 * Attribute value text EName extractors are a nested Map where the outer Map key is the element name and the inner Map
 * key is the attribute name.
 */
final class SchemaContentENameExtractor(
    val elementTextENameExtractors: Map[EName, TextENameExtractor],
    val attributeValueTextENameExtractors: Map[EName, Map[EName, TextENameExtractor]]
) extends DocumentENameExtractor {

  def findElemTextENameExtractor(elem: BackingElemApi): Option[TextENameExtractor] = {
    elementTextENameExtractors.get(elem.name)
  }

  def findAttributeValueENameExtractor(elem: BackingElemApi, attrName: EName): Option[TextENameExtractor] = {
    attributeValueTextENameExtractors.get(elem.name).flatMap(_.get(attrName))
  }
}

object SchemaContentENameExtractor {

  val defaultElementTextENameExtractors: Map[EName, TextENameExtractor] = Map.empty

  val defaultAttributeValueTextENameExtractors: Map[EName, Map[EName, TextENameExtractor]] = {
    Map(
      EName.parse("{http://www.w3.org/2001/XMLSchema}element") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
        EName.parse("substitutionGroup") -> QNameTextENameExtractor,
        EName.parse("type") -> QNameTextENameExtractor,
        // loc-free model attribute
        EName.parse("{http://locfreexbrl.org/2005/xbrldt}typedDomainKey") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}attribute") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
        EName.parse("type") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}group") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}attributeGroup") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}restriction") -> Map(
        EName.parse("base") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}extension") -> Map(
        EName.parse("base") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}keyref") -> Map(
        EName.parse("refer") -> QNameTextENameExtractor,
      ),
    )
  }

  val defaultInstance: SchemaContentENameExtractor = {
    new SchemaContentENameExtractor(defaultElementTextENameExtractors, defaultAttributeValueTextENameExtractors)
  }
}
