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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

/**
 * {@link CacheExceptionMessages}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheExceptionMessages implements com.openexchange.i18n.LocalizableStrings {

    /**
     * Initializes a new {@link CacheExceptionMessages}.
     */
    private CacheExceptionMessages() {
        super();
    }

    // A cache error occurred: %1$s
    public static final String CACHE_ERROR_MSG = "A cache error occurred: %1$s";

    // Missing cache configuration file at location: %1$s
    public static final String MISSING_CACHE_CONFIG_FILE_MSG = "Missing cache configuration file at location: %1$s";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // Missing configuration property: %1$s
    public static final String MISSING_CONFIGURATION_PROPERTY_MSG = "Missing configuration property: %1$s";

    // The default element attributes could not be retrieved.
    public static final String FAILED_ATTRIBUTE_RETRIEVAL_MSG = "The default element attributes could not be retrieved.";

    // A put into the cache failed.
    public static final String FAILED_PUT_MSG = "Put into cache failed.";

    // Safe put into cache failed. An object bound to given key already exists.
    public static final String FAILED_SAFE_PUT_MSG = "Safe put into cache failed. An object bound to given key already exists.";

    // Remove on cache failed
    public static final String FAILED_REMOVE_MSG = "Remove on cache failed";

    // The default element attributes could not be assigned.
    public static final String FAILED_ATTRIBUTE_ASSIGNMENT_MSG = "The default element attributes could not be assigned.";

    // No cache found for region name: %1$s
    public static final String MISSING_CACHE_REGION_MSG = "No cache found for region name: %1$s";

    // Missing default auxiliary defined by property: jcs.default=<aux-name>
    public static final String MISSING_DEFAULT_AUX_MSG = "Missing default auxiliary defined by property: jcs.default=<aux-name>";

    // Invalid cache region name \"%1$s\".
    public static final String INVALID_CACHE_REGION_NAME_MSG = "Invalid cache region name \"%1$s\".";

}
