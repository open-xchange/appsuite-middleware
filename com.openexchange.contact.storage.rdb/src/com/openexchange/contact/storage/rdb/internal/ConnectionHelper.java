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

package com.openexchange.contact.storage.rdb.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.contact.ContactSessionParameterNames;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ConnectionHelper}
 *
 * Provides read-only- and writable connections for accessing the database.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ConnectionHelper {

    private final DatabaseService databaseService;
    private final Session session;

    private Connection readOnlyConnection = null;
    private boolean backReadOnly;
    private Connection writableConnection = null;
    private boolean backWritable;
    private boolean committed;

    /**
     * Initializes a new {@link ConnectionHelper}.
     */
    public ConnectionHelper(Session session) throws OXException {
    	super();
    	this.databaseService = RdbServiceLookup.getService(DatabaseService.class, true);
        this.session = session;
    }

    /**
     * Gets a read-only database connection for the current session.
     *
     * @return A read-only connection
     * @throws OXException
     */
    public Connection getReadOnly() throws OXException {
        if (null == readOnlyConnection) {
            Object sessionConnection = session.getParameter(ContactSessionParameterNames.getParamReadOnlyConnection());
            if (null != sessionConnection && Connection.class.isInstance(sessionConnection)) {
                readOnlyConnection = (Connection)sessionConnection;
                backReadOnly = false;
            } else {
                readOnlyConnection = databaseService.getReadOnly(session.getContextId());
                backReadOnly = true;
            }
        }
        return readOnlyConnection;
    }

    /**
     * Gets a writable database connection for the current session. Auto-commit is set to <code>false</code> implicitly.
     *
     * @return A writable connection
     * @throws OXException
     */
    public Connection getWritable() throws OXException {
        if (null == writableConnection) {
            Object sessionConnection = session.getParameter(ContactSessionParameterNames.getParamWritableConnection());
            if (null != sessionConnection && Connection.class.isInstance(sessionConnection)) {
                writableConnection = (Connection)sessionConnection;
                backWritable = false;
            } else {
                writableConnection = databaseService.getWritable(session.getContextId());
                try {
                    writableConnection.setAutoCommit(false);
                } catch (SQLException e) {
                    throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
                backWritable = true;
            }
        }
        return writableConnection;
    }

    public void commit() throws SQLException {
        if (null != writableConnection) {
            writableConnection.commit();
            committed = true;
        }
    }

    /**
     * Backs all acquired database connections to the pool if needed.
     *
     * @throws OXException
     */
    public void back() throws OXException {
        backReadOnly();
        backWritable();
    }

    /**
     * Backs an acquired read-only connection to the pool if needed.
     *
     * @throws OXException
     */
    public void backReadOnly() throws OXException {
        if (null != readOnlyConnection && backReadOnly) {
            databaseService.backReadOnly(session.getContextId(), readOnlyConnection);
            readOnlyConnection = null;
        }
    }

    /**
     * Backs an acquired writable connection to the pool if needed, rolling back the transaction automatically if not yet committed.
     *
     * @throws OXException
     */
    public void backWritable() throws OXException {
        if (null != writableConnection) {
            if (false == committed) {
                DBUtils.rollback(writableConnection);
            }
            if (backWritable) {
                DBUtils.autocommit(writableConnection);
                databaseService.backWritable(session.getContextId(), writableConnection);
                writableConnection = null;
            }
        }
    }

    /**
     * Backs an acquired writable connection to the pool if needed, rolling back the transaction automatically if not yet committed.
     *
     * @throws OXException
     */
    public void backWritableAfterReading() throws OXException {
        if (null != writableConnection) {
            if (false == committed) {
                DBUtils.rollback(writableConnection);
            }
            if (backWritable) {
                DBUtils.autocommit(writableConnection);
                databaseService.backWritableAfterReading(session.getContextId(), writableConnection);
                writableConnection = null;
            }
        }
    }

}

