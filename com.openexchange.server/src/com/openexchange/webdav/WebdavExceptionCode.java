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

package com.openexchange.webdav;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.EnumComponent;

public enum WebdavExceptionCode implements OXExceptionCode {

    /**
     * Invalid value in element &quot;%1$s&quot;: %2$s.
     */
    INVALID_VALUE(WebdavExceptionMessage.INVALID_VALUE_MSG, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred.
     */
    IO_ERROR(WebdavExceptionMessage.IO_ERROR_MSG, CATEGORY_ERROR, 2),
    /**
     * Missing field %1$s.
     */
    MISSING_FIELD(WebdavExceptionMessage.MISSING_FIELD_MSG, CATEGORY_ERROR, 3),
    /**
     * Missing header field %1$s.
     */
    MISSING_HEADER_FIELD(WebdavExceptionMessage.MISSING_HEADER_FIELD_MSG, CATEGORY_ERROR, 4),
    /**
     * Invalid action %1$s.
     */
    INVALID_ACTION(WebdavExceptionMessage.INVALID_ACTION_MSG, CATEGORY_ERROR, 5),
    /**
     * %1$s is not a number.
     */
    NOT_A_NUMBER(WebdavExceptionMessage.NOT_A_NUMBER_MSG, CATEGORY_ERROR, 6),
    /**
     * No principal found: %1$s.
     */
    NO_PRINCIPAL(WebdavExceptionMessage.NO_PRINCIPAL_MSG, CATEGORY_ERROR, 7),
    /**
     * Empty passwords are not allowed.
     */
    EMPTY_PASSWORD(WebdavExceptionMessage.EMPTY_PASSWORD_MSG, CATEGORY_USER_INPUT, 8),
    /**
     * Unsupported authorization mechanism in "Authorization" header: %1$s.
     */
    UNSUPPORTED_AUTH_MECH(WebdavExceptionMessage.UNSUPPORTED_AUTH_MECH_MSG, CATEGORY_ERROR, 9),
    /**
     * Resolving user name "%1$s" failed.
     */
    RESOLVING_USER_NAME_FAILED(WebdavExceptionMessage.RESOLVING_USER_NAME_FAILED_MSG, CATEGORY_ERROR, 10),
    /**
     * Authentication failed for user name: %1$s
     */
    AUTH_FAILED(WebdavExceptionMessage.AUTH_FAILED_MSG, CATEGORY_ERROR, 11),
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR(WebdavExceptionMessage.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 11);

    private final String message;

    private final int detailNumber;

    private final Category category;

    private WebdavExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return EnumComponent.WEBDAV.getAbbreviation();
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
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
