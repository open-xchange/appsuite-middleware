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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask;

/**
 * {@link LegacyCalendarTablesUtf8Mb4UpdateTask}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class LegacyCalendarTablesUtf8Mb4UpdateTask extends SimpleConvertUtf8ToUtf8mb4UpdateTask {

    /**
     * Initializes a new {@link LegacyCalendarTablesUtf8Mb4UpdateTask}.
     */
    public LegacyCalendarTablesUtf8Mb4UpdateTask() {
        super(CalendarAddIndex2DatesMembersV2.class, "prg_dates", "del_dates", "prg_date_rights", "prg_dates_members", "del_date_rights", "del_dates_members");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask#before(com.openexchange.groupware.update.PerformParameters, java.sql.Connection)
     */
    @Override
    protected void before(PerformParameters params, Connection connection) throws SQLException {
        recreateKey(connection, "prg_dates", new String[] { "cid", "uid" }, new int[] { -1, 191 });
        recreateKey(connection, "del_dates", new String[] { "cid", "uid" }, new int[] { -1, 191 });
    }

    @Override
    protected void after(PerformParameters params, Connection connection) throws SQLException {
        // Manually change dateExternal and delDateExternal
        List<String> columnsToIgnore = Collections.singletonList("mailAddress");
        changeTable(connection, params.getSchema().getSchema(), "dateExternal", columnsToIgnore);
        changeTable(connection, params.getSchema().getSchema(), "delDateExternal", columnsToIgnore);
    }
}
