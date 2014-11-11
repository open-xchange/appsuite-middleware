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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.filestore.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageExceptionCodes;
import com.openexchange.filestore.impl.osgi.Services;

public class DBQuotaFileStorage implements QuotaFileStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QuotaFileStorage.class);

    private final int contextId;
    private final FileStorage fileStorage;
    private final long quota;

    /**
     * Initializes the QuotaFileStorage
     */
    public DBQuotaFileStorage(int contextId, long quota, FileStorage fs) throws OXException {
        super();
        this.contextId = contextId;
        this.quota = quota;
        fileStorage = fs;
        if (fileStorage == null) {
            throw QuotaFileStorageExceptionCodes.INSTANTIATIONERROR.create();
        }
    }

    private DatabaseService getDatabaseService() throws OXException {
        return Services.requireService(DatabaseService.class);
    }

    @Override
    public long getQuota() {
        return quota;
    }

    @Override
    public boolean deleteFile(final String identifier) throws OXException {
        final long fileSize = fileStorage.getFileSize(identifier);
        final boolean deleted = fileStorage.deleteFile(identifier);
        if (!deleted) {
            return false;
        }
        decUsage(fileSize);
        return true;
    }

    /**
     * Increases the QuotaUsage.
     *
     * @param usage by that the QuotaUsage has to be increased
     * @return true if Quota is full
     * @throws OXException on Database errors
     */
    protected boolean incUsage(final long usage) throws OXException {
        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? FOR UPDATE");
            sstmt.setInt(1, contextId);
            rs = sstmt.executeQuery();
            final long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong(1);
            } else {
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
            final long newUsage = oldUsage + usage;
            final long quota = this.quota;
            if (quota > 0 && newUsage > quota) {
                return true;
            }
            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, contextId);
            final int rows = ustmt.executeUpdate();
            if (1 != rows) {
                throw QuotaFileStorageExceptionCodes.UPDATE_FAILED.create(I(contextId));
            }
            con.commit();
            rollback = false;
        } catch (SQLException s) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(s);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            Databases.closeSQLStuff(rs);
            Databases.closeSQLStuff(sstmt);
            Databases.closeSQLStuff(ustmt);
            db.backWritable(contextId, con);
        }
        return false;
    }

    /**
     * Decreases the QuotaUsage.
     *
     * @param usage by that the Quota has to be decreased
     * @throws OXException
     */
    protected void decUsage(long usage) throws OXException {
        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {

            con.setAutoCommit(false);
            rollback = true;

            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? FOR UPDATE");
            sstmt.setInt(1, contextId);
            rs = sstmt.executeQuery();

            long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong("used");
            } else {
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
            long newUsage = oldUsage - usage;

            if (newUsage < 0) {
                newUsage = 0;
                final OXException e = QuotaFileStorageExceptionCodes.QUOTA_UNDERRUN.create(I(contextId));
                LOG.error("", e);
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, contextId);
            final int rows = ustmt.executeUpdate();
            if (1 != rows) {
                throw QuotaFileStorageExceptionCodes.UPDATE_FAILED.create(I(contextId));
            }

            con.commit();
            rollback = false;
        } catch (SQLException s) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(s);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            Databases.closeSQLStuff(rs);
            Databases.closeSQLStuff(sstmt);
            Databases.closeSQLStuff(ustmt);
            db.backWritable(contextId, con);
        }
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        final HashMap<String, Long> fileSizes = new HashMap<String, Long>();
        final SortedSet<String> set = new TreeSet<String>();
        for (final String identifier : identifiers) {
            boolean deleted;
            try {
                // Get size before attempting delete. File is not found afterwards
                final Long size = L(getFileSize(identifier));
                deleted = fileStorage.deleteFile(identifier);
                fileSizes.put(identifier, size);
            } catch (final OXException e) {
                if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                    throw e;
                }
                deleted = false;
            }
            if (!deleted) {
                set.add(identifier);
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

    @Override
    public long getUsage() throws OXException {
        DatabaseService db = getDatabaseService();
        Connection con = db.getReadOnly(contextId);

        PreparedStatement stmt = null;
        ResultSet result = null;
        final long usage;
        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=?");
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            if (result.next()) {
                usage = result.getLong(1);
            } else {
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
        } catch (final SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
            db.backReadOnly(contextId, con);
        }
        return usage;
    }

    @Override
    public String saveNewFile(final InputStream is) throws OXException {
        return saveNewFile(is, -1);
    }

    @Override
    public String saveNewFile(final InputStream is, long sizeHint) throws OXException {
        if (0 < sizeHint) {
            checkAvailable(sizeHint);
        }
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
        } catch (final OXException q) {
        	if (file != null) {
                fileStorage.deleteFile(file);
        	}
            throw q;
        }
        if (full) {
            throw QuotaFileStorageExceptionCodes.STORE_FULL.create();
        }
        return retval;
    }

    /**
     * Recalculates the Usage if it's inconsistent based on all physically existing files and writes it into quota_usage.
     */
    @Override
    public void recalculateUsage() throws OXException {
        Set<String> filesToIgnore = Collections.emptySet();
        recalculateUsage(filesToIgnore);
    }

    @Override
    public void recalculateUsage(Set<String> filesToIgnore) throws OXException {
        LOG.info("Recalculating usage for Context {}", contextId);

        SortedSet<String> filenames = fileStorage.getFileList();
        long entireFileSize = 0;
        for (String filename : filenames) {
            if (!filesToIgnore.contains(filename)) {
                entireFileSize += fileStorage.getFileSize(filename);
            }
        }

        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            stmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=?");
            stmt.setLong(1, entireFileSize);
            stmt.setInt(2, contextId);
            final int rows = stmt.executeUpdate();
            if (1 != rows) {
                throw QuotaFileStorageExceptionCodes.UPDATE_FAILED.create(I(contextId));
            }

            con.commit();
            rollback = false;
        } catch (SQLException s) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(s);
        } catch (RuntimeException s) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(s);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            Databases.closeSQLStuff(stmt);
            db.backWritable(contextId, con);
        }
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        return fileStorage.getFileList();
    }

    @Override
    public InputStream getFile(final String file) throws OXException {
        return fileStorage.getFile(file);
    }

    @Override
    public long getFileSize(final String name) throws OXException {
        return fileStorage.getFileSize(name);
    }

    @Override
    public String getMimeType(final String name) throws OXException {
        return fileStorage.getMimeType(name);
    }

    @Override
    public void remove() throws OXException {
        fileStorage.remove();
    }

    @Override
    public void recreateStateFile() throws OXException {
        fileStorage.recreateStateFile();
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        return fileStorage.stateFileIsCorrect();
    }

    @Override
    public long appendToFile(InputStream is, String name, long offset) throws OXException {
        return appendToFile(is, name, offset, -1);
    }

    @Override
    public long appendToFile(InputStream is, String name, long offset, long sizeHint) throws OXException {
        if (0 < sizeHint) {
            checkAvailable(sizeHint);
        }
        long newSize = -1;
        boolean notFoundError = false;
        try {
            newSize = fileStorage.appendToFile(is, name, offset);
            if (incUsage(newSize - offset)) {
                throw QuotaFileStorageExceptionCodes.STORE_FULL.create();
            }
        } catch (final OXException e) {
            if (FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                notFoundError = true;
            }
            throw e;
        } finally {
            if (false == notFoundError && -1 == newSize) {
                try {
                    fileStorage.setFileLength(offset, name);
                } catch (OXException e) {
                    LOG.warn("Error rolling back 'append' operation", e);
                }
            }
        }
        return newSize;
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        fileStorage.setFileLength(length, name);
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        return fileStorage.getFile(name, offset, length);
    }

    private void checkAvailable(long required) throws OXException {
        if (0 < required) {
            long quota = getQuota();
            if (0 < quota && quota < getUsage() + required) {
                throw QuotaFileStorageExceptionCodes.STORE_FULL.create();
            }
        }
    }

}
