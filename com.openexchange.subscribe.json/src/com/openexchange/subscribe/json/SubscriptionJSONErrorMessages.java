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

package com.openexchange.subscribe.json;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link SubscriptionJSONErrorMessages}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public enum SubscriptionJSONErrorMessages implements DisplayableOXExceptionCode {

    MISSING_PARAMETER(101, "Missing parameter %s", CATEGORY_ERROR),
    UNKNOWN_ACTION(102, "The requested action %s is not known.", CATEGORY_ERROR),
    UNKNOWN_SUBSCRIPTION(102, "The requested subscription is not known", CATEGORY_USER_INPUT, SubscriptionJSONExceptionMessage.UNKNOWN_SUBSCRIPTION_DISPLAY),

    JSONEXCEPTION(201, "A JSON error occurred", CATEGORY_ERROR),
    MISSING_FIELD(202, "Missing field(s): %s", CATEGORY_ERROR),
    MISSING_FORM_FIELD(203, "Missing form field(s): %s", CATEGORY_ERROR),

    THROWABLE(103, "An unexpected error occurred: %s", CATEGORY_ERROR),
    UNKNOWN_COLUMN(201, "Unknown column: %s", CATEGORY_USER_INPUT),

    /**
     * The operation is forbidden according to configuration.
     */
    FORBIDDEN_CREATE_MODIFY(204, "The operation is forbidden according to configuration.", CATEGORY_USER_INPUT, SubscriptionJSONExceptionMessage.FORBIDDEN_CREATE_MODIFY_MESSAGE),

    ;

    private final Category category;
    private final String message;
    private final int errorCode;
    private final String displayMessage;

    /**
     * Initializes a new {@link SubscriptionJSONErrorMessages}.
     */
    private SubscriptionJSONErrorMessages(final int errorCode, final String message, final Category category) {
        this(errorCode, message, category, null);
    }

    /**
     * Initializes a new {@link SubscriptionJSONErrorMessages}.
     */
    private SubscriptionJSONErrorMessages(final int errorCode, final String message, final Category category, final String displayMessage) {
        this.category = category;
        this.message = message;
        this.errorCode = errorCode;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "SUBH";
    }

    @Override
    public int getNumber() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * {@inheritDoc}
     */
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
