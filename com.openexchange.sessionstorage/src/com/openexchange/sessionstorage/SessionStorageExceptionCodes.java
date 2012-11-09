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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.sessionstorage;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link SessionStorageExceptionCodes} - Error codes for <b><code>com.openexchange.sessionstorage</code></b>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SessionStorageExceptionCodes implements OXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(SessionStorageExceptionMessages.UNEXPECTED_ERROR, 1, CATEGORY_ERROR),
    /**
     * No session found for identifier: %1$s
     */
    NO_SESSION_FOUND(SessionStorageExceptionMessages.NO_SESSION_FOUND, 2, CATEGORY_USER_INPUT),
    /**
     * Saving session with session identifier %1$s failed.
     */
    SAVE_FAILED(SessionStorageExceptionMessages.SAVE_FAILED_MSG, 3, Category.CATEGORY_ERROR),
    /**
     * Lookup for session with session identifier %1$s failed.
     */
    LOOKUP_FAILED(SessionStorageExceptionMessages.LOOKUP_FAILED_MSG, 4, Category.CATEGORY_ERROR),
    /**
     * Removing session with session identifier %1$s failed.
     */
    REMOVE_FAILED(SessionStorageExceptionMessages.REMOVE_FAILED_MSG, 5, Category.CATEGORY_ERROR),
    /**
     * Authentication identifier duplicate found. Existing session login: %1$s. Current denied login request: %2$s.
     */
    DUPLICATE_AUTHID(SessionStorageExceptionMessages.DUPLICATE_AUTHID_MSG, 6, Category.CATEGORY_ERROR),
    /**
     * Operation %1$s not supported.
     */
    UNSUPPORTED_OPERATION(SessionStorageExceptionMessages.UNSUPPORTED_OPERATION_MSG, 7, Category.CATEGORY_ERROR),
    /**
     * Lookup for session with alternative identifier %1$s failed.
     */
    ALTID_NOT_FOUND(SessionStorageExceptionMessages.ALTID_NOT_FOUND_MSG, 8, Category.CATEGORY_ERROR),
    /**
     * No sessions found for user %1$s in context %2$s.
     */
    NO_USERSESSIONS(SessionStorageExceptionMessages.NO_USERSESSIONS_MSG, 9, Category.CATEGORY_ERROR),
    /**
     * No sessions found for context %1$s.
     */
    NO_CONTEXTESSIONS(SessionStorageExceptionMessages.NO_CONTEXTSESSIONS_MSG, 10, Category.CATEGORY_ERROR),
    /**
     * No sessions found by random token %1$s,
     */
    RANDOM_NOT_FOUND(SessionStorageExceptionMessages.RANDOM_NOT_FOUND_MSG, 11, Category.CATEGORY_ERROR),

    ;

    private static final String PREFIX = "SST";

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int detailNumber;

    /**
     * Initializes a new {@link SessionStorageExceptionCodes}.
     */
    private SessionStorageExceptionCodes(String message, int detailNumber, Category category) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return detailNumber;
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
