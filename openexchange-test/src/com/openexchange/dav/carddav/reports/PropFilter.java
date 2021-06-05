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

package com.openexchange.dav.carddav.reports;

/**
 * {@link PropFilter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class PropFilter {

    private String name;
    private String matchType;
    private String textMatch;

    /**
     * Initializes a new {@link PropFilter}.
     * 
     * @param name
     * @param matchType
     * @param textMatch
     */
    public PropFilter(String name, String matchType, String textMatch) {
        super();
        this.name = name;
        this.matchType = matchType;
        this.textMatch = textMatch;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the matchType
     *
     * @return The matchType
     */
    public String getMatchType() {
        return matchType;
    }

    /**
     * Sets the matchType
     *
     * @param matchType The matchType to set
     */
    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    /**
     * Gets the textMatch
     *
     * @return The textMatch
     */
    public String getTextMatch() {
        return textMatch;
    }

    /**
     * Sets the textMatch
     *
     * @param textMatch The textMatch to set
     */
    public void setTextMatch(String textMatch) {
        this.textMatch = textMatch;
    }

}
