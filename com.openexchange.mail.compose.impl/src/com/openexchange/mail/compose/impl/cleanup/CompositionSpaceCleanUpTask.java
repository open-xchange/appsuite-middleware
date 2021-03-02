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

package com.openexchange.mail.compose.impl.cleanup;

import static com.eaio.util.text.HumanTime.exactly;
import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.Databases;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.AttachmentStorageIdentifier;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.mail.compose.AttachmentStorageIdentifier.KnownArgument;
import com.openexchange.mail.compose.impl.attachment.filestore.ContextAssociatedFileStorageAttachmentStorage;
import com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage;
import com.openexchange.mail.compose.impl.security.FileStorageCompositionSpaceKeyStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.UserAndContext;
import com.openexchange.user.UserService;

/**
 * {@link CompositionSpaceCleanUpTask} - A global task responsible for deleting expired composition spaces.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class CompositionSpaceCleanUpTask implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceCleanUpTask.class);

    /** The decimal format to use when printing milliseconds */
    private static final NumberFormat MILLIS_FORMAT = newNumberFormat();

    /** The accompanying lock for shared decimal format */
    private static final Lock MILLIS_FORMAT_LOCK = new ReentrantLock();

    /**
     * Creates a new {@code DecimalFormat} instance.
     *
     * @return The format instance
     */
    private static NumberFormat newNumberFormat() {
        NumberFormat f = NumberFormat.getInstance(Locale.US);
        if (f instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) f;
            df.applyPattern("#,##0");
        }
        return f;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CompositionSpaceCleanUpTask}.
     *
     * @param services The service look-up
     */
    public CompositionSpaceCleanUpTask(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        String prevName = currentThread.getName();
        currentThread.setName("CompositionSpaceCleanUpTask");
        try {
            long start = System.currentTimeMillis();
            ContextService contextService = services.getServiceSafe(ContextService.class);
            Updater updater = Updater.getInstance();
            NextSchema: for (Integer representativeContextId : contextService.getDistinctContextsPerSchema()) {
                UpdateStatus status = updater.getStatus(representativeContextId.intValue());
                if (status.blockingUpdatesRunning()) {
                    // Context-associated schema is currently updated. Abort clean-up for that schema
                    Optional<String> optSchema = getSchema(representativeContextId, contextService);
                    if (optSchema.isPresent()) {
                        LOG.info("Update running: Skipping clean-up of expired composition spaces for schema {} since that schema is currently updated", optSchema.get());
                    } else {
                        LOG.info("Update running: Skipping clean-up of expired composition spaces for schema association with context {} since that schema is currently updated", representativeContextId);
                    }
                    continue NextSchema;
                }
                if ((status.needsBlockingUpdates() || status.needsBackgroundUpdates()) && !status.blockingUpdatesRunning() && !status.backgroundUpdatesRunning()) {
                    // Context-associated schema needs an update. Abort clean-up for that schema
                    Optional<String> optSchema = getSchema(representativeContextId, contextService);
                    if (optSchema.isPresent()) {
                        LOG.info("Update needed: Skipping clean-up of expired composition spaces for schema {} since that schema needs an update", optSchema.get());
                    } else {
                        LOG.info("Update needed: Skipping clean-up of expired composition spaces for schema association with context {} since that schema needs an update", representativeContextId);
                    }
                    continue NextSchema;
                }

                // No update running or pending. Continue clean-up for that schema...
                DatabaseAccess databaseAccess = DatabaseAccessFactory.getInstance().createDatabaseAccessFor(representativeContextId.intValue(), services);
                Optional<String> optSchema = databaseAccess.getSchema();
                if (optSchema.isPresent()) {
                    LOG.debug("Going to delete expired composition spaces for schema {}", optSchema.get());
                } else {
                    LOG.debug("Going to delete expired composition spaces for schema association with context {}", representativeContextId);
                }
                try {
                    cleanUpForSchema(databaseAccess, optSchema);
                } catch (Exception e) {
                    if (optSchema.isPresent()) {
                        LOG.warn("Failed to delete expired composition spaces for schema {}", optSchema.get(), e);
                    } else {
                        LOG.warn("Failed to delete expired composition spaces for schema association with context {}", representativeContextId, e);
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            LOG.info("Composition space clean-up task took {}ms ({})", formatDuration(duration), exactly(duration, true));
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.warn("Failed to delete expired composition", t);
        } finally {
            currentThread.setName(prevName);
        }
    }

    private Optional<String> getSchema(Integer representativeContextId, ContextService contextService) {
        try {
            Map<PoolAndSchema, List<Integer>> associations = contextService.getSchemaAssociationsFor(Collections.singletonList(representativeContextId));
            return Optional.of(associations.keySet().iterator().next().getSchema());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String formatDuration(long duration) {
        if (MILLIS_FORMAT_LOCK.tryLock()) {
            try {
                return MILLIS_FORMAT.format(duration);
            } finally {
                MILLIS_FORMAT_LOCK.unlock();
            }
        }

        // Use thread-specific DecimalFormat instance
        NumberFormat format = newNumberFormat();
        return format.format(duration);
    }

    private void cleanUpForSchema(DatabaseAccess databaseAccess, Optional<String> optSchema) throws OXException {
        Set<UserAndContext> users = determineUsersWithOpenCompositionSpaces(databaseAccess);
        if (users.isEmpty()) {
            return;
        }

        boolean acquired = acquireCleanUpTaskLockForSchema(databaseAccess);
        if (acquired == false) {
            if (optSchema.isPresent()) {
                LOG.debug("Another process currently deletes expired composition spaces for schemas {}", optSchema.get());
            } else {
                LOG.debug("Another process currently deletes expired composition spaces for schema association with context {}", I(databaseAccess.getRepresentativeContextId()));
            }
            return;
        }

        // Lock acquired
        try {
            for (UserAndContext user : users) {
                try {
                    LOG.debug("Going to delete expired composition spaces for user {} in context {}", I(user.getUserId()), I(user.getContextId()));
                    cleanUpForUser(user, databaseAccess);
                } catch (Exception e) {
                    LOG.warn("Failed to delete expired composition spaces for user {} in context {}", I(user.getUserId()), I(user.getContextId()), e);
                }
            }
        } finally {
            releaseCleanUpTaskLockForSchema(databaseAccess);
        }

        if (optSchema.isPresent()) {
            LOG.debug("Successfully deleted expired composition spaces for schema {}", optSchema.get());
        } else {
            LOG.debug("Successfully deleted expired composition spaces for schema association with context {}", I(databaseAccess.getRepresentativeContextId()));
        }
    }

    private void cleanUpForUser(UserAndContext user, DatabaseAccess databaseAccess) throws OXException {
        Set<UUID> expiredCompositionSpaces = determineExpiredCompositionSpacesForUser(user, databaseAccess);
        if (expiredCompositionSpaces == null) {
            LOG.debug("Aborting deletion of expired composition spaces for user {} in context {} since either user or context has been dropepd meanwhile", I(user.getUserId()), I(user.getContextId()));
            return;
        }

        int numberOfExpiredSpaces = expiredCompositionSpaces.size();
        if (numberOfExpiredSpaces <= 0) {
            LOG.debug("Detected no expired composition spaces for user {} in context {}", I(user.getUserId()), I(user.getContextId()));
            return;
        }

        LOG.debug("Detected {} expired composition space(s) for user {} in context {}", I(numberOfExpiredSpaces), I(user.getUserId()), I(user.getContextId()));
        for (UUID compositionSpaceId : expiredCompositionSpaces) {
            try {
                deleteExpiredCompositionSpace(compositionSpaceId, user, databaseAccess);
                LOG.debug("Successfully deleted expired composition space '{}' of user {} in context {}", UUIDs.getUnformattedString(compositionSpaceId), I(user.getUserId()), I(user.getContextId()));
            } catch (Exception e) {
                LOG.warn("Failed to delete expired composition space '{}' of user {} in context {}", UUIDs.getUnformattedString(compositionSpaceId), I(user.getUserId()), I(user.getContextId()), e);
            }
        }
        LOG.debug("Successfully deleted expired composition spaces for user {} in context {}", I(user.getUserId()), I(user.getContextId()));
    }

    private void deleteExpiredCompositionSpace(UUID compositionSpaceId, UserAndContext user, DatabaseAccess databaseAccess) throws OXException {
        boolean modified = false;
        int rollback = 0;
        Connection writeCon = databaseAccess.acquireWritable();
        try {
            Databases.startTransaction(writeCon);
            rollback = 1;

            modified = deleteExpiredCompositionSpace(compositionSpaceId, user, writeCon);

            writeCon.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(writeCon);
                }
                Databases.autocommit(writeCon);
            }
            databaseAccess.releaseWritable(writeCon, modified == false);
        }
    }

    private boolean deleteExpiredCompositionSpace(UUID compositionSpaceId, UserAndContext user, Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            byte[] compositionSpaceIdAsByteArray = UUIDs.toByteArray(compositionSpaceId);
            int contextId = user.getContextId();
            boolean modified = false;

            stmt = writeCon.prepareStatement("DELETE FROM compositionSpaceAttachmentBinary WHERE uuid=?");
            stmt.setBytes(1, compositionSpaceIdAsByteArray);
            modified = stmt.executeUpdate() > 0;
            Databases.closeSQLStuff(stmt);
            stmt = null;

            {
                stmt = writeCon.prepareStatement("SELECT refId FROM compositionSpaceAttachmentMeta WHERE cid =? AND csid=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setBytes(2, compositionSpaceIdAsByteArray);
                stmt.setInt(3, KnownAttachmentStorageType.CONTEXT_ASSOCIATED_FILE_STORAGE.getType());
                rs = stmt.executeQuery();
                Set<String> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<String>();
                    do {
                        storageIdentifers.add(rs.getString(1));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    Pair<FileStorage, URI> fsAndUri = ContextAssociatedFileStorageAttachmentStorage.optFileStorage(contextId);
                    if (null != fsAndUri) {
                        FileStorage fileStorage = fsAndUri.getFirst();
                        try {
                            Set<String> undeletedFiles = fileStorage.deleteFiles(storageIdentifers.toArray(new String[storageIdentifers.size()]));
                            if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                                LOG.warn("Failed to delete the following files on context-associated filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to delete files on context-associated filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            {
                stmt = writeCon.prepareStatement("SELECT refId, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE cid =? AND csid=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setBytes(2, compositionSpaceIdAsByteArray);
                stmt.setInt(3, KnownAttachmentStorageType.DEDICATED_FILE_STORAGE.getType());
                rs = stmt.executeQuery();
                Set<AttachmentStorageIdentifier> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<AttachmentStorageIdentifier>();
                    do {
                        storageIdentifers.add(storageIdentifierFrom(rs, false));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    for (AttachmentStorageIdentifier storageIdentifier : storageIdentifers) {
                        Optional<Integer> dedicatedFileStorageId = storageIdentifier.getArgument(KnownArgument.FILE_STORAGE_IDENTIFIER);
                        int fileStorageId = dedicatedFileStorageId.get().intValue();

                        Pair<FileStorage, URI> fsAndUri = getFileStorage(fileStorageId, contextId);
                        if (fsAndUri != null) {
                            try {
                                FileStorage fileStorage = fsAndUri.getFirst();
                                Set<String> undeletedFiles = fileStorage.deleteFiles(new String[] { storageIdentifier.getIdentifier() });
                                if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                                    LOG.warn("Failed to delete the following files on filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                                }
                            } catch (Exception e) {
                                LOG.warn("Failed to delete files on filestore {}", fsAndUri.getSecond(), e);
                            }
                        }
                    }
                    storageIdentifers = null;
                }
            }

            stmt = writeCon.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE cid=? AND csid=?");
            stmt.setInt(1, contextId);
            stmt.setBytes(2, compositionSpaceIdAsByteArray);
            modified = stmt.executeUpdate() > 0;
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = writeCon.prepareStatement("DELETE FROM compositionSpace WHERE uuid=?");
            stmt.setBytes(1, compositionSpaceIdAsByteArray);
            modified = stmt.executeUpdate() > 0;
            Databases.closeSQLStuff(stmt);
            stmt = null;

            {
                FileStorageCompositionSpaceKeyStorage keyStorage = FileStorageCompositionSpaceKeyStorage.getInstance();
                if (null != keyStorage) {
                    keyStorage.clearCache();
                }

                stmt = writeCon.prepareStatement("SELECT refId FROM compositionSpaceKeyStorage WHERE uuid=? AND dedicatedFileStorageId=0");
                stmt.setBytes(1, compositionSpaceIdAsByteArray);
                rs = stmt.executeQuery();
                Set<String> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<String>();
                    do {
                        storageIdentifers.add(unobfuscate(rs.getString(1)));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    Pair<FileStorage, URI> fsAndUri = ContextAssociatedFileStorageAttachmentStorage.optFileStorage(contextId);
                    if (null != fsAndUri) {
                        FileStorage fileStorage = fsAndUri.getFirst();
                        try {
                            Set<String> undeletedFiles = fileStorage.deleteFiles(storageIdentifers.toArray(new String[storageIdentifers.size()]));
                            if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                                LOG.warn("Failed to delete the following files on context-associated filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to delete files on context-associated filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            {
                stmt = writeCon.prepareStatement("SELECT refId, dedicatedFileStorageId FROM compositionSpaceKeyStorage WHERE uuid=? AND dedicatedFileStorageId>0");
                stmt.setBytes(1, compositionSpaceIdAsByteArray);
                rs = stmt.executeQuery();
                Set<AttachmentStorageIdentifier> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<AttachmentStorageIdentifier>();
                    do {
                        storageIdentifers.add(storageIdentifierFrom(rs, true));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    for (AttachmentStorageIdentifier storageIdentifier : storageIdentifers) {
                        Optional<Integer> dedicatedFileStorageId = storageIdentifier.getArgument(KnownArgument.FILE_STORAGE_IDENTIFIER);
                        int fileStorageId = dedicatedFileStorageId.get().intValue();

                        Pair<FileStorage, URI> fsAndUri = getFileStorage(fileStorageId, contextId);
                        try {
                            FileStorage fileStorage = fsAndUri.getFirst();
                            Set<String> undeletedFiles = fileStorage.deleteFiles(new String[] { storageIdentifier.getIdentifier() });
                            if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                                LOG.warn("Failed to delete the following files on filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to delete files on filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            stmt = writeCon.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE uuid=?");
            stmt.setBytes(1, compositionSpaceIdAsByteArray);
            modified = stmt.executeUpdate() > 0;
            Databases.closeSQLStuff(stmt);
            stmt = null;

            return modified;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Un-Obfuscates given string.
     *
     * @param s The obfuscated string
     * @return The plain string
     * @throws OXException If service is missing
     */
    private String unobfuscate(String s) throws OXException {
        ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.absentService(ObfuscatorService.class);
        }
        return obfuscatorService.unobfuscate(s);
    }

    private static Pair<FileStorage, URI> getFileStorage(int dedicatedFileStorageId, int contextId) {
        try {
            return DedicatedFileStorageAttachmentStorage.getDedicatedFileStorage(dedicatedFileStorageId, contextId);
        } catch (Exception e) {
            return null;
        }
    }

    private AttachmentStorageIdentifier storageIdentifierFrom(ResultSet rs, boolean unobfuscate) throws SQLException, OXException {
        String storageIdentifier = unobfuscate ? unobfuscate(rs.getString("refId")) : rs.getString("refId");
        int dedicatedFileStorageId = rs.getInt("dedicatedFileStorageId");
        if (dedicatedFileStorageId <= 0) {
            return new AttachmentStorageIdentifier(storageIdentifier);
        }
        return new AttachmentStorageIdentifier(storageIdentifier, KnownArgument.FILE_STORAGE_IDENTIFIER, I(dedicatedFileStorageId));
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private Set<UUID> determineExpiredCompositionSpacesForUser(UserAndContext user, DatabaseAccess databaseAccess) throws OXException {
        ContextService contextService = services.getOptionalService(ContextService.class);
        if (contextService != null && contextService.exists(user.getContextId()) == false) {
            // Context no more existent
            return null;
        }

        UserService userService = services.getOptionalService(UserService.class);
        if (userService != null && userService.exists(user.getUserId(), user.getContextId()) == false) {
            // User no more existent
            return null;
        }

        long maxIdleTimeMillis = getMaxIdleTimeMillis(user.getUserId(), user.getContextId());
        if (maxIdleTimeMillis <= 0) {
            return Collections.emptySet();
        }

        long maxLastModifiedStamp = System.currentTimeMillis() - maxIdleTimeMillis;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = databaseAccess.acquireReadOnly();
        try {
            stmt = con.prepareStatement("SELECT uuid FROM compositionSpace WHERE cid=? AND user=? AND lastModified<?");
            stmt.setInt(1, user.getContextId());
            stmt.setInt(2, user.getUserId());
            stmt.setLong(3, maxLastModifiedStamp);
            rs = stmt.executeQuery();
            if (rs.next() == false) {
                return Collections.emptySet();
            }

            Set<UUID> expiredOnes = new HashSet<>();
            do {
                expiredOnes.add(UUIDs.toUUID(rs.getBytes(1)));
            } while (rs.next());
            return expiredOnes;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseAccess.releaseReadOnly(con);
        }
    }

    private Set<UserAndContext> determineUsersWithOpenCompositionSpaces(DatabaseAccess databaseAccess) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = databaseAccess.acquireReadOnly();
        try {
            stmt = con.prepareStatement("SELECT cid, user FROM compositionSpace");
            rs = stmt.executeQuery();
            if (rs.next() == false) {
                return Collections.emptySet();
            }

            Set<UserAndContext> users = new HashSet<>();
            do {
                users.add(UserAndContext.newInstance(rs.getInt(2), rs.getInt(1)));
            } while (rs.next());
            return users;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseAccess.releaseReadOnly(con);
        }
    }

    private static final byte[] LOCK_UUID_BYTES = UUIDs.toByteArray(UUIDs.fromUnformattedString("753f4fe1b7b24f39bcda244fac53060f"));

    private boolean acquireCleanUpTaskLockForSchema(DatabaseAccess databaseAccess) throws OXException {
        boolean modified = false;
        PreparedStatement stmt = null;
        Connection writeCon = databaseAccess.acquireWritable();
        try {
            stmt = writeCon.prepareStatement("INSERT INTO compositionSpace (uuid, cid, user, lastModified) VALUES (?, ?, ?, ?)");
            stmt.setBytes(1, LOCK_UUID_BYTES);
            stmt.setInt(2, 0);
            stmt.setInt(3, 1);
            stmt.setLong(4, 0L);
            try {
                modified = stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    return false;
                }
                throw e;
            }
            return true;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            databaseAccess.releaseWritable(writeCon, modified == false);
        }
    }

    private boolean releaseCleanUpTaskLockForSchema(DatabaseAccess databaseAccess) throws OXException {
        boolean modified = false;
        PreparedStatement stmt = null;
        Connection writeCon = databaseAccess.acquireWritable();
        try {
            stmt = writeCon.prepareStatement("DELETE FROM compositionSpace WHERE uuid=?");
            stmt.setBytes(1, LOCK_UUID_BYTES);
            modified = stmt.executeUpdate() > 0;
            return modified;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            databaseAccess.releaseWritable(writeCon, modified == false);
        }
    }

    private static final String PROP_MAX_IDLE_TIME_MILLIS = "com.openexchange.mail.compose.maxIdleTimeMillis";

    private long getMaxIdleTimeMillis(int userId, int contextId) throws OXException {
        String defaultValue = "1W";

        ConfigViewFactory viewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return ConfigTools.parseTimespan(defaultValue);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        return ConfigTools.parseTimespan(ConfigViews.getDefinedStringPropertyFrom(PROP_MAX_IDLE_TIME_MILLIS, defaultValue, view));
    }

}
