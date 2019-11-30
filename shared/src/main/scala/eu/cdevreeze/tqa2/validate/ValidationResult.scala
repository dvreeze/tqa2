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

/**
 * Validation result for a rule. When validating a rule, multiple validation results may be generated. The result should
 * be a case class instance, or a built-in Java type such as String. The validation result is typically an error result.
 *
 * TODO Restrictions on nested result.
 *
 * TODO Error versus warning.
 *
 * @author Chris de Vreeze
 */
final case class ValidationResult(rule: Rule, resultMessage: String, resultData: Any)
