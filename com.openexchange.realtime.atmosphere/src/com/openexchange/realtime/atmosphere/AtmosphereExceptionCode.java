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

package com.openexchange.realtime.atmosphere;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link AtmosphereExceptionCode}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public enum AtmosphereExceptionCode implements OXExceptionCode {

    /** The session information didn't match any ServerSession. */
    SESSIONINFO_DIDNT_MATCH_SERVERSESSION(AtmosphereExceptionMessage.SESSIONINFO_DIDNT_MATCH_SERVERSESSION_MSG, CATEGORY_ERROR, 1),
    /** Missing key \"%1$s\" in: \"%2$s\" */
    MISSING_KEY(AtmosphereExceptionMessage.MISSING_KEY_MSG, CATEGORY_ERROR, 2),
    /** Could not find a builder for the specified element: . \"%1$s\" */
    MISSING_BUILDER_FOR_ELEMENT(AtmosphereExceptionMessage.MISSING_BUILDER_FOR_ELEMENT_MSG, CATEGORY_ERROR, 3),
    /** Error while building Stanza: \"%1$s\" */
    ERROR_WHILE_BUILDING(AtmosphereExceptionMessage.ERROR_WHILE_BUILDING_MSG, CATEGORY_ERROR, 4),
    /** Could not find a transformer for the PayloadElement: \"%1$s\" */
    MISSING_TRANSFORMER_FOR_PAYLOADELEMENT(AtmosphereExceptionMessage.MISSING_TRANSFORMER_FOR_PAYLOADELEMENT_MSG, CATEGORY_ERROR, 5),
    /** Could not find an initializer for the specified stanza: . \"%1$s\" */
    MISSING_INITIALIZER_FOR_STANZA(AtmosphereExceptionMessage.MISSING_INITIALIZER_FOR_STANZA_MSG, CATEGORY_ERROR, 6),
    /** Error while transforming a PayloadElement: \"%1$s, %2$s\" */
    ERROR_WHILE_TRANSFORMING(AtmosphereExceptionMessage.ERROR_WHILE_TRANSFORMING_MSG, CATEGORY_ERROR, 7),
    /** Error while converting PayloadElement data: \"%1$s\" */
    ERROR_WHILE_CONVERTING(AtmosphereExceptionMessage.ERROR_WHILE_CONVERTING_MSG, CATEGORY_ERROR, 8),
    /** The following obligatory element is missing: \"%1$s\" */
    OBLIGATORY_ELEMENT_MISSING(AtmosphereExceptionMessage.OBLIGATORY_ELEMENT_MISSING_MSG, CATEGORY_ERROR, 9),
    /** Malformed Presence Data */
    PRESENCE_DATA_MALFORMED(AtmosphereExceptionMessage.PRESENCE_DATA_MALFORMED_MSG, CATEGORY_ERROR, 10),
    /** Malformed Presence Element: \"%1$s\" */
    PRESENCE_DATA_ELEMENT_MALFORMED(AtmosphereExceptionMessage.PRESENCE_DATA_ELEMENT_MALFORMED_MSG, CATEGORY_ERROR, 11),
    /** Illegal value \"%1$s\" for key \"%2$s\" in: \"%3$s\" */
    ILLEGAL_VALUE(AtmosphereExceptionMessage.ILLEGAL_VALUE_MSG, CATEGORY_ERROR, 12),
    /** Malformed POST Data \"%1$s\" */
    POST_DATA_MALFORMED(AtmosphereExceptionMessage.POST_DATA_MALFORMED_MSG, CATEGORY_ERROR, 13),
    ;

    private final String message;
    private final int number;
    private final Category category;

    private AtmosphereExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        number = detailNumber;
        this.category = category;
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
    public String getPrefix() {
        return "ATMOSPHERE";
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
