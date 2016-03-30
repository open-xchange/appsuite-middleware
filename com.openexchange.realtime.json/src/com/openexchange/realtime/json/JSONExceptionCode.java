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

package com.openexchange.realtime.json;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link JSONExceptionCode}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public enum JSONExceptionCode implements DisplayableOXExceptionCode {

    /** The session information didn't match any ServerSession. */
    SESSIONINFO_DIDNT_MATCH_SERVERSESSION("The session information didn't match any ServerSession", CATEGORY_ERROR, 1, null),
    /** Missing key \"%1$s\" in: \"%2$s\" */
    MISSING_KEY("Obligatory key \"%1$s\" is missing from the Stanza", CATEGORY_ERROR, 2, null),
    /** Could not find a builder for the specified element: . \"%1$s\" */
    MISSING_BUILDER_FOR_ELEMENT("Could not find a builder for the given element: . \"%1$s\"", CATEGORY_ERROR, 3, null),
    /** Error while building Stanza: \"%1$s\" */
    ERROR_WHILE_BUILDING("Error while building Stanza: \"%1$s\"", CATEGORY_ERROR, 4, null),
    /** Could not find a transformer for the PayloadElement: \"%1$s\" */
    MISSING_TRANSFORMER_FOR_PAYLOADELEMENT("Could not find a transformer for the PayloadElement: \"%1$s\"", CATEGORY_ERROR, 5, null),
    /** Could not find an initializer for the specified stanza: . \"%1$s\" */
    MISSING_INITIALIZER_FOR_STANZA("Could not find an initializer for the given stanza: . \"%1$s\"", CATEGORY_ERROR, 6, null),
    /** Error while transforming a PayloadElement: \"%1$s, %2$s\" */
    ERROR_WHILE_TRANSFORMING("Error while transforming a PayloadElement: \"%1$s, %2$s\"", CATEGORY_ERROR, 7, null),
    /** Error while converting PayloadElement data: \"%1$s\" */
    ERROR_WHILE_CONVERTING("Error while converting PayloadElement data: \"%1$s\"", CATEGORY_ERROR, 8, null),
    /** The following obligatory element is missing: \"%1$s\" */
    OBLIGATORY_ELEMENT_MISSING("The following obligatory element is missing: \"%1$s\"", CATEGORY_ERROR, 9, null),
    /** Malformed Presence Data */
    PRESENCE_DATA_MALFORMED("Malformed Presence Data", CATEGORY_ERROR, 10, null),
    /** Malformed Presence Element: \"%1$s\" */
    PRESENCE_DATA_ELEMENT_MALFORMED("Malformed Presence Element: \"%1$s\"", CATEGORY_ERROR, 11, null),
    /** Illegal value \"%1$s\" for key \"%2$s\" in: \"%3$s\" */
    ILLEGAL_VALUE("Illegal value \"%1$s\" for key \"%1$s\" in: \"%3$s\"", CATEGORY_ERROR, 12, null),
    /** Malformed POST Data \"%1$s\" */
    POST_DATA_MALFORMED("Malformed POST Data", CATEGORY_ERROR, 13, null),
    ;

    private final String message;
    private final int number;
    private final Category category;
    private String displayMessage;

    private JSONExceptionCode(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        number = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
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
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public String getPrefix() {
        return "RT_JSON";
    }

    @Override
    public String getMessage() {
        return message;
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
