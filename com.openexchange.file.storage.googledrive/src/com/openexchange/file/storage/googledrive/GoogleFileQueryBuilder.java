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
