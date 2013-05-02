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

package com.openexchange.realtime;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.realtime.exception.RealtimeExceptionMessages;

/**
 * {@link RealtimeStreamExceptionCodes} - Error codes for realtime framework.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public enum RealtimeStreamExceptionCodes implements OXExceptionCode {
    /** No appropriate channel found for recipient %1$s with payload namespace %2$s */
    NO_APPROPRIATE_CHANNEL(RealtimeExceptionMessages.NO_APPROPRIATE_CHANNEL, Category.EnumCategory.CONNECTIVITY, 2),
    /** The following needed service is missing: "%1$s" */
    NEEDED_SERVICE_MISSING(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 3),
    /** Unexpected error: %1$s */
    UNEXPECTED_ERROR(RealtimeExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 4),
    /** Invalid ID. Resource identifier is missing. */
    INVALID_ID(RealtimeExceptionMessages.INVALID_ID, CATEGORY_ERROR, 5),

    STREAM_BAD_FORMAT(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 1),
    STREAM_BAD_NAMESPACE_PREFIX(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 2),
    STREAM_CONFLICT(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 3),
    STREAM_CONNECTION_TIMEOUT(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 4),
    STREAM_HOST_GONE(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 5),
    STREAM_HOST_UNKNOWN(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 6),
    STREAM_IMPROPER_ADDRESSING(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 7),
    STREAM_INTERNAL_SERVER_ERROR(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 8),
    STREAM_INVALID_FROM(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 9),
    STREAM_INVALID_ID(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 10),
    STREAM_INVALID_NAMESPACE(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 10),
    STREAM_INVALID_XML(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 11),
    STREAM_NOT_AUTHORIZED(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 12),
    STREAM_POLICY_VIOLATION(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 13),
    STREAM_REMOTE_CONNECT_FAILED(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 14),
    STREAM_RESOURCE_CONSTRAINT(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 15),
    STREAM_RESTRICTED_XML(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 16),
    STREAM_SEE_OTHER_HOST(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 17),
    STREAM_SYSTEM_SHUTDOWN(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 18),
    STREAM_UNDEFINED_CONDITION(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 19),
    STREAM_UNSUPPORTED_ENCODING(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 20),
    STREAM_UNSUPPORTED_STANZA_TYPE(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 21),
    STREAM_UNSUPPORTED_VERSION(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 22),
    STREAM_XML_NOT_WELL_FORMED(RealtimeExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 23)
    ;

    private int number;

    private Category category;

    private String message;

    private RealtimeStreamExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.number = detailNumber;
        this.category = category;
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
        return "RT_STREAM";
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
