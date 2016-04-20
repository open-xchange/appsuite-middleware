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

package com.openexchange.rss;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link RssExceptionCodes} - Enumeration of all {@link OXException}s known in RSS module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum RssExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", null, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("I/O error '%1$s' occurred while loading RSS feed from URL: %2$s", RssExceptionMessages.GENERIC_ERROR_WITH_ARG2_MSG, CATEGORY_ERROR, 2),
    /**
     * Invalid RSS URL -- No or not well-formed XML content provided by URL: %1$s
     */
    INVALID_RSS("Invalid RSS URL or not well-formed XML content provided by URL: %1$s", RssExceptionMessages.INVALID_RSS_MSG, CATEGORY_USER_INPUT, 3),
    /**
     * HTTP error %1$s while loading RSS feed from URL: %2$s.
     */
    RSS_HTTP_ERROR("HTTP error %1$s while loading RSS feed from URL: %2$s", RssExceptionMessages.RSS_HTTP_ERROR_MSG, CATEGORY_SERVICE_DOWN, 4),
    /**
     * Timeout while reading the RSS feed from URL: %1$s
     */
    TIMEOUT_ERROR("Timeout while reading the RSS feed from URL: %1$s", RssExceptionMessages.TIMEOUT_ERROR_MSG, CATEGORY_SERVICE_DOWN, 5),
    /**
     * The RSS feed is exceeding the maximum allowed size of '%1$s'
     */
    RSS_SIZE_EXCEEDED("The RSS feed is exceeding the maximum allowed size of '%1$s' (%1$s bytes)", RssExceptionMessages.RSS_SIZE_EXCEEDED, CATEGORY_USER_INPUT, 6),
    /**
     * Cannot connect to RSS with URL: %1$s.
     */
    RSS_CONNECTION_ERROR("Cannot connect to RSS with URL: %1$s.", RssExceptionMessages.RSS_CONNECTION_ERROR_MSG, CATEGORY_USER_INPUT, 7),

    ;

    /**
     * The error code prefix for RSS module.
     */
    public static String PREFIX = "RSS";

    private String displayMessage;

    private Category category;

    private int detailNumber;

    private String message;

    private RssExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
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
    public String getPrefix() {
        return PREFIX;
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
    public OXException create(Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
