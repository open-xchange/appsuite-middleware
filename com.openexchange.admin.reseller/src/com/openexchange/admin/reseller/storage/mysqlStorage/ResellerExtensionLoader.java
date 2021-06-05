/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.reseller.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.database.Databases;
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
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            for (Collection<Context> partition : partition(contexts.values(), Databases.IN_LIMIT)) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement(Databases.getIN(SQL, partition.size()));
                    int pos = 1;
                    for (Context context : partition) {
                        OXContextExtensionImpl ctxext = (OXContextExtensionImpl)context.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
                        // add extension of none present (Bug 18881)
                        if ( null == ctxext ) {
                            ctxext = new OXContextExtensionImpl();
                            context.addExtension(ctxext);
                        }
                        stmt.setInt(pos++, context.getId().intValue());
                    }
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        int cid = rs.getInt(1);
                        Context context = contexts.get(I(cid));
                        OXContextExtensionImpl ctxext = (OXContextExtensionImpl)context.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
                        HashSet<Restriction> restrictions;
                        restrictions = OXResellerTools.array2HashSet(ctxext.getRestriction());
                        if ( null == restrictions ) {
                            restrictions = new HashSet<Restriction>();
                        }
                        context.removeExtension(ctxext);
                        ctxext.setCustomid(rs.getString(2));
                        int rid = rs.getInt(3);
                        if ( rid > 0 ) {
                            final Restriction res = new Restriction();
                            res.setId(I(rid));
                            res.setValue(rs.getString(4));
                            res.setName(rs.getString(5));
                            restrictions.add(res);
                            ctxext.setRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
                        }
                        ResellerAdmin ra = new ResellerAdmin(rs.getString(6));
                        ctxext.setOwner(ra);
                        context.addExtension(ctxext);
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } catch (DuplicateExtensionException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    /**
     * Generates consecutive subsets of a set, each of the same size (the final list may be smaller).
     *
     * @param original The set to return consecutive subsets of
     * @param partitionSize The desired size for each subset
     * @return A list of consecutive subsets
     * @throws IllegalArgumentException if {@code partitionSize} is not positive
     */
    private static <T> List<Collection<T>> partition(Collection<T> original, int partitionSize) {
        int total = original.size();
        if (partitionSize >= total) {
            return java.util.Collections.singletonList(original);
        }

        // Create a list of collections to return.
        List<Collection<T>> result = new LinkedList<Collection<T>>();

        // Create an iterator for the original collection.
        Iterator<T> it = original.iterator();

        // Create each new collection.
        Collection<T> s = new ArrayList<T>(partitionSize);
        s.add(it.next());
        for (int i = 1; i < total; i++) {
            if ((i % partitionSize) == 0) {
                result.add(s);
                s = new ArrayList<T>(partitionSize);
            }
            s.add(it.next());
        }
        result.add(s);
        return result;
    }

}
