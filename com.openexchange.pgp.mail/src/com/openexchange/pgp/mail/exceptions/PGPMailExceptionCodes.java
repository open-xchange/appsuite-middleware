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

package com.openexchange.pgp.mail.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link PGPMailExceptionCodes}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public enum PGPMailExceptionCodes implements DisplayableOXExceptionCode {

    IO_EXCEPTION("An IO error occured: '%1$s'", CATEGORY_ERROR, 1),
    PGP_EXCEPTION("A PGP error occured: '%1$s'", CATEGORY_ERROR, 2),
    MESSAGE_EXCEPTION("Message exception occured: '%1$s'", CATEGORY_USER_INPUT, 3),
    ;

    public static final String PREFIX = "PGP-MAIL";

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int detailNumber;

    /**
     * Initializes a new {@link PGPCoreExceptionCodes}.
     *
     * @param message The error message
     * @param category The category
     * @param number The error number
     *
     */
    private PGPMailExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    /**
     * Initializes a new {@link PGPCoreExceptionCodes}.
     *
     * @param message The error message
     * @param displayMessage The displayed error message
     * @param category The category
     * @param number The error number
     */
    private PGPMailExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.detailNumber = number;
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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.exception.OXExceptionCode#equals(com.openexchange.exception.OXException)
     */
    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.exception.OXExceptionCode#getNumber()
     */
    @Override
    public int getNumber() {
        return detailNumber;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.exception.OXExceptionCode#getCategory()
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.exception.OXExceptionCode#getPrefix()
     */
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.exception.OXExceptionCode#getMessage()
     */
    @Override
    public String getMessage() {
        return message;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.exception.DisplayableOXExceptionCode#getDisplayMessage()
     */
    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}
