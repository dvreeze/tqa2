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

package eu.cdevreeze.tqa2.docbuilder.jvm

import java.util.zip.ZipFile

/**
 * URI resolvers specifically for taxonomy packages
 * (see https://www.xbrl.org/Specification/taxonomy-package/REC-2016-04-19/taxonomy-package-REC-2016-04-19.html).
 *
 * @author Chris de Vreeze
 */
object TaxonomyPackageSaxUriResolvers {

  /**
   * Creates a UriResolver from a non-empty ZIP file collection (as ZipFile instances), each containing a taxonomy package with XML catalog.
   * Consider managing the ZipFile resources with the scala.util.Using API.
   */
  def forTaxonomyPackages(taxonomyPackageZipFiles: Seq[ZipFile]): SaxUriResolver = {
    require(taxonomyPackageZipFiles.nonEmpty, s"Expected at least one taxonomy package ZIP file, but did not get any")

    val partialUriResolvers: Seq[PartialSaxUriResolver] =
      taxonomyPackageZipFiles.map { zipFile =>
        TaxonomyPackagePartialSaxUriResolvers.forTaxonomyPackage(zipFile)
      }
    SaxUriResolvers.fromPartialSaxUriResolversWithoutFallback(partialUriResolvers)
  }

  /**
   * Creates a UriResolver from a ZIP file (as ZipFile instance) containing a taxonomy package with XML catalog.
   * Consider managing the ZipFile resource with the scala.util.Using API.
   */
  def forTaxonomyPackage(taxonomyPackageZipFile: ZipFile): SaxUriResolver = {
    forTaxonomyPackages(Seq(taxonomyPackageZipFile))
  }
}
