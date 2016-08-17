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
