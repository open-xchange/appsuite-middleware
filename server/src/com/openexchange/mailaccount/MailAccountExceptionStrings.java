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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mailaccount;

import com.openexchange.exceptions.LocalizableStrings;

/**
 * {@link MailAccountExceptionStrings} - The mail account exception strings.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountExceptionStrings implements LocalizableStrings {

    // Unknown operation: %1$s.
    public static final String UNKNOWN_OPERATION_MSG = "Unknown operation: %1$s.";

    // Cannot find mail account with identifier %1$s for user %2$s in context %3$s.
    public static final String NOT_FOUND_MSG = "Cannot find mail account with identifier %1$s for user %2$s in context %3$s.";

    // Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.
    public static final String CONFLICT_MSG = "Found two mail accounts with same identifier %1$s for user %2$s in context %3$s.";

    // A SQL error occurred: %1$s.
    public static final String SQL_ERROR_MSG = "A SQL error occurred: %1$s.";

    // A host could not be resolved: %1$s.
    public static final String UNKNOWN_HOST_ERROR_MSG = "A host could not be resolved: %1$s.";

    // Denied deletion of default mail account of user %1$s in context %2$s.
    public static final String NO_DEFAULT_DELETE_MSG = "Denied deletion of default mail account of user %1$s in context %2$s.";

    // Denied update of default mail account of user %1$s in context %2$s.
    public static final String NO_DEFAULT_UPDATE_MSG = "Denied update of default mail account of user %1$s in context %2$s.";

    // No duplicate default account allowed.
    public static final String NO_DUPLICATE_DEFAULT_MSG = "No duplicate default account allowed.";

    // Password encryption failed.
    public static final String PASSWORD_ENCRYPTION_FAILED_MSG = "Password encryption failed.";

    // Password decryption failed.
    public static final String PASSWORD_DECRYPTION_FAILED_MSG = "Password decryption failed.";

    /**
     * Initializes a new {@link MailAccountExceptionStrings}.
     */
    private MailAccountExceptionStrings() {
        super();
    }

}
