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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * ContactsChangedFromUpdateTask
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Ben Pahne</a>
 *
 */
public final class ContactsGlobalMoveUpdateTask implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactsGlobalMoveUpdateTask.class));

    public ContactsGlobalMoveUpdateTask() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTask#addedWithVersion()
     */
    @Override
    public int addedWithVersion() {
        return 16;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTask#getPriority()
     */
    @Override
    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String STR_INFO = "Performing update task 'ContactsGlobalMoveUpdateTask'";

    //private static final String SQL_QUERY = "SELECT created_from,changed_from,cid FROM prg_contacts WHERE changed_from IS NULL";

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTask#perform(com.openexchange.groupware.update.Schema,
     *      int)
     */

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        correctTable("prg_contacts", contextId);
    }

    private static final String SQL_QUERY = "SELECT created_from,cid,intfield01 FROM prg_contacts WHERE fid = "+FolderObject.SYSTEM_LDAP_FOLDER_ID+" AND userid is NULL";


    public void correctTable(final String sqltable, final int contextId) throws OXException {

        if (LOG.isInfoEnabled()) {
            LOG.info(STR_INFO);
        }

        Connection writeCon = null;
        final PreparedStatement stmt = null;
        Statement st = null;
        ResultSet resultSet = null;
        FolderObject des = null;
        try {

            writeCon = Database.get(contextId, true);
            try {
                st = writeCon.createStatement();

                Context ct = null;
                OXFolderAccess oxa = null;
                resultSet = st.executeQuery(SQL_QUERY);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("UPDATING WRONG GLOBAL ADDRESSBOOK CONTACTS: MOVING BACK TO OWNER'S PRIVATE ADDRESSBOOK");
                }

                while (resultSet.next()) {
                    final int creator  = resultSet.getInt(1);
                    final int cid = resultSet.getInt(2);
                    final int id  = resultSet.getInt(3);


                    ct = ContextStorage.getInstance().loadContext(cid);
                    oxa = new OXFolderAccess(writeCon, ct);
                    des = oxa.getDefaultFolder(creator, FolderObject.CONTACT);

                    if (LOG.isWarnEnabled()) {
                        LOG.warn("UPDATING OPBJECT "+id+" IN CONTEXT "+cid+" MOVING TO "+des.getObjectID());
                    }

                    final StringBuilder sb = new StringBuilder("UPDATE prg_contacts SET fid = ");
                    sb.append(des.getObjectID());
                    sb.append(" , changing_date = ");
                    sb.append(System.currentTimeMillis());

                    sb.append(" , changed_from = ");
                    sb.append(ct.getMailadmin());

                    sb.append(" WHERE cid = ");
                    sb.append(cid);
                    sb.append(" AND intfield01 = ");
                    sb.append(id);
                    st.addBatch(sb.toString());
                }


                st.executeBatch();
                st.close();

            } catch (final SQLException e) {
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
        } finally {
            closeSQLStuff(resultSet, stmt);
            closeSQLStuff(null, st);
            if (writeCon != null) {
                Database.back(contextId, true, writeCon);
            }
        }
    }

}
