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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ApiClientExceptions}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public enum ApiClientExceptions implements DisplayableOXExceptionCode {

    /** Can not access the link %1$s */
    NO_ACCESS("Can not access the link %1$s", Category.CATEGORY_ERROR, 1),

    /** An I/O error occurred: %1$s */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 2),

    /** The link %1$s is invalid */
    INVALID_TARGET("The link \"%1$s\" is invalid.", Category.CATEGORY_ERROR, 3, OXExceptionStrings.BAD_REQUEST),

    /** A functional necessary cookie is missing */
    MISSING_COOKIE("A functional necessary cookie is missing.", Category.CATEGORY_ERROR, 4),

    /** The targeted host %1$s is not covered by the same origin policy for %2$s . */
    NOT_SAME_ORIGIN("The targeted host %1$s is not covered by the same origin policy for %2$s.", Category.CATEGORY_ERROR, 5),

    /** The requested URL doesn't contain characteristics that identifies a certain API module. Can't communicate with unknown API module. */
    UNKOWN_API("The requested URL doesn't contain characteristics that identifies a certain API module. Can't communicate with unknown API module.", Category.CATEGORY_ERROR, 6),

    /** Unexpected error [%1$s] */
    UNEXPECTED_ERROR("Unexpected error [%1$s]", Category.CATEGORY_ERROR, 7),

    /** Unexpected error while building JSON [%1$s] */
    JSON_ERROR("Unexpected error while building JSON [%1$s]", Category.CATEGORY_ERROR, 8),

    /** A parameter for the request is missing. */
    MISSING_PARAMETER("A parameter for the request is missing.", Category.CATEGORY_ERROR, 9),

    /** The remote server responded with a client error code of %1$d. */
    CLIENT_ERROR("The remote server responded with a client error code of %1$d.", Category.CATEGORY_USER_INPUT, 400),

    /** The remote server responded with a server error code of %1$d. */
    REMOTE_SERVER_ERROR("A parameter for the request is missing.", Category.CATEGORY_SERVICE_DOWN, 500),
    ;

    /**
     * The error code prefix for password-change module.
     */
    public static final String PREFIX = "ACE";

    private final Category category;

    private final int detailNumber;

    private final String message;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link ApiClientExceptions}.
     * 
     * @param message The message
     * @param category The category
     * @param detailNumber The exception number
     */
    private ApiClientExceptions(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Initializes a new {@link ApiClientExceptions}.
     * 
     * @param message The message
     * @param category The category
     * @param detailNumber The exception number
     * @param displayMessage The display message to send to the client
     */
    private ApiClientExceptions(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
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
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
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
