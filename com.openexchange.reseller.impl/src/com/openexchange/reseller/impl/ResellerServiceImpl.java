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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableList;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerExceptionCodes;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.reseller.data.ResellerAdmin.ResellerAdminBuilder;
import com.openexchange.reseller.data.Restriction;

/**
 * {@link ResellerServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class ResellerServiceImpl implements ResellerService {

    private static final Logger LOG = LoggerFactory.getLogger(ResellerServiceImpl.class);

    private static final String DATABASE_COLUMN_VALUE = "value";
    private static final String DATABASE_COLUMN_NAME = "name";
    private static final String DATABASE_COLUMN_ID = "rid";

    private static final String GET_RESELLER_FOR_CTX = "SELECT sid FROM context2subadmin WHERE cid=?";
    private static final String GET_RESELLER = "SELECT sid FROM subadmin WHERE sid=?";
    private static final String GET_RESELLER_ID = "SELECT sid FROM subadmin WHERE name=?";
    private static final String GET_PARENT_RESELLER = "SELECT sid FROM subadmin WHERE pid=?";
    private static final String GET_RESELLER_DATA = "SELECT sid, pid, name, displayName, password, passwordMech, salt FROM subadmin WHERE sid=?";
    private static final String GET_ALL_RESELLER_DATA = "SELECT sid, pid, name, displayName, password, passwordMech, salt FROM subadmin";
    private static final String GET_RESELLER_RESTRICTIONS = "SELECT subadmin_restrictions.rid,sid,name,value FROM subadmin_restrictions INNER JOIN restrictions ON subadmin_restrictions.rid=restrictions.rid WHERE sid=?";

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
            prep = con.prepareStatement(GET_RESELLER_FOR_CTX);
            prep.setInt(1, cid);
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw ResellerExceptionCodes.NO_RESELLER_FOUND_FOR_CTX.create(Integer.valueOf(cid));
            }
            return getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), con);
        } catch (SQLException e) {
            LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public ResellerAdmin getResellerByName(String resellerName) throws OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement(GET_RESELLER_ID);
            prep.setString(1, resellerName);
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw ResellerExceptionCodes.NO_RESELLER_WITH_NAME_FOUND.create(resellerName);
            }
            return getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), con);
        } catch (SQLException e) {
            LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public ResellerAdmin getResellerById(int resellerId) throws OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement(GET_RESELLER);
            prep.setInt(1, resellerId);
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw ResellerExceptionCodes.NO_RESELLER_FOUND.create(Integer.valueOf(resellerId));
            }
            return getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), con);
        } catch (SQLException e) {
            LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public List<ResellerAdmin> getResellerAdminPath(int cid) throws OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement(GET_RESELLER_FOR_CTX);
            prep.setInt(1, cid);
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw ResellerExceptionCodes.NO_RESELLER_FOUND_FOR_CTX.create(Integer.valueOf(cid));
            }

            List<ResellerAdmin> path = new ArrayList<>();
            ResellerAdmin admin = getData(ResellerAdmin.builder().id(I(rs.getInt(1))).build(), con);
            path.add(admin);
            while (admin.getParentId() != null && admin.getParentId().intValue() != 0) {
                admin = getData(ResellerAdmin.builder().id(admin.getParentId()).build(), con);
                path.add(admin);
            }
            Collections.reverse(path);
            return path;
        } catch (SQLException e) {
            LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }

    }

    @Override
    public List<ResellerAdmin> getSubResellers(int parentId) throws OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement(GET_PARENT_RESELLER);
            prep.setInt(1, parentId);
            rs = prep.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<ResellerAdmin> subadmins = new ArrayList<>();
            do {
                subadmins.addAll(getData(ImmutableList.of(ResellerAdmin.builder().id(I(rs.getInt(1))).build()), con));
            } while (rs.next());
            return subadmins;
        } catch (SQLException e) {
            LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    @Override
    public List<ResellerAdmin> getAll() throws OXException {
        Connection con = dbService.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement(GET_ALL_RESELLER_DATA);
            rs = prep.executeQuery();

            List<ResellerAdmin> ret = new LinkedList<>();
            while (rs.next()) {
                ret.add(parseResellerAdminBuilder(rs).build());
            }
            return ret;
        } catch (SQLException e) {
            LOG.error("", e);
            throw ResellerExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, prep);
            dbService.backReadOnly(con);
        }
    }

    ////////////////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Retrieves the metadata for the specified {@link ResellerAdmin}
     *
     * @param admin The {@link ResellerAdmin}
     * @param con The optional {@link ConnectException}
     * @return The {@link ResellerAdmin} metadata
     * @throws SQLException if an SQL error is occurred
     * @throws OXException if an OX error is occurred
     */
    private ResellerAdmin getData(ResellerAdmin admin, Connection connection) throws SQLException, OXException {
        return getData(ImmutableList.of(admin), connection).get(0);
    }

    /**
     * Retrieves the metadata for the specified {@link ResellerAdmin}s
     *
     * @param admins The {@link ResellerAdmin}s
     * @param con The optional {@link ConnectException}
     * @return The {@link ResellerAdmin} metadata
     * @throws SQLException if an SQL error is occurred
     * @throws OXException if an OX error is occurred
     */
    private List<ResellerAdmin> getData(final List<ResellerAdmin> admins, Connection con) throws SQLException, OXException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean connectionInit = false;
        if (con == null) {
            con = dbService.getReadOnly();
            connectionInit = true;
        }
        try {
            List<ResellerAdmin> ret = new ArrayList<>(admins.size());
            for (final ResellerAdmin adm : admins) {
                prep = con.prepareStatement(GET_RESELLER_DATA);
                prep.setInt(1, adm.getId().intValue());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw ResellerExceptionCodes.NO_RESELLER_FOUND.create(adm.getId());
                }

                Integer id = Integer.valueOf(rs.getInt("sid"));
                Integer parentId = Integer.valueOf(rs.getInt("pid"));
                ResellerAdminBuilder builder = parseResellerAdminBuilder(rs);

                List<Restriction> restrictions = getRestrictionDataForAdmin(id, parentId, con);
                if (false == restrictions.isEmpty()) {
                    builder.restrictions(restrictions);
                }
                ret.add(builder.build());
            }
            return ret;
        } finally {
            Databases.closeSQLStuff(rs, prep);
            if (connectionInit) {
                dbService.backReadOnly(con);
            }
        }
    }

    /**
     * Retrieves the subadmin restrictions for the {@link ResellerAdmin} with the specified identifier
     *
     * @param id The reseller identifier
     * @param parentId The parent of the reseller
     * @param con The {@link Connection}
     * @return The restrictions
     * @throws SQLException if an SQL error is occurred
     */
    private List<Restriction> getRestrictionDataForAdmin(Integer id, Integer parentId, final Connection con) throws SQLException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement(GET_RESELLER_RESTRICTIONS);
            prep.setInt(1, i(i(parentId) > 0 ? parentId : id));
            rs = prep.executeQuery();

            Set<Restriction> res = null;
            while (rs.next()) {
                final Restriction r = parseRestriction(rs);
                if (i(parentId) > 0 && Restriction.SUBADMIN_CAN_CREATE_SUBADMINS.equals(r.getName())) {
                    continue;
                }
                if (res == null) {
                    res = new HashSet<>();
                }
                res.add(r);
            }
            return res == null ? ImmutableList.of() : ImmutableList.copyOf(res);
        } finally {
            Databases.closeSQLStuff(rs, prep);
        }
    }

    /**
     * Parses the specified {@link ResultSet} to a {@link Restriction}
     *
     * @param resultSet The {@link ResultSet} to parse
     * @return The {@link Restriction}
     * @throws SQLException if an SQL error is occurred
     */
    private Restriction parseRestriction(ResultSet resultSet) throws SQLException {
        return new Restriction(Integer.valueOf(resultSet.getInt(DATABASE_COLUMN_ID)), resultSet.getString(DATABASE_COLUMN_NAME), resultSet.getString(DATABASE_COLUMN_VALUE));
    }

    /**
     * Parses the specified {@link ResultSet} to a {@link ResellerAdminBuilder}
     *
     * @param resultSet The {@link ResultSet} to parse
     * @return The {@link ResellerAdminBuilder}
     * @throws SQLException if an SQL error is occurred
     */
    private ResellerAdminBuilder parseResellerAdminBuilder(ResultSet resultSet) throws SQLException {
        ResellerAdminBuilder builder = ResellerAdmin.builder().id(Integer.valueOf(resultSet.getInt("sid")));
        builder.name(resultSet.getString(DATABASE_COLUMN_NAME));
        builder.parentId(Integer.valueOf(resultSet.getInt("pid")));
        builder.displayname(resultSet.getString("displayName"));
        builder.password(resultSet.getString("password"));
        builder.passwordMech(resultSet.getString("passwordMech"));
        builder.salt(resultSet.getString("salt"));
        return builder;
    }
}
