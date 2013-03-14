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

/**
 * {@link RealtimeStanzaExceptionCodes} - Stanza error codes for the realtime framework.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public enum RealtimeStanzaExceptionCodes implements OXExceptionCode {
    /** No appropriate channel found for recipient %1$s with payload namespace %2$s */
    NO_APPROPRIATE_CHANNEL(RealtimeStanzaExceptionMessages.NO_APPROPRIATE_CHANNEL, Category.EnumCategory.CONNECTIVITY, 2),
    /** The following needed service is missing: "%1$s" */
    NEEDED_SERVICE_MISSING(RealtimeStanzaExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 3),
    /** Unexpected error: %1$s */
    UNEXPECTED_ERROR(RealtimeStanzaExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 4),
    /** Invalid ID. Resource identifier is missing. */
    INVALID_ID(RealtimeStanzaExceptionMessages.INVALID_ID, CATEGORY_ERROR, 5),
    
    //--- elements from stanza error namespace http://xmpp.org/rfcs/rfc3920.html#def C.7.
    STANZA_BAD_REQUEST(RealtimeStanzaExceptionMessages.STANZA_BAD_REQUEST_MSG, CATEGORY_USER_INPUT, 6),
    STANZA_CONFILCT(RealtimeStanzaExceptionMessages.STANZA_CONFILCT_MSG, CATEGORY_SERVICE_DOWN, 7),
    STANZA_FEATURE_NOT_IMPLEMENTED(RealtimeStanzaExceptionMessages.STANZA_FEATURE_NOT_IMPLEMENTED_MSG, CATEGORY_SERVICE_DOWN, 8),
    STANZA_FORBIDDEN(RealtimeStanzaExceptionMessages.STANZA_FORBIDDEN_MSG, CATEGORY_SERVICE_DOWN, 9),
    STANZA_GONE(RealtimeStanzaExceptionMessages.STANZA_GONE_MSG, CATEGORY_SERVICE_DOWN, 10),
    STANZA_INTERNAL_SERVER_ERROR(RealtimeStanzaExceptionMessages.STANZA_INTERNAL_SERVER_ERROR_MSG, CATEGORY_SERVICE_DOWN, 11),
    STANZA_ITEM_NOT_FOUND(RealtimeStanzaExceptionMessages.STANZA_ITEM_NOT_FOUND_MSG, CATEGORY_SERVICE_DOWN, 12),
    STANZA_JID_MALFORMED(RealtimeStanzaExceptionMessages.STANZA_JID_MALFORMED_MSG, CATEGORY_SERVICE_DOWN, 13),
    STANZA_NOT_ACCEPTABLE(RealtimeStanzaExceptionMessages.STANZA_NOT_ACCEPTABLE_MSG, CATEGORY_SERVICE_DOWN, 14),
    STANZA_NOT_AUTHORIZED(RealtimeStanzaExceptionMessages.STANZA_NOT_AUTHORIZED_MSG, CATEGORY_SERVICE_DOWN, 15),
    STANZA_NOT_ALLOWED(RealtimeStanzaExceptionMessages.STANZA_NOT_ALLOWED_MSG, CATEGORY_SERVICE_DOWN, 16),
    STANZA_PAYMENT_REQUIRED(RealtimeStanzaExceptionMessages.STANZA_PAYMENT_REQUIRED_MSG, CATEGORY_SERVICE_DOWN, 17),
    STANZA_POLICY_VIOLATION(RealtimeStanzaExceptionMessages.STANZA_POLICY_VIOLATION_MSG, CATEGORY_SERVICE_DOWN, 18),
    STANZA_RECIPIENT_UNAVAILABLE(RealtimeStanzaExceptionMessages.STANZA_RECIPIENT_UNAVAILABLE_MSG, CATEGORY_SERVICE_DOWN, 19),
    STANZA_REDIRECT(RealtimeStanzaExceptionMessages.STANZA_REDIRECT_MSG, CATEGORY_SERVICE_DOWN, 20),
    STANZA_REGISTRATION_REQUIRED(RealtimeStanzaExceptionMessages.STANZA_REGISTRATION_REQUIRED_MSG, CATEGORY_SERVICE_DOWN, 21),
    STANZA_REMOTE_SERVER_NOT_FOUND(RealtimeStanzaExceptionMessages.STANZA_REMOTE_SERVER_NOT_FOUND_MSG, CATEGORY_SERVICE_DOWN, 22),
    STANZA_REMOTE_SERVER_TIMEOUT(RealtimeStanzaExceptionMessages.STANZA_REMOTE_SERVER_TIMEOUT_MSG, CATEGORY_SERVICE_DOWN, 23),
    STANZA_RESOURCE_CONSTRAINT(RealtimeStanzaExceptionMessages.STANZA_RESOURCE_CONSTRAINT_MSG, CATEGORY_SERVICE_DOWN, 24),
    STANZA_SERVICE_UNAVAILABLE(RealtimeStanzaExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 25),
    STANZA_SUBSCRIPTION_REQUIRED(RealtimeStanzaExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 26),
    STANZA_UNDEFINED_CONDITION(RealtimeStanzaExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 27),
    STANZA_UNEXPECTED_REQUEST(RealtimeStanzaExceptionMessages.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 28),
    ;
    
    private int number;

    private Category category;

    private String message;

    private RealtimeStanzaExceptionCodes(final String message, final Category category, final int detailNumber) {
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
        return "RT_STANZA";
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
