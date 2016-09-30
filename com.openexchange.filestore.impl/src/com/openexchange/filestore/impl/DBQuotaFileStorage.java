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

package com.openexchange.filestore.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import com.openexchange.filestore.QuotaFileStorageListener;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link DBQuotaFileStorage} - Delegates file storage operations to associated {@link FileStorage} instance while accounting quota in
 * <code>'filestore_usage'</code> database table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DBQuotaFileStorage implements QuotaFileStorage, Serializable /* For cache service */{

    private static final long serialVersionUID = -4048657112670657310L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(QuotaFileStorage.class);

    private static final ServiceListing<QuotaFileStorageListener> EMPTY_LISTENERS = new ServiceListing<QuotaFileStorageListener>() {

        @Override
        public Iterator<QuotaFileStorageListener> iterator() {
            return new Iterator<QuotaFileStorageListener>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public QuotaFileStorageListener next() {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    // Ignore
                }

            };
        }

        @Override
        public List<QuotaFileStorageListener> getServiceList() {
            return Collections.emptyList();
        }
    };

    private final int contextId;
    private final transient FileStorage fileStorage;
    private final long quota;
    private final int ownerId;
    private final URI uri;
    private final ServiceListing<QuotaFileStorageListener> listeners;

    /**
     * Initializes a new {@link DBQuotaFileStorage} for an owner.
     *
     * @param contextId The context identifier
     * @param ownerId The file storage owner or <code>0</code> (zero); the owner determines to what 'filestore_usage' entry the quota gets
     *            accounted
     * @param quota The assigned quota
     * @param fs The file storage associated with the owner
     * @param uri The URI that fully qualifies this file storage
     * @param listeners The quota listeners
     * @throws OXException If initialization fails
     */
    public DBQuotaFileStorage(int contextId, int ownerId, long quota, FileStorage fs, URI uri, ServiceListing<QuotaFileStorageListener> listeners) throws OXException {
        super();
        this.listeners = null == listeners ? EMPTY_LISTENERS : listeners;
        if (fs == null) {
            throw QuotaFileStorageExceptionCodes.INSTANTIATIONERROR.create();
        }
        this.uri = uri;
        this.contextId = contextId;
        this.ownerId = ownerId;
        this.quota = quota;
        fileStorage = fs;
    }

    private DatabaseService getDatabaseService() throws OXException {
        return Services.requireService(DatabaseService.class);
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public long getQuota() {
        return quota;
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        final long fileSize = fileStorage.getFileSize(identifier);
        final boolean deleted = fileStorage.deleteFile(identifier);
        if (!deleted) {
            return false;
        }
        decUsage(identifier, fileSize);
        return true;
    }

    /**
     * Increases the quota usage.
     *
     * @param id The identifier of the associated file
     * @param usage The value by which the quota is supposed to be increased
     * @return <code>true</code> if quota is exceeded; otherwise <code>false</code>
     * @throws OXException If a database error occurs
     */
    protected boolean incUsage(String id, long usage) throws OXException {
        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=? FOR UPDATE");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, ownerId);
            rs = sstmt.executeQuery();
            final long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong(1);
            } else {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.NO_USAGE_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
            final long newUsage = oldUsage + usage;
            final long quota = this.quota;
            if ((quota == 0) || (quota > 0 && newUsage > quota)) {
                // Advertise exceeded quota to listeners
                for (QuotaFileStorageListener listener : listeners.getServiceList()) {
                    try {
                        listener.onQuotaExceeded(id, usage, oldUsage, quota, ownerId, contextId);
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                }
                return true;
            }

            // Advertise usage increment to listeners
            for (QuotaFileStorageListener listener : listeners.getServiceList()) {
                listener.onUsageIncrement(id, usage, oldUsage, quota, ownerId, contextId);
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=? AND user=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, contextId);
            ustmt.setInt(3, ownerId);
            final int rows = ustmt.executeUpdate();
            if (rows == 0) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.UPDATE_FAILED_USER.create(I(ownerId), I(contextId));
                }
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
     * @param id The identifier of the associated file
     * @param usage by that the Quota has to be decreased
     * @throws OXException
     */
    protected void decUsage(String id, long usage) throws OXException {
        decUsage(Collections.singletonList(id), usage);
    }

    /**
     * Decreases the QuotaUsage.
     *
     * @param ids The identifiers of the associated files
     * @param usage by that the Quota has to be decreased
     * @throws OXException
     */
    protected void decUsage(List<String> ids, long usage) throws OXException {
        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=? FOR UPDATE");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, ownerId);
            rs = sstmt.executeQuery();

            long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong("used");
            } else {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.NO_USAGE_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
            long newUsage = oldUsage - usage;

            if (newUsage < 0) {
                newUsage = 0;
                final OXException e = QuotaFileStorageExceptionCodes.QUOTA_UNDERRUN.create(I(ownerId), I(contextId));
                LOGGER.error("", e);
            }

            // Advertise usage increment to listeners
            for (QuotaFileStorageListener listener : listeners.getServiceList()) {
                try {
                    listener.onUsageDecrement(ids, usage, oldUsage, quota, ownerId, contextId);
                } catch (Exception e) {
                    LOGGER.warn("", e);
                }
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=? AND user=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, contextId);
            ustmt.setInt(3, ownerId);

            int rows = ustmt.executeUpdate();
            if (1 != rows) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.UPDATE_FAILED_USER.create(I(ownerId), I(contextId));
                }
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
        Map<String, Long> fileSizes = new HashMap<String, Long>();
        SortedSet<String> set = new TreeSet<String>();
        for (String identifier : identifiers) {
            boolean deleted;
            try {
                // Get size before attempting delete. File is not found afterwards
                Long size = L(getFileSize(identifier));
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
        long sum = 0L;
        for (Long fileSize : fileSizes.values()) {
            sum += fileSize.longValue();
        }
        decUsage(Arrays.asList(identifiers), sum);
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
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, ownerId);
            result = stmt.executeQuery();
            if (!result.next()) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.NO_USAGE_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }

            usage = result.getLong(1);
        } catch (final SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(result, stmt);
            db.backReadOnly(contextId, con);
        }
        return usage;
    }

    @Override
    public String saveNewFile(InputStream is) throws OXException {
        return saveNewFile(is, -1);
    }

    @Override
    public String saveNewFile(InputStream is, long sizeHint) throws OXException {
        if (0 < sizeHint) {
            checkAvailable(sizeHint);
        }

        String file = null;
        try {
            // Store new file
            file = fileStorage.saveNewFile(is);
            String retval = file;

            // Check against quota limitation
            boolean full = incUsage(file, fileStorage.getFileSize(file));
            if (full) {
                throw QuotaFileStorageExceptionCodes.STORE_FULL.create();
            }

            // Null'ify reference (to avoid preliminary deletion) & return new file identifier
            file = null;
            return retval;
        } finally {
            if (file != null) {
                fileStorage.deleteFile(file);
            }
        }
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
        if (ownerId > 0) {
            LOGGER.info("Recalculating usage for owner {} in context {}", ownerId, contextId);
        } else {
            LOGGER.info("Recalculating usage for context {}", contextId);
        }

        SortedSet<String> filenames = fileStorage.getFileList();
        long entireFileSize = 0;
        for (String filename : filenames) {
            if (!filesToIgnore.contains(filename)) {
                try {
                    entireFileSize += fileStorage.getFileSize(filename);
                } catch (OXException e) {
                    if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                }
            }
        }

        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            stmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=? AND user=?");
            stmt.setLong(1, entireFileSize);
            stmt.setInt(2, contextId);
            stmt.setInt(3, ownerId);
            final int rows = stmt.executeUpdate();
            if (1 != rows) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.UPDATE_FAILED_USER.create(I(ownerId), I(contextId));
                }
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
    public InputStream getFile(String file) throws OXException {
        return fileStorage.getFile(file);
    }

    @Override
    public long getFileSize(String name) throws OXException {
        return fileStorage.getFileSize(name);
    }

    @Override
    public String getMimeType(String name) throws OXException {
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
            if (incUsage(name, newSize - offset)) {
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
                    LOGGER.warn("Error rolling back 'append' operation", e);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + ownerId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DBQuotaFileStorage)) {
            return false;
        }
        DBQuotaFileStorage other = (DBQuotaFileStorage) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (ownerId != other.ownerId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DBQuotaFileStorage [contextId=" + contextId + ", quota=" + quota + ", ownerId=" + ownerId + ", uri=" + uri + "]";
    }

}
