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

package com.openexchange.xing;

/**
 * {@link LeadDescription} - A lead description passed to XING on sign-up request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LeadDescription {

    private String email;
    private String lastName;
    private String firstName;
    private boolean tandcCheck;
    private Language language;

    /**
     * Initializes a new {@link LeadDescription}.
     */
    public LeadDescription() {
        super();
    }

    /**
     * Gets the E-Mail address
     *
     * @return The E-Mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the E-Mail address
     *
     * @param email The E-Mail address to set
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Gets the last name
     *
     * @return The last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last Name
     *
     * @param lastName The last name to set
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the first name
     *
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name
     *
     * @param firstName The first name to set
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the tandc check flag
     *
     * @return The tandc check flag
     */
    public boolean isTandcCheck() {
        return tandcCheck;
    }

    /**
     * Sets the tandc check flag
     *
     * @param tandcCheck The tandc check flag to set
     */
    public void setTandcCheck(final boolean tandcCheck) {
        this.tandcCheck = tandcCheck;
    }

    /**
     * Gets the language
     *
     * @return The language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the language
     *
     * @param language The language to set
     */
    public void setLanguage(final Language language) {
        this.language = language;
    }

}
