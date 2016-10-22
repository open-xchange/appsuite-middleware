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

package com.openexchange.oauth.impl.internal.groupware;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.oauth.API;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RenameMigrateLinkedInServiceIdUpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RenameMigrateLinkedInServiceIdUpdateTask extends UpdateTaskAdapter {

    private final DatabaseService dbService;

    private static final String RENAME_LINKED_IN = "UPDATE oauthAccounts SET serviceId=? WHERE cid=? AND serviceId=?";
    private static final String MIGRATE_LINKED_IN = "UPDATE oauthAccounts SET scope=? WHERE cid=? AND serviceId=?";

    /**
     * Initialises a new {@link RenameMigrateLinkedInServiceIdUpdateTask}.
     */
    public RenameMigrateLinkedInServiceIdUpdateTask(DatabaseService dbService) {
        super();
        this.dbService = dbService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Connection writeCon;
        try {
            writeCon = dbService.getForUpdateTask(contextId);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            startTransaction(writeCon);
            stmt = writeCon.prepareStatement(RENAME_LINKED_IN);
            stmt.setString(1, API.LINKEDIN.getFullName());
            stmt.setInt(2, contextId);
            stmt.setString(3, "com.openexchange.socialplugin.linkedin");
            stmt.execute();
            stmt.close();

            stmt = writeCon.prepareStatement(MIGRATE_LINKED_IN);
            stmt.setString(1, OXScope.contacts_ro.name());
            stmt.setInt(2, contextId);
            stmt.setString(3, API.LINKEDIN.getFullName());
            stmt.execute();

            writeCon.commit();
        } catch (SQLException e) {
            DBUtils.rollback(writeCon);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(writeCon);
            closeSQLStuff(stmt);
            dbService.backForUpdateTask(contextId, writeCon);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[] { OAuthCreateTableTask2.class.getName(), OAuthAddScopeColumnTask.class.getName() };
    }
}
