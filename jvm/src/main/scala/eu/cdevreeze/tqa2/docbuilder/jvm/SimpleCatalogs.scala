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
import java.io.File
import java.net.URI

import eu.cdevreeze.tqa2.docbuilder.SimpleCatalog

/**
 * Support for easy creation of SimpleCatalog instances on the JVM.
 *
 * @author Chris de Vreeze
 */
object SimpleCatalogs {

  /**
   * Creates a SimpleCatalog from locally mirrored sites, all immediately under the same local parent directory. The scheme
   * (http or https) is not mirrored, but the host names are immediate subdirectories of the local parent directory. To make
   * up for the lost scheme information, the "https" sites must be provided as 2nd parameter. The default is "http".
   * The result catalog holds entries for all sites that are locally mirrored.
   */
  def fromLocalMirrorRootDirectory(localRootDir: File, httpsSites: Set[String]): SimpleCatalog = {
    require(localRootDir.isDirectory, s"Not a directory: $localRootDir")

    val hostDirs = localRootDir.listFiles(_.isDirectory).toSeq

    val uriRewrites: Map[String, String] = hostDirs.map { hostDir =>
      val host = hostDir.getName
      val scheme = if (httpsSites.contains(host)) "https" else "http"
      val startString = new URI(scheme, "//" + host + "/", null).toString.ensuring(_.endsWith("/")) // scalastyle:off
      val rewritePrefix = hostDir.toURI.toString.ensuring(_.endsWith("/"))

      startString -> rewritePrefix
    }.toMap

    SimpleCatalog.from(uriRewrites)
  }

  /**
   * Creates a SimpleCatalog from locally mirrored sites, all immediately under the same local parent directory. The scheme
   * (http or https) is not mirrored, but the host names are immediate subdirectories of the local parent directory. To make
   * up for the lost scheme information, the schemes per site (to include in the catalog) must be given as 2nd parameter.
   * Only schemes "http" and "https" are allowed. The result catalog only holds entries for the sites for which schemes
   * have been provided (and that are locally mirrored).
   */
  def fromLocalMirrorRootDirectory(localRootDir: File, siteSchemes: Map[String, String]): SimpleCatalog = {
    require(localRootDir.isDirectory, s"Not a directory: $localRootDir")

    val hostDirs = localRootDir.listFiles(_.isDirectory).toSeq.filter(d => siteSchemes.contains(d.getName))

    val uriRewrites: Map[String, String] = hostDirs.map { hostDir =>
      val host = hostDir.getName
      val scheme = siteSchemes.getOrElse(host, sys.error(s"No scheme given for host $host"))
      require(Set("http", "https").contains(scheme), s"Unknown/unsupported scheme: $scheme")

      val startString = new URI(scheme, "//" + host + "/", null).toString.ensuring(_.endsWith("/")) // scalastyle:off
      val rewritePrefix = hostDir.toURI.toString.ensuring(_.endsWith("/"))

      startString -> rewritePrefix
    }.toMap

    SimpleCatalog.from(uriRewrites)
  }
}
