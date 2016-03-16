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

package com.openexchange.jslob;

import com.openexchange.exception.Category;
import com.openexchange.exception.Category.EnumCategory;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link JSlobExceptionCodes} - The error code for JSlob module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum JSlobExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An unexpected error occurred: %1$s
     */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", CATEGORY_ERROR, 1, null),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 2, null),
    /**
     * No JSlob storage found for identifier: %1$s
     */
    NOT_FOUND("No JSlob storage found for identifier: %1$s", CATEGORY_ERROR, 3, null),
    /**
     * No JSlob found for service %1$s.
     */
    NOT_FOUND_EXT("No JSlob found for service %1$s.", CATEGORY_USER_INPUT, 4, null),
    /**
     * Conflicting deletion of JSlob for service %1$s.
     */
    CONFLICT("Conflicting deletion of JSlob for service %1$s.", CATEGORY_USER_INPUT, 5, null),
    /**
     * Path does not exist: %1$s
     */
    PATH_NOT_FOUND("Path doesn't exist: %1$s", CATEGORY_USER_INPUT, 6, null),
    /**
     * Invalid path: %1$s.
     */
    INVALID_PATH("Invalid path: %1$s.", CATEGORY_USER_INPUT, 7, null),
    /**
     * Referenced JSlob %1$s must not be set for service %2$s. Nothing will be done.
     */
    SET_NOT_SUPPORTED("Referenced JSlob %1$s must not be set for service %2$s. Nothing will be done.", CATEGORY_WARNING, 8,
        JSlobExceptionMessages.SET_NOT_SUPPORTED),
    /**
     * "%1$s" is a reserved identifier. Please choose a different one.
     */
    RESERVED_IDENTIFIER("\"%1$s\" is a reserved identifier. Please choose a different one.", EnumCategory.CATEGORY_USER_INPUT, 9, null),
    /**
     * The JSlob %1$s is too big in context %2$d for user %3$d.
     */
    JSLOB_TOO_BIG("The JSlob %1$s is too big in context %2$d for user %3$d.", EnumCategory.CATEGORY_USER_INPUT, 10, null),
    /**
     * Path is protected and must not be changed: %1$s
     */
    PROTECTED("Path is protected and must not be changed: %1$s", CATEGORY_USER_INPUT, 11, null),

    ;

    /**
     * The error code prefix for JSlob exceptions.
     */
    public static final String PREFIX = "JSNCON".intern();

    private final Category category;

    private final int number;

    private final String message;

    private String displayMessage;


    private JSlobExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        number = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return number;
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

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
