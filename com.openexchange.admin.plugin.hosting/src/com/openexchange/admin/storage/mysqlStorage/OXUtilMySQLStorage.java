
package com.openexchange.admin.storage.mysqlStorage;

import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.storage.sqlStorage.OXUtilSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.IDGenerator;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author d7
 * @author cutmasta
 * 
 */
public class OXUtilMySQLStorage extends OXUtilSQLStorage {

    // SQL STATEMENTS GO HERE #################
    // #########################################

    private final static Log log = LogFactory.getLog(OXUtilMySQLStorage.class);

    public OXUtilMySQLStorage() {
    }

    @Override
    public int createMaintenanceReason(final MaintenanceReason reason)
            throws StorageException {

        Connection con = null;
        PreparedStatement stmt = null;

        try {

            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit(false);
            final int res_id = IDGenerator.getId(con);
            con.commit();
            stmt = con.prepareStatement("INSERT INTO reason_text (id,text) VALUES(?,?)");
            stmt.setInt(1, res_id);
            stmt.setString(2, reason.getText());
            stmt.executeUpdate();
            con.commit();

            return res_id;
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);   
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }        
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pexp) {
            log.error("Pool error", pexp);
            throw new StorageException(pexp);
        } catch (final SQLException ecp) {
            log.error("Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }

            try {
                if (con != null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    public void changeDatabase(final Database db) throws StorageException {

        Connection con = null;
        PreparedStatement prep = null;
        
        try {

            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit(false);
            
            if(db.getDisplayname()!=null && db.getDisplayname().length()>0){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.name = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setString(1, db.getDisplayname());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getLogin()!=null && db.getLogin().length()>0){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.login = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setString(1, db.getLogin());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getPassword()!=null && db.getPassword().length()>0){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.password = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setString(1, db.getPassword());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getDriver()!=null && db.getDriver().length()>0){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.driver = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setString(1, db.getDriver());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getPoolInitial()!=null){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.initial = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setInt(1, db.getPoolInitial());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getPoolMax()!=null){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.max = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setInt(1, db.getPoolMax());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getPoolHardLimit()!=null){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.hardlimit = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setInt(1, db.getPoolHardLimit());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getUrl()!=null && db.getUrl().length()>0){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_pool.url = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setString(1, db.getUrl());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getClusterWeight()!=null){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_cluster.weight = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setInt(1, db.getClusterWeight());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            if(db.getMaxUnits()!=null){
                prep = con.prepareStatement("UPDATE db_pool,db_cluster SET db_cluster.max_units = ? WHERE db_pool.db_pool_id = ? AND (db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)");
                prep.setInt(1, db.getMaxUnits());
                prep.setInt(2, db.getId());
                prep.setInt(3, db.getId());
                prep.setInt(4, db.getId());
                prep.executeUpdate();
                prep.close();
            }
            
            con.commit();
            prep.close();
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);  
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException sqle) {
            log.error("SQL Error", sqle);
            throw new StorageException(sqle);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException ee) {
                log.error("Error closing statement", ee);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException e) {
                log.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    @Override
    public void changeFilestore(final Filestore fstore) throws StorageException {

        Connection configdb_write_con = null;
        PreparedStatement prep = null;

        try {

            configdb_write_con = cache.getWRITEConnectionForCONFIGDB();
            configdb_write_con.setAutoCommit(false);

            prep = configdb_write_con.prepareStatement("UPDATE filestore SET uri = ? WHERE id = ?");
            prep.setString(1, fstore.getUrl());
            prep.setInt(2, fstore.getId());
            prep.executeUpdate();
            prep.close();

            if (fstore.getSize() != -1) {

                final Long l_max = new Long("8796093022208");
                long store_size = fstore.getSize();
                if (store_size > l_max.longValue()) {
                    throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
                }
                store_size *= Math.pow(2, 20);
                prep = configdb_write_con.prepareStatement("UPDATE filestore SET size = ? WHERE id = ?");
                prep.setLong(1, store_size);
                prep.setInt(2, fstore.getId());
                prep.executeUpdate();
                prep.close();
            }

            prep = configdb_write_con.prepareStatement("UPDATE filestore SET max_context = ? WHERE id = ?");
            prep.setInt(1, fstore.getMaxContexts());
            prep.setInt(2, fstore.getId());
            prep.executeUpdate();
            prep.close();

            configdb_write_con.commit();
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);         
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                log.error("Error processing rollback of configdb connection!", expd);
            }
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException exp) {
            log.error("SQL Error", exp);
            try {
                if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                    configdb_write_con.rollback();
                }
            } catch (final SQLException expd) {
                log.error("Error processing rollback of configdb connection!", expd);
            }
            throw new StorageException(exp);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (configdb_write_con != null) {
                    cache.pushConfigDBWrite(configdb_write_con);
                }
            } catch (final PoolException ecp) {
                log.error("Error pushing configdb connection to pool!", ecp);
            }
        }
    }

    @Override
    public void createDatabase(final Database db) throws StorageException {
        final OXUtilMySQLStorageCommon oxutilcommon = new OXUtilMySQLStorageCommon();
        oxutilcommon.createDatabase(db);
    }

    @Override
    public void deleteDatabase(final Database db) throws StorageException {
        final OXUtilMySQLStorageCommon oxutilcommon = new OXUtilMySQLStorageCommon();
        oxutilcommon.deleteDatabase(db);
    }

    @Override
    public void deleteMaintenanceReason(final int[] reason_ids)
            throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit(false);
            for (int element : reason_ids) {
                stmt = con.prepareStatement("DELETE FROM reason_text WHERE id = ?");
                stmt.setInt(1, element);
                stmt.executeUpdate();
                stmt.close();
            }
            con.commit();
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Erroe closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    @Override
    public MaintenanceReason[] getAllMaintenanceReasons()
            throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getREADConnectionForCONFIGDB();

            stmt = con.prepareStatement("SELECT id,text FROM reason_text");
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<MaintenanceReason> list = new ArrayList<MaintenanceReason>();
            while (rs.next()) {
                list.add(new MaintenanceReason(rs.getInt("id"), rs.getString("text")));
            }
            rs.close();

            return list.toArray(new MaintenanceReason[list.size()]);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBRead(con);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }

        }
    }

    @Override
    public MaintenanceReason[] getMaintenanceReasons(final int[] reason_id)
            throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getREADConnectionForCONFIGDB();

            final StringBuilder sb = new StringBuilder();
            sb.append("SELECT id,text FROM reason_text WHERE id IN ");

            
            sb.append("(");
            for (int i = 0; i < reason_id.length; i++) {
                sb.append("?,");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")");

            stmt = con.prepareStatement(sb.toString());
            int stmt_count = 1;
            for (int element : reason_id) {
                stmt.setInt(stmt_count,element);
                stmt_count++;
            }
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<MaintenanceReason> list = new ArrayList<MaintenanceReason>();
            while (rs.next()) {
                list.add(new MaintenanceReason(rs.getInt("id"), rs.getString("text")));
            }
            rs.close();

            return list.toArray(new MaintenanceReason[list.size()]);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBRead(con);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }

        }
    }

    /**
     * @see com.openexchange.admin.storage.sqlStorage.OXUtilSQLStorage#listFilestores(java.lang.String)
     */
    @Override
    public Filestore[] listFilestores(final String search_pattern)
            throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        final String my_search_pattern = search_pattern.replace('*', '%');

        try {
            con = cache.getREADConnectionForCONFIGDB();

            stmt = con.prepareStatement("SELECT id,uri,size,max_context,COUNT(cid) FROM filestore LEFT JOIN context ON filestore.id = context.filestore_id WHERE uri LIKE ? GROUP BY filestore.id");

            stmt.setString(1, my_search_pattern);
            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Filestore> tmp = new ArrayList<Filestore>();

            while (rs.next()) {
                final Filestore fs = new Filestore();
                fs.setId(rs.getInt("id"));
                fs.setUrl(rs.getString("uri"));
                long size = rs.getLong("size");
                size /= Math.pow(2, 20);
                fs.setSize(size);
                fs.setMaxContexts(rs.getInt("max_context"));
                fs.setCurrentContexts(rs.getInt("COUNT(cid)"));
                tmp.add(fs);
            }

            return tmp.toArray(new Filestore[tmp.size()]);

        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBRead(con);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    @Override
    public int registerDatabase(final Database db) throws StorageException {

        Connection con = null;
        PreparedStatement prep = null;
        try {

            con = cache.getWRITEConnectionForCONFIGDB();

            con.setAutoCommit(false);
            final int db_id = IDGenerator.getId(con);
            con.commit();
            con.setAutoCommit(true);

            prep = con.prepareStatement("INSERT INTO db_pool VALUES (?,?,?,?,?,?,?,?,?);");
            prep.setInt(1, db_id);
            if (db.getUrl() != null) {
                prep.setString(2, db.getUrl());
            } else {
                prep.setNull(2, Types.VARCHAR);
            }
            if (db.getDriver() != null) {
                prep.setString(3, db.getDriver());
            } else {
                prep.setNull(3, Types.VARCHAR);
            }
            if (db.getLogin() != null) {
                prep.setString(4, db.getLogin());
            } else {
                prep.setNull(4, Types.VARCHAR);
            }
            if (db.getPassword() != null) {
                prep.setString(5, db.getPassword());
            } else {
                prep.setNull(5, Types.VARCHAR);
            }
            prep.setInt(6, db.getPoolHardLimit());
            prep.setInt(7, db.getPoolMax());
            prep.setInt(8, db.getPoolInitial());
            if (db.getDisplayname() != null) {
                prep.setString(9, db.getDisplayname());
            } else {
                prep.setNull(9, Types.VARCHAR);
            }

            prep.executeUpdate();
            prep.close();

            if (db.isMaster()) {

                con.setAutoCommit(false);
                final int c_id = IDGenerator.getId(con);
                con.commit();
                con.setAutoCommit(true);

                prep = con.prepareStatement("INSERT INTO db_cluster VALUES (?,?,?,?,?);");
                prep.setInt(1, c_id);

                // I am the master, set read_db_pool_id = 0
                prep.setInt(2, 0);
                prep.setInt(3, db_id);
                prep.setInt(4, db.getClusterWeight());
                prep.setInt(5, db.getMaxUnits());
                prep.executeUpdate();
                prep.close();

            } else {
                prep = con.prepareStatement("SELECT db_pool_id FROM db_pool WHERE db_pool_id = ?");
                prep.setInt(1, db.getMasterId());
                ResultSet rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("No such master with ID=" + db.getMasterId());
                }
                rs.close();
                prep.close();

                prep = con.prepareStatement("SELECT cluster_id FROM db_cluster WHERE write_db_pool_id = ?");
                prep.setInt(1, db.getMasterId());
                rs = prep.executeQuery();
                if (!rs.next()) {
                    throw new StorageException("No such master with ID=" + db.getMasterId() + " IN db_cluster TABLE");
                }
                final int cluster_id = rs.getInt("cluster_id");
                rs.close();
                prep.close();

                prep = con.prepareStatement("UPDATE db_cluster SET read_db_pool_id=? WHERE cluster_id=?;");

                prep.setInt(1, db_id);
                prep.setInt(2, cluster_id);
                prep.executeUpdate();
                prep.close();

            }

            return db_id;
        }catch (DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG,dt);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException ee) {
                log.error("Error closing statement", ee);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException e) {
                log.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    @Override
    public int registerFilestore(final Filestore fstore)
            throws StorageException {
        Connection con = null;
        long store_size = fstore.getSize();
        final Long l_max = new Long("8796093022208");
        if (store_size > l_max.longValue()) {
            throw new StorageException("Filestore size to large for database (max=" + l_max.longValue() + ")");
        }
        store_size *= Math.pow(2, 20);
        PreparedStatement stmt = null;
        try {

            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit(false);
            final int fstore_id = IDGenerator.getId(con);
            con.commit();
            stmt = con.prepareStatement("INSERT INTO filestore (id,uri,size,max_context) VALUES (?,?,?,?)");
            stmt.setInt(1, fstore_id);
            stmt.setString(2, fstore.getUrl());
            stmt.setLong(3, store_size);
            stmt.setInt(4, fstore.getMaxContexts());
            stmt.executeUpdate();
            con.commit();

            return fstore_id;
        }catch (DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG,dt);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of configdb connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    @Override
    public int registerServer(final String serverName) throws StorageException {

        Connection con = null;
        PreparedStatement prep = null;
        try {

            con = cache.getWRITEConnectionForCONFIGDB();

            con.setAutoCommit(false);
            final int srv_id = IDGenerator.getId(con);
            con.commit();
            con.setAutoCommit(true);
            prep = con.prepareStatement("INSERT INTO server VALUES (?,?);");
            prep.setInt(1, srv_id);
            if (serverName != null) {
                prep.setString(2, serverName);
            } else {
                prep.setNull(2, Types.VARCHAR);
            }
            prep.executeUpdate();
            prep.close();

            return srv_id;
        }catch (DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG,dt);            
            throw AdminCache.parseDataTruncation(dt);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of ox connection!", exp);
            }
            throw new StorageException(ecp);

        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException e) {
                log.error("Error pushing configdb connection to pool!", e);
            }
        }

    }

    @Override
    public Database[] searchForDatabase(final String search_pattern)
            throws StorageException {

        Connection con = null;
        PreparedStatement pstmt = null;
        PreparedStatement cstmt = null;

        try {

            con = cache.getREADConnectionForCONFIGDB();
            final String my_search_pattern = search_pattern.replace('*', '%');

            pstmt = con.prepareStatement("SELECT db_pool_id,url,driver,login,password,hardlimit,max,initial,name,weight,max_units,read_db_pool_id,write_db_pool_id FROM db_pool,db_cluster WHERE ( db_pool_id = db_cluster.write_db_pool_id OR db_pool_id = db_cluster.read_db_pool_id) AND name LIKE ?");
            pstmt.setString(1, my_search_pattern);
            final ResultSet rs = pstmt.executeQuery();
            final ArrayList<Database> tmp = new ArrayList<Database>();

            while (rs.next()) {

                final Database db = new Database();

                Boolean ismaster = Boolean.TRUE;
                final int readid = rs.getInt("read_db_pool_id");
                final int writeid = rs.getInt("write_db_pool_id");
                final int id = rs.getInt("db_pool_id");
                int masterid = 0;
                int nrcontexts = 0;
                if (readid == id) {
                    ismaster = Boolean.FALSE;
                    masterid = writeid;
                } else {
                    // we are master
                    cstmt = con.prepareStatement("SELECT COUNT(cid) FROM context_server2db_pool WHERE write_db_pool_id = ?");
                    cstmt.setInt(1, writeid);
                    final ResultSet rs1 = cstmt.executeQuery();
                    if (!rs1.next()) {
                        throw new StorageException("Unable to count contexts");
                    }
                    nrcontexts = Integer.parseInt(rs1.getString("COUNT(cid)"));
                    rs1.close();
                    cstmt.close();
                }
                db.setClusterWeight(rs.getInt("weight"));
                db.setDisplayname(rs.getString("name"));
                db.setDriver(rs.getString("driver"));
                db.setId(rs.getInt("db_pool_id"));
                db.setLogin(rs.getString("login"));
                db.setMaster(ismaster.booleanValue());
                db.setMasterId(masterid);
                db.setMaxUnits(rs.getInt("max_units"));
                db.setPassword(rs.getString("password"));
                db.setPoolHardLimit(rs.getInt("hardlimit"));
                db.setPoolInitial(rs.getInt("initial"));
                db.setPoolMax(rs.getInt("max"));
                db.setUrl(rs.getString("url"));
                db.setCurrentUnits(nrcontexts);
                tmp.add(db);
            }
            rs.close();

            return tmp.toArray(new Database[tmp.size()]);

        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQl Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of ox connection!", exp);
            }
            throw new StorageException(ecp);

        } finally {

            try {
                if (cstmt != null) {
                    cstmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }

            if (con != null) {
                try {
                    cache.pushConfigDBRead(con);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public Server[] searchForServer(final String search_pattern)
            throws StorageException {

        Connection con = null;
        PreparedStatement stmt = null;
        try {

            con = cache.getREADConnectionForCONFIGDB();

            stmt = con.prepareStatement("SELECT name,server_id FROM server WHERE name LIKE ? OR server_id = ?");

            final String my_search_pattern = search_pattern.replace('*', '%');
            stmt.setString(1, my_search_pattern);
            stmt.setString(2, my_search_pattern);

            final ResultSet rs = stmt.executeQuery();
            final ArrayList<Server> tmp = new ArrayList<Server>();
            while (rs.next()) {
                final Server srv = new Server();
                srv.setId(rs.getInt("server_id"));
                srv.setName(rs.getString("name"));
                tmp.add(srv);
            }
            rs.close();

            return tmp.toArray(new Server[tmp.size()]);
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of ox connection!", exp);
            }
            throw new StorageException(ecp);

        } finally {

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            if (con != null) {
                try {
                    if (con != null) {
                        cache.pushConfigDBRead(con);
                    }
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }

        }
    }

    @Override
    public void unregisterDatabase(final int db_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {

            con = cache.getWRITEConnectionForCONFIGDB();
            stmt = con.prepareStatement("DELETE FROM db_cluster WHERE read_db_pool_id = ? OR write_db_pool_id = ?");
            stmt.setInt(1, db_id);
            stmt.setInt(2, db_id);
            stmt.executeUpdate();
            stmt.close();

            stmt = con.prepareStatement("DELETE FROM db_pool WHERE db_pool_id = ?");
            stmt.setInt(1, db_id);
            stmt.executeUpdate();
            stmt.close();

        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of ox connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            if (con != null) {
                try {
                    cache.pushConfigDBWrite(con);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

    @Override
    public void unregisterFilestore(final int store_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit(false);
            stmt = con.prepareStatement("DELETE FROM filestore WHERE id = ?");
            stmt.setInt(1, store_id);
            stmt.executeUpdate();
            con.commit();
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (final SQLException exp) {
                log.error("Error processing rollback of ox connection!", exp);
            }
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException exp) {
                log.error("Error pushing configdb connection to pool!", exp);
            }
        }
    }

    @Override
    public void unregisterServer(final int server_id) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            stmt = con.prepareStatement("DELETE FROM server WHERE server_id = ?");
            stmt.setInt(1, server_id);
            stmt.executeUpdate();
            stmt.close();
        } catch (final PoolException pe) {
            log.error("Pool Error", pe);
            throw new StorageException(pe);
        } catch (final SQLException ecp) {
            log.error("SQL Error", ecp);           
            throw new StorageException(ecp);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            if (con != null) {
                try {
                    cache.pushConfigDBWrite(con);
                } catch (final PoolException exp) {
                    log.error("Error pushing configdb connection to pool!", exp);
                }
            }
        }
    }

}
