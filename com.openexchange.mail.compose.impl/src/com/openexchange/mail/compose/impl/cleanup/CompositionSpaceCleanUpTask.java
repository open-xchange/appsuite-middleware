/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.AttachmentStorageIdentifier;
import com.openexchange.mail.compose.AttachmentStorageIdentifier.KnownArgument;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
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
public class CompositionSpaceCleanUpTask implements CleanUpExecution {

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
    public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        try {
            return Databases.tableExists(connectionProvider.getConnection(), "compositionSpace");
        } catch (SQLException e) {
            LOG.warn("Unable to look-up \"compositionSpace\" table", e);
        }
        return false;
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        long start = System.currentTimeMillis();

        try {
            cleanUpForSchema(connectionProvider.getConnection(), schema);
        } catch (Exception e) {
            LOG.warn("Failed to clean-up expired composition spaces for schema association with context {}", I(representativeContextId), e);
        }

        long duration = System.currentTimeMillis() - start;
        LOG.info("Composition space clean-up task took {}ms ({})", formatDuration(duration), exactly(duration, true));
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

    private void cleanUpForSchema(Connection connection, String schema) throws OXException {
        Set<UserAndContext> users = determineUsersWithOpenCompositionSpaces(connection);
        if (users.isEmpty()) {
            return;
        }

        for (UserAndContext user : users) {
            try {
                LOG.debug("Going to delete expired composition spaces for user {} in context {}", I(user.getUserId()), I(user.getContextId()));
                cleanUpForUser(user, connection);
            } catch (Exception e) {
                LOG.warn("Failed to delete expired composition spaces for user {} in context {}", I(user.getUserId()), I(user.getContextId()), e);
            }
        }
        LOG.debug("Successfully deleted expired composition spaces for schema {}", schema);
    }

    private void cleanUpForUser(UserAndContext user, Connection connection) throws OXException {
        Set<UUID> expiredCompositionSpaces = determineExpiredCompositionSpacesForUser(user, connection);
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
                deleteExpiredCompositionSpace(compositionSpaceId, user, connection);
                LOG.debug("Successfully deleted expired composition space '{}' of user {} in context {}", UUIDs.getUnformattedStringObjectFor(compositionSpaceId), I(user.getUserId()), I(user.getContextId()));
            } catch (Exception e) {
                LOG.warn("Failed to delete expired composition space '{}' of user {} in context {}", UUIDs.getUnformattedString(compositionSpaceId), I(user.getUserId()), I(user.getContextId()), e);
            }
        }
        LOG.debug("Successfully deleted expired composition spaces for user {} in context {}", I(user.getUserId()), I(user.getContextId()));
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

    private Set<UUID> determineExpiredCompositionSpacesForUser(UserAndContext user, Connection connection) throws OXException {
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
        try {
            stmt = connection.prepareStatement("SELECT uuid FROM compositionSpace WHERE cid=? AND user=? AND lastModified<?");
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
        }
    }

    private Set<UserAndContext> determineUsersWithOpenCompositionSpaces(Connection connection) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT cid, user FROM compositionSpace");
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
