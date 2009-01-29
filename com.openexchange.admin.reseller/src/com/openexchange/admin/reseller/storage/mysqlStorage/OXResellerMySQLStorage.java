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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException.Code;
import com.openexchange.admin.reseller.storage.sqlStorage.OXResellerSQLStorage;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
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

    private static final String DATABASE_COLUMN_VALUE = "value";

    private static final String DATABASE_COLUMN_NAME = "name";

    private static final String DATABASE_COLUMN_ID = "rid";

    private static AdminCache cache = null;

    private static final Log log = LogFactory.getLog(OXResellerMySQLStorage.class);

    static {
        cache = ClientAdminThreadExtended.cache;
    }

    public OXResellerMySQLStorage() {
    }

//    private boolean hasRestriction(final Connection con, final int sid, final int rid) throws StorageException {
//        PreparedStatement prep = null;
//        ResultSet rs = null;
//        try {
//            prep = con.prepareStatement("SELECT rid FROM subadmin_restrictions WHERE sid=? AND rid=?");
//            prep.setInt(1, sid);
//            prep.setInt(2, rid);
//            rs = prep.executeQuery();
//            if( rs.next() ) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (SQLException e) {
//            log.error(e.getMessage(), e);
//            throw new StorageException(e.getMessage());
//        } finally {
//            cache.closeSqlStuff(null, prep, rs);
//        }
//    }
    
    @Override
    public void change(final ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        
        try {
            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            
            final String name = adm.getName();
            int sid = 0;
            if( adm.getId() != null ) {
                sid = adm.getId();
            } else if( name != null ) {
                prep = oxcon.prepareStatement("SELECT sid FROM subadmin WHERE name=?");
                prep.setString(1, name);
                rs = prep.executeQuery();
                if( !rs.next() ) {
                    throw new StorageException("unable to get id for " + name);
                }
                sid = rs.getInt("sid");
                prep.close();
            } else {
                throw new InvalidDataException("either ID or name must be specified");
            }
            if( name != null ) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET name=? WHERE sid=?");
                prep.setString(1, name);
                prep.setInt(2, sid);
                prep.executeUpdate();
                prep.close();
            }

            final String displayName = adm.getDisplayname();
            if( displayName != null ) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET displayName=? WHERE sid=?");
                prep.setString(1, displayName);
                prep.setInt(2, sid);
                prep.executeUpdate();
                prep.close();
            }
            
            final Integer parentId = adm.getParentId();
            if( parentId != null ) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET pid=? WHERE sid=?");
                prep.setInt(1, parentId);
                prep.setInt(2, sid);
                prep.executeUpdate();
                prep.close();
            }

            final String password = adm.getPassword();
            if( password != null ) {
                prep = oxcon.prepareStatement("UPDATE subadmin SET password=?, passwordMech=? WHERE sid=?");
                prep.setString(1, cache.encryptPassword(adm));
                prep.setString(2, adm.getPasswordMech());
                prep.setInt(3, sid);
                prep.executeUpdate();
                prep.close();
            }
            
            HashSet<Restriction> res = adm.getRestrictions();
            if( res != null ) {
                prep = oxcon.prepareStatement("DELETE FROM subadmin_restrictions WHERE sid=?");
                prep.setInt(1, sid);
                prep.executeUpdate();
                prep.close();
                for(final Restriction r : res) {
                    int rid = r.getId();
                    prep = oxcon.prepareStatement("INSERT INTO subadmin_restrictions (sid,rid,value) VALUES(?,?,?)");
                    prep.setInt(1, sid);
                    prep.setInt(2, rid);
                    prep.setString(3, r.getValue());
                    prep.executeUpdate();
                    prep.close();
                }
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
            cache.closeConfigDBSqlStuff(oxcon,prep,rs);
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
            prep.close();
            
            final HashSet<Restriction> res = adm.getRestrictions();
            if( res != null ) {
                Iterator<Restriction> i = res.iterator();
                while( i.hasNext() ) {
                    Restriction r = i.next();
                    prep = oxcon.prepareStatement("INSERT INTO subadmin_restrictions (sid,rid,value) VALUES (?,?,?)");
                    prep.setInt(1,adm_id);
                    prep.setInt(2,r.getId());
                    prep.setString(3, r.getValue());
                    prep.executeUpdate();
                    prep.close();
                }
            }
            
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
            cache.closeConfigDBSqlStuff(oxcon,prep);
        }
    }

    @Override
    public void delete(final ResellerAdmin adm) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        
        try {

            ResellerAdmin tmp = getData(new ResellerAdmin[]{adm})[0];

            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);
            
            prep = oxcon.prepareStatement("DELETE FROM subadmin_restrictions WHERE sid=?");
            prep.setInt(1, tmp.getId());
            prep.executeUpdate();
            prep.close();
            
            prep = oxcon.prepareStatement("DELETE FROM subadmin WHERE sid=?");
            prep.setInt(1, tmp.getId());
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
        } finally {
            cache.closeConfigDBSqlStuff(oxcon,prep);
        }
    }

    @Override
    public ResellerAdmin[] list(final String search_pattern) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForConfigDB();
            final String search_patterntmp = search_pattern.replace('*', '%');
            prep = con.prepareStatement("SELECT * FROM subadmin WHERE sid LIKE ? OR name LIKE ?");
            prep.setString(1, search_patterntmp);
            prep.setString(2, search_patterntmp);
            rs = prep.executeQuery();

            final ArrayList<ResellerAdmin> ret = new ArrayList<ResellerAdmin>();
            while( rs.next() ) {
                ResellerAdmin adm = new ResellerAdmin();
                adm.setId(rs.getInt("sid"));
                adm.setName(rs.getString(DATABASE_COLUMN_NAME));
                adm.setDisplayname(rs.getString("displayName"));
                adm.setPassword(rs.getString("password"));
                adm.setPasswordMech(rs.getString("passwordMech"));
                adm.setParentId(rs.getInt("pid"));
                ret.add(adm);
            }
            return ret.toArray(new ResellerAdmin[ret.size()]);
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeConfigDBSqlStuff(con, prep, rs);
        }
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
                newadm.setName(rs.getString(DATABASE_COLUMN_NAME));
                newadm.setId(rs.getInt("sid"));
                newadm.setParentId(rs.getInt("pid"));
                newadm.setDisplayname(rs.getString("displayName"));
                newadm.setPassword(rs.getString("password"));
                newadm.setPasswordMech(rs.getString("passwordMech"));

                rs.close();
                prep.close();

                prep = con.prepareStatement("SELECT subadmin_restrictions.rid,sid,name,value FROM subadmin_restrictions INNER JOIN restrictions ON subadmin_restrictions.rid=restrictions.rid WHERE sid=?");
                prep.setInt(1, newadm.getId());
                rs = prep.executeQuery();
                
                HashSet<Restriction> res = new HashSet<Restriction>();
                while( rs.next() ) {
                    Restriction r = new Restriction();
                    r.setId(rs.getInt(DATABASE_COLUMN_ID));
                    r.setName(rs.getString(DATABASE_COLUMN_NAME));
                    r.setValue(rs.getString(DATABASE_COLUMN_VALUE));
                    res.add(r);
                }
                if(res.size() > 0) {
                    newadm.setRestrictions(res);
                }
                ret.add(newadm);
                rs.close();
                prep.close();
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
            cache.closeConfigDBSqlStuff(con, prep, rs);
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
            cache.closeConfigDBSqlStuff(con, prep, rs);
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
            cache.closeConfigDBSqlStuff(oxcon,prep);
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
            cache.closeConfigDBSqlStuff(oxcon,prep);
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
            cache.closeConfigDBSqlStuff(oxcon,prep,rs);
        }
    }

    @Override
    public boolean ownsContext(final Context ctx, final int admid) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            oxcon = cache.getConnectionForConfigDB();
            if( ctx == null ) {
                prep = oxcon.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
                prep.setInt(1, admid);
                rs = prep.executeQuery();
                if( ! rs.next() ) {
                    return false;
                }
                return true;
            } else {
                prep = oxcon.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
                prep.setInt(1, ctx.getId());
                rs = prep.executeQuery();
                if( ! rs.next() ) {
                    return false;
                }
                if( rs.getInt("sid") != admid ){
                    return false;
                }
                return true;
            }
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
        } finally {
            cache.closeConfigDBSqlStuff(oxcon,prep,rs);
        }
    }

    @Override
    public boolean ownsContext(final Context ctx, final Credentials creds) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            ResellerAdmin adm = getData(new ResellerAdmin[]{new ResellerAdmin(creds.getLogin(),creds.getPassword())})[0];
            oxcon = cache.getConnectionForConfigDB();
            if( ctx == null ) {
                prep = oxcon.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
                prep.setInt(1, adm.getId());
                rs = prep.executeQuery();
                if( ! rs.next() ) {
                    return false;
                }
                return true;
            } else {
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
            }
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
        } finally {
            cache.closeConfigDBSqlStuff(oxcon,prep,rs);
        }
    }

    @Override
    public Map<String, Restriction> listRestrictions(final String search_pattern) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForConfigDB();
            final String search_patterntmp = search_pattern.replace('*', '%');
            prep = con.prepareStatement("SELECT * FROM restrictions WHERE rid LIKE ? OR name LIKE ?");
            prep.setString(1, search_patterntmp);
            prep.setString(2, search_patterntmp);
            rs = prep.executeQuery();

            final Map<String, Restriction> ret = new HashMap<String, Restriction>();
            while (rs.next()) {
                final String name = rs.getString(DATABASE_COLUMN_NAME);
                ret.put(name, new Restriction(Integer.valueOf(rs.getInt(DATABASE_COLUMN_ID)), rs.getString(DATABASE_COLUMN_NAME)));
            }
            return ret;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeConfigDBSqlStuff(con, prep, rs);
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
    private void checkMaxContextRestriction(final Connection con, final ResellerAdmin adm, final int maxvalue) throws StorageException, OXResellerException, SQLException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT COUNT(cid) FROM context2subadmin WHERE sid=?");
            prep.setInt(1, adm.getId());
            rs = prep.executeQuery();
            if( ! rs.next() ) {
                throw new StorageException("unable to count the number of context belonging to " + adm.getName());
            }
            if( rs.getInt("COUNT(cid)") >= maxvalue ) {
                throw new OXResellerException(Code.MAXIMUM_NUMBER_CONTEXT_REACHED, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cache.closeConfigDBSqlStuff(null, prep, rs);
        }
    }
    
    /**
     * Check whether maxvalue of {@link Restriction.MAX_CONTEXT_QUOTA} has been reached
     * 
     * @param con
     * @param adm
     * @param maxvalue
     * @throws StorageException
     * @throws OXResellerException
     * @throws SQLException 
     * @throws PoolException 
     */
    private void checkMaxContextQuotaRestriction(final Connection con, final ResellerAdmin adm, final long maxvalue) throws StorageException, OXResellerException, SQLException, PoolException {
        PreparedStatement prep = null;
        PreparedStatement prep2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        Connection oxcon = null;
        int cid = -1;
        try {
            prep = con.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
            prep.setInt(1, adm.getId());
            rs = prep.executeQuery();
            long qused = 0;
            while( rs.next() ) {
                cid = rs.getInt("cid");
                oxcon = cache.getConnectionForContext(cid);
                prep2 = oxcon.prepareStatement("SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ?");
                prep2.setInt(1, cid);
                rs2 = prep2.executeQuery();
                if( rs2.next() ) {
                    qused += rs2.getLong(1);
                }
                prep2.close();
                rs2.close();
                cache.pushConnectionForContext(cid, oxcon);
                // set to null to prevent double pushback in finally
                oxcon = null;
            }
            if( (qused /= Math.pow(2, 20)) >= maxvalue ) {
                throw new OXResellerException(Code.MAXIMUM_OVERALL_CONTEXT_QUOTA, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cache.closeContextSqlStuff(oxcon, cid);
            cache.closeConfigDBSqlStuff(null, prep, rs);
            cache.closeConfigDBSqlStuff(null, prep2, rs2);
        }
    }

    private void checkMaxOverallUserRestriction(final Connection con, final ResellerAdmin adm, final int maxvalue, final boolean contextMode) throws StorageException, OXResellerException, SQLException, PoolException {
        PreparedStatement prep = null;
        PreparedStatement prep2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        Connection oxcon = null;
        int cid = -1;
        try {
            prep = con.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
            prep.setInt(1, adm.getId());
            rs = prep.executeQuery();
            // start count at one for the current context to be created when called from Context Plugin
            // methods, because the context to be created is not yet listed in context2subadmin table
            int count = contextMode ? 1 : 0;
            while( rs.next() ) {
                cid = rs.getInt("cid");
                oxcon = cache.getConnectionForContext(cid);
                prep2 = oxcon.prepareStatement("SELECT COUNT(cid) FROM user WHERE cid=?");
                prep2.setInt(1, cid);
                rs2 = prep2.executeQuery();
                if( rs2.next() ) {
                    count += rs2.getInt(1);
                }
                prep2.close();
                rs2.close();
                cache.pushConnectionForContext(cid, oxcon);
                // set to null to prevent double pushback in finally
                oxcon = null;
            }
            if( count > maxvalue ) {
                throw new OXResellerException(Code.MAXIMUM_OVERALL_NUMBER_OF_CONTEXT_REACHED, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cache.closeContextSqlStuff(oxcon, cid);
            cache.closeConfigDBSqlStuff(null, prep, rs);
            cache.closeConfigDBSqlStuff(null, prep2, rs2);
        }
    }

    private void checkMaxOverallUserRestrictionByModuleAccess(final Connection con, final ResellerAdmin adm, final String restrictionvalue, final int maxvalue, final boolean contextMode) throws StorageException, OXResellerException, SQLException, PoolException {
        PreparedStatement prep = null;
        PreparedStatement prep2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        Connection oxcon = null;
        int cid = -1;
        try {
            prep = con.prepareStatement("SELECT cid FROM context2subadmin WHERE sid=?");
            prep.setInt(1, adm.getId());
            rs = prep.executeQuery();
            // start count at one for the current context to be created when called from Context Plugin
            // methods, because the context to be created is not yet listed in context2subadmin table
            int count = contextMode ? 1 : 0;
            while( rs.next() ) {
                cid = rs.getInt("cid");
                oxcon = cache.getConnectionForContext(cid);
                prep2 = oxcon.prepareStatement("SELECT COUNT(cid) FROM user WHERE cid=?");
                prep2.setInt(1, cid);
                rs2 = prep2.executeQuery();
                if( rs2.next() ) {
                    count += rs2.getInt(1);
                }
                prep2.close();
                rs2.close();
                cache.pushConnectionForContext(cid, oxcon);
                // set to null to prevent double pushback in finally
                oxcon = null;
            }
            if( count > maxvalue ) {
                throw new OXResellerException(Code.MAXIMUM_OVERALL_NUMBER_OF_USERS_REACHED, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cache.closeContextSqlStuff(oxcon, cid);
            cache.closeConfigDBSqlStuff(null, prep, rs);
            cache.closeConfigDBSqlStuff(null, prep2, rs2);
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
    private void checkMaxUserRestriction(final Context ctx, final int maxvalue) throws StorageException, OXResellerException, SQLException, PoolException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        Connection oxcon = null;
        int cid = -1;
        try {
            cid = ctx.getId();
            oxcon = cache.getConnectionForContext(cid);
            prep = oxcon.prepareStatement("SELECT COUNT(cid) FROM user WHERE cid=?");
            prep.setInt(1, cid);
            rs = prep.executeQuery();
            if( ! rs.next() ) {
                throw new StorageException("unable to count the number of users belonging to " + ctx.getName());
            }
            if( rs.getInt(1) >= maxvalue ) {
                throw new OXResellerException(Code.MAXIMUM_NUMBER_OF_USERS_PER_CONTEXT_REACHED, String.valueOf(maxvalue));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cache.closeConfigDBSqlStuff(null, prep, rs);
            cache.closeContextSqlStuff(oxcon, cid);
            cache.pushConnectionForContext(cid, oxcon);
            // set to null to prevent double pushback in finally
            oxcon = null;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface#checkRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public void checkPerSubadminRestrictions(final Credentials creds, final String... restriction_types) throws StorageException {
        ResellerAdmin adm = getData(new ResellerAdmin[]{new ResellerAdmin(creds.getLogin(),creds.getPassword())})[0];
        HashSet<Restriction> restrictions = adm.getRestrictions();
        if( restrictions != null && restrictions.size() > 0 ) {
            Connection con = null;
            try {
                con = cache.getConnectionForConfigDB();
                for(final Restriction res : restrictions) {
                    for(final String tocheck : restriction_types) {
                        if( res.getName().equals(tocheck) && tocheck.equals(Restriction.MAX_CONTEXT_PER_SUBADMIN)) {
                            checkMaxContextRestriction(con, adm, Integer.parseInt(res.getValue()));
                        } else if( res.getName().equals(tocheck) && tocheck.equals(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN)) {
                            checkMaxContextQuotaRestriction(con, adm, Long.parseLong(res.getValue()));
                        } else if( res.getName().equals(tocheck) && tocheck.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN)) {
                            checkMaxOverallUserRestriction(con, adm, Integer.parseInt(res.getValue()), true);
                        } else if( res.getName().startsWith(tocheck) && tocheck.startsWith(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX)) {
                            checkMaxOverallUserRestrictionByModuleAccess(con, adm, res.getName(), Integer.parseInt(res.getValue()), true);
                        }
                    }
                }
            } catch (RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new StorageException(e.getMessage());
            } catch (PoolException e) {
                log.error(e.getMessage(), e);
                throw new StorageException(e.getMessage());
            } catch (OXResellerException e) {
                log.error(e.getMessage(), e);
                throw new StorageException(e.getMessage());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new StorageException(e.getMessage());
            } finally {
                cache.closeConfigDBSqlStuff(con, null);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface#checkPerContextRestrictions(com.openexchange.admin.rmi.dataobjects.Context, java.lang.String[])
     */
    @Override
    public void checkPerContextRestrictions(Context ctx, String... restriction_types) throws StorageException {
        Connection con = null;

        try {
            for(final String tocheck : restriction_types) {
                if( tocheck.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN)) {
                    final ResellerAdmin adm = getResellerAdminForContext(ctx);
                    HashSet<Restriction> restrictions = adm.getRestrictions();
                    if( restrictions != null && restrictions.size() > 0 ) {
                        for(final Restriction res : restrictions) {
                            if( res.getName().equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN) ) {
                                con = cache.getConnectionForConfigDB();
                                checkMaxOverallUserRestriction(con, adm, Integer.parseInt(res.getValue()), false);
                            }
                        }
                    }
                } else if( tocheck.equals(Restriction.MAX_USER_PER_CONTEXT)) {
                    HashSet<Restriction> restrictions = getRestrictionsFromContext(ctx);
                    if( restrictions != null && restrictions.size() > 0 ) {
                        for(final Restriction res : restrictions) {
                            if( res.getName().equals(Restriction.MAX_USER_PER_CONTEXT) ) {
                                checkMaxUserRestriction(ctx, Integer.parseInt(res.getValue()));
                            }
                        }
                    }
                }
            }
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (OXResellerException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeConfigDBSqlStuff(con, null);
        }
        
    }

    /**
     * @param con
     * @param ctx
     * @return
     * @throws SQLException
     * @throws StorageException
     * @throws PoolException 
     */
    private final ResellerAdmin getResellerAdminForContext(final Context ctx) throws SQLException, StorageException, PoolException {
        PreparedStatement prep = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = cache.getConnectionForConfigDB();
            prep = con.prepareStatement("SELECT sid FROM context2subadmin WHERE cid=?");
            prep.setInt(1, ctx.getId());
            rs = prep.executeQuery();
            if( ! rs.next() ) {
                throw new StorageException("unable to determine owner of Context " + ctx.getId());
            }
            return getData(new ResellerAdmin[]{new ResellerAdmin(rs.getInt(1))})[0];
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cache.closeConfigDBSqlStuff(con, prep, rs);
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface#applyRestrictionsToContext(java.util.HashSet, com.openexchange.admin.rmi.dataobjects.Context)
     */
    @Override
    public void applyRestrictionsToContext(HashSet<Restriction> restrictions, Context ctx) throws StorageException {
        Connection oxcon = null;
        PreparedStatement prep = null;
        
        try {
            oxcon = cache.getConnectionForConfigDB();
            oxcon.setAutoCommit(false);

            final int cid = ctx.getId();
            prep = oxcon.prepareStatement("DELETE FROM context_restrictions WHERE cid=?");
            prep.setInt(1, cid);
            prep.executeUpdate();
            prep.close();
            if( restrictions != null ) {
                final Iterator<Restriction> i = restrictions.iterator();
                while( i.hasNext() ) {
                    final Restriction r = i.next();
                    prep = oxcon.prepareStatement("INSERT INTO context_restrictions (cid,rid,value) VALUES (?,?,?)");
                    prep.setInt(1, cid);
                    prep.setInt(2, r.getId());
                    prep.setString(3, r.getValue());
                    prep.executeUpdate();
                    prep.close();
                }
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
        } finally {
            cache.closeConfigDBSqlStuff(oxcon,prep);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface#getRestrictionsFromContext(com.openexchange.admin.rmi.dataobjects.Context)
     */
    @Override
    public HashSet<Restriction> getRestrictionsFromContext(Context ctx) throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            con = cache.getConnectionForConfigDB();
            prep = con.prepareStatement("SELECT context_restrictions.rid,cid,name,value FROM context_restrictions INNER JOIN restrictions ON context_restrictions.rid=restrictions.rid WHERE cid=?");
            prep.setInt(1, ctx.getId());
            rs = prep.executeQuery();

            HashSet<Restriction> res = new HashSet<Restriction>();
            while( rs.next() ) {
                res.add(new Restriction(rs.getInt(DATABASE_COLUMN_ID), rs.getString(DATABASE_COLUMN_NAME), rs.getString(DATABASE_COLUMN_VALUE)));
            }
            return res.size() > 0 ? res : null;
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeConfigDBSqlStuff(con, prep, rs);
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface#initDatabaseRestrictions()
     */
    @Override
    public void initDatabaseRestrictions() throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        try {
            con = cache.getConnectionForConfigDB();
            con.setAutoCommit(false);
            for(final String res : new String[]{Restriction.MAX_CONTEXT_PER_SUBADMIN,
                    Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN,
                    Restriction.MAX_OVERALL_USER_PER_SUBADMIN,
                    Restriction.MAX_USER_PER_CONTEXT} ) {
                final int rid = IDGenerator.getId(con);
                prep = con.prepareStatement("INSERT INTO restrictions (rid,name) VALUES (?,?)");
                prep.setInt(1, rid);
                prep.setString(2, res);
                prep.executeUpdate();
            }
            cache.initAccessCombinations();
            for(final Entry<String, UserModuleAccess> mentry : cache.getAccessCombinationNames()) {
                final String mname = mentry.getKey();
                for(final String prefix : new String[]{
                        Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX,
                        Restriction.MAX_OVERALL_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX } ) {
                final int rid = IDGenerator.getId(con);
                prep = con.prepareStatement("INSERT INTO restrictions (rid,name) VALUES (?,?)");
                prep.setInt(1, rid);
                prep.setString(2, prefix+mname);
                prep.executeUpdate();
                }
            }
            con.commit();
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            doRollback(con);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            doRollback(con);
            throw new StorageException(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            doRollback(con);
            throw new StorageException(e.getMessage());
        } catch (OXGenericException e) {
            log.error(e.getMessage(), e);
            doRollback(con);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeConfigDBSqlStuff(con, prep);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface#removeDatabaseRestrictions()
     */
    @Override
    public void removeDatabaseRestrictions() throws StorageException {
        Connection con = null;
        PreparedStatement prep = null;
        try {
            con = cache.getConnectionForConfigDB();
            con.setAutoCommit(false);
            prep = con.prepareStatement("DELETE FROM restrictions");
            prep.executeUpdate();
            con.commit();
        } catch (PoolException e) {
            log.error(e.getMessage(), e);
            doRollback(con);
            throw new StorageException(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            doRollback(con);
            throw new StorageException(e.getMessage());
        } finally {
            cache.closeConfigDBSqlStuff(con, prep);
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface#updateModuleAccessRestrictions()
     */
    @Override
    public void updateModuleAccessRestrictions() throws StorageException {
        //TODO: to be implemented
    }

    private void doRollback(final Connection con) {
        try {
            con.rollback();
        } catch (final SQLException e2) {
            log.error("Error doing rollback", e2);
        }
    }

}
