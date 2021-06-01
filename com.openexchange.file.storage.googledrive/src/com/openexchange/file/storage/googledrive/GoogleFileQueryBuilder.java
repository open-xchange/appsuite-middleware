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

package com.openexchange.file.storage.googledrive;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link GoogleFileQueryBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class GoogleFileQueryBuilder {

    private final static SimpleDateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }

    private final StringBuilder builder;

    private boolean hasStarted = true;

    /**
     * Initializes a new {@link GoogleFileQueryBuilder}.
     * 
     */
    public GoogleFileQueryBuilder() {
        super();
        builder = new StringBuilder();
        hasStarted = false;
    }

    /**
     * Initializes a new {@link GoogleFileQueryBuilder}.
     * 
     * @param query An query to extend
     */
    public GoogleFileQueryBuilder(String query) {
        super();
        builder = new StringBuilder(query);
    }

    /**
     * Extends a query to search only in subfolder
     * 
     * @param parentFolderId The identifier of the parent folder
     * @return The query
     */
    public GoogleFileQueryBuilder searchForChildren(String parentFolderId) {
        and().quote().append(parentFolderId).quote().append(" in parents");
        return this;
    }

    /**
     * Extends a query to search for a name pattern
     * 
     * @param pattern The pattern to search the name by
     * @return The query
     */
    public GoogleFileQueryBuilder containsName(String pattern) {
        return and().append(" name contains ").quote().append(pattern).quote();
    }

    /**
     * Extends a query to search for one specific name
     * 
     * @param name The file name to search
     * @return The query
     */
    public GoogleFileQueryBuilder equalsName(String name) {
        return and().append(" name = ").quote().append(name).quote();
    }

    /**
     * Extends a query to search modification timestamps greater than the given one
     * 
     * @param updateSince The timestamp of the modification date to search
     * @return The query
     */
    public GoogleFileQueryBuilder modificationDateGreaterThan(long updateSince) {
        synchronized (DATE_FORMAT) {
            return and().append(" modifiedDate > ").quote().append(DATE_FORMAT.format(new Date(updateSince))).quote();
        }
    }

    /**
     * Returns the query as String
     * 
     * @return The query
     */
    public String build() {
        return builder.toString();
    }

    // ---------------------------- HELPERS ----------------------------

    /**
     * Adds an <code>and</code> to the query if it has elements before
     * 
     * @return The {@link StringBuilder} instance
     */
    private GoogleFileQueryBuilder and() {
        if (hasStarted) {
            builder.append(" and ");
        } else {
            hasStarted = true;
        }
        return this;
    }

    private GoogleFileQueryBuilder quote() {
        builder.append('\'');
        return this;
    }

    private GoogleFileQueryBuilder append(String s) {
        builder.append(s);
        return this;
    }

}
