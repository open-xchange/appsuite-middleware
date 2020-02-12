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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.WorkingLevel;

/**
 * {@link RemoveDAVUserAgentNamesForMWB58}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
public class RemoveDAVUserAgentNamesForMWB58 implements UpdateTaskV2 {

    private static String[] userAgentNames = { "Mac OS Calendar (CalDAV)",
        "Mac OS Addressbook (CardDAV)",
        "iOS Addressbook and Calendar (CalDAV/CardDAV)",
        "Mozilla Thunderbird / Lightning (CalDAV)",
        "eM Client (CalDAV/CardDAV)",
        "eM Client for OX App Suite (CalDAV/CardDAV)",
        "OX Sync on Android (CalDAV/CardDAV)",
        "CalDAV-Sync on Android (CalDAV)",
        "CardDAV-Sync on Android (CardDAV)",
        "SmoothSync on Android (CalDAV/CardDAV)",
        "DAVdroid on Android (CalDAV/CardDAV)",
        "DAVx\u2075 on Android (CalDAV/CardDAV)",
        "Outlook CalDav Synchronizer (CalDAV/CardDAV)",
        "Windows Phone Contacts and Calendar (CalDAV/CardDAV)",
        "Windows Contacts and Calendar (CalDAV/CardDAV)",
        "Sync Client (CalDAV)",
        "Sync Client (CardDAV)",
        "CalDAV/CardDAV"};
    
    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            if (!Databases.tablesExist(con, "user_attribute")) {
                return;
            }
            PreparedStatement stmt = con.prepareStatement(Databases.getIN("DELETE FROM user_attribute WHERE name IN (", userAgentNames.length));
            try {
                for (int i = 0; i < userAgentNames.length; i++) {
                    stmt.setString(i + 1, "client:" + userAgentNames[i]);
                }
                stmt.executeUpdate();
            } finally {
                stmt.close();
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.ChangePrimaryKeyForUserAttribute.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, WorkingLevel.SCHEMA);
    }

}
