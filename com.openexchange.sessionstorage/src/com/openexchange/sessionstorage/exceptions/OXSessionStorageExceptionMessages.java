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

package com.openexchange.sessionstorage.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OXSessionStorageExceptionMessages}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class OXSessionStorageExceptionMessages implements LocalizableStrings {

    public final static String SESSIONSTORAGE_START_FAILED_MSG = "Start of SessionStorageService failed.";

    public final static String SESSIONSTORAGE_SAVE_FAILED_MSG = "Saving session with session identifier %1$s failed.";

    public final static String SESSIONSTORAGE_REMOVE_FAILED_MSG = "Removing session with session identifier %1$s failed.";

    public final static String SESSIONSTORAGE_SESSION_NOT_FOUND_MSG = "No session with session identifier %1$s found.";

    public final static String SESSIONSTORAGE_DUPLICATE_AUTHID_MSG = "Authentication identifier duplicate found. Existing session login: %1$s. Current denied login request: %2$s.";

    public final static String SESSIONSTORAGE_UNSUPPORTED_OPERATION_MSG = "Operation %1$s not supported.";

    public final static String SESSIONSTORAGE_ALTID_NOT_FOUND_MSG = "Lookup for session with public identifier %1$s failed";

    public final static String SESSIONSTORAGE_NO_USERSESSIONS_MSG = "No sessions found for user %1$s in context %2$s";

    public final static String SESSIONSTORAGE_NO_CONTEXTSESSIONS_MSG = "No sessions found for context %1$s";

    public final static String SESSIONSTORAGE_RANDOM_NOT_FOUND_MSG = "No sessions found by random token %1$s";

    public final static String SESSIONSTORAGE_CONFIG_FILE_MSG = "Error in config file.";

    /**
     * Initializes a new {@link OXSessionStorageExceptionMessages}.
     */
    private OXSessionStorageExceptionMessages() {
        super();
    }

}
