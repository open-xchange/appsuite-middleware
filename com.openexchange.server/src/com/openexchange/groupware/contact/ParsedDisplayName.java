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

package com.openexchange.groupware.contact;

import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;

/**
 * {@link ParsedDisplayName}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ParsedDisplayName {

    private String displayName;
    private String givenName;
    private String surName;

    /**
     * Initializes a new {@link ParsedDisplayName}.
     *
     * @param The display name to parse
     */
    public ParsedDisplayName(String displayName) {
        super();
        this.displayName = displayName;
        parse(displayName);
    }

    /**
     * Applies the parsed display name to the supplied contact.
     *
     * @param contact The contact to apply the parsed names to
     * @return The contact
     */
    public Contact applyTo(Contact contact) {
        if (null != contact) {
            contact.setDisplayName(displayName);
            contact.setGivenName(givenName);
            contact.setSurName(surName);
        }
        return contact;
    }

    /**
     * Gets the parsed given name
     *
     * @return The given name
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Gets the original display name
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the parsed surname
     *
     * @return The surname
     */
    public String getSurName() {
        return surName;
    }

    private void parse(String displayName) {
        this.displayName = displayName;
        while (0 < displayName.length() && ('<' == displayName.charAt(0) || '"' == displayName.charAt(0) || '\'' == displayName.charAt(0))) {
            displayName = displayName.substring(1);
        }
        while (0 < displayName.length() && ('>' == displayName.charAt(displayName.length() - 1) ||
            '"' == displayName.charAt(displayName.length() - 1) || '\'' == displayName.charAt(displayName.length() - 1))) {
            displayName = displayName.substring(0, displayName.length() - 1);
        }
        if (Strings.isEmpty(displayName)) {
            return;
        }
        String[] splitted;
        if (0 < displayName.indexOf(',')) {
            splitted = Strings.splitByComma(displayName.trim());
            com.openexchange.tools.arrays.Arrays.reverse(splitted);
        } else {
            splitted = Strings.splitByWhitespaces(displayName.trim());
        }
        parse(splitted);
    }

    private void parse(String[] splitted) {
        if (1 == splitted.length) {
            givenName = splitted[0].trim();
        } else if (2 == splitted.length) {
            givenName = splitted[0].trim();
            surName = splitted[1].trim();
        } else {
            for (int i = 0; i < splitted.length - 1; i++) {
                String name = splitted[i].trim();
                givenName = null == givenName ? name : givenName + ' ' + name;
            }
            surName = splitted[splitted.length - 1].trim();
        }
    }

}
