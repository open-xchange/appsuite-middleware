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

package com.openexchange.contact.picture.json;

/**
 * {@link PictureRequestParameter}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public enum PictureRequestParameter {

    /** Parameter name for the contact ID */
    CONTACT("contact_id"),

    /** Parameter name for the folder ID */
    CONTACT_FOLDER("folder_id"),

    /** Parameter name for the account ID */
    ACCOUNT_ID("account_id"),
    
    /** Parameter name for mail addresses */
    MAIL("email"),

    /** Parameter name for the user ID */
    USER("user_id"),

    /**
     * Parameter name for a guest user
     * <p>
     * Used for identification in share scenarios
     */
    GUEST_USER("user"),

    /**
     * Parameter name for a guest user in a specific context
     * <p>
     * Used for identification in share scenarios
     */
    GUEST_CONTEXT("context");

    private final String parameter;

    /**
     * Initializes a new {@link PictureRequestParameter}.
     * 
     */
    private PictureRequestParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * Gets the parameter
     *
     * @return The parameter
     */
    public String getParameter() {
        return parameter;
    }

}
