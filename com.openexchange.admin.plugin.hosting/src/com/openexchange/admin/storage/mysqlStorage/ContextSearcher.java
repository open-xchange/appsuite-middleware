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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.database.Databases;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * Executes some SQL statements searching for context identifier with a separate thread.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContextSearcher extends AbstractTask<Collection<Integer>> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContextSearcher.class);

    private final AdminCacheExtended cache;
    private final String sql;
    private final String pattern;

    /**
     * Initializes a new {@link ContextSearcher}.
     *
     * @param cache The cache reference used to acquire/release a connection
     * @param sql The SQL statement to execute
     * @param pattern The search pattern to use
     */
    public ContextSearcher(AdminCacheExtended cache, String sql, String pattern) {
        super();
        this.cache = cache;
        this.sql = sql;
        this.pattern = pattern;
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("listContext searcher");
    }

    @Override
    public Collection<Integer> call() throws StorageException {
        Connection con = acquireConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(sql);
            if (null != pattern) {
                stmt.setString(1, pattern);
            }
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Integer> cids = new ArrayList<>();
            do {
                cids.add(I(rs.getInt(1)));
            } while (rs.next());
            return cids;
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseConnection(con);
        }
    }

    private void releaseConnection(Connection con) {
        try {
            cache.pushReadConnectionForConfigDB(con);
        } catch (Exception x) {
            LOG.error("Failed to push ConfigDB connection back to pool.", x);
        }
    }

    private Connection acquireConnection() throws StorageException {
        try {
            return cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e);
        }
    }

}
