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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import com.openexchange.tools.file.external.FileStorageException;
import com.openexchange.tools.file.external.QuotaFileStorage;
import com.openexchange.tools.file.external.QuotaFileStorageException;
import com.openexchange.tools.sql.DBUtils;

public class QuotaFileStorageImpl implements QuotaFileStorage {

    /**
     * The context of the QuotaFileStorage
     */
    private final Context context;

    /**
     * The FileStorage the QuotaFS works on
     */
    private final FileStorage fileStorage;

    /**
     * The ContextId
     */
    private final int cid;

    /**
     * Service for DB Connections
     */
    private final DatabaseService db;

    /**
     * The Logfile
     */
    private final Log LOG = LogFactory.getLog(QuotaFileStorage.class);

    /**
     * Initializes the QuotaFileStorage
     * 
     * @param context Context for the Quota.
     * @param fs The physical FileStorage.
     * @param db The DatabaseService.
     * @throws QuotaFileStorageException
     */
    public QuotaFileStorageImpl(final Context context, final FileStorage fs, final DatabaseService db) throws QuotaFileStorageException {
        this.context = context;
        cid = context.getContextId();
        fileStorage = fs;

        if (fileStorage == null) {
            throw new QuotaFileStorageException(QuotaFileStorageException.Code.INSTANTIATIONERROR);
        }

        this.db = db;

    }

    /**
     * Returns the Context specific Quota
     */
    public long getQuota() {
        return context.getFileStorageQuota();
    }

    /**
     * Deletes a File
     * 
     * @param identifier The file that has to be deleted
     * @return true if file was deleted correctly
     */
    public boolean deleteFile(final String identifier) throws QuotaFileStorageException {
        try {
            final long fileSize = fileStorage.getFileSize(identifier);

            final boolean deleted = fileStorage.deleteFile(identifier);
            if (!deleted) {
                return false;
            } else {
                decUsage(fileSize);
                return true;
            }
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }

    }

    /**
     * Increases the QuotaUsage.
     * 
     * @param usage by that the QuotaUsage has to be increased
     * @return true if Quota is full
     * @throws QuotaFileStorageException on Database errors
     */
    protected boolean incUsage(final long usage) throws QuotaFileStorageException {
        final Connection con;
        try {
            con = db.getWritable(context);
        } catch (final DBPoolingException e) {
            throw new QuotaFileStorageException(e);
        }

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        boolean full = false;

        try {
            con.setAutoCommit(false);

            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid = ? FOR UPDATE");
            sstmt.setInt(1, cid);
            rs = sstmt.executeQuery();

            long oldUsage = 0;
            while (rs.next()) {
                oldUsage = rs.getLong(1);
            }

            final long newUsage = oldUsage + usage;
            if (newUsage > context.getFileStorageQuota()) {
                full = true;
            } else {
                ustmt = con.prepareStatement("UPDATE filestore_usage SET used = ? WHERE cid = ?");
                ustmt.setLong(1, newUsage);
                ustmt.setInt(2, cid);
                ustmt.execute();
            }

            con.commit();
        } catch (final SQLException s) {
            DBUtils.rollback(con);
            throw new QuotaFileStorageException(QuotaFileStorageException.Code.SQLSTATEMENTERROR, s);
        } finally {
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(rs);
            DBUtils.closeSQLStuff(sstmt);
            DBUtils.closeSQLStuff(ustmt);
            close(con);
        }

        return full;

    }

    /**
     * Decreases the QuotaUsage.
     * 
     * @param usage by that the Quota has to be decreased
     * @throws QuotaFileStorageException
     */
    protected void decUsage(final long usage) throws QuotaFileStorageException {
        final Connection con;
        try {
            con = db.getWritable(context);
        } catch (final DBPoolingException e) {
            throw new QuotaFileStorageException(e);
        }

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;

        try {

            con.setAutoCommit(false);

            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid = ? FOR UPDATE");
            sstmt.setInt(1, cid);
            rs = sstmt.executeQuery();

            long oldUsage = 0;
            while (rs.next()) {
                oldUsage = rs.getLong("used");
            }
            long newUsage = oldUsage - usage;

            if (newUsage < 0) {
                newUsage = 0;
                final QuotaFileStorageException e = new QuotaFileStorageException(QuotaFileStorageException.Code.QUOTA_UNDERRUN, cid);
                LOG.fatal(e.getMessage(), e);
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used = ? WHERE cid = ?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, cid);
            ustmt.execute();

            con.commit();
        } catch (final SQLException s) {
            DBUtils.rollback(con);
            throw new QuotaFileStorageException(QuotaFileStorageException.Code.SQLSTATEMENTERROR, s);
        } finally {
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(rs);
            DBUtils.closeSQLStuff(sstmt);
            DBUtils.closeSQLStuff(ustmt);
            close(con);
        }
    }

