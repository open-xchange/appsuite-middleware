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


package com.openexchange.push.udp;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

public enum PushUDPExceptionCode implements DisplayableOXExceptionCode {
    /**
     * Push UDP Exception.
     */
    PUSH_UDP_EXCEPTION("Push UDP Exception.", 1, CATEGORY_ERROR, null),
    /**
     * Missing Push UDP configuration.
     */
    MISSING_CONFIG("Missing Push UDP configuration.", 2, CATEGORY_CONFIGURATION, null),
    /**
     * User ID is not a number: %1$s.
     */
    USER_ID_NAN("User ID is not a number: %1$s.", 3, CATEGORY_ERROR, null),
    /**
     * Context ID is not a number: %1$s.
     */
    CONTEXT_ID_NAN("Context ID is not a number: %1$s.", 4, CATEGORY_ERROR, null),
    /**
     * Magic bytes are not a number: %1$s.
     */
    MAGIC_NAN("Magic bytes are not a number: %1$s.", 5, CATEGORY_ERROR, null),
    /**
     * Invalid Magic bytes: %1$s.
     */
    INVALID_MAGIC("Invalid Magic bytes: %1$s.", 6, CATEGORY_ERROR, null),
    /**
     * Folder ID is not a number: %1$s.
     */
    FOLDER_ID_NAN("Folder ID is not a number: %1$s.", 7, CATEGORY_ERROR, null),
    /**
     * Module is not a number: %1$s.
     */
    MODULE_NAN("Module is not a number: %1$s.", 8, CATEGORY_ERROR, null),
    /**
     * Port is not a number: %1$s.
     */
    PORT_NAN("Port is not a number: %1$s.", 9, CATEGORY_ERROR, null),
    /**
     * Request type is not a number: %1$s.
     */
    TYPE_NAN("Request type is not a number: %1$s.", 10, CATEGORY_ERROR, null),
    /**
     * Length is not a number: %1$s.
     */
    LENGTH_NAN("Length is not a number: %1$s.", 11, CATEGORY_ERROR, null),
    /**
     * Invalid user IDs: %1$s.
     */
    INVALID_USER_IDS("Invalid user IDs: %1$s.", 12, CATEGORY_ERROR, null),
    /**
     * Unknown request type: %1$s.
     */
    INVALID_TYPE("Unknown request type: %1$s.", 13, CATEGORY_ERROR, null),
    /**
     * Missing payload in datagram package.
     */
    MISSING_PAYLOAD("Missing payload in datagram package.", 14, CATEGORY_ERROR, null),
    /**
     * No UDP channel is configured.
     */
    NO_CHANNEL("No UDP channel is configured.", 15, CATEGORY_ERROR, null),
    /**
     * Unable to resolve the specified hostname '%1$s' to an IP address.
     */
    UNRESOLVABLE_HOSTNAME("Unable to resolve the specified hostname '%1$s' to an IP address.", 16, CATEGORY_ERROR, null);
    ;

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int detailNumber;
    
    private String displayMessage;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private PushUDPExceptionCode(final String message, final int detailNumber, final Category category, String displayMessage) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return "PUSHUDP";
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
