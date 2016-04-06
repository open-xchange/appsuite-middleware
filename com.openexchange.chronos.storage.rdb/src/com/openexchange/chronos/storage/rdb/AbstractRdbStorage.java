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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractRdbStorage}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class AbstractRdbStorage implements TransactionallyStorage {

    private Connection writeConnection;
    private Connection readConnection;
    protected DatabaseService dbService;
    protected int contextID;

    protected AbstractRdbStorage(int contextId) throws OXException {
        this.contextID = contextId;
        this.dbService = Services.getService(DatabaseService.class, true);
    }

    @Override
    public void startTransaction() throws OXException {
        if (writeConnection != null) {
            try {
                writeConnection.setAutoCommit(true);
            } catch (SQLException e) {
                throw EventExceptionCode.MYSQL.create(e);
            }
        }
    }

    @Override
    public void commit() throws OXException {
        if (writeConnection != null) {
            try {
                writeConnection.commit();
            } catch (SQLException e) {
                throw EventExceptionCode.MYSQL.create(e);
            }
        }
    }

    @Override
    public void endTransaction() throws OXException {
        if (writeConnection != null) {
            try {
                commit();
                writeConnection.setAutoCommit(false);
            } catch (SQLException e) {
                throw EventExceptionCode.MYSQL.create(e);
            }
        }
    }

    @Override
    public void close() {
        if (writeConnection != null) {
            dbService.backWritable(contextID, writeConnection);
            writeConnection = null;
        }
        if (readConnection != null) {
            dbService.backReadOnly(contextID, readConnection);
            readConnection = null;
        }
    }

    protected Connection getConnection(boolean writeable) throws OXException {
        if (writeable) {
            if (writeConnection == null) {
                writeConnection = dbService.getWritable(contextID);
            }
            return writeConnection;
        } else {
            if (readConnection == null) {
                readConnection = dbService.getReadOnly(contextID);
            }
            return readConnection;
        }

    }

}