    /**
     * set the QuotaUsage to a specific value
     * 
     * @param usage new value of the QuotaUsage
     * @throws QuotaFileStorageException
     */
    protected void setUsage(final long usage) throws QuotaFileStorageException {
        final Connection con;
        try {
            con = db.getWritable(context);
        } catch (final DBPoolingException e) {
            throw new QuotaFileStorageException(e);
        }

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;

        try {
            con.setAutoCommit(false);

            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid = ? FOR UPDATE");
            sstmt.setInt(1, cid);
            sstmt.executeQuery();

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used = ? WHERE cid = ?");
            ustmt.setLong(1, usage);
            ustmt.setInt(2, cid);
            ustmt.execute();

            con.commit();
        } catch (final SQLException s) {
            DBUtils.rollback(con);
            throw new QuotaFileStorageException(QuotaFileStorageException.Code.SQLSTATEMENTERROR, s);
        } finally {
            DBUtils.autocommit(con);
            DBUtils.closeSQLStuff(sstmt);
            DBUtils.closeSQLStuff(ustmt);
            close(con);
        }
    }

    /**
     * Closes Connections
     * 
     * @param con
     * @param sstmt
     * @param ustmt
     */
    protected void close(final Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Delete a List of Files.
     * 
     * @return A list of Files that could not be deleted.
     */
    public Set<String> deleteFiles(final String[] identifiers) throws QuotaFileStorageException {
        final HashMap<String, Long> fileSizes = new HashMap<String, Long>();
        final SortedSet<String> set = new TreeSet<String>();

        for (final String identifier : identifiers) {
            boolean deleted;
            try {
                deleted = fileStorage.deleteFile(identifier);
                fileSizes.put(identifier, getFileSize(identifier));
                if (!deleted) {
                    set.add(identifier);
                }
            } catch (final FileStorageException e) {
                throw new QuotaFileStorageException(e);
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

    /**
     * Get the actual Usage of the Quota.
     */
    public long getUsage() throws QuotaFileStorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;

        final Connection con;
        try {
            con = db.getReadOnly(context);
        } catch (final DBPoolingException p) {
            throw new QuotaFileStorageException(p);
        }

        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid = ?");
            stmt.setInt(1, cid);
            result = stmt.executeQuery();

            if (!result.next()) {
                return 0;
            } else {
                return result.getLong(1);
            }
        } catch (final SQLException e) {
            throw new QuotaFileStorageException(QuotaFileStorageException.Code.SQLSTATEMENTERROR, e);
        } finally {
            DBUtils.closeSQLStuff(result);
            DBUtils.closeSQLStuff(stmt);
            close(con);
        }
    }

    /**
     * Save a new File in the FileStorage. Usage will be increased.
     */
    public String saveNewFile(final InputStream is) throws QuotaFileStorageException {
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
        } catch (final QuotaFileStorageException q) {
            try {
                fileStorage.deleteFile(file);
            } catch (final FileStorageException f) {
                throw new QuotaFileStorageException(f);
            }
            throw new QuotaFileStorageException(q);
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
        if (full) {
            throw new QuotaFileStorageException(QuotaFileStorageException.Code.STORE_FULL);
        }

        return retval;

    }

    /**
     * Recalculates the Usage if it's inconsistent based on all physically existing files.
     */
    public void recalculateUsage() throws QuotaFileStorageException {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("Recalculating usage for Context " + cid);
            }
            final SortedSet<String> filenames = fileStorage.getFileList();
            long entireFileSize = 0;

            for (final String filename : filenames) {
                entireFileSize += fileStorage.getFileSize(filename);
            }

            setUsage(entireFileSize);
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

    /**
     * Returns a SortedSet with all Files in the Storage.
     */
    public SortedSet<String> getFileList() throws QuotaFileStorageException {
        try {
            return fileStorage.getFileList();
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

    /**
     * Returns a specific File from the Storage.
     */
    public InputStream getFile(final String file) throws QuotaFileStorageException {
        try {
            return fileStorage.getFile(file);
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

    /**
     * Returns the size of a File in the Store.
     */
    public long getFileSize(final String name) throws QuotaFileStorageException {
        try {
            return fileStorage.getFileSize(name);
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

    /**
     * Returns the MimeType of a File in the Store.
     */
    public String getMimeType(final String name) throws QuotaFileStorageException {
        try {
            return fileStorage.getMimeType(name);
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

    /**
     * Removes the whole FileStorage. Quota will be set to zero.
     */
    public void remove() throws QuotaFileStorageException {
        try {
            fileStorage.remove();
            setUsage(0);
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

    /**
     * Recreates the State File.
     */
    public void recreateStateFile() throws QuotaFileStorageException {
        try {
            fileStorage.recreateStateFile();
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

    public boolean stateFileIsCorrect() throws FileStorageException {
        try {
            return fileStorage.stateFileIsCorrect();
        } catch (final FileStorageException e) {
            throw new QuotaFileStorageException(e);
        }
    }

}
