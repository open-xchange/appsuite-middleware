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

package com.openexchange.webdav;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link WebdavExceptionMessage}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class WebdavExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link WebdavExceptionMessage}.
     */
    public WebdavExceptionMessage() {
        super();
    }

    /**
     * Invalid value in element &quot;%1$s&quot;: %2$s.
     */
    public final static String INVALID_VALUE_MSG = "Invalid value in element \"%1$s\": %2$s.";

    /**
     * An I/O error occurred.
     */
    public final static String IO_ERROR_MSG = "An I/O error occurred.";

    /**
     * Missing field %1$s.
     */
    public final static String MISSING_FIELD_MSG = "Missing field %1$s.";

    /**
     * Missing header field %1$s.
     */
    public final static String MISSING_HEADER_FIELD_MSG = "Missing header field %1$s.";

    /**
     * Invalid action %1$s.
     */
    public final static String INVALID_ACTION_MSG = "Invalid action %1$s.";

    /**
     * %1$s is not a number.
     */
    public final static String NOT_A_NUMBER_MSG = "%1$s is not a number.";

    /**
     * No principal found: %1$s.
     */
    public final static String NO_PRINCIPAL_MSG = "No principal found: %1$s.";

    /**
     * Empty passwords are not allowed.
     */
    public final static String EMPTY_PASSWORD_MSG = "Empty passwords are not allowed.";

    /**
     * Unsupported authorization mechanism in "Authorization" header: %1$s.
     */
    public final static String UNSUPPORTED_AUTH_MECH_MSG = "Unsupported authorization mechanism in \"Authorization\" header: %1$s.";

    /**
     * Resolving user name "%1$s" failed.
     */
    public final static String RESOLVING_USER_NAME_FAILED_MSG = "Resolving user name \"%1$s\" failed.";

    /**
     * Authentication failed for user name: %1$s
     */
    public final static String AUTH_FAILED_MSG = "Authentication failed for user name: %1$s";

    /**
     * Unexpected error: %1$s
     */
    public final static String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s";

}
