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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.storage.sqlStorage.OXResellerSQLStorage;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.impl.IDGenerator;

/**
 * @author choeger
 *
 */
public final class OXResellerMySQLStorage extends OXResellerSQLStorage {

    private static AdminCache cache = null;

    private static final Log log = LogFactory.getLog(OXResellerMySQLStorage.class);

    static {
        cache = ClientAdminThreadExtended.cache;
    }

    public OXResellerMySQLStorage() {
    }

    private void setIdOrName(final boolean hasId, final PreparedStatement prep, final ResellerAdmin adm, final int idx) throws SQLException {
        if( hasId ) {
            prep.setInt(idx, adm.getId());
        } else {
            prep.setString(idx, adm.getName());
        }
    }
    
    @Override
    public void change(final ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        
        try {
            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            
            final String name = adm.getName();
            final String q1 = "UPDATE subadmin ";
            String q2 = null;
            boolean hasId = false;
            if( adm.getId() != null ) {
                q2 = " WHERE sid=?";
                hasId = true;
            } else if( name != null ) {
                q2 = " WHERE name=?";
            } else {
                throw new InvalidDataException("either ID or name must be specified");
            }
            if( name != null ) {
                prep = oxcon.prepareStatement(q1 + "SET name=?" + q2);
                prep.setString(1, name);
                setIdOrName(hasId, prep, adm, 2);
                prep.executeUpdate();
                prep.close();
            }

            final String displayName = adm.getDisplayname();
            if( displayName != null ) {
                prep = oxcon.prepareStatement(q1 + "SET displayName=?" + q2);
                prep.setString(1, displayName);
                setIdOrName(hasId, prep, adm, 2);
                prep.executeUpdate();
                prep.close();
            }
            
            final Integer parentId = adm.getParentId();
            if( parentId != null ) {
                prep = oxcon.prepareStatement(q1 + "SET pid=?" + q2);
                prep.setInt(1, parentId);
                setIdOrName(hasId, prep, adm, 2);
                prep.executeUpdate();
                prep.close();
            }

            final String password = adm.getPassword();
            if( password != null ) {
                prep = oxcon.prepareStatement(q1 + "SET password=?, passwordMech=?" + q2);
                prep.setString(1, cache.encryptPassword(adm));
                prep.setString(2, adm.getPasswordMech());
                setIdOrName(hasId, prep, adm, 3);
                prep.executeUpdate();
                prep.close();
            }
            
            oxcon.commit();
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            doRollback(oxcon);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(oxcon,prep);
        }
    }

    @Override
    public ResellerAdmin create(final ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        
        try {
            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            
            final int adm_id = IDGenerator.getId(oxcon);
            
            prep = oxcon.prepareStatement("INSERT INTO subadmin (sid,pid,name,displayName,password,passwordMech) VALUES (?,?,?,?,?,?)");
            prep.setInt(1, adm_id);
            prep.setInt(2,adm.getParentId());
            prep.setString(3,adm.getName());
            prep.setString(4,adm.getDisplayname());
            prep.setString(5,cache.encryptPassword(adm));
            prep.setString(6,adm.getPasswordMech());
            
            prep.executeUpdate();
            
            oxcon.commit();
            
            adm.setId(adm_id);
            return adm;
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            doRollback(oxcon);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(oxcon,prep);
        }
    }

    @Override
    public void delete(final ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        
        try {
            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            
            String query = "DELETE FROM subadmin WHERE ";
            boolean hasId = false;
            if( adm.getId() != null ) {
                query += "sid=?";
                hasId = true;
            } else if( adm.getName() != null ) {
                query += "name=?";
            } else {
                throw new InvalidDataException("either ID or name must be specified");
            }
            
            prep = oxcon.prepareStatement(query);
            if( hasId ) {
                prep.setInt(1, adm.getId());
            } else {
                prep.setString(1, adm.getName());
            }
            prep.executeUpdate();
            
            oxcon.commit();
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            doRollback(oxcon);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(oxcon,prep);
        }
    }

