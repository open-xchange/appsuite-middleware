package com.openexchange.admin.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;

public class OXUtilMySQLStorageCommon {
    
    private static final Log log = LogFactory.getLog(OXUtilMySQLStorageCommon.class);
    
    private static AdminCache cache = null;
    
    static {
        cache = ClientAdminThread.cache;
    }

    public void createDatabase(final Database db) throws StorageException {

        Connection con = null;
        Statement st = null;
        try {

            String sql_pass = "";
            if (db.getPassword() != null) {
                sql_pass = db.getPassword();
            }

            con = cache.getSimpleSqlConnection(db.getUrl(), db.getLogin(), sql_pass, db.getDriver());

            try {
                con.setCatalog(db.getScheme());
                // if exists, show error
                throw new StorageException("Database \"" + db.getScheme() + "\" already exists");
            } catch (final SQLException ecp) {
            }

            if (con.getAutoCommit()) {
                con.setAutoCommit(false);
            }

            // initial create of the "database"
            st = con.createStatement();
            st.addBatch("CREATE DATABASE `" + db.getScheme() + "` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci");
            st.executeBatch();

            pumpData2Database(cache.getOXDBInitialQueries(), "ox initial", con, db.getScheme());

        } catch (final OXGenericException oxgen) {
            log.error("Error reading DB init Queries!", oxgen);
            throw new StorageException(oxgen);
        } catch (final ClassNotFoundException cnf) {
            log.error("Driver not found to create database ", cnf);
            throw new StorageException(cnf);
        } catch (final SQLException cp) {
            log.error("SQL Error", cp);
            try {
                if (con != null && !con.getAutoCommit()) {
                    con.rollback();
                }
            } catch (final Throwable expd) {
                log.error("Error processing rollback of simple connection!", expd);
            }
            throw new StorageException(cp);
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            cache.closeSimpleConnection(con);
        }

    }

    private void pumpData2Database(final ArrayList db_queries, final String ident, final Connection con, final String database) throws SQLException {
        Statement st = null;
        try {
            con.setCatalog(database);
            st = con.createStatement();
            for (int a = 0; a < db_queries.size(); a++) {
                st.addBatch("" + db_queries.get(a));
            }
            st.executeBatch();
            con.commit();
        } catch (final SQLException ecp) {
            log.fatal("SQL Error occured processing queries for: \"" + ident.toUpperCase() + "\" ", ecp);
            try {
                if (con != null && !con.getAutoCommit()) {
                    con.rollback();
                }
            } catch (final SQLException expd) {
                log.error("Error processing rollback of simple connection!", expd);
            }
            throw ecp;
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }

        }

    }

    public void deleteDatabase(final Database db) throws StorageException {
        Connection con = null;
        Statement st = null;
        try {

            con = cache.getSimpleSqlConnection(db.getUrl(), db.getLogin(), db.getPassword(), db.getDriver());
            if (con.getAutoCommit()) {
                con.setAutoCommit(false);
            }
            st = con.createStatement();
            st.addBatch("DROP DATABASE if exists `" + db.getScheme() + "`");
            st.executeBatch();

            con.commit();
        } catch (final ClassNotFoundException cnf) {
            log.error("Driver not found to create database ");
            throw new StorageException(cnf);
        } catch (final SQLException cp) {
            log.error("SQL Error", cp);
            try {
                con.rollback();
            } catch (final SQLException ecp) {
                log.error("Error processing rollback of simple connection!", ecp);
            }
            throw new StorageException(cp);
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (final SQLException e) {
                log.error("", e);
            }
            cache.closeSimpleConnection(con);
        }
    }

}
