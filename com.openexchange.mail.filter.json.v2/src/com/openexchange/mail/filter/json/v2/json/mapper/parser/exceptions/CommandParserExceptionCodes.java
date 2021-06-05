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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link CommandParserExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CommandParserExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * No parser found under the specified key '%1$s'
     */
    UNKNOWN_PARSER("No parser found under the specified key '%1$s'", CATEGORY_ERROR, 1),
    /**
     * Unable to encode the field '%1$s' of the '%2$s' rule
     */
    UNABLE_TO_ENCODE("Unable to encode the field '%1$s' of the '%2$s' rule", CATEGORY_ERROR, 2),
    /**
     * Unable to decode the field '%1$s' of the '%2$s' rule
     */
    UNABLE_TO_DECODE("Unable to decode the field '%1$s' of the '%2$s' rule", CATEGORY_ERROR, 3),
    /**
     * No simplified rule found for the specified key '%1$s'
     */
    UNKOWN_SIMPLIFIED_RULE("No simplified rule found for the specified key '%1$s'", CATEGORY_ERROR, 4),
    /**
     * The filter action '%1$s' is not allowed
     */
    NOT_ALLOWED("This filter action is not allowed: '%1$s'", CATEGORY_USER_INPUT, 5)

    ;

    private static final String PREFIX = "MAIL-FILTER";

    private final String message;

    private final Category category;

    private final int number;

    private final String displayMessage;

    private CommandParserExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    private CommandParserExceptionCodes(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.category = category;
        number = detailNumber;
        this.displayMessage = displayMessage;

    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(final OXException e) {
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
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

}
