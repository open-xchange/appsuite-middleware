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

package com.openexchange.passwordchange.history.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.tools.update.Tools;

/**
 * {@link PasswordChangeHistoryCreateTableTask}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHistoryCreateTableTask extends AbstractCreateTableImpl implements UpdateTaskV2 {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHistoryCreateTableTask.class);
    private static final String SEQUENCE_NAME = "sequence_password_history";
    private static final String HISTORY_NAME = "user_password_history";

    private static String getSequenceTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE sequence_password_history (");
        sb.append("cid INT4 UNSIGNED NOT NULL,");
        sb.append("id INT4 UNSIGNED NOT NULL,");
        sb.append("PRIMARY KEY (cid));");
        return sb.toString();
    }

    private static String getHistoryTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE user_password_histoy (");
        sb.append("cid INT4 UNSIGNED NOT NULL,");
        sb.append("id INT4 UNSIGNED NOT NULL,");
        sb.append("uid INT4 UNSIGNED NOT NULL,");
        sb.append("created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        sb.append("source VARCHAR(256) NULL DEFAULT NULL,");
        sb.append("ip VARCHAR(256) NULL DEFAULT NULL,");
        sb.append("PRIMARY KEY (cid, id));");
        return sb.toString();
    }

    /**
     * Initializes a new {@link PasswordChangeHistoryCreateTableTask}.
     */
    public PasswordChangeHistoryCreateTableTask() {
        super();
    }

    private void createTable(final String tablename, final String sqlCreate, final int contextId) throws OXException {
        final Connection writeCon = Database.get(contextId, true);
        PreparedStatement stmt = null;
        try {
            if (Tools.tableExists(writeCon, tablename)) {
                return;
            }
            stmt = writeCon.prepareStatement(sqlCreate);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            Database.back(contextId, true, writeCon);
        }
    }

    @Override
    public String[] requiredTables() {
        return new String[] {};
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { SEQUENCE_NAME, HISTORY_NAME };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        final int contextID = params.getContextId();
        createTable(SEQUENCE_NAME, getSequenceTable(), contextID);
        createTable(HISTORY_NAME, getHistoryTable(), contextID);
        LOG.info("UpdateTask 'PasswordChangeHistoryCreateTableTask' successfully performed!");
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { getSequenceTable(), getHistoryTable() };
    }
}
