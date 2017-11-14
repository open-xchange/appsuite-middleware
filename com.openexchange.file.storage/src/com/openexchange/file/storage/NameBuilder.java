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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage;

import org.apache.commons.io.FilenameUtils;
import com.openexchange.java.Strings;

/**
 * {@link NameBuilder} - Utility class to determine a non-conflicting name through trial & error.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NameBuilder {

    /**
     * Gets the appropriate {@code NameBuilder} instance for specified name.
     * <p>
     * Provided name is either considered be a folder or a file name dependent on occurrence of extension separator character, which is a dot.
     * If contained, the name is considered be a file name; otherwise a folder name.
     *
     * @param name The name; either a folder or a file name
     * @return The appropriate {@code NameBuilder} instance
     */
    public static NameBuilder nameBuilderFor(String name) {
        if (Strings.isEmpty(name)) {
            return null;
        }

        int index = FilenameUtils.indexOfExtension(name);
        if (index < 0) {
            return new NameBuilder(name);
        }

        String baseName = name.substring(0, index);
        String extension = name.substring(index);
        return new NameBuilder(baseName, extension);
    }

    // --------------------------------------------------------------------------

    private final String baseName;
    private final String extension;
    private int count;

    /**
     * Initializes a new {@link NameBuilder}.
     *
     * @param baseName The base name; e.g. <code>"myfolder"</code>
     */
    public NameBuilder(String baseName) {
        this(baseName, null);
    }

    /**
     * Initializes a new {@link NameBuilder}.
     *
     * @param baseName The base name; e.g. <code>"myfile"</code>
     * @param extension The optional extension; e.g. <code>".txt"</code>
     */
    public NameBuilder(String baseName, String extension) {
        super();
        this.baseName = baseName;
        this.extension = Strings.isEmpty(extension) ? null : (extension.startsWith(".") ? extension : "." + extension);
        count = 0;
    }

    /**
     * Advances this instance
     *
     * @return This instance
     */
    public NameBuilder advance() {
        count++;
        return this;
    }

    @Override
    public String toString() {
        if (null == extension) {
            return count == 0 ? baseName : new StringBuilder(baseName).append(" (").append(count).append(')').toString();
        }

        StringBuilder sb = new StringBuilder(baseName);
        if (count > 0) {
            sb.append(" (").append(count).append(')');
        }
        return sb.append(extension).toString();
    }

}
