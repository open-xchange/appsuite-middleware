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
