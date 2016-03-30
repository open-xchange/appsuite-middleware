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

package com.openexchange.publish;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link PublicationErrorMessage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public enum PublicationErrorMessage implements DisplayableOXExceptionCode {

    /**
     * A SQL Error occurred.
     */
    SQL_ERROR(CATEGORY_ERROR, 1, PublicationErrorMessage.SQL_EXCEPTION_MSG, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * A parsing error occurred: %1$s.
     */
    PARSE_EXCEPTION(CATEGORY_ERROR, 2, PublicationErrorMessage.PARSE_EXCEPTION_MSG),
    /**
     * Could not load publications of type %1$s
     */
    NO_LOADER_FOUND_EXCEPTION(CATEGORY_ERROR, 3, PublicationErrorMessage.NO_LOADER_FOUND_MSG),
    /**
     * Can not save a given ID.
     */
    ID_GIVEN_EXCEPTION(CATEGORY_ERROR, 4, PublicationErrorMessage.ID_GIVEN_MSG),
    /**
     * Cannot find the publication site (according ID and Context).
     */
    PUBLICATION_NOT_FOUND_EXCEPTION(CATEGORY_USER_INPUT, 5, PublicationErrorMessage.PUBLICATION_NOT_FOUND_MSG, PublicationExceptionMessages.PUBLICATION_NOT_FOUND_MSG_DISPLAY),
    /**
     * %s has already been taken (field: %s)
     */
    UNIQUENESS_CONSTRAINT_VIOLATION_EXCEPTION(CATEGORY_USER_INPUT, 6, PublicationErrorMessage.UNIQUENESS_CONSTRAINT_VIOLATION),
    /**
     * You do not have the permissions to perform the chosen action (%s)
     */
    ACCESS_DENIED_EXCEPTION(CATEGORY_PERMISSION_DENIED, 7, PublicationErrorMessage.ACCESS_DENIED_MSG, PublicationExceptionMessages.ACCESS_DENIED_MSG_DISPLAY),
    /**
     * Published document has been deleted in meantime and therefore is no longer available.
     */
    NOT_FOUND_EXCEPTION(CATEGORY_PERMISSION_DENIED, 8, PublicationErrorMessage.NOT_FOUND_MSG, PublicationExceptionMessages.NOT_FOUND_MSG_DISPLAY),

    ;

    private static final String SQL_EXCEPTION_MSG = "A SQL error occurred.";

    private static final String PARSE_EXCEPTION_MSG = "A parsing error occurred: %1$s.";

    private static final String NO_LOADER_FOUND_MSG = "Could not load publications of type %1$s";

    private static final String ID_GIVEN_MSG = "Unable to save a given ID.";

    private static final String PUBLICATION_NOT_FOUND_MSG = "Cannot find the publication site.";

    private static final String UNIQUENESS_CONSTRAINT_VIOLATION = "%1$s has already been taken (field: %2$s)";

    private static final String ACCESS_DENIED_MSG = "You do not have the permissions to perform the chosen action (%s)";

    private static final String NOT_FOUND_MSG = "The published document has been deleted in the meantime and therefore is no longer available.";

    private Category category;
    private int errorCode;
    private String message;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link PublicationErrorMessage}.
     * 
     * @param category
     * @param errorCode
     * @param message
     */
    private PublicationErrorMessage(final Category category, final int errorCode, final String message) {
        this(category, errorCode, message, null);
    }

    /**
     * Initializes a new {@link PublicationErrorMessage}.
     * 
     * @param category
     * @param errorCode
     * @param message
     * @param displayMessage
     */
    private PublicationErrorMessage(final Category category, final int errorCode, final String message, final String displayMessage) {
        this.category = category;
        this.errorCode = errorCode;
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "PUB";
    }

    @Override
    public Category getCategory() {
        return category;
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
