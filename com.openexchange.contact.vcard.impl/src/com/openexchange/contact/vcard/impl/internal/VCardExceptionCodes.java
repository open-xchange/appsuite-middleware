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

package com.openexchange.contact.vcard.impl.internal;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link VCardExceptionCodes}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum VCardExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", MESSAGE, Category.CATEGORY_ERROR, 1),
    /**
     * No vCard found <br />
     * No vCard could be found in the supplied file. Please use a valid vCard file and try again.
     */
    NO_VCARD("No vCard found", VCardExceptionMessages.NO_VCARD_FOUND_MSG, Category.CATEGORY_USER_INPUT, 2),
    /**
     * Validation failed for [%1$s]: %2$s (W%3$d) <br />
     * Validation failed for property \"%1$s\": %2$s
     */
    VALIDATION_FAILED("Validation failed for [%1$s]: %2$s (W%3$d)", VCardExceptionMessages.VALIDATION_FAILED_MSG, Category.CATEGORY_WARNING, 3),
    /**
     * Parser warning: %1$s <br />
     * Error reading vCard: %1$s
     */
    PARSER_ERROR("Parser warning: %1$s", VCardExceptionMessages.PARSER_ERROR_MSG, Category.CATEGORY_WARNING, 4),
    /**
     * Conversion failed for [%1$s]: %2$s <br />
     * Conversion failed for property \"%1$s\": %2$s
     */
    CONVERSION_FAILED("Conversion failed for [%1$s]: %2$s", VCardExceptionMessages.CONVERSION_FAILED_MSG, Category.CATEGORY_WARNING, 5),
    /**
     * Maximum vCard size of %1$d bytes exceeded
     * The vCard exceeds the maximum allowed size and can't be imported.
     */
    MAXIMUM_SIZE_EXCEEDED("Maximum vCard size of %1$d bytes exceeded", VCardExceptionMessages.MAXIMUM_SIZE_EXCEEDED_MSG, Category.CATEGORY_USER_INPUT, 6),
    /**
     * The original vCard could not be stored: %1$s
     */
    ORIGINAL_VCARD_NOT_STORED("The original vCard could not be stored: %1$s", MESSAGE, Category.CATEGORY_WARNING, 7),

    ;

    public static final String PREFIX = "VCARD".intern();

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    private VCardExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
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
    public boolean equals(OXException e) {
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
