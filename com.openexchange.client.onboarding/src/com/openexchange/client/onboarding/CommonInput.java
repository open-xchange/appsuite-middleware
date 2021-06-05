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

package com.openexchange.client.onboarding;

/**
 * {@link CommonInput} - An enumeration for common key-value-pairs input.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum CommonInput {

    /**
     * The common input in case the user is not supposed to enter anything.
     */
    NONE(new String[0]),
    /**
     * The common input in case the user is supposed to enter an E-Mail address.
     */
    EMAIL_ADDRESS("email"),
    /**
     * The common input in case the user is supposed to enter a phone number.
     */
    PHONE_NUMBER("number"),

    ;

    private final String[] elementNames;
    private final String firstElementName;

    private CommonInput(String... elementNames) {
        if (null != elementNames && elementNames.length > 0) {
            // First form element
            {
                String name = elementNames[0];
                firstElementName = name;
            }

            this.elementNames = elementNames;
        } else {
            this.firstElementName = null;
            this.elementNames = null;
        }
    }

    /**
     * Gets the element names
     *
     * @return The element names or <code>null</code>
     */
    public String[] getElementNames() {
        return elementNames;
    }

    /**
     * Gets the name of the first element
     *
     * @return The name of the first element or <code>null</code>
     */
    public String getFirstElementName() {
        return firstElementName;
    }

}
