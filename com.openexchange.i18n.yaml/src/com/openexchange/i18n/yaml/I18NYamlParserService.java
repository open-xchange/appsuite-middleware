/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
