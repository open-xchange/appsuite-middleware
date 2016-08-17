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

package com.openexchange.admin.autocontextid.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.admin.autocontextid.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.autocontextid.storage.sqlStorage.OXAutoCIDSQLStorage;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.impl.IDGenerator;

/**
 * @author choeger
 */
public final class OXAutoCIDMySQLStorage extends OXAutoCIDSQLStorage {

    private static AdminCache cache = null;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXAutoCIDMySQLStorage.class);

    static {
        cache = ClientAdminThreadExtended.cache;
    }

    public OXAutoCIDMySQLStorage() {
        super();
    }

    @Override
    public int generateContextId() throws StorageException {
        final Connection con;
        try {
            con = cache.getWriteConnectionForConfigDB();
        } catch (final PoolException e) {
            log.error("", e);
            throw new StorageException(e.getMessage());
        }
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            int id = IDGenerator.getId(con, -2);
            con.commit();
            rollback = false;
            return id;
        } catch (final SQLException e) {
            throw new StorageException(e.getMessage());
        } finally {
            if (rollback) {
                doRollback(con);
            }
            cache.closeWriteConfigDBSqlStuff(con, null);
        }
    }

    private static void doRollback(final Connection con) {
        if (null != con) {
            try {
                con.rollback();
            } catch (final SQLException e2) {
                log.error("Error doing rollback", e2);
            }
        }
    }

}
