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

package com.openexchange.twitter;

import twitter4j.TwitterException;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link TwitterExceptionCodes} - Enumeration about all {@link TwitterException}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum TwitterExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", Category.CATEGORY_ERROR, 1, null),
    /**
     * Missing property: %1$s
     */
    MISSING_PROPERTY("Missing property: %1$s", Category.CATEGORY_ERROR, 2, null),
    /**
     * Invalid property value in property "%1$s": %2$s
     */
    INVALID_PROPERTY("Invalid property value in property \"%1$s\": %2$s", Category.CATEGORY_ERROR, 3, null),
    /**
     * The consumer key/consumer secret pair is missing in configuration.
     */
    MISSING_CONSUMER_KEY_SECRET("The consumer key/consumer secret pair is missing in configuration.", Category.CATEGORY_ERROR, 4, null),
    /**
     * The access token for twitter user %1$s could not be obtained.
     */
    ACCESS_TOKEN_FAILED("The access token for twitter user %1$s could not be obtained.", Category.CATEGORY_ERROR, 5, null),
    /**
     * The configured consumer key/consumer secret pair is invalid. Please provide a valid consumer key/consumer secret through
     * configuration.
     */
    INVALID_CONSUMER_KEY_SECRET("The configured consumer key/consumer secret pair is invalid. Please provide a valid consumer key/consumer"
        + " secret through configuration.", Category.CATEGORY_ERROR, 6, null),
    /**
     * Please (re-)authorize your Twitter accounts.<br>
     * Twitter responded with: %1$s
     */
    REAUTHORIZE_ERROR("Please (re-)authorize your Twitter accounts.\nTwitter responded with: %1$s", CATEGORY_USER_INPUT, 7,
        TwitterExceptionMessages.REAUTHORIZE_ERROR_MSG),
    /**
     * The request is understood, but it has been refused or access is not allowed: %1$s
     */
    DENIED_ERROR("The request is understood, but it has been refused or access is not allowed: %1$s", CATEGORY_USER_INPUT, 8,
        TwitterExceptionMessages.DENIED_ERROR_MSG),
    /**
     * Invalid format in search query.
     */
    INVALID_QUERY("Invalid format in search query.", CATEGORY_USER_INPUT, 9, TwitterExceptionMessages.INVALID_QUERY_MSG),

    ;

    private final Category category;

    private final int detailNumber;

    private final String message;
    
    private String displayMessage;

    private TwitterExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return "TWITTER";
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
    public String getDisplayMessage() {
        return displayMessage;
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
