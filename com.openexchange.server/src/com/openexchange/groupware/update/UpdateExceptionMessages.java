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

package com.openexchange.groupware.update;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * Exception message texts for the {@link OXException}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UpdateExceptionMessages implements LocalizableStrings {

    public static final String ONLY_REDUCE_DISPLAY = "The current version number %1$s is already lower than or equal to the desired version number %2$s.";

    public static final String LOADING_TASK_FAILED_DISPLAY = "Error loading update task \"%1$s\".";

    public static final String UNKNOWN_SCHEMA_DISPLAY = "Unknown schema name: \"%1$s\".";

    public static final String UNKNOWN_CONCURRENCY_DISPLAY = "Update task \"%1$s\" returned an unknown concurrency level. Running as blocking task.";

    public static final String RESET_FORBIDDEN_DISPLAY = "The version can not be set back if the update tasks handling has been migrated to the Remember Executed Update Tasks concept on schema \"%1$s\".";

    public static final String UNRESOLVABLE_DEPENDENCIES_DISPLAY = "Unable to determine next update task to execute. Executed: \"%1$s\". Enqueued: \"%2$s\". Scheduled: \"%3$s\".";

    public static final String WRONG_ROW_COUNT_DISPLAY = "Processed a wrong number of rows in database. Expected %1$d rows but worked on %2$d rows.";

    public static final String UPDATE_FAILED_DISPLAY = "Updating schema \"%1$s\" failed. Cause: \"%2$s\".";

    public static final String BLOCKING_FIRST_DISPLAY = "Blocking tasks (\"%1$s\") must be executed before background tasks can be executed (\"%2$s\").";

    public static final String UNKNOWN_TASK_DISPLAY = "Unknown task: \"%1$s\".";

    public static final String COLUMN_NOT_FOUND_DISPLAY = "Column \"%1$s\" not found in table \"%2$s\".";

    private UpdateExceptionMessages() {
        super();
    }
}
