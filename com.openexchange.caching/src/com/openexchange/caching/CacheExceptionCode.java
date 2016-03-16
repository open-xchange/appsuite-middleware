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

package com.openexchange.caching;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The error code enumeration for cache.
 */
public enum CacheExceptionCode implements DisplayableOXExceptionCode {

    /**
     * A cache error occurred: %1$s
     */
    CACHE_ERROR("A cache error occurred: %1$s", MESSAGE, Category.CATEGORY_ERROR, 1),
    /**
     * Missing cache configuration file at location: %1$s
     */
    MISSING_CACHE_CONFIG_FILE("Missing cache configuration file at location: %1$s", MESSAGE, Category.CATEGORY_CONFIGURATION, 2),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", MESSAGE, Category.CATEGORY_ERROR, 3),
    /**
     * Missing configuration property: %1$s
     */
    MISSING_CONFIGURATION_PROPERTY("Missing configuration property: %1$s", MESSAGE, Category.CATEGORY_CONFIGURATION, 4),
    /**
     * The default element attributes could not be retrieved.
     */
    FAILED_ATTRIBUTE_RETRIEVAL("The default element attributes could not be retrieved.", MESSAGE, Category.CATEGORY_ERROR, 5),
    /**
     * 'Put' into cache failed.
     */
    FAILED_PUT("'Put' into cache failed.", MESSAGE, Category.CATEGORY_ERROR, 6),
    /**
     * 'Safe put' into cache failed. An object bound to given key already exists.
     */
    FAILED_SAFE_PUT("'Safe put' into cache failed. An object bound to given key already exists.", MESSAGE, Category.CATEGORY_ERROR, 7),
    /**
     * Remove on cache failed
     */
    FAILED_REMOVE("Remove on cache failed", MESSAGE, Category.CATEGORY_ERROR, 8),
    /**
     * The default element attributes could not be assigned.
     */
    FAILED_ATTRIBUTE_ASSIGNMENT("The default element attributes could not be assigned.", MESSAGE, Category.CATEGORY_ERROR, 9),
    /**
     * No cache found for region name: %1$s
     */
    MISSING_CACHE_REGION("No cache found for region name: %1$s", MESSAGE, Category.CATEGORY_CONFIGURATION, 10),
    /**
     * Missing default auxiliary defined by property: jcs.default=<aux-name>
     */
    MISSING_DEFAULT_AUX("Missing default auxiliary defined by property: jcs.default=<aux-name>", MESSAGE, Category.CATEGORY_CONFIGURATION, 11),
    /**
     * Invalid cache region name \"%1$s\".
     */
    INVALID_CACHE_REGION_NAME("Invalid cache region name \"%1$s\".", MESSAGE, Category.CATEGORY_ERROR, 12),
    /**
     * Method not supported.
     */
    UNSUPPORTED_OPERATION("Method not supported.", MESSAGE, Category.CATEGORY_ERROR, 13)
    ;

    /** The cache exception prefix */
    private static final String PREFIX = "CAC";

    /**
     * Gets the prefix for cache exception.
     *
     * @return The <code>"CAC"</code> prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    // ------------------------------------------------------------------------------------- //

    private final String message;

    private final String displayMessage;

    private final int detailNumber;

    private final Category category;

    private CacheExceptionCode(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public int getNumber() {
        return detailNumber;
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
