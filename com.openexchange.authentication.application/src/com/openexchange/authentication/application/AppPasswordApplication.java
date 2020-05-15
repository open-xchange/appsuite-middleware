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

package com.openexchange.authentication.application;

import java.util.Arrays;

/**
 * 
 * {@link AppPasswordApplication} Contains an application type that is used to define
 * scope of permitted actions
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordApplication {

    /**
     * Return a new Builder
     * builder
     *
     * @return new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String type;
        private String displayName;
        private String[] scopes;
        private int sortOrder;

        /**
         * Initializes a new {@link Builder}.
         */
        public Builder() {
            this.displayName = "";
            this.type = "";
            this.scopes = new String[0];
            this.sortOrder = -1;
        }

        /**
         * Sets the display name for the application
         * setDisplayName
         *
         * @param displayName
         * @return Builder
         */
        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets the type for the application
         * setName
         *
         * @param type
         * @return
         */
        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the scopes for the application
         * setScope
         *
         * @param scopes
         * @return
         */
        public Builder setScopes(String[] scopes) {
            this.scopes = scopes;
            return this;
        }

        /**
         * Sets the sort order for the application for the UI
         * setSort
         *
         * @param sort
         * @return
         */
        public Builder setSort(int sort) {
            this.sortOrder = sort;
            return this;
        }

        public AppPasswordApplication build() {
            return new AppPasswordApplication(type, displayName, scopes, sortOrder);
        }

    }

    private final String type;          // Name of the application
    private final String displayName;   // Display name to the user
    private final String scopes[];         // String of permitted scopes
    private final int sort;             // Sort order for user display

    /**
     * Class to contain settings for each application that may have a specific password
     * 
     * Initializes a new {@link AppPasswordApplication}.
     * 
     * @param type Type for the application
     * @param displayName Display Name for the application
     * @param scopes restricted scopes for the application as comma-separated list
     */
    public AppPasswordApplication(String type, String displayName, String[] scopes) {
        this(type, displayName, scopes, -1);
    }

    /**
     * Initializes a new {@link AppPasswordApplication}.
     * 
     * @param type Type for the application
     * @param displayName Display Name for the application
     * @param scopes restricted scopes for the application
     * @param int Sort order for UI display
     */
    public AppPasswordApplication(String type, String displayName, String[] scopes, int sort) {
        super();
        this.displayName = displayName;
        this.type = type;
        this.scopes = scopes;
        this.sort = sort;
    }

    /**
     * Return the displayName
     * getDisplayName
     *
     * @return The display Name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Return the type of the application
     *
     * @return the type of the application
     */
    public String getType() {
        return type;
    }

    /**
     * Get the restricted scopes for the application
     * getScope
     *
     * @return the scopes of the application
     */
    public String[] getScopes() {
        return scopes;
    }

    /**
     * Return sort order
     * getSortOrder
     *
     * @return Sort order
     */
    public Integer getSortOrder() {
        if (sort == -1) {
            return null;
        }
        return Integer.valueOf(sort);
    }

    @Override
    public String toString() {
        return "AppPasswordApplication [type=" + type + ", scopes=" + Arrays.toString(scopes) + "]";
    }

}
