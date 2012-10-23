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

package com.openexchange.sessionstorage.hazelcast.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link OXHazelcastSessionStorageExceptionCodes}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public enum OXHazelcastSessionStorageExceptionCodes implements OXExceptionCode {

    HAZELCAST_SESSIONSTORAGE_START_FAILED(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_START_FAILED_MSG, 1, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_SAVE_FAILED(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_SAVE_FAILED_MSG, 2, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_LOOKUP_FAILED(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_LOOKUP_FAILED_MSG, 3, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_REMOVE_FAILED(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_REMOVE_FAILED_MSG, 4, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_SESSION_NOT_FOUND(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_SESSION_NOT_FOUND_MSG, 5, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_NO_ENCRYPTION_KEY(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_NO_ENCRYPTION_KEY_MSG, 6, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_DUPLICATE_AUTHID(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_DUPLICATE_AUTHID_MSG, 7, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_UNSUPPORTED_OPERATION(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_UNSUPPORTED_OPERATION_MSG, 8, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_ALTID_NOT_FOUND_MSG, 9, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_NO_USERSESSIONS(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_NO_USERSESSIONS_MSG, 10, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_NO_CONTEXTESSIONS(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_NO_CONTEXTSESSIONS_MSG, 11, Category.CATEGORY_ERROR),
    HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND(OXHazelcastSessionStorageExceptionMessages.HAZELCAST_SESSIONSTORAGE_RANDOM_NOT_FOUND_MSG, 12, Category.CATEGORY_ERROR);

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
     * Initializes a new {@link OXHazelcastSessionStorageExceptionCodes}.
     */
    private OXHazelcastSessionStorageExceptionCodes(String message, int detailNumber, Category category) {
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
        return "SES";
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
