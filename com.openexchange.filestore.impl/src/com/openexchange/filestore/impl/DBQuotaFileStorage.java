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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.Purpose;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageExceptionCodes;
import com.openexchange.filestore.QuotaFileStorageListener;
import com.openexchange.filestore.QuotaLimitService;
import com.openexchange.filestore.QuotaUsageService;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.osgi.ServiceListings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.user.UserService;

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

    private static final String REGION = "SingleUserContext";

    // -------------------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int requestingUserId;
    private final transient FileStorage fileStorage;
    private final long quota;
    private final int ownerId;
    private final URI uri;
    private final ServiceListing<QuotaFileStorageListener> listeners;
    private final ServiceListing<QuotaUsageService> usageServices;
    private final ServiceListing<QuotaLimitService> limitServices;
    private final Purpose purpose;

    /**
     * Initializes a new {@link DBQuotaFileStorage} for an owner.
     *
     * @param contextId The context identifier
     * @param userId The identifier of the user for which the file storage instance was requested; pass <code>0</code> if not requested for a certain user
     * @param ownerId The file storage owner or <code>0</code> (zero); the owner determines to what 'filestore_usage' entry the quota gets
     *            accounted
     * @param quota The assigned quota
     * @param fs The file storage associated with the owner
     * @param uri The URI that fully qualifies this file storage
     * @param nature The storage's nature
     * @param listeners The quota listeners
     * @param usageServices The tracked usage services
     * @param limitServices The tracked limit services
     * @throws OXException If initialization fails
     */
    public DBQuotaFileStorage(int contextId, Info info, int ownerId, long quota, FileStorage fs, URI uri, ServiceListing<QuotaFileStorageListener> listeners, ServiceListing<QuotaUsageService> usageServices, ServiceListing<QuotaLimitService> limitServices) throws OXException {
        super();
        if (fs == null) {
            throw QuotaFileStorageExceptionCodes.INSTANTIATIONERROR.create();
        }

        this.usageServices = null == usageServices ? ServiceListings.<QuotaUsageService> emptyList() : usageServices;
        this.limitServices = null == limitServices ? ServiceListings.<QuotaLimitService> emptyList() : limitServices;
        this.listeners = null == listeners ? ServiceListings.<QuotaFileStorageListener> emptyList() : listeners;

        this.requestingUserId = null == info ? 0 : info.getUserId();
        this.purpose = null == info ? Purpose.ADMINISTRATIVE : info.getPurpose();

        this.uri = uri;
        this.contextId = contextId;
        this.ownerId = ownerId;
        this.quota = quota;
        fileStorage = fs;
    }

    private DatabaseService getDatabaseService() throws OXException {
        return Services.requireService(DatabaseService.class);
    }

    private QuotaUsageService getHighestRankedUsageService(int userId, int contextId) throws OXException  {
        for (QuotaUsageService usageService : usageServices) {
            if (usageService.isApplicableFor(userId, contextId)) {
                return usageService;
            }
        }
        return null;
    }

    private QuotaLimitService getHighestRankedLimitService(int userId, int contextId) throws OXException {
        for (QuotaLimitService limitService : limitServices) {
            if (limitService.isApplicableFor(userId, contextId)) {
                return limitService;
            }
        }
        return null;
    }

    private Cache optCache() {
        try {
            CacheService optService = Services.optService(CacheService.class);
            return null == optService ? null : optService.getCache(REGION);
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(DBQuotaFileStorageService.class);
            logger.warn("Could not return cache instance", e);
            return null;
        }
    }

    private int isSingleUserContext(int contextId) throws OXException {
        Cache cache = optCache();
        if (null == cache) {
            return doIsSingleUserContext(contextId);
        }

        Object object = cache.get(Integer.valueOf(contextId));
        if (!(object instanceof Integer)) {
            synchronized (this) {
                object = cache.get(Integer.valueOf(contextId));
                if (!(object instanceof Integer)) {
                    Integer singleUser = Integer.valueOf(doIsSingleUserContext(contextId));
                    cache.put(Integer.valueOf(contextId), singleUser, false);
                    return singleUser.intValue();
                }
            }
        }
        return ((Integer) object).intValue();
    }

    private int doIsSingleUserContext(int contextId) throws OXException {
        UserService userService = Services.optService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }

        int[] userIds = userService.listAllUser(contextId, false, false);
        return 1 == userIds.length ? userIds[0] : 0;
    }

    private int considerService() throws OXException {
        if (purpose == Purpose.ADMINISTRATIVE) {
            // Do not invoke services in for administrative purpose(s)
            return 0;
        }

        boolean dedicatedStorage = ownerId > 0;
        int singleUser = isSingleUserContext(contextId);
        boolean isSingleUserContext = singleUser > 0;

        // Check for a single-user context
        if (isSingleUserContext) {
            int effectiveUserId = requestingUserId > 0 ? requestingUserId : (singleUser > 0 ? singleUser : 0);
            return effectiveUserId;
        }

        // No single-user context, but maybe a dedicated storage
        if (dedicatedStorage && requestingUserId > 0 && requestingUserId == ownerId) {
            // User-associated storage is valid for Drive or if everything is accounted to the single user
            if (isSingleUserContext || purpose == Purpose.DRIVE) {
                int effectiveUserId = requestingUserId > 0 ? requestingUserId : (singleUser > 0 ? singleUser : 0);
                return effectiveUserId;
            }
        }

        // Do not invoke services as this storage is not adequate for call-backs to those services
        return 0;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public long getQuota() throws OXException {
        if (quota < 0) {
            // Marked as unlimited
            return Long.MAX_VALUE;
        }

        int effectiveUserId = considerService();
        if (effectiveUserId > 0) {
            QuotaLimitService limitService = getHighestRankedLimitService(effectiveUserId, contextId);
            return null == limitService ? quota : limitService.getLimit(effectiveUserId, contextId);
        }

        // Otherwise return given quota limit
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
     * @param required The value by which the quota is supposed to be increased
     * @return <code>true</code> if quota is exceeded; otherwise <code>false</code>
     * @throws OXException If a database error occurs
     */
    protected boolean incUsage(String id, long required) throws OXException {
        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);

        Long toDecrement = null;
        QuotaUsageService usageService = null;
        int effectiveUserId = 0;

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            // Grab the current usage from database
            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=? FOR UPDATE");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, ownerId);
            rs = sstmt.executeQuery();
            if (!rs.next()) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.NO_USAGE_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
            long oldUsageFromDb = rs.getLong(1);
            long newUsageForDb = oldUsageFromDb + required;

            // Check if usage is exceeded regarding effective new usage
            if (quota < 0) {
                // Unlimited quota...
            } else {
                effectiveUserId = considerService();
                usageService = effectiveUserId > 0 ? getHighestRankedUsageService(effectiveUserId, contextId) : null;
                if (usageService != null) {
                    long effectiveOldUsage = usageService.getUsage(effectiveUserId, contextId);
                    long effectiveNewUsage = effectiveOldUsage + required;
                    long effectiveLimit = getQuota();
                    if (checkExceededQuota(id, effectiveLimit, required, effectiveNewUsage, effectiveOldUsage)) {
                        return true;
                    }

                    // Advertise usage increment to listeners
                    for (QuotaFileStorageListener listener : listeners) {
                        listener.onUsageIncrement(id, required, effectiveOldUsage, effectiveLimit, ownerId, contextId);
                    }

                    // Increment usage
                    usageService.incrementUsage(required, effectiveUserId, contextId);
                    toDecrement = Long.valueOf(required);
                } else {
                    long quota = getQuota();
                    if (checkExceededQuota(id, quota, required, newUsageForDb, oldUsageFromDb)) {
                        return true;
                    }

                    // Advertise usage increment to listeners
                    for (QuotaFileStorageListener listener : listeners) {
                        listener.onUsageIncrement(id, required, oldUsageFromDb, quota, ownerId, contextId);
                    }
                }
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=? AND user=?");
            ustmt.setLong(1, newUsageForDb);
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

                if (null != usageService && null != toDecrement) {
                    usageService.decrementUsage(toDecrement.longValue(), effectiveUserId, contextId);
                }
            }
            Databases.autocommit(con);
            Databases.closeSQLStuff(rs);
            Databases.closeSQLStuff(sstmt);
            Databases.closeSQLStuff(ustmt);
            db.backWritable(contextId, con);
        }
        return false;
    }

    private void checkAvailable(String id, long required) throws OXException {
        if (0 < required) {
            long quota = getQuota();
            long oldUsage = getUsage();
            long usage = oldUsage + required;
            if (checkExceededQuota(id, quota, required, usage, oldUsage)) {
                throw QuotaFileStorageExceptionCodes.STORE_FULL.create();
            }
        }
    }

    private boolean checkExceededQuota(String id, long quota, long required, long newUsage, long oldUsage) {
        if ((quota == 0) || (quota > 0 && newUsage > quota)) {
            // Advertise exceeded quota to listeners
            for (QuotaFileStorageListener listener : listeners) {
                try {
                    listener.onQuotaExceeded(id, required, oldUsage, quota, ownerId, contextId);
                } catch (Exception e) {
                    LOGGER.warn("", e);
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkNoQuota(String id) throws OXException {
        long quota = getQuota();
        if (quota == 0) {
            // Advertise no quota to listeners
            for (QuotaFileStorageListener listener : listeners) {
                try {
                    listener.onNoQuotaAvailable(id, ownerId, contextId);
                } catch (Exception e) {
                    LOGGER.warn("", e);
                }
            }
            return true;
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
     * @param released by that the Quota has to be decreased
     * @throws OXException
     */
    protected void decUsage(List<String> ids, long released) throws OXException {
        DatabaseService db = getDatabaseService();
        Connection con = db.getWritable(contextId);

        Long toIncrement = null;
        QuotaUsageService usageService = null;
        int effectiveUserId = 0;

        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            long toReleaseBy = released;

            // Grab current usage from database
            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=? FOR UPDATE");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, ownerId);
            rs = sstmt.executeQuery();
            if (!rs.next()) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.NO_USAGE_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
            long oldUsageFromDb = rs.getLong(1);
            long newUsageForDb = oldUsageFromDb - toReleaseBy;
            if (newUsageForDb < 0) {
                newUsageForDb = 0;
                toReleaseBy = oldUsageFromDb;
                final OXException e = QuotaFileStorageExceptionCodes.QUOTA_UNDERRUN.create(I(ownerId), I(contextId));
                LOGGER.error("", e);
            }

            effectiveUserId = considerService();
            usageService = effectiveUserId > 0 ? getHighestRankedUsageService(effectiveUserId, contextId) : null;
            if (usageService != null) {
                long effectiveOldUsage = usageService.getUsage(effectiveUserId, contextId);
                long effectiveToReleasedBy = toReleaseBy;
                long effectiveNewUsage = effectiveOldUsage - effectiveToReleasedBy;
                if (effectiveNewUsage < 0) {
                    effectiveNewUsage = 0;
                    effectiveToReleasedBy = effectiveOldUsage;
                    final OXException e = QuotaFileStorageExceptionCodes.QUOTA_UNDERRUN.create(I(ownerId), I(contextId));
                    LOGGER.error("", e);
                }

                // Advertise usage decrement to listeners
                for (QuotaFileStorageListener listener : listeners) {
                    try {
                        listener.onUsageDecrement(ids, toReleaseBy, effectiveOldUsage, getQuota(), ownerId, contextId);
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                }

                // Decrement usage
                usageService.decrementUsage(effectiveToReleasedBy, effectiveUserId, contextId);
                toIncrement = Long.valueOf(effectiveToReleasedBy);
            } else {
                // Advertise usage increment to listeners
                for (QuotaFileStorageListener listener : listeners) {
                    try {
                        listener.onUsageDecrement(ids, toReleaseBy, oldUsageFromDb, getQuota(), ownerId, contextId);
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                }
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=? AND user=?");
            ustmt.setLong(1, newUsageForDb);
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

                if (null != usageService && null != toIncrement) {
                    usageService.incrementUsage(toIncrement.longValue(), effectiveUserId, contextId);
                }
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
        int effectiveUserId = considerService();
        QuotaUsageService usageService = effectiveUserId > 0 ? getHighestRankedUsageService(effectiveUserId, contextId) : null;
        if (usageService != null) {
            return usageService.getUsage(effectiveUserId, contextId);
        }

        // Otherwise grab the usage from database
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
        if (checkNoQuota(null)) {
            throw QuotaFileStorageExceptionCodes.STORE_FULL.create();
        }
        if (0 < sizeHint) {
            checkAvailable(null, sizeHint);
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
        if (checkNoQuota(name)) {
            throw QuotaFileStorageExceptionCodes.STORE_FULL.create();
        }
        if (0 < sizeHint) {
            checkAvailable(name, sizeHint);
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
