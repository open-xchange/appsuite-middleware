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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 */
public class ResellerContextFilter implements Filter<Integer, Integer> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResellerContextFilter.class);

    private final AdminCache cache;

    private final ResellerAdmin admin;

    public ResellerContextFilter(AdminCache cache, ResellerAdmin admin) {
        super();
        this.cache = cache;
        this.admin = admin;
    }

    @Override
    public Integer[] filter(Collection<Integer> input) throws PipesAndFiltersException {
        List<Integer> cids;
        if( null == admin ) {
            return input.toArray(new Integer[input.size()]);
        }
        try {
            cids = filterContexts(input);
        } catch (StorageException e) {
            throw new PipesAndFiltersException(e);
        }
        return cids.toArray(new Integer[cids.size()]);
    }

    private static final String GET_SIDS_SQL = "SELECT sid FROM subadmin WHERE pid = ?";

    private List<Integer> filterContexts(Collection<Integer> cids) throws StorageException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e);
        }
        List<Integer> retval = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // determine all subadmin sids having me as a parent
            stmt = con.prepareStatement(GET_SIDS_SQL);
            stmt.setInt(1, admin.getId());
            rs = stmt.executeQuery();
            String GET_CIDS_SQL = "SELECT cid FROM context2subadmin WHERE sid IN (";
            GET_CIDS_SQL += admin.getId() + ",";
            while( rs.next() ) {
                GET_CIDS_SQL += rs.getInt(1) + ",";
            }
            GET_CIDS_SQL = GET_CIDS_SQL.substring(0, GET_CIDS_SQL.length()-1);
            GET_CIDS_SQL += ") AND cid IN (";
            stmt.close();
            rs.close();

            stmt = con.prepareStatement(getIN(GET_CIDS_SQL, cids.size()));
            int pos = 1;
            for (Integer cid : cids) {
                stmt.setInt(pos++, cid.intValue());
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                retval.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (PoolException e) {
                LOG.error("Error pushing connection to pool!", e);
            }
        }
        return retval;
    }
}
