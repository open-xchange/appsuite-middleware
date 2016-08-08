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

package com.openexchange.reseller.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerExceptionCodes;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.reseller.data.Restriction;

/**
 * {@link ResellerServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class ResellerServiceImpl implements ResellerService {

    private static final Logger LOG = LoggerFactory.getLogger(ResellerServiceImpl.class);

    private static final String DATABASE_COLUMN_VALUE = "value";

    private static final String DATABASE_COLUMN_NAME = "name";

    private static final String DATABASE_COLUMN_ID = "rid";

    // -------------------------------------------------------------------------------------------------

    private final DatabaseService dbService;

    /**
     * Initializes a new {@link ResellerServiceImpl}.
     *
     * @param databaseService The database service to use
     */
    public ResellerServiceImpl(DatabaseService databaseService) {
        super();
        dbService = databaseService;
    }

    @Override
    public ResellerAdmin getReseller(int cid) throws OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, cid);
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw ResellerExceptionCodes.NO_RESELLER_FOUND_FOR_CTX.create(Integer.valueOf(cid));
            }
            return getData(new ResellerAdmin[] { new ResellerAdmin(rs.getInt(1)) })[0];
        } catch (final SQLException e) {
            LOG.error("", e);
            throw new OXException(e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    private ResellerAdmin[] getData(final ResellerAdmin[] admins) throws SQLException, OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            List<ResellerAdmin> ret = new ArrayList<>(admins.length);
            for (final ResellerAdmin adm : admins) {
                prep = con.prepareStatement("SELECT sid, pid, name, displayName, password, passwordMech FROM subadmin WHERE sid=?");
                prep.setInt(1, adm.getId().intValue());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw ResellerExceptionCodes.NO_RESELLER_FOUND.create(adm.getId());
                }

                ResellerAdmin newadm = new ResellerAdmin(adm.getId(), adm.getName());
                newadm.setName(rs.getString(DATABASE_COLUMN_NAME));
                newadm.setId(Integer.valueOf(rs.getInt("sid")));
                newadm.setParentId(Integer.valueOf(rs.getInt("pid")));
                newadm.setDisplayname(rs.getString("displayName"));
                newadm.setPassword(rs.getString("password"));
                newadm.setPasswordMech(rs.getString("passwordMech"));

                rs.close();
                prep.close();

                newadm = getRestrictionDataForAdmin(newadm, con);

                ret.add(newadm);
            }
            return ret.toArray(new ResellerAdmin[ret.size()]);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    private ResellerAdmin getRestrictionDataForAdmin(final ResellerAdmin admin, final Connection con) throws SQLException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT subadmin_restrictions.rid,sid,name,value FROM subadmin_restrictions INNER JOIN restrictions ON subadmin_restrictions.rid=restrictions.rid WHERE sid=?");
            prep.setInt(1, admin.getParentId().intValue() > 0 ? admin.getParentId().intValue() : admin.getId().intValue());
            rs = prep.executeQuery();

            Set<Restriction> res = new HashSet<>();
            while (rs.next()) {
                final Restriction r = new Restriction();
                r.setId(Integer.valueOf(rs.getInt(DATABASE_COLUMN_ID)));
                r.setName(rs.getString(DATABASE_COLUMN_NAME));
                r.setValue(rs.getString(DATABASE_COLUMN_VALUE));
                if (admin.getParentId().intValue() > 0 && Restriction.SUBADMIN_CAN_CREATE_SUBADMINS.equals(r.getName())) {
                    continue;
                }
                res.add(r);
            }

            int size = res.size();
            if (size > 0) {
                admin.setRestrictions(res.toArray(new Restriction[size]));
            }

            return admin;
        } finally {
            Databases.closeSQLStuff(rs, prep);
        }
    }

    @Override
    public List<ResellerAdmin> getAll() throws OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            String query = "SELECT sid, pid, name, displayName, password, passwordMech FROM subadmin";
            prep = con.prepareStatement(query);
            rs = prep.executeQuery();

            List<ResellerAdmin> ret = new LinkedList<>();
            while (rs.next()) {
                ResellerAdmin newadm = new ResellerAdmin();
                newadm.setName(rs.getString(DATABASE_COLUMN_NAME));
                newadm.setId(Integer.valueOf(rs.getInt("sid")));
                newadm.setParentId(Integer.valueOf(rs.getInt("pid")));
                newadm.setDisplayname(rs.getString("displayName"));
                newadm.setPassword(rs.getString("password"));
                newadm.setPasswordMech(rs.getString("passwordMech"));
                newadm = getRestrictionDataForAdmin(newadm, con);
                ret.add(newadm);
            }
            return ret;
        } catch (SQLException e) {
            LOG.error("", e);
            throw new OXException(e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

}
