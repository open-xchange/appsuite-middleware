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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * {@link LoginInfoLoader}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LoginInfoLoader implements Filter<Context, Context> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginInfoLoader.class);

    private final AdminCache cache;

    public LoginInfoLoader(final AdminCache cache) {
        super();
        this.cache = cache;
    }

    @Override
    public Context[] filter(final Collection<Context> input) throws PipesAndFiltersException {
        final Map<Integer, Context> contexts = new HashMap<Integer, Context>(input.size());
        for (final Context context : input) {
            contexts.put(context.getId(), context);
        }
        try {
            loadLoginInfo(contexts);
        } catch (final StorageException e) {
            throw new PipesAndFiltersException(e);
        }
        return contexts.values().toArray(new Context[contexts.size()]);
    }

    private static final String SQL = "SELECT cid,login_info FROM login2context WHERE cid IN (";

    private void loadLoginInfo(final Map<Integer, Context> contexts) throws StorageException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (final PoolException e) {
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(getIN(SQL, contexts.size()));
            int pos = 1;
            for (final Integer cid : contexts.keySet()) {
                stmt.setInt(pos++, cid.intValue());
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                final int cid = rs.getInt(1);
                final String loginMapping = rs.getString(2);
                // Do not return the context identifier as a mapping. This can cause errors if changing login mappings afterwards! See
                // bug 11094 for details!
                final Context context = contexts.get(I(cid));
                if (!context.getIdAsString().equals(loginMapping)) {
                    context.addLoginMapping(loginMapping);
                }
            }
        } catch (final SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (final PoolException e) {
                LOG.error("", e);
            }
        }
    }
}
