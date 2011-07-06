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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.proxy;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.proxy.exception.ProxyExceptionFactory;

/**
 * {@link ProxyExceptionCodes} - Enumeration about all {@link ProxyException}s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ProxyExceptionCodes implements OXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(ProxyExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * Duplicate proxy registration: %1$s
     */
    DUPLICATE_REGISTRATION(ProxyExceptionMessages.DUPLICATE_REGISTRATION_MSG, CATEGORY_ERROR, 2),
    /**
     * Malformed URL: %1$s
     */
    MALFORMED_URL(ProxyExceptionMessages.MALFORMED_URL_MSG, CATEGORY_ERROR, 3),
    /**
     * HTTP request to VoipNow server %1$s failed. Status line: %2$s
     */
    HTTP_REQUEST_FAILED(ProxyExceptionMessages.HTTP_REQUEST_FAILED_MSG, CATEGORY_ERROR, 4),
    /**
     * Malformed URI: %1$s
     */
    MALFORMED_URI(ProxyExceptionMessages.MALFORMED_URI_MSG, CATEGORY_ERROR, 5),
    /**
     * Invalid session identifier: %1$s
     */
    INVALID_SESSION_ID(ProxyExceptionMessages.INVALID_SESSION_ID_MSG, CATEGORY_ERROR, 6);

    private final Category category;

    private final int detailNumber;

    private final String message;

    private ProxyExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getDetailNumber() {
        return detailNumber;
    }

    public String getHelp() {
        return null;
    }

    /**
     * Creates a new twitter exception of this error type with specified message arguments.
     * 
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public ProxyException create(final Object... messageArgs) {
        return ProxyExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new twitter exception of this error type with specified cause and message arguments.
     * 
     * @param cause The cause
     * @param messageArgs The message arguments
     * @return A new twitter exception
     */
    public ProxyException create(final Throwable cause, final Object... messageArgs) {
        return ProxyExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
