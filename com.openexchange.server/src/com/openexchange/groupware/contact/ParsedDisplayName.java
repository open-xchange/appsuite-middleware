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
