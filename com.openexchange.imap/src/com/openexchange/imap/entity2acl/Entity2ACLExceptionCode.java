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

package com.openexchange.imap.entity2acl;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

public enum Entity2ACLExceptionCode implements DisplayableOXExceptionCode {

    /**
     * Implementing class could not be found
     */
    CLASS_NOT_FOUND("Implementing class could not be found", Category.CATEGORY_ERROR, 1, null),
    /**
     * An I/O error occurred while creating the socket connection to IMAP server (%1$s): %2$s
     */
    CREATING_SOCKET_FAILED("An I/O error occurred while creating the socket connection to IMAP server (%1$s): %2$s",
        Category.CATEGORY_SERVICE_DOWN, 2, null),
    /**
     * Instantiating the class failed.
     */
    INSTANTIATION_FAILED("Instantiating the class failed.", Category.CATEGORY_ERROR, 3, null),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", Category.CATEGORY_SERVICE_DOWN, 4, null),
    /**
     * Missing property %1$s in system.properties.
     */
    MISSING_SETTING("Missing property %1$s in imap.properties.", Category.CATEGORY_CONFIGURATION, 5, null),
    /**
     * Unknown IMAP server: %1$s
     */
    UNKNOWN_IMAP_SERVER("Unknown IMAP server: %1$s", Category.CATEGORY_ERROR, 6, null),
    /**
     * Missing IMAP server arguments to resolve IMAP login to a user
     */
    MISSING_ARG("Missing IMAP server arguments to resolve IMAP login to a user", Category.CATEGORY_ERROR, 7, null),
    /**
     * IMAP login %1$s could not be resolved to a user
     */
    RESOLVE_USER_FAILED("IMAP login %1$s could not be resolved to a user", Category.CATEGORY_ERROR, 8, null),
    /**
     * User %1$s from context %2$s is not known on IMAP server "%3$s".
     */
    UNKNOWN_USER("User %1$s from context %2$s is not known on IMAP server \"%3$s\".", Category.CATEGORY_ERROR, 9, null),
    /**
     * Group %1$s from context %2$s is not known on IMAP server \"%3$s\" (display name=%4$s).
     */
    UNKNOWN_GROUP("Group %1$s from context %2$s is not known on IMAP server \"%3$s\" (display name=%4$s).", Category.CATEGORY_USER_INPUT, 10, Entity2ACLExceptionMessages.UNKNOWN_GROUP_MSG),
    ;

    private final Category category;
    private final String message;
    private final int number;
    private final String displayMessage;

    /**
     * Default constructor.
     */
    private Entity2ACLExceptionCode(String message, Category category, int detailNumber, String displayMessage) {
        this.message = message;
        this.category = category;
        this.number = detailNumber;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "ACL";
    }

    /**
     * @return the category.
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * @return the message.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @return the number.
     */
    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
