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

package com.openexchange.file.storage.search;

import static com.openexchange.java.Strings.toLowerCase;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.java.Strings;


/**
 * {@link AbstractStringSearchTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractStringSearchTerm implements SearchTerm<String> {

    /** The pattern */
    protected final String pattern;

    /** Whether to compare ignore-case or case-sensitive */
    protected final boolean ignoreCase;

    /** Whether to perform a substring or equals check */
    protected final boolean substringSearch;

    /**
     * Initializes a new {@link AbstractStringSearchTerm}.
     */
    protected AbstractStringSearchTerm(final String pattern, final boolean ignoreCase, final boolean substringSearch) {
        super();
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
        this.substringSearch = substringSearch;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    /**
     * Checks whether to perform a substring or equals check
     *
     * @return The substring-search flag
     */
    public boolean isSubstringSearch() {
        return substringSearch;
    }

    /**
     * Gets the ignore-case flag
     *
     * @return The ignore-case flag
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public boolean matches(final File file) throws OXException {
        final String str = getString(file);
        if (Strings.isEmpty(str)) {
            return false;
        }

        if (substringSearch) {
            return ignoreCase ? (toLowerCase(str).indexOf(toLowerCase(pattern)) >= 0) : (str.indexOf(pattern) >= 0);
        }

        return ignoreCase ? (toLowerCase(str).equals(toLowerCase(pattern))) : (str.equals(pattern));
    }

    /**
     * Gets the string to compare with.
     *
     * @param file The file to retrieve the string from
     * @return The string
     */
    protected abstract String getString(File file);

}
