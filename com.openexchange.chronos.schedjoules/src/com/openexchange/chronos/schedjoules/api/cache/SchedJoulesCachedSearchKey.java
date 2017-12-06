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

package com.openexchange.chronos.schedjoules.api.cache;

/**
 * {@link SchedJoulesCachedSearchKey}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCachedSearchKey {

    private final String query;
    private final String locale;
    private final int countryId;
    private final int categoryId;
    private final int maxRows;

    /**
     * Initialises a new {@link SchedJoulesCachedSearchKey}.
     */
    public SchedJoulesCachedSearchKey(final String query, final String locale, final int countryId, final int categoryId, final int maxRows) {
        super();
        this.query = query;
        this.locale = locale;
        this.countryId = countryId;
        this.categoryId = categoryId;
        this.maxRows = maxRows;
    }

    /**
     * Gets the query
     *
     * @return The query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Gets the locale
     *
     * @return The locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets the countryId
     *
     * @return The countryId
     */
    public int getCountryId() {
        return countryId;
    }

    /**
     * Gets the categoryId
     *
     * @return The categoryId
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     * Gets the maxRows
     *
     * @return The maxRows
     */
    public int getMaxRows() {
        return maxRows;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + categoryId;
        result = prime * result + countryId;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        result = prime * result + maxRows;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchedJoulesCachedSearchKey other = (SchedJoulesCachedSearchKey) obj;
        if (categoryId != other.categoryId) {
            return false;
        }
        if (countryId != other.countryId) {
            return false;
        }
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        if (maxRows != other.maxRows) {
            return false;
        }
        if (query == null) {
            if (other.query != null) {
                return false;
            }
        } else if (!query.equals(other.query)) {
            return false;
        }
        return true;
    }

}
