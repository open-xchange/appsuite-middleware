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

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.osgi.framework.BundleContext;
import com.google.common.collect.ImmutableSet;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.dataobjects.CustomField;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException.Code;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.reseller.storage.sqlStorage.OXResellerSQLStorage;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.password.mechanism.PasswordDetails;

/**
 * {@link OXResellerMySQLStorage}
 * 
 * @author choeger
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class OXResellerMySQLStorage extends OXResellerSQLStorage {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXResellerMySQLStorage.class);

    private static final ResellerAdmin masteradmin = new ResellerAdmin(I(0), "oxadminmaster");

    private static final String CAPABILITIES_REGION_NAME = "CapabilitiesReseller";
    private static final String CONFIGURATION_REGION_NAME = "ConfigurationReseller";
    private static final String TAXONOMIES_REGION_NAME = "TaxonomiesReseller";

    private static final String DATABASE_COLUMN_VALUE = "value";
    private static final String DATABASE_COLUMN_NAME = "name";
    private static final String DATABASE_COLUMN_ID = "rid";

    private static AdminCache cache = null;

    static {
        cache = ClientAdminThreadExtended.cache;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link OXResellerMySQLStorage}.
     */
    public OXResellerMySQLStorage() {
        super();
    }

    @Override
    public void change(ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;

        LOGGER.debug("change admin {}", adm);

        boolean rollback = false;
        try {
            oxcon = cache.getWriteConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            rollback = true;

            String name = adm.getName();
            int sid = 0;
            if (adm.getId() != null) {
                sid = adm.getId().intValue();
            } else if (name != null) {
                prep = oxcon.prepareStatement("SELECT sid FROM subadmin WHERE name=?");
                prep.setString(1, name);
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("unable to get id for " + name);
                }
                sid = rs.getInt("sid");
                prep.close();
            } else {
                throw new InvalidDataException("either ID or name must be specified");
            }
            if (name != null) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET name=? WHERE sid=?");
                prep.setString(1, name);
                prep.setInt(2, sid);
                prep.executeUpdate();
                prep.close();
            }

            String displayName = adm.getDisplayname();
            if (displayName != null) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET displayName=? WHERE sid=?");
                prep.setString(1, displayName);
                prep.setInt(2, sid);
                prep.executeUpdate();
                prep.close();
            }

            Integer parentId = adm.getParentId();
            if (parentId != null) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET pid=? WHERE sid=?");
                prep.setInt(1, parentId.intValue());
                prep.setInt(2, sid);
                prep.executeUpdate();
                prep.close();
            }

            String password = adm.getPassword();
            if (password != null) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET password=?, passwordMech=?, salt=? WHERE sid=?");
                PasswordDetails passwordDetails = cache.encryptPassword(adm);
                prep.setString(1, passwordDetails.getEncodedPassword());
                prep.setString(2, passwordDetails.getPasswordMech());
                prep.setBytes(3, passwordDetails.getSalt());
                prep.setInt(4, sid);
                prep.executeUpdate();
                prep.close();
            }

            Restriction[] res = adm.getRestrictions();
            if (res != null) {
                prep = oxcon.prepareStatement("DELETE FROM subadmin_restrictions WHERE sid=?");
                prep.setInt(1, sid);
                prep.executeUpdate();
                prep.close();
                for (Restriction r : res) {
                    int rid = r.getId().intValue();
                    prep = oxcon.prepareStatement("INSERT INTO subadmin_restrictions (sid,rid,value) VALUES(?,?,?)");
                    prep.setInt(1, sid);
                    prep.setInt(2, rid);
                    prep.setString(3, r.getValue());
                    prep.executeUpdate();
                    prep.close();
                }
            }

            changeCapabilities(adm, oxcon);
            changeConfiguration(adm, oxcon);
            changeTaxonomies(adm, oxcon);
            invalidateResellerCaches(adm.getId().intValue(), CAPABILITIES_REGION_NAME, CONFIGURATION_REGION_NAME, TAXONOMIES_REGION_NAME);

            oxcon.commit();
            rollback = false;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            // no Rollback needed as the connection is null at this moment
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            if (rollback) {
                doRollback(oxcon);
            }
            cache.closeWriteConfigDBSqlStuff(oxcon, prep, rs);
        }
    }

    @Override
    public ResellerAdmin create(ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;

        LOGGER.debug("create admin {}", adm);

        boolean rollback = false;
        try {
            oxcon = cache.getWriteConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            rollback = true;

            int adm_id = IDGenerator.getId(oxcon);

            prep = oxcon.prepareStatement("INSERT INTO subadmin (sid,pid,name,displayName,password,passwordMech,salt) VALUES (?,?,?,?,?,?,?)");
            prep.setInt(1, adm_id);
            prep.setInt(2, adm.getParentId().intValue());
            prep.setString(3, adm.getName());
            prep.setString(4, adm.getDisplayname());
            PasswordDetails encryptPassword = cache.encryptPassword(adm);
            prep.setString(5, encryptPassword.getEncodedPassword());
            prep.setString(6, encryptPassword.getPasswordMech());
            prep.setBytes(7, encryptPassword.getSalt());

            prep.executeUpdate();
            prep.close();

            HashSet<Restriction> res = OXResellerTools.array2HashSet(adm.getRestrictions());
            if (res != null) {
                Iterator<Restriction> i = res.iterator();
                while (i.hasNext()) {
                    Restriction r = i.next();
                    prep = oxcon.prepareStatement("INSERT INTO subadmin_restrictions (sid,rid,value) VALUES (?,?,?)");
                    prep.setInt(1, adm_id);
                    prep.setInt(2, r.getId().intValue());
                    prep.setString(3, r.getValue());
                    prep.executeUpdate();
                    prep.close();
                }
            }

            oxcon.commit();
            rollback = false;

            adm.setId(I(adm_id));
            return adm;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            if (rollback) {
                doRollback(oxcon);
            }
            cache.closeWriteConfigDBSqlStuff(oxcon, prep);
        }
    }

    @Override
    public void delete(ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;

        LOGGER.debug("delete admin {}", adm);

        boolean rollback = false;
        try {

            ResellerAdmin tmp = getData(new ResellerAdmin[] { adm })[0];

            oxcon = cache.getWriteConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            rollback = true;

            prep = oxcon.prepareStatement("DELETE FROM subadmin_restrictions WHERE sid=?");
            prep.setInt(1, tmp.getId().intValue());
            prep.executeUpdate();
            prep.close();

            prep = oxcon.prepareStatement("DELETE FROM subadmin_capabilities WHERE sid=?;");
            prep.setInt(1, tmp.getId().intValue());
            prep.executeUpdate();
            prep.close();

            prep = oxcon.prepareStatement("DELETE FROM subadmin_taxonomies WHERE sid=?;");
            prep.setInt(1, tmp.getId().intValue());
            prep.executeUpdate();
            prep.close();

            prep = oxcon.prepareStatement("DELETE FROM subadmin_config_properties WHERE sid=?;");
            prep.setInt(1, tmp.getId().intValue());
            prep.executeUpdate();
            prep.close();

            prep = oxcon.prepareStatement("DELETE FROM subadmin WHERE sid=?");
            prep.setInt(1, tmp.getId().intValue());
            prep.executeUpdate();

            oxcon.commit();
            rollback = false;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            if (rollback) {
                doRollback(oxcon);
            }
            cache.closeWriteConfigDBSqlStuff(oxcon, prep);
        }
    }

    @Override
    public ResellerAdmin[] list(String search_pattern, int pid) throws StorageException {

        LOGGER.debug("list using pattern {}, parent id {}", search_pattern, I(pid));

        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        String query = "SELECT * FROM subadmin WHERE ( sid LIKE ? OR name LIKE ?)";
        if (pid > 0) {
            query += " AND pid=?";
        }
        try {
            con = cache.getReadConnectionForConfigDB();
            String search_patterntmp = search_pattern.replace('*', '%');
            prep = con.prepareStatement(query);
            prep.setString(1, search_patterntmp);
            prep.setString(2, search_patterntmp);
            if (pid > 0) {
                prep.setInt(3, pid);
            }
            rs = prep.executeQuery();

            ArrayList<ResellerAdmin> ret = new ArrayList<ResellerAdmin>();
            while (rs.next()) {
                ResellerAdmin adm = new ResellerAdmin();
                adm.setId(I(rs.getInt("sid")));
                adm.setName(rs.getString(DATABASE_COLUMN_NAME));
                adm.setDisplayname(rs.getString("displayName"));
                adm.setPassword(rs.getString("password"));
                adm.setPasswordMech(rs.getString("passwordMech"));
                adm.setParentId(I(rs.getInt("pid")));
                adm.setSalt(rs.getBytes("salt"));
                adm = getRestrictionDataForAdmin(adm, con);
                adm.setCapabilities(getCapabilities(adm, con));
                adm.setTaxonomies(getTaxonomies(adm, con));
                adm.setConfiguration(getConfiguration(adm, con));
                ret.add(adm);
            }
            return ret.toArray(new ResellerAdmin[ret.size()]);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public ResellerAdmin[] list(String search_pattern) throws StorageException {
        return list(search_pattern, 0);
    }

    @Override
    public ResellerAdmin[] getData(ResellerAdmin[] admins) throws StorageException {
        return getData(admins, 0);
    }

    @Override
    public ResellerAdmin[] getData(ResellerAdmin[] admins, int pid) throws StorageException {
        LOGGER.debug("getData");

        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            ArrayList<ResellerAdmin> ret = new ArrayList<ResellerAdmin>();
            con = cache.getReadConnectionForConfigDB();
            for (ResellerAdmin adm : admins) {
                ResellerAdmin newadm = new ResellerAdmin(adm.getId(), adm.getName());
                String query = "SELECT * FROM subadmin WHERE ";
                boolean hasId = false;
                if (adm.getId() != null) {
                    query += "sid=?";
                    hasId = true;
                } else if (adm.getName() != null) {
                    query += "name=?";
                } else {
                    throw new InvalidDataException("either ID or name must be specified");
                }
                if (pid > 0) {
                    query += " AND pid=?";
                }
                prep = con.prepareStatement(query);
                if (hasId) {
                    prep.setInt(1, adm.getId().intValue());
                } else {
                    prep.setString(1, adm.getName());
                }
                if (pid > 0) {
                    prep.setInt(2, pid);
                }
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("unable to get data of reseller admin: " + (hasId ? "id=" + adm.getId() : "name=" + adm.getName()));
                }
                newadm.setName(rs.getString(DATABASE_COLUMN_NAME));
                newadm.setId(I(rs.getInt("sid")));
                newadm.setParentId(I(rs.getInt("pid")));
                newadm.setDisplayname(rs.getString("displayName"));
                newadm.setPassword(rs.getString("password"));
                newadm.setPasswordMech(rs.getString("passwordMech"));
                newadm.setSalt(rs.getBytes("salt"));

                rs.close();
                prep.close();

                newadm = getRestrictionDataForAdmin(newadm, con);

                newadm.setCapabilities(getCapabilities(newadm, con));
                newadm.setTaxonomies(getTaxonomies(newadm, con));
                newadm.setConfiguration(getConfiguration(newadm, con));

                ret.add(newadm);
            }
            return ret.toArray(new ResellerAdmin[ret.size()]);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public boolean existsAdmin(ResellerAdmin adm, int pid) throws StorageException {
        return existsAdmin(new ResellerAdmin[] { adm }, pid);
    }

    @Override
    public boolean existsAdmin(ResellerAdmin adm) throws StorageException {
        return existsAdmin(new ResellerAdmin[] { adm }, 0);
    }

    @Override
    public boolean existsAdmin(ResellerAdmin[] admins) throws StorageException {
        return existsAdmin(masteradmin);
    }

    private ResellerAdmin getRestrictionDataForAdmin(ResellerAdmin admin, Connection con) throws SQLException {
        PreparedStatement prep = con.prepareStatement("SELECT subadmin_restrictions.rid,sid,name,value FROM subadmin_restrictions INNER JOIN restrictions ON subadmin_restrictions.rid=restrictions.rid WHERE sid=?");
        ResultSet rs = null;
        try {
            if (admin.getParentId().intValue() > 0) {
                prep.setInt(1, admin.getParentId().intValue());
            } else {
                prep.setInt(1, admin.getId().intValue());
            }

            rs = prep.executeQuery();

            HashSet<Restriction> res = new HashSet<Restriction>();
            while (rs.next()) {
                Restriction r = new Restriction();
                r.setId(I(rs.getInt(DATABASE_COLUMN_ID)));
                r.setName(rs.getString(DATABASE_COLUMN_NAME));
                r.setValue(rs.getString(DATABASE_COLUMN_VALUE));
                if (admin.getParentId().intValue() > 0 && r.getName().equals(Restriction.SUBADMIN_CAN_CREATE_SUBADMINS)) {
                    continue;
                }
                res.add(r);
            }
            if (res.size() > 0) {
                admin.setRestrictions(res.toArray(new Restriction[res.size()]));
            }
            rs.close();
            prep.close();
            return admin;
        } finally {
            closeSQLStuff(rs, prep);
        }
    }

    @Override
    public void restore(Context ctx, int subadmin, Restriction[] restrictions, CustomField[] customFields) throws StorageException {
        LOGGER.debug("restore {}", ctx.getId());

        Connection oxcon = null;
        PreparedStatement prep = null;
        int rollback = 0;
        try {
            oxcon = cache.getWriteConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            rollback = 1;

            if (subadmin > 0) {
                prep = oxcon.prepareStatement("INSERT INTO context2subadmin (sid,cid) VALUES(?,?)");
                prep.setInt(1, subadmin);
                prep.setInt(2, ctx.getId().intValue());
                prep.executeUpdate();
                Databases.closeSQLStuff(prep);
                prep = null;
            }

            if (restrictions != null) {
                for (Restriction r : restrictions) {
                    prep = oxcon.prepareStatement("INSERT INTO context_restrictions (cid,rid,value) VALUES (?,?,?)");
                    prep.setInt(1, ctx.getId().intValue());
                    prep.setInt(2, r.getId().intValue());
                    prep.setString(3, r.getValue());
                    prep.executeUpdate();
                    Databases.closeSQLStuff(prep);
                    prep = null;
                }
            }

            if (customFields != null) {
                for (CustomField customField : customFields) {
                    prep = oxcon.prepareStatement("INSERT INTO context_customfields (cid, customid, createTimestamp, modifyTimestamp) VALUES (?,?, ?, ?)");
                    prep.setInt(1, ctx.getId().intValue());
                    prep.setString(2, customField.getCustomId());
                    prep.setLong(3, customField.getCreateTimestamp());
                    prep.setLong(4, customField.getModifyTimestamp());
                    prep.executeUpdate();
                    Databases.closeSQLStuff(prep);
                    prep = null;
                }
            }

            oxcon.commit();
            rollback = 2;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            Databases.closeSQLStuff(prep);
            if (rollback > 0) {
                if (rollback == 1) {
                    doRollback(oxcon);
                }
                Databases.autocommit(oxcon);
            }
            if (oxcon != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(oxcon);
                } catch (PoolException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    @Override
    public void ownContextToAdmin(Context ctx, Credentials creds) throws StorageException {
        LOGGER.debug("ownContext {} to admin {}", ctx.getId(), creds.getLogin());

        Connection oxcon = null;
        PreparedStatement prep = null;
        int rollback = 0;
        try {
            ResellerAdmin adm = getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
            if (ctx.getId() == null) {
                throw new InvalidDataException("ContextID must not be null");
            }

            oxcon = cache.getWriteConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            rollback = 1;

            prep = oxcon.prepareStatement("INSERT INTO context2subadmin (sid,cid) VALUES(?,?)");
            prep.setInt(1, adm.getId().intValue());
            prep.setInt(2, ctx.getId().intValue());
            prep.executeUpdate();

            oxcon.commit();
            rollback = 2;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            // no Rollback needed as the connection is null at this moment
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            Databases.closeSQLStuff(prep);
            if (rollback > 0) {
                if (rollback == 1) {
                    doRollback(oxcon);
                }
                Databases.autocommit(oxcon);
            }
            if (oxcon != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(oxcon);
                } catch (PoolException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    @Override
    public int unownContextFromAdmin(Context ctx, Credentials creds) throws StorageException {
        try {
            ResellerAdmin adm = getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
            return unownContextFromAdmin(ctx, adm);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        }
    }

    @Override
    public int unownContextFromAdmin(Context ctx, ResellerAdmin adm) throws StorageException {
        LOGGER.debug("unownContext {} from admin {}", ctx.getId(), adm.getName());

        Connection oxcon = null;
        PreparedStatement prep = null;
        int rollback = 0;
        try {
            if (adm.getId() == null) {
                throw new InvalidDataException("ResellerAdminID must not be null");
            }
            if (ctx.getId() == null) {
                throw new InvalidDataException("ContextID must not be null");
            }
            oxcon = cache.getWriteConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            rollback = 1;

            int subadminId = adm.getId().intValue();

            prep = oxcon.prepareStatement("DELETE FROM context2subadmin WHERE sid=? AND cid=?");
            prep.setInt(1, subadminId);
            prep.setInt(2, ctx.getId().intValue());
            prep.executeUpdate();

            oxcon.commit();
            rollback = 2;

            return subadminId;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            // no Rollback needed as the connection is null at this moment
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            Databases.closeSQLStuff(prep);
            if (rollback > 0) {
                if (rollback == 1) {
                    doRollback(oxcon);
                }
                Databases.autocommit(oxcon);
            }
            if (oxcon != null) {
                try {
                    cache.pushWriteConnectionForConfigDB(oxcon);
                } catch (PoolException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    @Override
    public ResellerAdmin getContextOwner(Context ctx) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        LOGGER.debug("getContextOwner from context {}", ctx.getId());
        try {
            if (ctx.getId() == null) {
                throw new InvalidDataException("ContextID must not be null");
            }
            oxcon = cache.getReadConnectionForConfigDB();
            prep = oxcon.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (!rs.next()) {
                return masteradmin;
            }
            return getData(new ResellerAdmin[] { new ResellerAdmin(rs.getInt("sid")) })[0];
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(oxcon, prep, rs);
        }
    }

    @Override
    public boolean ownsContext(Context ctx, int admid) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        LOGGER.debug("ownsContext ctx={}  admin={}", null == ctx ? null : ctx.getId(), I(admid));
        try {
            oxcon = cache.getReadConnectionForConfigDB();
            if (ctx == null) {
                prep = oxcon.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
                prep.setInt(1, admid);
                rs = prep.executeQuery();
                if (!rs.next()) {
                    return false;
                }
                return true;
            }

            prep = oxcon.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (!rs.next()) {
                return false;
            }
            if (rs.getInt("sid") != admid) {
                return false;
            }
            return true;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(oxcon, prep, rs);
        }
    }

    @Override
    public boolean ownsContextOrIsPidOfOwner(Context ctx, int admid) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        LOGGER.debug("ownsContextOrIsPidOfOwner ctx={}  admin={}", null == ctx ? null : ctx.getId(), I(admid));
        try {
            oxcon = cache.getReadConnectionForConfigDB();
            if (ctx == null) {
                prep = oxcon.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
                prep.setInt(1, admid);
                rs = prep.executeQuery();
                if (!rs.next()) {
                    return false;
                }
                return true;
            }

            prep = oxcon.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (!rs.next()) {
                return false;
            }
            int ownedSid = rs.getInt("sid");

            if (ownedSid == admid) {
                return true;
            }

            rs.close();
            prep.close();
            prep = oxcon.prepareStatement("SELECT sid FROM subadmin WHERE pid=? AND sid=?");
            prep.setInt(1, admid);
            prep.setInt(2, ownedSid);
            rs = prep.executeQuery();
            if (!rs.next()) {
                return false;
            }
            return true;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(oxcon, prep, rs);
        }
    }

    @Override
    public boolean checkOwnsContextAndSetSid(Context ctx, Credentials creds) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        LOGGER.debug("checkOwnsContextAndSetSid ctx={}  admin={}", null == ctx ? null : ctx.getId(), creds.getLogin());
        try {
            ResellerAdmin adm = getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
            oxcon = cache.getReadConnectionForConfigDB();
            if (ctx == null) {
                prep = oxcon.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
                prep.setInt(1, adm.getId().intValue());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    return false;
                }
                return true;
            }

            prep = oxcon.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (!rs.next()) {
                return false;
            }
            int sid = rs.getInt("sid");
            if (sid != adm.getId().intValue()) {
                return false;
            }
            OXContextExtensionImpl firstExtensionByName = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
            if (null == firstExtensionByName) {
                try {
                    ctx.addExtension(new OXContextExtensionImpl(sid));
                } catch (DuplicateExtensionException e) {
                    throw new StorageException(e);
                }
            } else {
                firstExtensionByName.setSid(sid);
            }
            return true;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(oxcon, prep, rs);
        }
    }

    @Override
    public Map<String, Restriction> listRestrictions(String search_pattern) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        LOGGER.debug("listRestrictions search_pattern={}", search_pattern);
        try {
            con = cache.getReadConnectionForConfigDB();
            String search_patterntmp = search_pattern.replace('*', '%');
            prep = con.prepareStatement("SELECT * FROM restrictions WHERE rid LIKE ? OR name LIKE ?");
            prep.setString(1, search_patterntmp);
            prep.setString(2, search_patterntmp);
            rs = prep.executeQuery();

            Map<String, Restriction> ret = new HashMap<String, Restriction>();
            while (rs.next()) {
                String name = rs.getString(DATABASE_COLUMN_NAME);
                ret.put(name, new Restriction(Integer.valueOf(rs.getInt(DATABASE_COLUMN_ID)), rs.getString(DATABASE_COLUMN_NAME)));
            }
            return ret;
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } finally {
            cache.closeReadConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public void checkPerSubadminRestrictions(Credentials creds, UserModuleAccess access, boolean contextAdmin, String... restriction_types) throws StorageException {
        LOGGER.debug("checkPerSubadminRestrictions");
        ResellerAdmin adm = getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
        HashSet<Restriction> restrictions = OXResellerTools.array2HashSet(adm.getRestrictions());
        // default is: not allowed to create SUBADMINS
        if (restrictions == null) {
            restrictions = new HashSet<Restriction>();
        }
        {
            Restriction subadminCanCreateSubadminsRestriction = new Restriction(Restriction.SUBADMIN_CAN_CREATE_SUBADMINS, "false");
            if (!restrictions.contains(subadminCanCreateSubadminsRestriction)) { // Contains is solely performed by name
                restrictions.add(subadminCanCreateSubadminsRestriction);
            }
        }
        if (restrictions.size() > 0) {
            Connection con = null;
            try {
                con = cache.getReadConnectionForConfigDB();
                for (Restriction res : restrictions) {
                    for (String tocheck : restriction_types) {
                        String name = res.getName();
                        String value = res.getValue();
                        if (name.equals(tocheck)) {
                            if (tocheck.equals(Restriction.MAX_CONTEXT_PER_SUBADMIN)) {
                                //long tstart = System.currentTimeMillis();
                                checkMaxContextRestriction(con, adm, Integer.parseInt(value));
                                //long tend = System.currentTimeMillis();
                                //System.out.println("checkMaxContextRestriction: " + (tend - tstart) + " ms");
                            } else if (tocheck.equals(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN)) {
                                //long tstart = System.currentTimeMillis();
                                checkMaxContextQuotaRestriction(con, adm, Long.parseLong(value));
                                //long tend = System.currentTimeMillis();
                                //System.out.println("checkMaxContextQuotaRestriction: " + (tend - tstart) + " ms");
                            } else if (tocheck.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN)) {
                                //long tstart = System.currentTimeMillis();
                                checkMaxOverallUserRestriction(con, adm, Integer.parseInt(value), true);
                                //long tend = System.currentTimeMillis();
                                //System.out.println("checkMaxOverallUserRestriction: " + (tend - tstart) + " ms");
                            } else if (tocheck.equals(Restriction.SUBADMIN_CAN_CREATE_SUBADMINS)) {
                                if (!OXResellerTools.isTrue(value)) {
                                    throw new OXResellerException(OXResellerException.Code.SUBADMIN_NOT_ALLOWED_TO_CREATE_SUBADMIN, adm.getName());
                                }
                            } else if (tocheck.equals(Restriction.MAX_SUBADMIN_PER_SUBADMIN)) {
                                checkSubadminRestriction(con, adm, Integer.parseInt(value));
                            }
                        } else if (name.startsWith(tocheck)) {
                            if (tocheck.startsWith(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX)) {
                                //long tstart = System.currentTimeMillis();
                                checkMaxOverallUserRestrictionByModuleAccess(con, adm.getId().intValue(), res, access, true, contextAdmin);
                                //long tend = System.currentTimeMillis();
                                //System.out.println("checkMaxOverallUserRestrictionByModuleAccess: " + (tend - tstart) + " ms");
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                LOGGER.error("", e);
                throw StorageException.storageExceotionFor(e);
            } catch (PoolException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (OXResellerException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (SQLException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (ClassNotFoundException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (OXGenericException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } finally {
                cache.closeReadConfigDBSqlStuff(con, null);
            }
        }
    }

    @Override
    public void checkPerContextRestrictions(Context ctx, UserModuleAccess access, boolean contextAdmin, String... restriction_types) throws StorageException {
        LOGGER.debug("checkPerContextRestrictions");
        Connection con = null;

        try {
            con = cache.getReadConnectionForConfigDB();
            ResellerAdmin adm = getResellerAdminForContext(ctx, con);
            Restriction[] admrestrictions = adm.getRestrictions();
            Restriction[] ctxrestrictions = getRestrictionsFromContext(ctx, con);
            if (admrestrictions != null && admrestrictions.length > 0) {
                for (Restriction res : admrestrictions) {
                    String name = res.getName();
                    for (String tocheck : restriction_types) {
                        if (tocheck.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN) && name.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN)) {
                            //long tstart = System.currentTimeMillis();
                            checkMaxOverallUserRestriction(con, adm, Integer.parseInt(res.getValue()), false);
                            //long tend = System.currentTimeMillis();
                            //System.out.println("checkMaxOverallUserRestriction: " + (tend - tstart) + " ms");
                        } else if (tocheck.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX) && name.startsWith(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX)) {
                            //long tstart = System.currentTimeMillis();
                            checkMaxOverallUserRestrictionByModuleAccess(con, adm.getId().intValue(), res, access, false, contextAdmin);
                            //long tend = System.currentTimeMillis();
                            //System.out.println("checkMaxOverallUserRestrictionByModuleAccess: " + (tend - tstart) + " ms");
                        }
                    }
                }
            }
            if (ctxrestrictions != null && ctxrestrictions.length > 0) {
                for (Restriction res : ctxrestrictions) {
                    String name = res.getName();
                    for (String tocheck : restriction_types) {
                        if (tocheck.equals(Restriction.MAX_USER_PER_CONTEXT) && name.equals(Restriction.MAX_USER_PER_CONTEXT)) {
                            //long tstart = System.currentTimeMillis();
                            checkMaxUserRestriction(ctx, Integer.parseInt(res.getValue()));
                            //long tend = System.currentTimeMillis();
                            //System.out.println("checkMaxUserRestriction: " + (tend - tstart) + " ms");
                        } else if (tocheck.equals(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX) && name.startsWith(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX)) {
                            //long tstart = System.currentTimeMillis();
                            checkMaxUserRestrictionByModuleAccess(ctx, res, access, contextAdmin);
                            //long tend = System.currentTimeMillis();
                            //System.out.println("checkMaxUserRestrictionByModuleAccess: " + (tend - tstart) + " ms");
                        }
                    }
                }
            }
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (OXResellerException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (OXGenericException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } finally {
            cache.closeReadConfigDBSqlStuff(con, null);
        }

    }

    @Override
    public Restriction[] applyRestrictionsToContext(Restriction[] restrictions, Context ctx) throws StorageException {
        LOGGER.debug("applyRestrictionsToContext {}", ctx);
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;

        List<Restriction> dropped = null;

        boolean rollback = false;
        try {
            oxcon = cache.getWriteConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            rollback = true;

            int cid = ctx.getId().intValue();
            prep = oxcon.prepareStatement("SELECT rid, value FROM context_restrictions WHERE cid=?");
            prep.setInt(1, cid);
            rs = prep.executeQuery();
            if (rs.next()) {
                dropped = new LinkedList<Restriction>();
                do {
                    dropped.add(new Restriction(Integer.valueOf(rs.getInt(1)), null, rs.getString(2)));
                } while (rs.next());
            }
            Databases.closeSQLStuff(rs, prep);
            rs = null;
            prep = null;

            prep = oxcon.prepareStatement("DELETE FROM context_restrictions WHERE cid=?");
            prep.setInt(1, cid);
            prep.executeUpdate();
            Databases.closeSQLStuff(prep);
            prep = null;

            if (restrictions != null) {
                for (Restriction r : restrictions) {
                    prep = oxcon.prepareStatement("INSERT INTO context_restrictions (cid,rid,value) VALUES (?,?,?)");
                    prep.setInt(1, cid);
                    prep.setInt(2, r.getId().intValue());
                    prep.setString(3, r.getValue());
                    prep.executeUpdate();
                    Databases.closeSQLStuff(prep);
                    prep = null;
                }
            }

            oxcon.commit();
            rollback = false;
        } catch (DataTruncation dt) {
            LOGGER.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            if (rollback) {
                doRollback(oxcon);
            }
            cache.closeWriteConfigDBSqlStuff(oxcon, prep, rs);
        }

        return dropped == null ? new Restriction[0] : dropped.toArray(new Restriction[dropped.size()]);
    }

    @Override
    public Restriction[] getRestrictionsFromContext(Context ctx) throws StorageException {
        LOGGER.debug("getRestrictionsFromContext {}", ctx);
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            return getRestrictionsFromContext(ctx, con);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(con, null, null);
        }
    }

    @Override
    public void initDatabaseRestrictions() throws StorageException {
        LOGGER.debug("initDatabaseRestrictions");
        Connection con = null;
        PreparedStatement prep = null;

        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDB();
            con.setAutoCommit(false);
            rollback = true;

            for (String res : Restriction.ALL_RESTRICTIONS) {
                int rid = IDGenerator.getId(con);
                prep = con.prepareStatement("INSERT INTO restrictions (rid,name) VALUES (?,?)");
                prep.setInt(1, rid);
                prep.setString(2, res);
                prep.executeUpdate();
                prep.close();
            }
            cache.initAccessCombinations();
            for (String mname : cache.getAccessCombinationNames().keySet()) {
                for (String prefix : new String[] { Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX, Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX }) {
                    int rid = IDGenerator.getId(con);
                    prep = con.prepareStatement("INSERT INTO restrictions (rid,name) VALUES (?,?)");
                    prep.setInt(1, rid);
                    prep.setString(2, prefix + mname);
                    prep.executeUpdate();
                    prep.close();
                }
            }

            con.commit();
            rollback = true;
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (OXGenericException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } finally {
            if (rollback) {
                doRollback(con);
            }
            cache.closeWriteConfigDBSqlStuff(con, prep);
        }
    }

    @Override
    public void removeDatabaseRestrictions() throws StorageException {
        LOGGER.debug("removeDatabaseRestrictions");
        Connection con = null;
        PreparedStatement prep = null;
        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDB();
            con.setAutoCommit(false);
            rollback = true;

            prep = con.prepareStatement("DELETE FROM restrictions");
            prep.executeUpdate();

            con.commit();
            rollback = false;
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } finally {
            if (rollback) {
                doRollback(con);
            }
            cache.closeWriteConfigDBSqlStuff(con, prep);
        }
    }

    @Override
    public String getCustomId(Context ctx) throws StorageException {
        LOGGER.debug("getCustomId from context {}", ctx);
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            con = cache.getReadConnectionForConfigDB();
            prep = con.prepareStatement("SELECT customid FROM context_customfields WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return rs.getString(1);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } finally {
            cache.closeReadConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public void writeCustomId(Context ctx) throws StorageException {
        LOGGER.debug("writeCustomId to context {}", ctx);
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        OXContextExtensionImpl contextExtension = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
        if (contextExtension != null && contextExtension.getCustomid() != null) {
            try {
                con = cache.getWriteConnectionForConfigDB();
                prep = con.prepareStatement("SELECT cid FROM context_customfields WHERE cid=?");
                prep.setInt(1, ctx.getId().intValue());
                rs = prep.executeQuery();
                boolean idexists = false;
                if (rs.next()) {
                    idexists = true;
                }
                prep.close();
                rs.close();
                if (idexists) {
                    prep = con.prepareStatement("UPDATE context_customfields SET customid=? WHERE cid=?");
                    prep.setString(1, contextExtension.getCustomid());
                    prep.setInt(2, ctx.getId().intValue());
                } else {
                    prep = con.prepareStatement("INSERT INTO context_customfields (cid,customid) VALUES(?,?)");
                    prep.setInt(1, ctx.getId().intValue());
                    prep.setString(2, contextExtension.getCustomid());
                }
                prep.executeUpdate();
                prep.close();
            } catch (PoolException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (SQLException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (RuntimeException e) {
                LOGGER.error("", e);
                throw StorageException.storageExceotionFor(e);
            } finally {
                cache.closeWriteConfigDBSqlStuff(con, prep, rs);
            }
        }
    }

    @Override
    public CustomField[] deleteCustomFields(Context ctx) throws StorageException {
        LOGGER.debug("deleteCustomFields from context {}", ctx);
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            con = cache.getWriteConnectionForConfigDB();

            prep = con.prepareStatement("SELECT customid, createTimestamp, modifyTimestamp FROM context_customfields WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (!rs.next()) {
                return new CustomField[0];
            }

            List<CustomField> dropped = new LinkedList<CustomField>();
            do {
                dropped.add(new CustomField(rs.getString(1), rs.getLong(2), rs.getLong(3)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, prep);
            rs = null;
            prep = null;

            prep = con.prepareStatement("DELETE FROM context_customfields WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            prep.executeUpdate();
            Databases.closeSQLStuff(prep);
            prep = null;

            return dropped.toArray(new CustomField[dropped.size()]);
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } finally {
            cache.closeWriteConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public void generateCreateTimestamp(Context ctx) throws StorageException {
        LOGGER.debug("generateCreateTimestamp for context {}", ctx);
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            con = cache.getWriteConnectionForConfigDB();
            prep = con.prepareStatement("INSERT INTO context_customfields (cid,createTimestamp,modifyTimestamp) VALUES(?,?,?)");
            long ctime = System.currentTimeMillis();
            prep.setInt(1, ctx.getId().intValue());
            prep.setLong(2, ctime);
            prep.setLong(3, ctime);
            prep.executeUpdate();
            prep.close();
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } finally {
            cache.closeWriteConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public void updateModifyTimestamp(Context ctx) throws StorageException {
        LOGGER.debug("updateModifyTimestamp for context {}", ctx);
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            con = cache.getWriteConnectionForConfigDB();
            prep = con.prepareStatement("SELECT * FROM context_customfields WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();

            if (rs.next()) {
                prep.close();
                rs.close();
                prep = con.prepareStatement("UPDATE context_customfields SET modifyTimestamp=? WHERE cid=?");
                long ctime = System.currentTimeMillis();
                prep.setLong(1, ctime);
                prep.setInt(2, ctx.getId().intValue());
                prep.executeUpdate();
                prep.close();
            }
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } finally {
            cache.closeWriteConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public void updateModuleAccessRestrictions() throws StorageException, OXResellerException {
        LOGGER.debug("updateModuleAccessRestrictions");
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con = cache.getWriteConnectionForConfigDB();
            cache.initAccessCombinations();
            HashSet<String> usedCombinations = new HashSet<String>();
            // find out, which restrictions are already used/referenced
            // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
            for (String query : new String[] { "SELECT r.name FROM subadmin_restrictions AS sr LEFT JOIN restrictions AS r ON ( r.rid=sr.rid ) WHERE r.name LIKE ? OR r.name LIKE ? GROUP BY r.name", "SELECT r.name FROM context_restrictions  AS cr LEFT JOIN restrictions AS r ON ( r.rid=cr.rid ) WHERE r.name LIKE ? OR r.name LIKE ? GROUP BY r.name" }) {
                prep = con.prepareStatement(query);
                prep.setString(1, Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX + "%");
                prep.setString(2, Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX + "%");
                rs = prep.executeQuery();
                while (rs.next()) {
                    usedCombinations.add(rs.getString(1));
                }
                prep.close();
                rs.close();
            }

            // if referenced restrictions are going to be removed, throw exception
            StringBuffer sb = new StringBuffer();
            HashMap<String, UserModuleAccess> newCombinations = cache.getAccessCombinationNames();
            for (String fullname : usedCombinations) {
                String cname = null;
                if (fullname.startsWith(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX)) {
                    cname = fullname.substring(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX.length());
                } else if (fullname.startsWith(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX)) {
                    cname = fullname.substring(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX.length());
                }
                if (cname != null && !newCombinations.containsKey(cname)) {
                    sb.append(fullname);
                    sb.append(",");
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                throw new OXResellerException(Code.MODULE_ACCESS_RESTRICTIONS_IN_USE, sb.toString());
            }

            // find out which restrictions to remove/add
            Map<String, Restriction> curCombinations = listRestrictions("*");

            con.setAutoCommit(false);
            rollback = true;
            for (String cname : newCombinations.keySet()) {
                String percontext = Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX + cname;
                String persubadmin = Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX + cname;
                if (!curCombinations.containsKey(percontext)) {
                    addRestriction(con, percontext);
                }
                if (!curCombinations.containsKey(persubadmin)) {
                    addRestriction(con, persubadmin);
                }
            }
            for (String fullname : curCombinations.keySet()) {
                String cname = null;
                if (fullname.startsWith(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX)) {
                    cname = fullname.substring(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX.length());
                } else if (fullname.startsWith(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX)) {
                    cname = fullname.substring(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX.length());
                }
                if (cname != null && !newCombinations.containsKey(cname)) {
                    removeRestriction(con, fullname);
                }
            }

            con.commit();
            rollback = false;
        } catch (PoolException e) {
            LOGGER.error("", e);
            // no Rollback needed as the connection is null at this moment
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (OXGenericException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceotionFor(e);
        } finally {
            if (rollback) {
                doRollback(con);
            }
            cache.closeWriteConfigDBSqlStuff(con, prep, rs);
        }
    }

    @Override
    public void updateRestrictions() throws StorageException, OXResellerException {
        LOGGER.debug("updateRestrictions");
        Connection con = null;
        PreparedStatement prep = null;

        HashSet<String> missingRestrictions = new HashSet<String>();
        Map<String, Restriction> curCombinations = listRestrictions("*");
        for (String res : Restriction.ALL_RESTRICTIONS) {
            if (!curCombinations.containsKey(res)) {
                missingRestrictions.add(res);
            }
        }
        if (missingRestrictions.size() > 0) {
            boolean rollback = false;
            try {
                con = cache.getWriteConnectionForConfigDB();
                con.setAutoCommit(false);
                rollback = true;

                for (String res : missingRestrictions) {
                    int rid = IDGenerator.getId(con);
                    prep = con.prepareStatement("INSERT INTO restrictions (rid,name) VALUES (?,?)");
                    prep.setInt(1, rid);
                    prep.setString(2, res);
                    prep.executeUpdate();
                    prep.close();
                }

                con.commit();
                rollback = false;
            } catch (PoolException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (SQLException e) {
                LOGGER.error("", e);
                throw new StorageException(e.getMessage());
            } catch (RuntimeException e) {
                LOGGER.error("", e);
                throw StorageException.storageExceotionFor(e);
            } finally {
                if (rollback) {
                    doRollback(con);
                }
                cache.closeWriteConfigDBSqlStuff(con, prep);
            }
        }
    }

    ////////////////////////////////////////// HELPERS //////////////////////////////////////////

    /**
     * Checks whether the specified reseller admins exist
     *
     * @param admins The reseller adamins
     * @param pid The pid The parent
     * @return
     * @throws StorageException
     */
    private boolean existsAdmin(ResellerAdmin[] admins, int pid) throws StorageException {
        LOGGER.debug("existsAdmin");

        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;

        Credentials mastercreds = cache.getMasterCredentials();
        try {
            con = cache.getReadConnectionForConfigDB();
            for (ResellerAdmin adm : admins) {
                String name = adm.getName();
                // cannot create radm with same name like master admin
                if (name != null && null != mastercreds && mastercreds.getLogin().equals(name)) {
                    return true;
                }
                String query = "SELECT sid FROM subadmin WHERE ";
                boolean hasId = false;
                if (adm.getId() != null) {
                    query += "sid=?";
                    hasId = true;
                } else if (name != null) {
                    query += "name=?";
                } else {
                    throw new InvalidDataException("either ID or name must be specified");
                }
                if (pid > 0) {
                    query += " AND pid=?";
                }
                prep = con.prepareStatement(query);
                if (hasId) {
                    prep.setInt(1, adm.getId().intValue());
                } else {
                    prep.setString(1, name);
                }
                if (pid > 0) {
                    prep.setInt(2, pid);
                }
                rs = prep.executeQuery();
                if (!rs.next()) {
                    return false;
                }
                rs.close();
                prep.close();
            }
            return true;
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeReadConfigDBSqlStuff(con, prep, rs);
        }
    }

    /**
     * Check whether maxvalue of {@link Restriction.MAX_CONTEXT} has been reached
     *
     * @param con
     * @param adm
     * @param maxvalue
     * @throws StorageException
     * @throws OXResellerException
     * @throws SQLException
     */
    private void checkMaxContextRestriction(Connection con, ResellerAdmin adm, int maxvalue) throws StorageException, OXResellerException, SQLException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            String query = "SELECT COUNT(cid) FROM context2subadmin WHERE sid IN (";
            int pid = adm.getParentId().intValue();
            prep = con.prepareStatement(getIN(query, pid > 0 ? 2 : 1));
            prep.setInt(1, adm.getId().intValue());
            if (pid > 0) {
                prep.setInt(2, pid);
            }
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw new StorageException("unable to count the number of context belonging to " + adm.getName());
            }
            if (rs.getInt("COUNT(cid)") >= maxvalue) {
                throw new OXResellerException(Code.MAXIMUM_NUMBER_CONTEXT_REACHED, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw e;
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw StorageException.storageExceptionFor(e);
        } finally {
            cache.closeConfigDBSqlStuff(prep, rs);
        }
    }

    /**
     * Check whether maxvalue of {@link Restriction.MAX_CONTEXT_QUOTA} has been reached
     *
     * @param con
     * @param adm
     * @param maxvalue
     * @throws OXResellerException
     * @throws SQLException
     * @throws PoolException
     */
    private void checkMaxContextQuotaRestriction(Connection con, ResellerAdmin adm, long maxvalue) throws OXResellerException, SQLException, PoolException {
        PreparedStatement prep = null;
        PreparedStatement prep2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        Connection oxcon = null;
        int cid = -1;
        try {
            prep = con.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
            prep.setInt(1, adm.getId().intValue());
            rs = prep.executeQuery();
            long qused = 0;
            while (rs.next()) {
                cid = rs.getInt("cid");
                oxcon = cache.getConnectionForContext(cid);
                prep2 = oxcon.prepareStatement("SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ? AND filestore_usage.user = 0");
                prep2.setInt(1, cid);
                rs2 = prep2.executeQuery();
                if (rs2.next()) {
                    qused += rs2.getLong(1);
                }
                prep2.close();
                rs2.close();
                cache.pushConnectionForContextAfterReading(cid, oxcon);
                // set to null to prevent double pushback in finally
                oxcon = null;
            }
            qused = qused >> 20;
            if (qused >= maxvalue) {
                throw new OXResellerException(Code.MAXIMUM_OVERALL_CONTEXT_QUOTA, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw e;
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw e;
        } finally {
            cache.closeContextSqlStuff(oxcon, cid, true);
            cache.closeConfigDBSqlStuff(prep, rs);
            cache.closeConfigDBSqlStuff(prep2, rs2);
        }
    }

    private void checkMaxOverallUserRestriction(Connection con, ResellerAdmin adm, int maxvalue, boolean contextMode) throws OXResellerException, SQLException, PoolException {
        PreparedStatement prep = null;
        PreparedStatement prep2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        Connection oxcon = null;
        int cid = -1;
        try {
            prep = con.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
            prep.setInt(1, adm.getId().intValue());
            rs = prep.executeQuery();
            // start count at one for the current context to be created when called from Context Plugin
            // methods, because the context to be created is not yet listed in context2subadmin table
            int count = contextMode ? 1 : 0;
            while (rs.next()) {
                cid = rs.getInt("cid");
                oxcon = cache.getConnectionForContext(cid);
                prep2 = oxcon.prepareStatement("SELECT COUNT(cid) FROM user WHERE cid=?");
                prep2.setInt(1, cid);
                rs2 = prep2.executeQuery();
                if (rs2.next()) {
                    count += rs2.getInt(1);
                }
                prep2.close();
                rs2.close();
                cache.pushConnectionForContextAfterReading(cid, oxcon);
                // set to null to prevent double pushback in finally
                oxcon = null;
            }
            if (count > maxvalue) {
                throw new OXResellerException(Code.MAXIMUM_OVERALL_NUMBER_OF_CONTEXT_REACHED, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw e;
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw e;
        } finally {
            cache.closeContextSqlStuff(oxcon, cid, true);
            cache.closeConfigDBSqlStuff(prep, rs);
            cache.closeConfigDBSqlStuff(prep2, rs2);
        }
    }

    private void checkSubadminRestriction(Connection con, ResellerAdmin adm, int maxvalue) throws OXResellerException, SQLException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT COUNT(sid) FROM subadmin WHERE pid=?");
            prep.setInt(1, adm.getId().intValue());
            rs = prep.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count >= maxvalue) {
                    throw new OXResellerException(Code.MAXIMUM_NUMBER_OF_SUBADMIN_PER_SUBADMIN_REACHED, String.valueOf(maxvalue));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw e;
        } finally {
            cache.closeConfigDBSqlStuff(prep, rs);
        }
    }

    private boolean isSameModuleAccess(UserModuleAccess a, UserModuleAccess b) {
        return a.equals(b);
    }

    private int countUsersByModuleAccess(Context ctx, UserModuleAccess access) throws StorageException {
        try {
            UserPermissionBits ubp = new UserPermissionBits(0, 0, null, ctx.getId().intValue());
            ubp.setCalendar(access.getCalendar());
            ubp.setContact(access.getContacts());
            ubp.setFullPublicFolderAccess(access.getEditPublicFolders());
            ubp.setFullSharedFolderAccess(access.getReadCreateSharedFolders());
            ubp.setICal(access.getIcal());
            ubp.setInfostore(access.getInfostore());
            ubp.setSyncML(access.getSyncml());
            ubp.setTask(access.getTasks());
            ubp.setVCard(access.getVcard());
            ubp.setWebDAV(access.getWebdav());
            ubp.setWebDAVXML(access.getWebdavXml());
            ubp.setWebMail(access.getWebmail());
            ubp.setDelegateTasks(access.getDelegateTask());
            ubp.setEditGroup(access.getEditGroup());
            ubp.setEditResource(access.getEditResource());
            ubp.setEditPassword(access.getEditPassword());
            ubp.setCollectEmailAddresses(access.isCollectEmailAddresses());
            ubp.setMultipleMailAccounts(access.isMultipleMailAccounts());
            ubp.setSubscription(access.isSubscription());
            ubp.setPublication(access.isPublication());
            ubp.setActiveSync(access.isActiveSync());
            ubp.setUSM(access.isUSM());
            ubp.setOLOX20(access.isOLOX20());

            int ret = RdbUserPermissionBitsStorage.adminCountUsersByPermission(ctx.getId().intValue(), ubp, null);
            if (ret < 0) {
                throw new StorageException("unable to count number of users by module access");
            }
            return ret;
        } catch (SQLException sqle) {
            LOGGER.error("SQL Error ", sqle);
            throw new StorageException(sqle.toString());
        } catch (OXException e) {
            LOGGER.error("DBPool error", e);
            throw new StorageException(e);
        }
    }

    private void checkMaxOverallUserRestrictionByModuleAccess(Connection con, int admid, Restriction res, UserModuleAccess newaccess, boolean contextMode, boolean contextAdmin) throws StorageException, OXResellerException, SQLException, ClassNotFoundException, OXGenericException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        int cid = -1;
        try {
            if (newaccess == null) {
                throw new OXResellerException(Code.MODULE_ACCESS_NOT_NULL);
            }
            cache.initAccessCombinations();
            String name = res.getName();
            UserModuleAccess namedaccess = cache.getNamedAccessCombination(name.substring(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX.length()), contextAdmin);
            if (isSameModuleAccess(newaccess, namedaccess)) {

                prep = con.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
                prep.setInt(1, admid);
                rs = prep.executeQuery();
                // start count at one for the current context to be created when called from Context Plugin
                // methods, because the context to be created is not yet listed in context2subadmin table
                int count = contextMode ? 1 : 0;
                int maxvalue = Integer.parseInt(res.getValue());
                while (rs.next()) {
                    cid = rs.getInt("cid");
                    Context ctx = new Context(I(cid));
                    count += countUsersByModuleAccess(ctx, namedaccess);
                    if (count > maxvalue) {
                        throw new OXResellerException(Code.MAXIMUM_OVERALL_NUMBER_OF_USERS_BY_MODULEACCESS_REACHED, name + ":" + String.valueOf(maxvalue));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw e;
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
            throw e;
        } catch (OXGenericException e) {
            LOGGER.error("", e);
            throw e;
        } finally {
            cache.closeConfigDBSqlStuff(prep, rs);
        }
    }

    /**
     * @param ctx
     * @param maxvalue
     * @throws StorageException
     * @throws OXResellerException
     * @throws SQLException
     * @throws PoolException
     */
    private void checkMaxUserRestriction(Context ctx, int maxvalue) throws StorageException, OXResellerException, SQLException, PoolException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        Connection oxcon = null;
        int cid = -1;
        try {
            cid = ctx.getId().intValue();
            oxcon = cache.getConnectionForContext(cid);
            prep = oxcon.prepareStatement("SELECT COUNT(cid) FROM user WHERE cid=?");
            prep.setInt(1, cid);
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw new StorageException("unable to count the number of users belonging to " + ctx.getName());
            }
            if (rs.getInt(1) > maxvalue) {
                throw new OXResellerException(Code.MAXIMUM_NUMBER_OF_USERS_PER_CONTEXT_REACHED, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw e;
        } catch (PoolException e) {
            LOGGER.error("", e);
            throw e;
        } finally {
            cache.closeConfigDBSqlStuff(prep, rs);
            cache.closeContextSqlStuff(oxcon, cid);
            // set to null to prevent double pushback in finally
            oxcon = null;
        }
    }

    /**
     * @param ctx
     * @param res
     * @param newaccess
     * @throws StorageException
     * @throws OXResellerException
     * @throws ClassNotFoundException
     * @throws OXGenericException
     */
    private void checkMaxUserRestrictionByModuleAccess(Context ctx, Restriction res, UserModuleAccess newaccess, boolean contextAdmin) throws StorageException, OXResellerException, ClassNotFoundException, OXGenericException {
        try {
            cache.initAccessCombinations();
            String name = res.getName();
            UserModuleAccess namedaccess = cache.getNamedAccessCombination(name.substring(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX.length()), contextAdmin);
            if (isSameModuleAccess(newaccess, namedaccess)) {
                int maxvalue = Integer.parseInt(res.getValue());
                if (countUsersByModuleAccess(ctx, namedaccess) > maxvalue) {
                    throw new OXResellerException(Code.MAXIMUM_OVERALL_NUMBER_OF_USERS_BY_MODULEACCESS_REACHED, name + ":" + String.valueOf(maxvalue));
                }

            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
            throw e;
        } catch (OXGenericException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    /**
     * @param ctx
     * @param con
     * @return
     * @throws SQLException
     * @throws StorageException
     */
    private ResellerAdmin getResellerAdminForContext(Context ctx, Connection con) throws SQLException, StorageException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();
            if (!rs.next()) {
                throw new StorageException("unable to determine owner of Context " + ctx.getId());
            }
            return getData(new ResellerAdmin[] { new ResellerAdmin(rs.getInt(1)) })[0];
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw e;
        } catch (StorageException e) {
            LOGGER.error("", e);
            throw e;
        } finally {
            cache.closeConfigDBSqlStuff(prep, rs);
        }
    }

    /**
     * @param ctx
     * @param con
     * @return
     * @throws StorageException
     */
    private Restriction[] getRestrictionsFromContext(Context ctx, Connection con) throws StorageException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT context_restrictions.rid,cid,name,value FROM context_restrictions INNER JOIN restrictions ON context_restrictions.rid=restrictions.rid WHERE cid=?");
            prep.setInt(1, ctx.getId().intValue());
            rs = prep.executeQuery();

            HashSet<Restriction> res = new HashSet<Restriction>();
            while (rs.next()) {
                res.add(new Restriction(I(rs.getInt(DATABASE_COLUMN_ID)), rs.getString(DATABASE_COLUMN_NAME), rs.getString(DATABASE_COLUMN_VALUE)));
            }
            return res.size() > 0 ? res.toArray(new Restriction[res.size()]) : null;
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeConfigDBSqlStuff(prep, rs);
        }
    }

    private void removeRestriction(Connection con, String name) throws SQLException {
        PreparedStatement prep = con.prepareStatement("DELETE FROM restrictions WHERE name = ?");
        try {
            prep.setString(1, name);
            prep.executeUpdate();
            prep.close();
        } finally {
            closeSQLStuff(prep);
        }
    }

    private void addRestriction(Connection con, String name) throws SQLException {
        int rid = IDGenerator.getId(con);
        PreparedStatement prep = con.prepareStatement("INSERT INTO restrictions (rid,name) VALUES (?,?)");
        try {
            prep.setInt(1, rid);
            prep.setString(2, name);
            prep.executeUpdate();
            prep.close();
        } finally {
            closeSQLStuff(prep);
        }
    }

    private void doRollback(Connection con) {
        if (null != con) {
            try {
                con.rollback();
            } catch (SQLException e2) {
                LOGGER.error("Error doing rollback", e2);
            }
        }
    }

    /**
     * Changes the capabilities for the specified reseller admin
     *
     * @param resellerAdmin The reseller admin
     * @param connection The writeable connection
     * @throws StorageException if a storage error is occurred
     */
    private void changeCapabilities(ResellerAdmin resellerAdmin, Connection connection) throws StorageException {
        int resellerId = resellerAdmin.getId();
        Set<String> capsToDrop = resellerAdmin.getCapabilitiesToDrop();
        Set<String> capsToAdd = resellerAdmin.getCapabilitiesToAdd();
        Set<String> capsToRemove = resellerAdmin.getCapabilitiesToRemove();

        PreparedStatement stmt = null;
        try {
            // First drop
            if (null != capsToDrop && !capsToDrop.isEmpty()) {
                for (String cap : capsToDrop) {
                    if (null == stmt) {
                        stmt = connection.prepareStatement("DELETE FROM subadmin_capabilities WHERE sid=? AND capability=?");
                        stmt.setInt(1, resellerId);
                    }
                    stmt.setString(2, cap);
                    stmt.addBatch();
                    if (cap.startsWith("-")) {
                        stmt.setString(2, cap.substring(1));
                        stmt.addBatch();
                    } else {
                        stmt.setString(2, "-" + cap);
                        stmt.addBatch();
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Determine what is already present
            Set<String> existing;
            {
                stmt = connection.prepareStatement("SELECT capability FROM subadmin_capabilities WHERE sid=?");
                stmt.setInt(1, resellerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    existing = new HashSet<String>(16);
                    do {
                        existing.add(rs.getString(1));
                    } while (rs.next());
                } else {
                    existing = Collections.<String> emptySet();
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
                rs = null;
            }
            Set<String> capsToInsert = new HashSet<String>(capsToAdd);
            // Delete existing ones
            if (null != capsToRemove && !capsToRemove.isEmpty()) {
                for (String cap : capsToRemove) {
                    if (existing.contains(cap)) {
                        if (null == stmt) {
                            stmt = connection.prepareStatement("DELETE FROM subadmin_capabilities WHERE sid=? AND capability=?");
                            stmt.setInt(1, resellerId);
                        }
                        stmt.setString(2, cap);
                        stmt.addBatch();
                        existing.remove(cap);
                    }
                    String plusCap = "+" + cap;
                    if (existing.contains(plusCap)) {
                        if (null == stmt) {
                            stmt = connection.prepareStatement("DELETE FROM subadmin_capabilities WHERE sid=? AND capability=?");
                            stmt.setInt(1, resellerId);
                        }
                        stmt.setString(2, plusCap);
                        stmt.addBatch();
                        existing.remove(plusCap);
                    }
                    String minusCap = "-" + cap;
                    if (!existing.contains(minusCap)) {
                        capsToInsert.add(minusCap);
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Insert new ones
            if (!capsToInsert.isEmpty()) {
                for (String capToAdd : capsToAdd) {
                    String minusCap = "-" + capToAdd;
                    if (existing.contains(minusCap)) {
                        if (null == stmt) {
                            stmt = connection.prepareStatement("DELETE FROM subadmin_capabilities WHERE sid=? AND capability=?");
                            stmt.setInt(1, resellerId);
                        }
                        stmt.setString(2, minusCap);
                        stmt.addBatch();
                    }
                }
                if (null != stmt) {
                    stmt.executeBatch();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                stmt = connection.prepareStatement("INSERT INTO subadmin_capabilities (sid, capability) VALUES (?, ?)");
                stmt.setInt(1, resellerId);
                for (String cap : capsToInsert) {
                    if (cap.startsWith("-")) {
                        // A capability to remove
                        stmt.setString(2, cap);
                        stmt.addBatch();
                    } else {
                        if (!existing.contains(cap) && !existing.contains("+" + cap)) {
                            // A capability to add
                            stmt.setString(2, cap);
                            stmt.addBatch();
                        }
                    }
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
        } catch (SQLException e) {
            LOGGER.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Changes the configuration for the specified reseller
     *
     * @param resellerAdmin The reseller admin
     * @param connection The writeable connection
     * @throws StorageException
     */
    private void changeConfiguration(ResellerAdmin resellerAdmin, Connection connection) throws StorageException {
        int resellerId = resellerAdmin.getId();
        PreparedStatement stmt = null;
        try {
            if (resellerAdmin.isConfigurationToRemoveSet() && false == resellerAdmin.getConfigurationToRemove().isEmpty()) {
                Set<String> keys = resellerAdmin.getConfigurationToRemove().stream().filter(p -> p.startsWith("com.openexchange.capability")).collect(Collectors.toSet());
                stmt = connection.prepareStatement("DELETE FROM subadmin_config_properties WHERE sid = ? AND propertyKey IN (" + buildKeySubQuery((keys)) + ") ;");
                stmt.setInt(1, resellerId);
                int parameterIndex = 2;
                for (String key : keys) {
                    stmt.setString(parameterIndex++, key);
                }
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            if (resellerAdmin.isConfigurationToAddSet() && false == resellerAdmin.getConfigurationToAdd().isEmpty()) {
                stmt = connection.prepareStatement("INSERT INTO subadmin_config_properties (sid, propertyKey, propertyValue) VALUES (?,?,?) ON DUPLICATE KEY UPDATE propertyKey=?, propertyValue=?;");
                stmt.setInt(1, resellerId);
                for (Entry<String, String> entry : resellerAdmin.getConfigurationToAdd().entrySet()) {
                    if (entry.getKey().startsWith("com.openexchange.capability")) {
                        continue;
                    }
                    int parameterIndex = 2;
                    stmt.setString(parameterIndex++, entry.getKey());
                    stmt.setString(parameterIndex++, entry.getValue());
                    stmt.setString(parameterIndex++, entry.getKey());
                    stmt.setString(parameterIndex++, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
        } catch (SQLException e) {
            LOGGER.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Changes the taxonomies for the specified reseller
     *
     * @param resellerAdmin The reseller admin
     * @param connection The writeable connection
     * @throws StorageException
     */
    private void changeTaxonomies(ResellerAdmin resellerAdmin, Connection connection) throws StorageException {
        int resellerId = resellerAdmin.getId();
        PreparedStatement stmt = null;
        try {
            if (resellerAdmin.isTaxonomiesToRemoveSet() && false == resellerAdmin.getTaxonomiesToRemove().isEmpty()) {
                Set<String> keys = resellerAdmin.getTaxonomiesToRemove();
                stmt = connection.prepareStatement("DELETE FROM subadmin_taxonomies WHERE sid = ? AND taxonomy IN (" + buildKeySubQuery((keys)) + ") ;");
                stmt.setInt(1, resellerId);
                int parameterIndex = 2;
                for (String key : keys) {
                    stmt.setString(parameterIndex++, key);
                }
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            if (resellerAdmin.isTaxonomiesToAddSet() && false == resellerAdmin.getTaxonomiesToAdd().isEmpty()) {
                stmt = connection.prepareStatement("INSERT INTO subadmin_taxonomies (sid, taxonomy) VALUES (?,?) ON DUPLICATE KEY UPDATE taxonomy=?;");
                stmt.setInt(1, resellerId);
                for (String entry : resellerAdmin.getTaxonomiesToAdd()) {
                    int parameterIndex = 2;
                    stmt.setString(parameterIndex++, entry);
                    stmt.setString(parameterIndex++, entry);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
        } catch (SQLException e) {
            LOGGER.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Invalidates reseller cache regions
     * 
     * @param resellerId The reseller identifier
     * @param cacheRegions The cache regions
     */
    private void invalidateResellerCaches(int resellerId, String... cacheRegions) {
        BundleContext context = AdminCache.getBundleContext();
        if (null == context) {
            return;
        }
        CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return;
        }
        try {
            for (String cacheRegion : cacheRegions) {
                Cache cache = cacheService.getCache(cacheRegion);
                cache.remove(resellerId);
            }
        } catch (OXException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * Retrieves all configuration properties for the specified reseller admin
     * 
     * @param adm The reseller admin
     * @param con The connection
     * @return The configuration map
     * @throws StorageException if an error is occurred
     */
    private Map<String, String> getConfiguration(ResellerAdmin adm, Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT propertyKey, propertyValue FROM subadmin_config_properties WHERE sid = ?;");
            int pIndex = 1;
            stmt.setInt(pIndex++, adm.getId());
            rs = stmt.executeQuery();

            Map<String, String> props = new HashMap<>(4);
            while (rs.next()) {
                props.put(rs.getString(1), rs.getString(2));
            }
            return props;
        } catch (SQLException e) {
            LOGGER.error("SQL Error", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Retrieves all taxonomies for the specified reseller admin
     * 
     * @param adm The reseller admin
     * @param con The connection
     * @return The taxonomies set
     * @throws StorageException if an error is occurred
     */
    private Set<String> getTaxonomies(ResellerAdmin adm, Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT taxonomy FROM subadmin_taxonomies WHERE sid=?");
            stmt.setLong(1, adm.getId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return ImmutableSet.of();
            }
            Set<String> taxonomies = new HashSet<String>();
            do {
                taxonomies.add(rs.getString(1));
            } while (rs.next());
            return taxonomies;
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Retrieves all capabilities for the specified reseller admin
     * 
     * @param adm The reseller admin
     * @param con The connection
     * @return The capabilities set
     * @throws StorageException if an error is occurred
     */
    private Set<String> getCapabilities(ResellerAdmin adm, Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT capability FROM subadmin_capabilities WHERE sid=?");
            stmt.setLong(1, adm.getId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return ImmutableSet.of();
            }
            Set<String> capas = new HashSet<String>();
            do {
                capas.add(rs.getString(1));
            } while (rs.next());
            return capas;
        } catch (SQLException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Builds the key subquery
     *
     * @param keys The set with keys
     * @return The subquery
     */
    private String buildKeySubQuery(Set<?> keys) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            b.append("?,");
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }
}
