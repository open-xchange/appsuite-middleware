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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.folderstorage.oauth;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link OAuthFolderErrorCodes}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public enum OAuthFolderErrorCodes implements DisplayableOXExceptionCode {

    /**
     * No account access permitted
     */
    NO_ACCOUNT_ACCESS("No account access permitted", CATEGORY_PERMISSION_DENIED, 1, 403, OAuthFolderErrorMessages.NO_ACCOUNT_ACCESS_MSG);

    private static final String HTTP_STATUS = "HTTP_STATUS";
    private static final String PREFIX = "OAUTHFLD";

    /**
     * The prefix for this error codes.
     */
    public static String prefix() {
        return PREFIX;
    }

    private final Category category;
    private final int detailNumber;
    private final String message;
    private final String displayMessage;
    private final int statuscode;

    /**
     * Initializes a new {@link OAuthFolderErrorCodes}.
     *
     * @param message
     * @param category
     * @param detailNumber
     * @param errorCode
     */
    private OAuthFolderErrorCodes(final String message, final Category category, final int detailNumber, final int errorCode) {
        this(message, category, detailNumber, errorCode, null);
    }

    /**
     * Initializes a new {@link OAuthFolderErrorCodes}.
     *
     * @param message
     * @param category
     * @param detailNumber
     * @param statuscode
     * @param displayMessage
     */
    private OAuthFolderErrorCodes(final String message, final Category category, final int detailNumber, final int statuscode, final String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.statuscode = statuscode;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
    public String getDisplayMessage() {
        return this.displayMessage;
    }

    /**
     * Gets the errorCode
     *
     * @return The errorCode
     */
    private int getHttpStatusCode() {
        return statuscode;
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
        return addStatusCode(OXExceptionFactory.getInstance().create(this, new Object[0]));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return addStatusCode(OXExceptionFactory.getInstance().create(this, (Throwable) null, args));
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return addStatusCode(OXExceptionFactory.getInstance().create(this, cause, args));
    }

    private OXException addStatusCode(OXException e) {
        e.setArgument(HTTP_STATUS, I(getHttpStatusCode()));
        return e;
    }

    public static Integer getHttpStatus(OXException e) {
        Object result = e.getArgument(HTTP_STATUS);
        return result instanceof Integer ? (Integer) result : I(-1);
    }

}
