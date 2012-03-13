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

package com.openexchange.groupware.update.internal;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * Exception message texts for the {@link OXException}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class SchemaExceptionMessages implements LocalizableStrings {

    public static final String MISSING_VERSION_ENTRY_MSG = "No row found in table version in schema %1$s.";

    public static final String MULTIPLE_VERSION_ENTRY_MSG = "Multiple rows found in table version in schema %1$s.";

    public static final String ALREADY_LOCKED_MSG = "Update conflict detected. Another process is currently updating schema %1$s.";

    public static final String LOCK_FAILED_MSG = "Table update failed. Schema %1$s could not be locked.";

    public static final String UPDATE_CONFLICT_MSG = "Update conflict detected. Schema %1$s is not marked as locked.";

    public static final String UNLOCK_FAILED_MSG = "Schema %1$s could not be unlocked. Lock information could no be removed from database.";

    public static final String SQL_PROBLEM_MSG = "A SQL problem occurred: %1$s.";

    public static final String DATABASE_DOWN_MSG = "Cannot get database connection.";

    public static final String WRONG_ROW_COUNT_MSG = "Processed a wrong number of rows in database. Expected %1$d rows but worked on %2$d rows.";

    /**
     * Prevent instantiation.
     */
    private SchemaExceptionMessages() {
        super();
    }
}
