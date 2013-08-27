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

package com.openexchange.subscribe;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;


/**
 * {@link SubscriptionErrorMessage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public enum SubscriptionErrorMessage implements OXExceptionCode {

    /**
     * A SQL Error occurred.
     */
    SQLException(CATEGORY_ERROR, 1, SubscriptionErrorStrings.TRY_AGAIN, SubscriptionErrorStrings.SQL_ERROR),
    /**
     * A parsing error occurred: %1$s.
     */
    ParseException(CATEGORY_ERROR, 2, SubscriptionErrorStrings.WELL_FORMED, SubscriptionErrorStrings.PARSING_ERROR),
    /**
     * Can not save a given ID.
     */
    IDGiven(CATEGORY_ERROR, 3, SubscriptionErrorStrings.DONT_SET_ID, SubscriptionErrorStrings.CANT_SAVE_ID),
    /**
     * Could not find Subscription (according ID and Context).
     */
    SubscriptionNotFound(CATEGORY_USER_INPUT, 5, SubscriptionErrorStrings.PROVIDE_VALID_ID, SubscriptionErrorStrings.CANT_FIND_SUBSCRIPTION),
    /**
     * Parsing error.
     */
    ParsingError(CATEGORY_ERROR, 6, SubscriptionErrorStrings.CHECK_VALUE, SubscriptionErrorStrings.PARSING_ERROR2),
    /**
     * The login or password you entered are wrong.
     */
    INVALID_LOGIN(CATEGORY_USER_INPUT, 7, SubscriptionErrorStrings.CORRECT_PASSWORD, SubscriptionErrorStrings.WRONG_PASSWORD),
    COMMUNICATION_PROBLEM(CATEGORY_SERVICE_DOWN, 8, SubscriptionErrorStrings.CHECK_WEBSITE, SubscriptionErrorStrings.SERVICE_UNAVAILABLE),
    TEMPORARILY_UNAVAILABLE(CATEGORY_SERVICE_DOWN, 8, SubscriptionErrorStrings.TRY_AGAIN_LATER, SubscriptionErrorStrings.SERVICE_TEMPORARILY_UNAVAILABLE),
    INVALID_WORKFLOW(CATEGORY_CONFIGURATION, 9, SubscriptionErrorStrings.OUTPUT_MUST_MATCH_INPUT, SubscriptionErrorStrings.INCONSISTENT_WORKFLOW),
    INACTIVE_SOURCE(CATEGORY_CONFIGURATION, 10, SubscriptionErrorStrings.INACTIVE_SOURCE, SubscriptionErrorStrings.SUBSCRIPTION_SOURCE_CANT_PROVIDE_DATA),
    MISSING_ARGUMENT(CATEGORY_USER_INPUT, 11, SubscriptionErrorStrings.MISSING_ARGUMENT, SubscriptionErrorStrings.MISSING_ARGUMENT),
    PERMISSION_DENIED(CATEGORY_WARNING, 12, SubscriptionErrorStrings.PERMISSION_DENIED, SubscriptionErrorStrings.PERMISSION_DENIED),
    /**
     * Please specify your full E-Mail address as login name.
     */
    EMAIL_ADDR_LOGIN(CATEGORY_TRY_AGAIN, 13, SubscriptionErrorStrings.EMAIL_ADDR_LOGIN, SubscriptionErrorStrings.EMAIL_ADDR_LOGIN),
    /**
     * An unexpected error occurred: %1$s.
     */
    UNEXPECTED_ERROR(CATEGORY_ERROR, 9999, SubscriptionErrorStrings.UNEXPECTED_ERROR, SubscriptionErrorStrings.UNEXPECTED_ERROR),
    ;

    private Category category;
    private int errorCode;
    private String help;
    private String message;

    private SubscriptionErrorMessage(final Category category, final int errorCode, final String help, final String message) {
        this.category = category;
        this.errorCode = errorCode;
        this.help = help;
        this.message = message;
    }

    @Override
    public String getPrefix() {
        return "SUB";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return errorCode;
    }

    public String getHelp() {
        return help;
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
