/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.i18n.yaml;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link I18NYamlParserService} - Parses i18n strings out of YAML files.
 * <p>
 * Translateable string literals in .yml files require
 * <ul>
 * <li>to have their name/key end with <code>"_t10e"</code> appendix and
 * <li>the string literal is supposed to be an unfolded string wrapped by double-quotes
 * </ul>
 * E.g.
 * <pre>
 *  element01:
 *      key01: true
 *      key02_t10e: "This is a string that is translatable."
 *      key03: 1234
 *      ...
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface I18NYamlParserService {

    /**
     * Parses translatable string literals from specified YAML file.
     *
     * @param fileName The name of the YAML file
     * @return The translatable string literals
     * @throws OXException If parse attempt fails
     */
    List<String> parseTranslatablesFromFile(String fileName) throws OXException;

    /**
     * Parses translatable string literals from all YAML files contained in given directory.
     *
     * @param dirName The directory name
     * @param recursive <code>true</code> for recursively processing sub-directories; otherwise <code>false</code>
     * @return The translatable string literals from all contained YAML files
     * @throws OXException If parse attempt fails
     */
    List<String> parseTranslatablesFromDirectory(String dirName, boolean recursive) throws OXException;

}
