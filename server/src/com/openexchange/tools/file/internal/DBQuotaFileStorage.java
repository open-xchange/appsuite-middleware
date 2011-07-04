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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.tools.file.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.OXException;
import com.openexchange.tools.file.external.QuotaFileStorage;
import com.openexchange.tools.file.external.QuotaOXException;
import com.openexchange.tools.file.external.QuotaOXException.Code;
import com.openexchange.tools.sql.DBUtils;

public class DBQuotaFileStorage implements QuotaFileStorage {

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(QuotaFileStorage.class));

    /**
     * The context of the QuotaFileStorage
     */
    private final Context context;

    /**
     * The FileStorage the QuotaFS works on
     */
    private final FileStorage fileStorage;

    /**
     * Service for DB Connections
     */
    private final DatabaseService db;

    /**
     * Initializes the QuotaFileStorage
     * 
     * @param context Context for the Quota.
     * @param fs The physical FileStorage.
     * @param db The DatabaseService.
     * @throws QuotaOXException
     */
    public DBQuotaFileStorage(final Context context, final FileStorage fs, final DatabaseService db) throws QuotaOXException {
        super();
        this.context = context;
        fileStorage = fs;
        this.db = db;
        if (fileStorage == null) {
            throw new QuotaOXException(QuotaOXException.Code.INSTANTIATIONERROR);
        }
    }

    public long getQuota() {
        return context.getFileStorageQuota();
    }

    public boolean deleteFile(final String identifier) throws QuotaOXException {
        try {
            final long fileSize = fileStorage.getFileSize(identifier);
            final boolean deleted = fileStorage.deleteFile(identifier);
            if (!deleted) {
                return false;
            }
            decUsage(fileSize);
            return true;
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    /**
     * Increases the QuotaUsage.
     * 
     * @param usage by that the QuotaUsage has to be increased
     * @return true if Quota is full
     * @throws QuotaOXException on Database errors
     */
    protected boolean incUsage(final long usage) throws QuotaOXException {
        final Connection con;
        try {
            con = db.getWritable(context);
        } catch (final DBPoolingException e) {
            throw new QuotaOXException(e);
        }

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            con.setAutoCommit(false);
            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? FOR UPDATE");
            sstmt.setInt(1, context.getContextId());
            rs = sstmt.executeQuery();
            final long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong(1);
            } else {
                throw new QuotaOXException(Code.NO_USAGE, I(context.getContextId()));
            }
            long newUsage = oldUsage + usage;
            long quota = context.getFileStorageQuota();
            if (quota > 0 && newUsage > quota) {
                return true;
            }
            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, context.getContextId());
            int rows = ustmt.executeUpdate();
            if (1 != rows) {
                throw new QuotaOXException(Code.UPDATE_FAILED, I(context.getContextId()));
            }
            con.commit();
        } catch (final SQLException s) {
            DBUtils.rollback(con);
            throw new QuotaOXException(QuotaOXException.Code.SQLSTATEMENTERROR, s);
        } finally {
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(rs);
            DBUtils.closeSQLStuff(sstmt);
            DBUtils.closeSQLStuff(ustmt);
            db.backWritable(context, con);
        }
        return false;
    }

    /**
     * Decreases the QuotaUsage.
     * 
     * @param usage by that the Quota has to be decreased
     * @throws QuotaOXException
     */
    protected void decUsage(final long usage) throws QuotaOXException {
        final Connection con;
        try {
            con = db.getWritable(context);
        } catch (final DBPoolingException e) {
            throw new QuotaOXException(e);
        }

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;

        try {

            con.setAutoCommit(false);

            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? FOR UPDATE");
            sstmt.setInt(1, context.getContextId());
            rs = sstmt.executeQuery();

            final long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong("used");
            } else {
                throw new QuotaOXException(Code.NO_USAGE, I(context.getContextId()));
            }
            long newUsage = oldUsage - usage;

            if (newUsage < 0) {
                newUsage = 0;
                final QuotaOXException e = new QuotaOXException(QuotaOXException.Code.QUOTA_UNDERRUN, I(context.getContextId()));
                LOG.fatal(e.getMessage(), e);
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, context.getContextId());
            int rows = ustmt.executeUpdate();
            if (1 != rows) {
                throw new QuotaOXException(Code.UPDATE_FAILED, I(context.getContextId()));
            }

            con.commit();
        } catch (final SQLException s) {
            DBUtils.rollback(con);
            throw new QuotaOXException(QuotaOXException.Code.SQLSTATEMENTERROR, s);
        } finally {
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(rs);
            DBUtils.closeSQLStuff(sstmt);
            DBUtils.closeSQLStuff(ustmt);
            db.backWritable(context, con);
        }
    }

    public Set<String> deleteFiles(final String[] identifiers) throws QuotaOXException {
        final HashMap<String, Long> fileSizes = new HashMap<String, Long>();
        final SortedSet<String> set = new TreeSet<String>();
        for (final String identifier : identifiers) {
            boolean deleted;
            try {
                Long size = L(getFileSize(identifier)); // Get size before attempting delete. File is not found afterwards
                deleted = fileStorage.deleteFile(identifier);
                fileSizes.put(identifier, size);
                if (!deleted) {
                    set.add(identifier);
                }
            } catch (final OXException e) {
                throw new QuotaOXException(e);
            }

        }
        fileSizes.keySet().removeAll(set);
        long sum = 0;
        for (final long fileSize : fileSizes.values()) {
            sum += fileSize;
        }
        decUsage(sum);
        return set;
    }

    public long getUsage() throws QuotaOXException {
        final Connection con;
        try {
            con = db.getReadOnly(context);
        } catch (final DBPoolingException p) {
            throw new QuotaOXException(p);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        final long usage;
        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=?");
            stmt.setInt(1, context.getContextId());
            result = stmt.executeQuery();
            if (result.next()) {
                usage = result.getLong(1);
            } else {
                throw new QuotaOXException(Code.NO_USAGE, I(context.getContextId()));
            }
        } catch (final SQLException e) {
            throw new QuotaOXException(QuotaOXException.Code.SQLSTATEMENTERROR, e);
        } finally {
            DBUtils.closeSQLStuff(result);
            DBUtils.closeSQLStuff(stmt);
            db.backReadOnly(context, con);
        }
        return usage;
    }

    public String saveNewFile(final InputStream is) throws QuotaOXException {
        String file = null;
        String retval = null;
        final boolean full;
        try {

            file = fileStorage.saveNewFile(is);
            final long fileSize = fileStorage.getFileSize(file);
            retval = file;

            full = incUsage(fileSize);

            if (full) {
                retval = null;
                fileStorage.deleteFile(file);
            }
        } catch (final QuotaOXException q) {
            try {
                fileStorage.deleteFile(file);
            } catch (final OXException f) {
                throw new QuotaOXException(f);
            }
            throw new QuotaOXException(q);
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
        if (full) {
            throw new QuotaOXException(QuotaOXException.Code.STORE_FULL);
        }
        return retval;

    }

    /**
     * Recalculates the Usage if it's inconsistent based on all physically existing files and writes it into quota_usage.
     */
    public void recalculateUsage() throws QuotaOXException {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("Recalculating usage for Context " + context.getContextId());
            }
            final SortedSet<String> filenames = fileStorage.getFileList();
            long entireFileSize = 0;

            for (final String filename : filenames) {
                entireFileSize += fileStorage.getFileSize(filename);
            }

            final Connection con;
            try {
                con = db.getWritable(context);
            } catch (final DBPoolingException e) {
                throw new QuotaOXException(e);
            }

            PreparedStatement stmt = null;
            ResultSet result = null;
            
            try {
                stmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=?");
                stmt.setLong(1, entireFileSize);
                stmt.setInt(2, context.getContextId());
                int rows = stmt.executeUpdate();
                if (1 != rows) {
                    throw new QuotaOXException(Code.UPDATE_FAILED, I(context.getContextId()));
                }
            } catch (final SQLException s) {
                DBUtils.rollback(con);
                throw new QuotaOXException(QuotaOXException.Code.SQLSTATEMENTERROR, s);
            } finally {
                autocommit(con);
                closeSQLStuff(result, stmt);
                db.backWritable(context, con);
            }
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    public SortedSet<String> getFileList() throws QuotaOXException {
        try {
            return fileStorage.getFileList();
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    public InputStream getFile(final String file) throws QuotaOXException {
        try {
            return fileStorage.getFile(file);
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    public long getFileSize(final String name) throws QuotaOXException {
        try {
            return fileStorage.getFileSize(name);
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    public String getMimeType(final String name) throws QuotaOXException {
        try {
            return fileStorage.getMimeType(name);
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    public void remove() throws QuotaOXException {
        try {
            fileStorage.remove();
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    public void recreateStateFile() throws QuotaOXException {
        try {
            fileStorage.recreateStateFile();
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }

    public boolean stateFileIsCorrect() throws OXException {
        try {
            return fileStorage.stateFileIsCorrect();
        } catch (final OXException e) {
            throw new QuotaOXException(e);
        }
    }
}
