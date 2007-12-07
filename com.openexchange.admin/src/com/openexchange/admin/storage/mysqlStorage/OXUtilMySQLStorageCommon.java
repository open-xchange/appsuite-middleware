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
package com.openexchange.admin.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
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
        }catch (final DataTruncation dt){
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
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

    private void pumpData2Database(final ArrayList<String> db_queries, final String ident, final Connection con, final String database) throws SQLException {
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
            st.execute("DROP DATABASE if exists `" + db.getScheme() + "`");

            con.commit();
        } catch (final ClassNotFoundException cnf) {
            log.error("Driver not found to delete database.", cnf);
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
