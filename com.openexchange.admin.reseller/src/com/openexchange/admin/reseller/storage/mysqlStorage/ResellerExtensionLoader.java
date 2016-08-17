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

package com.openexchange.admin.reseller.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * Loads the extension data of multiple contexts in a fast multithreaded manner.
 *
 * @author choeger
 */
public class ResellerExtensionLoader implements Filter<Context, Context> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResellerExtensionLoader.class);

    private final AdminCache cache;

    public ResellerExtensionLoader(AdminCache cache) {
        super();
        this.cache = cache;
    }

    @Override
    public Context[] filter(Collection<Context> input) throws PipesAndFiltersException {
        Map<Integer, Context> contexts = new HashMap<Integer, Context>(input.size());
        for (Context context : input) {
            contexts.put(context.getId(), context);
        }
        try {
            loadExtensionsForContexts(contexts);
        } catch (StorageException e) {
            throw new PipesAndFiltersException(e);
        }
        return contexts.values().toArray(new Context[contexts.size()]);
    }

    private static final String SQL = "SELECT cf.cid, cf.customid, cr.rid, cr.value, r.name, s.name FROM context_customfields AS cf LEFT JOIN context_restrictions AS cr ON cf.cid = cr.cid LEFT JOIN restrictions AS r ON cr.rid = r.rid LEFT JOIN context2subadmin AS cs ON cf.cid = cs.cid LEFT JOIN subadmin AS s ON cs.sid = s.sid WHERE cf.cid IN (";

    private void loadExtensionsForContexts(Map<Integer, Context> contexts) throws StorageException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(getIN(SQL, contexts.size()));
            int pos = 1;
            for (Entry<Integer, Context> cidEntry : contexts.entrySet()) {
                final Context ctx = cidEntry.getValue();
                OXContextExtensionImpl ctxext = (OXContextExtensionImpl)ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
                // add extension of none present (Bug 18881)
                if( null == ctxext ) {
                    ctxext = new OXContextExtensionImpl();
                    ctx.addExtension(ctxext);
                }
                stmt.setInt(pos++, cidEntry.getKey().intValue());
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                int cid = rs.getInt(1);
                Context context = contexts.get(I(cid));
                OXContextExtensionImpl ctxext = (OXContextExtensionImpl)context.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
                HashSet<Restriction> restrictions;
                restrictions = OXResellerTools.array2HashSet(ctxext.getRestriction());
                if( null == restrictions ) {
                    restrictions = new HashSet<Restriction>();
                }
                context.removeExtension(ctxext);
                ctxext.setCustomid(rs.getString(2));
                int rid = rs.getInt(3);
                if( rid > 0 ) {
                    final Restriction res = new Restriction();
                    res.setId(rid);
                    res.setValue(rs.getString(4));
                    res.setName(rs.getString(5));
                    restrictions.add(res);
                    ctxext.setRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
                }
                ResellerAdmin ra = new ResellerAdmin(rs.getString(6));
                ctxext.setOwner(ra);
                context.addExtension(ctxext);
            }
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } catch (DuplicateExtensionException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (PoolException e) {
                LOG.error("Error pushing connection to pool!", e);
            }
        }
    }
}
