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

package com.openexchange.xing.access;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link XingExceptionCodes} - Enumeration of all errors.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum XingExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", Category.CATEGORY_ERROR, 1, null),
    /**
     * A XING error occurred: %1$s
     */
    XING_ERROR("A XING error occurred: %1$s", Category.CATEGORY_ERROR, 2, XingExceptionMessages.XING_ERROR_MSG),
    /**
     * Invalid XING URL: %1$s
     */
    INVALID_XING_URL("Invalid XING URL: %1$s", Category.CATEGORY_ERROR, 3, null),
    /**
     * XING URL does not denote a directory: %1$s
     */
    NOT_A_FOLDER("XING URL does not denote a directory: %1$s", Category.CATEGORY_ERROR, 4, null),
    /**
     * The XING resource does not exist: %1$s
     */
    NOT_FOUND("The XING resource does not exist: %1$s", Category.CATEGORY_ERROR, 5, XingExceptionMessages.NOT_FOUND_MSG),
    /**
     * Update denied for XING resource: %1$s
     */
    UPDATE_DENIED("Update denied for XING resource: %1$s", Category.CATEGORY_ERROR, 6, XingExceptionMessages.UPDATE_DENIED_MSG),
    /**
     * Delete denied for XING resource: %1$s
     */
    DELETE_DENIED("Delete denied for XING resource: %1$s", Category.CATEGORY_ERROR, 7, XingExceptionMessages.DELETE_DENIED_MSG),
    /**
     * XING URL does not denote a file: %1$s
     */
    NOT_A_FILE("XING URL does not denote a file: %1$s", Category.CATEGORY_ERROR, 8, null),
    /**
     * Missing file name.
     */
    MISSING_FILE_NAME("Missing file name.", Category.CATEGORY_ERROR, 12, XingExceptionMessages.MISSING_FILE_NAME_MSG),
    /**
     * Versioning not supported by XING file storage.
     */
    VERSIONING_NOT_SUPPORTED("Versioning not supported by XING file storage.", Category.CATEGORY_ERROR, 13, null),
    /**
     * Missing configuration for account "%1$s".
     */
    MISSING_CONFIG("Missing configuration for account \"%1$s\".", Category.CATEGORY_CONFIGURATION, 14,
        XingExceptionMessages.MISSING_CONFIG_MSG),
    /**
     * Bad or expired access token. Need to re-authenticate user.
     */
    UNLINKED_ERROR("Bad or expired access token. Need to re-authenticate user.", Category.CATEGORY_USER_INPUT, 15, XingExceptionMessages.UNLINKED_ERROR_MSG),
    /**
     * Invalid E-Mail address: %1$s
     */
    INVALID_EMAIL_ADDRESS("Invalid E-Mail address: %1$s", Category.CATEGORY_USER_INPUT, 16, XingExceptionMessages.INVALID_EMAIL_ADDRESS_MSG),
    /**
     * The E-Mail address already belongs to a XING user: %1$s
     */
    ALREADY_MEMBER("The E-Mail address already belongs to a XING user: %1$s", Category.CATEGORY_USER_INPUT, 17, XingExceptionMessages.ALREADY_MEMBER_MSG),
    /**
     * Invitation attempt failed for any reason
     */
    INVITATION_FAILED("Invitation attempt failed for any reason", Category.CATEGORY_USER_INPUT, 18, XingExceptionMessages.INVITATION_FAILED_MSG),
    /**
     * The E-Mail address does not belongs to a XING user: %1$s
     */
    NOT_A_MEMBER("The E-Mail address does not belongs to a XING user: %1$s", Category.CATEGORY_USER_INPUT, 19, XingExceptionMessages.NOT_A_MEMBER_MSG),
    /**
     * XING user "%1$s" is already directly connected.
     */
    ALREADY_CONNECTED("XING user \"%1$s\" is already directly connected.", Category.CATEGORY_USER_INPUT, 20, XingExceptionMessages.ALREADY_CONNECTED_MSG),
    /**
     * The URL parameters 'since' and 'until' are mutually exclusive.
     */
    MUTUALLY_EXCLUSIVE("The URL parameters 'since' and 'until' are mutually exclusive", Category.CATEGORY_ERROR, 21, null),
    /**
     * Comment size exceeds 600 chars.
     */
    COMMENT_SIZE_EXCEEDED("Comment size exceeds 600 chars.", Category.CATEGORY_USER_INPUT, 22, XingExceptionMessages.COMMENT_SIZE_EXCEEDED_MSG),
    /**
     * Status message size exceeds 420 chars.
     */
    STATUS_MESSAGE_SIZE_EXCEEDED("Status message size exceeds 420 chars.", Category.CATEGORY_USER_INPUT, 23, XingExceptionMessages.STATUS_MESSAGE_SIZE_EXCEEDED_MSG),
    /**
     * Text message size exceeds 140 chars.
     */
    TEXT_MESSAGE_SIZE_EXCEEDED("Text message size exceeds 140 chars.", Category.CATEGORY_USER_INPUT, 24, XingExceptionMessages.TEXT_MESSAGE_SIZE_EXCEEDED_MSG),
    /**
     * Already sent invitation to E-Mail address: %1$s
     */
    ALREADY_INVITED("Already sent invitation to E-Mail address: %1$s", Category.CATEGORY_USER_INPUT, 25, XingExceptionMessages.ALREADY_INVITED_MSG),
    /**
     * The mandatory field %1$s is missing in the request body.
     */
    MANDATORY_REQUEST_DATA_MISSING("The mandatory field %1$s is missing in the request body.", Category.CATEGORY_USER_INPUT, 26, XingExceptionMessages.MANDATORY_PARAMETER_MISSING_MSG),
    /**
     * The XING server is not available.
     */
    XING_SERVER_UNAVAILABLE("The XING server is not available.", Category.CATEGORY_SERVICE_DOWN, 27, XingExceptionMessages.XING_SERVER_UNAVAILABLE_MSG),
    /**
     * A XING account has already been requested for E-Mail address %1$s.
     */
    LEAD_ALREADY_EXISTS("A XING account has already been requested for E-Mail address %1$s.", Category.CATEGORY_USER_INPUT, 28, XingExceptionMessages.LEAD_ALREADY_EXISTS_MSG),
    /**
     * Insufficient privileges. The associated XING app does not hold the required permissions in order to perform the requested action.
     */
    INSUFFICIENT_PRIVILEGES("Insufficient privileges. The associated XING app does not hold the required permissions in order to perform the requested action.", Category.CATEGORY_PERMISSION_DENIED, 29, XingExceptionMessages.INSUFFICIENT_PRIVILEGES_MSG),
    /**
     * No XING OAuth access available for user %1$s in context %2$s.
     */
    NO_OAUTH_ACCOUNT("No XING OAuth access available for user %1$s in context %2$s.", Category.CATEGORY_CONFIGURATION, 30, XingExceptionMessages.NO_OAUTH_ACCOUNT_MSG),

    ;

    private final Category category;

    private final int detailNumber;

    private final String message;

    private String displayMessage;

    private XingExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
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
    public String getPrefix() {
        return "XING";
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
