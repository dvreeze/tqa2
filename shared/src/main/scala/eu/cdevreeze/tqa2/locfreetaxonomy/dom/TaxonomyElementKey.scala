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

import eu.cdevreeze.tqa2.common.locfreexlink

/**
 * Taxonomy element key in a locator-free taxonomy.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyElementKey extends locfreexlink.XLinkResource {
  // TODO
}

object TaxonomyElementKey {

  // General categories of taxonomy element keys

  trait SchemaComponentKey extends TaxonomyElementKey {
    // TODO
  }

  trait AppinfoContentKey extends TaxonomyElementKey {
    // TODO
  }

  // Specific taxonomy element keys

  trait ConceptKey extends SchemaComponentKey {
    // TODO
  }

  trait ElementKey extends SchemaComponentKey {
    // TODO
  }

  trait TypeKey extends SchemaComponentKey {
    // TODO
  }

  trait RoleKey extends AppinfoContentKey {
    // TODO
  }

  trait ArcroleKey extends AppinfoContentKey {
    // TODO
  }

  trait AnyElementKey extends TaxonomyElementKey {
    // TODO
  }
}
