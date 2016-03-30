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

package com.openexchange.jslob.storage.db.groupware;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link DBJSlobIncreaseBlobSizeTask} - Changes the column "data" from table "jsonStorage" from type BLOB (64KB) to MEDIUMBLOB (16MB).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DBJSlobIncreaseBlobSizeTask extends UpdateTaskAdapter {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DBJSlobIncreaseBlobSizeTask}.
     *
     * @param services The service look-up
     */
    public DBJSlobIncreaseBlobSizeTask(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final DatabaseService dbService = services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final int contextId = params.getContextId();
        final Connection writeCon = dbService.getForUpdateTask(contextId);
        boolean writeOperationPerformed = false;
        boolean rollback = false;
        try {
            writeCon.setAutoCommit(false); // BEGIN
            rollback = true;

            writeOperationPerformed = doPerform(writeCon);

            writeCon.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.autocommit(writeCon);
            }
            if (writeOperationPerformed) {
                dbService.backForUpdateTask(contextId, writeCon);
            } else {
                dbService.backForUpdateTaskAfterReading(contextId, writeCon);
            }
        }
    }

    private boolean doPerform(final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            boolean writeOperationPerformed = false;

            // Check current type name
            String typeName = Tools.getColumnTypeName(writeCon, "jsonStorage", "data");
            if (!"MEDIUMBLOB".equalsIgnoreCase(typeName)) {
                final Column column = new Column("data", "MEDIUMBLOB");
                Tools.modifyColumns(writeCon, "jsonStorage", column);
                writeOperationPerformed = true;
            }

            return writeOperationPerformed;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { DBJSlobCreateTableTask.class.getName() };
    }
}