    @Override
    public ResellerAdmin[] list(final String search_pattern) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResellerAdmin[] getData(final ResellerAdmin[] admins) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            final ArrayList<ResellerAdmin> ret = new ArrayList<ResellerAdmin>();
            con = cache.getConnectionForConfigDB();
            for(final ResellerAdmin adm : admins ) {
                final ResellerAdmin newadm = (ResellerAdmin) adm.clone();
                String query = "SELECT * FROM subadmin WHERE ";
                boolean hasId = false;
                if( adm.getId() != null ) {
                    query += "sid=?";
                    hasId = true;
                } else if( adm.getName() != null ) {
                    query += "name=?";
                } else {
                    throw new InvalidDataException("either ID or name must be specified");
                }
                prep = con.prepareStatement(query);
                if( hasId ) {
                    prep.setInt(1, adm.getId());
                } else {
                    prep.setString(1, adm.getName());
                }
                rs = prep.executeQuery();
                if( ! rs.next() ) {
                    throw new StorageException("unable to get data of reseller admin: " +
                            (hasId ? "id=" + adm.getId() : "name=" + adm.getName()));
                }
                newadm.setName(rs.getString("name"));
                newadm.setId(rs.getInt("sid"));
                newadm.setParentId(rs.getInt("pid"));
                newadm.setDisplayname(rs.getString("displayName"));
                newadm.setPassword(rs.getString("password"));
                newadm.setPasswordMech(rs.getString("passwordMech"));
                ret.add(newadm);
            }
            return ret.toArray(new ResellerAdmin[ret.size()]);
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(con, prep, rs);
        }
    }

    @Override
    public boolean existsAdmin(ResellerAdmin adm) throws StorageException {
        return existsAdmin(new ResellerAdmin[]{adm});
    }

    @Override
    public boolean existsAdmin(ResellerAdmin[] admins) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        
        final Credentials mastercreds = cache.getMasterCredentials();
        try {
            con = cache.getConnectionForConfigDB();
            for(final ResellerAdmin adm : admins ) {
                final String name = adm.getName();
                // cannot create radm with same name like master admin
                if( name != null && mastercreds.getLogin().equals(name) ) {
                    return true;
                }
                String query = "SELECT sid FROM subadmin WHERE ";
                boolean hasId = false;
                if( adm.getId() != null ) {
                    query += "sid=?";
                    hasId = true;
                } else if( name != null ) {
                    query += "name=?";
                } else {
                    throw new InvalidDataException("either ID or name must be specified");
                }
                prep = con.prepareStatement(query);
                if( hasId ) {
                    prep.setInt(1, adm.getId());
                } else {
                    prep.setString(1, name);
                }
                rs = prep.executeQuery();
                if(! rs.next() ) {
                    return false;
                } else {
                    rs.close();
                    prep.close();
                }
            }
            return true;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(con, prep, rs);
        }
    }

    @Override
    public void ownContextToAdmin(final Context ctx, final Credentials creds) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        try {
            ResellerAdmin adm = getData(new ResellerAdmin[]{new ResellerAdmin(creds.getLogin(),creds.getPassword())})[0];
            if( ctx.getId() == null ) {
                throw new InvalidDataException("ContextID must not be null");
            }
            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            prep = oxcon.prepareStatement("INSERT INTO context2subadmin (sid,cid) VALUES(?,?)");
            prep.setInt(1, adm.getId());
            prep.setInt(2, ctx.getId());
            prep.executeUpdate();
            
            oxcon.commit();
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            doRollback(oxcon);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(oxcon,prep);
        }
    }

    @Override
    public void unownContextFromAdmin(final Context ctx, final Credentials creds) throws StorageException {
        try {
            ResellerAdmin adm = getData(new ResellerAdmin[]{new ResellerAdmin(creds.getLogin(),creds.getPassword())})[0];
            unownContextFromAdmin(ctx, adm);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void unownContextFromAdmin(final Context ctx, final ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        try {
            if( adm.getId() == null ) {
                throw new InvalidDataException("ResellerAdminID must not be null");
            }
            if( ctx.getId() == null ) {
                throw new InvalidDataException("ContextID must not be null");
            }
            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            prep = oxcon.prepareStatement("DELETE FROM context2subadmin WHERE sid=? AND cid=?");
            prep.setInt(1, adm.getId());
            prep.setInt(2, ctx.getId());
            prep.executeUpdate();
            
            oxcon.commit();
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            doRollback(oxcon);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            doRollback(oxcon);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(oxcon,prep);
        }
    }

    @Override
    public ResellerAdmin getContextOwner(final Context ctx) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            if( ctx.getId() == null ) {
                throw new InvalidDataException("ContextID must not be null");
            }
            oxcon = cache.getConnectionForConfigDB();
            prep = oxcon.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId());
            rs = prep.executeQuery();
            if( ! rs.next() ) {
                return null;
            }
            return getData(new ResellerAdmin[]{new ResellerAdmin(rs.getInt("sid"))})[0];
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(oxcon,prep,rs);
        }
    }

    @Override
    public boolean ownsContext(final Context ctx, final Credentials creds) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            ResellerAdmin adm = getData(new ResellerAdmin[]{new ResellerAdmin(creds.getLogin(),creds.getPassword())})[0];
            if( ctx.getId() == null ) {
                throw new InvalidDataException("ContextID must not be null");
            }
            oxcon = cache.getConnectionForConfigDB();
            prep = oxcon.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId());
            rs = prep.executeQuery();
            if( ! rs.next() ) {
                return false;
            }
            if( rs.getInt("sid") != adm.getId() ){
                return false;
            }
            return true;
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeSqlStuff(oxcon,prep,rs);
        }
    }

    private void doRollback(final Connection con) {
        try {
            con.rollback();
        } catch (final SQLException e2) {
            log.error("Error doing rollback", e2);
        }
    }

}
