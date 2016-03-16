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

package com.openexchange.exception;


/**
 * {@link Categories} - A utility class for {@link Category}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Categories implements OXExceptionConstants {

    /**
     * Initializes a new {@link Categories}.
     */
    private Categories() {
        super();
    }

    /**
     * Resolves specified name to a known category.
     *
     * @param name The category's name
     * @return The resolved category or {@link Category#CATEGORY_ERROR} if unresolveable
     */
    public static Category getKnownCategoryByName(final String name) {
        return getKnownCategoryByName(name, CATEGORY_ERROR);
    }

    /**
     * Resolves specified name to a known category.
     *
     * @param name The category's name
     * @param fallback The fall-back value to return
     * @return The resolved category or <code>fallback</code> if unresolveable
     */
    public static Category getKnownCategoryByName(final String name, final Category fallback) {
        if (CATEGORY_ERROR.toString().equalsIgnoreCase(name)) {
            return CATEGORY_ERROR;
        }
        if (CATEGORY_CAPACITY.toString().equalsIgnoreCase(name)) {
            return CATEGORY_CAPACITY;
        }
        if (CATEGORY_CONFIGURATION.toString().equalsIgnoreCase(name)) {
            return CATEGORY_CONFIGURATION;
        }
        if (CATEGORY_CONFLICT.toString().equalsIgnoreCase(name)) {
            return CATEGORY_CONFLICT;
        }
        if (CATEGORY_CONNECTIVITY.toString().equalsIgnoreCase(name)) {
            return CATEGORY_CONNECTIVITY;
        }
        if (CATEGORY_PERMISSION_DENIED.toString().equalsIgnoreCase(name)) {
            return CATEGORY_PERMISSION_DENIED;
        }
        if (CATEGORY_SERVICE_DOWN.toString().equalsIgnoreCase(name)) {
            return CATEGORY_SERVICE_DOWN;
        }
        if (CATEGORY_TRUNCATED.toString().equalsIgnoreCase(name)) {
            return CATEGORY_TRUNCATED;
        }
        if (CATEGORY_TRY_AGAIN.toString().equalsIgnoreCase(name)) {
            return CATEGORY_TRY_AGAIN;
        }
        if (CATEGORY_USER_INPUT.toString().equalsIgnoreCase(name)) {
            return CATEGORY_USER_INPUT;
        }
        if (CATEGORY_WARNING.toString().equalsIgnoreCase(name)) {
            return CATEGORY_WARNING;
        }
        // Unknown...
        return fallback;
    }

    /**
     * Gets the former category number.
     *
     * @param category The category
     * @return The associated number or <tt>-1</tt>
     */
    public static int getFormerCategoryNumber(final Category category) {
        if (null == category) {
            return -1;
        }
        final String name = category.toString();
        if (CATEGORY_ERROR.toString().equalsIgnoreCase(name)) {
            return 8;
        }
        if (CATEGORY_CAPACITY.toString().equalsIgnoreCase(name)) {
            return 11;
        }
        if (CATEGORY_CONFIGURATION.toString().equalsIgnoreCase(name)) {
            return 2;
        }
        if (CATEGORY_CONFLICT.toString().equalsIgnoreCase(name)) {
            return 9;
        }
        if (CATEGORY_CONNECTIVITY.toString().equalsIgnoreCase(name)) {
            return 6;
        }
        if (CATEGORY_PERMISSION_DENIED.toString().equalsIgnoreCase(name)) {
            return 3;
        }
        if (CATEGORY_SERVICE_DOWN.toString().equalsIgnoreCase(name)) {
            return 5;
        }
        if (CATEGORY_TRUNCATED.toString().equalsIgnoreCase(name)) {
            return 12;
        }
        if (CATEGORY_TRY_AGAIN.toString().equalsIgnoreCase(name)) {
            return 4;
        }
        if (CATEGORY_USER_INPUT.toString().equalsIgnoreCase(name)) {
            return 1;
        }
        if (CATEGORY_WARNING.toString().equalsIgnoreCase(name)) {
            return 13;
        }
        return -1;
    }

}
