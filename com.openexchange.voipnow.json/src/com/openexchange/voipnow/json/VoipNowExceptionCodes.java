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

package com.openexchange.voipnow.json;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.voipnow.json.exception.VoipNowExceptionFactory;

/**
 * {@link VoipNowExceptionCodes} - Enumeration about all {@link VoipNowException}s.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum VoipNowExceptionCodes implements OXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(VoipNowExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * Missing property: %1$s
     */
    MISSING_PROPERTY(VoipNowExceptionMessages.MISSING_PROPERTY_MSG, CATEGORY_ERROR, 2),
    /**
     * Invalid property value in property "%1$s": %2$s
     */
    INVALID_PROPERTY(VoipNowExceptionMessages.INVALID_PROPERTY_MSG, CATEGORY_ERROR, 3),
    /**
     * Missing main extension attribute for user %1$s in context %2$s.
     */
    MISSING_MAIN_EXTENSION(VoipNowExceptionMessages.MISSING_MAIN_EXTENSION_MSG, CATEGORY_CONFIGURATION, 4),
    /**
     * HTTP request to VoipNow server %1$s failed. Status line: %2$s
     */
    HTTP_REQUEST_FAILED(VoipNowExceptionMessages.HTTP_REQUEST_FAILED_MSG, CATEGORY_ERROR, 5),
    /**
     * VoipNow request failed. Error code: %1$s. Error message: %2$s
     */
    VOIPNOW_REQUEST_FAILED(VoipNowExceptionMessages.VOIPNOW_REQUEST_FAILED_MSG, CATEGORY_ERROR, 6),
    /**
     * A remote error occurred: %1$s
     */
    REMOTE_ERROR(VoipNowExceptionMessages.REMOTE_ERROR_MSG, CATEGORY_ERROR, 7),
    /**
     * A SOAP fault occurred: %1$s
     */
    SOAP_FAULT(VoipNowExceptionMessages.SOAP_FAULT_MSG, CATEGORY_ERROR, 8),
    /**
     * A HTTP error occurred: %1$s
     */
    HTTP_ERROR(VoipNowExceptionMessages.HTTP_ERROR_MSG, CATEGORY_ERROR, 9),
    /**
     * Unparseable HTTP response: %1$s
     */
    UNPARSEABLE_HTTP_RESPONSE(VoipNowExceptionMessages.UNPARSEABLE_HTTP_RESPONSE_MSG, CATEGORY_ERROR, 10);

    private final Category category;

    private final int detailNumber;

    private final String message;

    private VoipNowExceptionCodes(final String message, final Category category, final int detailNumber) {
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
     * Creates a new VoipNow exception of this error type with specified message arguments.
     * 
     * @param messageArgs The message arguments
     * @return A new VoipNow exception
     */
    public VoipNowException create(final Object... messageArgs) {
        return VoipNowExceptionFactory.getInstance().create(this, messageArgs);
    }

    /**
     * Creates a new VoipNow exception of this error type with specified cause and message arguments.
     * 
     * @param cause The cause
     * @param messageArgs The message arguments
     * @return A new VoipNow exception
     */
    public VoipNowException create(final Throwable cause, final Object... messageArgs) {
        return VoipNowExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
