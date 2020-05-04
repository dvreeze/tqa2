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

package eu.cdevreeze.tqa2.internal.converttaxonomy

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor
import eu.cdevreeze.yaidom2.utils.namespaces.ENameFinder

/**
 * Schema import generator, looking at used namespaces for which an xs:import is needed, and generating those
 * xs:import elements, adding them to the schema given.
 *
 * @author Chris de Vreeze
 */
final class SchemaImportGenerator(val namespacePrefixMapper: NamespacePrefixMapper, val documentENameExtractor: DocumentENameExtractor) {

  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(namespacePrefixMapper)

  import elemCreator._
  import nodebuilder.NodeBuilderCreator._

  /**
   * Generates and adds xs:import elements for namespaces used in schema content. Already existing xs:import
   * elements are not removed. Schema location attributes are not added!
   */
  def generateAndAddXsImports(schema: BackingNodes.Elem): nodebuilder.Elem = {
    require(schema.name == ENames.XsSchemaEName, s"Expected an xs:schema, but got a ${schema.name}")
    require(
      schema.findAllDescendantElemsOrSelf.forall(_.scope.defaultNamespaceOption.isEmpty),
      s"Default namespace used, which is not allowed (in ${schema.docUri})"
    )

    val namespacesUsedInSchemaContent: Seq[String] = ENameFinder.findAllNamespaces(schema, documentENameExtractor).toSeq.sorted

    val alreadyImportedNamespaces: Set[String] =
      schema.filterChildElems(_.name == ENames.XsImportEName).map(_.attr(ENames.NamespaceEName)).toSet

    val tnsSet: Set[String] = schema.attrOption(ENames.TargetNamespaceEName).toSet

    val namespacesToImport: Seq[String] = namespacesUsedInSchemaContent
      .filterNot(alreadyImportedNamespaces)
      .filterNot(tnsSet)
      .filterNot(Set(Namespaces.XsNamespace))

    val schemaBuilder: nodebuilder.Elem = nodebuilder.Elem.from(schema)

    namespacesToImport.foldLeft(schemaBuilder) {
      case (accSchema, ns) =>
        addXsImport(accSchema, ns)
    }
  }

  def addXsImport(schema: nodebuilder.Elem, namespace: String): nodebuilder.Elem = {
    assert(schema.name == ENames.XsSchemaEName)

    val xsImport: nodebuilder.Elem =
      emptyElem(ENames.XsImportEName, schema.prefixedScope).creationApi
        .plusAttribute(ENames.NamespaceEName, namespace)
        .underlying

    addXsImport(schema, xsImport)
  }

  def addXsImport(schema: nodebuilder.Elem, namespace: String, schemaLocation: URI): nodebuilder.Elem = {
    assert(schema.name == ENames.XsSchemaEName)

    val xsImport: nodebuilder.Elem =
      emptyElem(ENames.XsImportEName, schema.prefixedScope).creationApi
        .plusAttribute(ENames.NamespaceEName, namespace)
        .plusAttribute(ENames.SchemaLocationEName, schemaLocation.toString)
        .underlying

    addXsImport(schema, xsImport)
  }

  private def addXsImport(schema: nodebuilder.Elem, xsImport: nodebuilder.Elem): nodebuilder.Elem = {
    assert(schema.name == ENames.XsSchemaEName)
    assert(xsImport.name == ENames.XsImportEName)

    val namespace: String = xsImport.attr(ENames.NamespaceEName)

    if (schema.filterChildElems(_.name == ENames.XsImportEName).exists(_.attr(ENames.NamespaceEName) == namespace)) {
      schema
    } else {
      val childIndex: Int = schema.children.takeWhile {
        case che: nodebuilder.Elem =>
          (che.name == ENames.XsAnnotationEName) || (che.name == ENames.XsImportEName && che.attr(ENames.NamespaceEName) < namespace)
        case _ =>
          true
      }.size

      schema.creationApi.plusChild(childIndex, xsImport).underlying
    }
  }
}

object SchemaImportGenerator {

  def apply(namespacePrefixMapper: NamespacePrefixMapper, documentENameExtractor: DocumentENameExtractor): SchemaImportGenerator = {
    new SchemaImportGenerator(namespacePrefixMapper, documentENameExtractor)
  }
}
