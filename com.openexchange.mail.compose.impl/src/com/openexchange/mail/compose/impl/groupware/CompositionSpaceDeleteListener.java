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

package com.openexchange.mail.compose.impl.groupware;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCode;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.java.util.Pair;
import com.openexchange.mail.compose.AttachmentStorageIdentifier;
import com.openexchange.mail.compose.AttachmentStorageIdentifier.KnownArgument;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.mail.compose.impl.attachment.filestore.ContextAssociatedFileStorageAttachmentStorage;
import com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage;
import com.openexchange.mail.compose.impl.security.FileStorageCompositionSpaceKeyStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;

/**
 * {@link CompositionSpaceDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CompositionSpaceDeleteListener implements DeleteListener {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceDeleteListener.class);
    }

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CompositionSpaceDeleteListener}.
     */
    public CompositionSpaceDeleteListener(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            deleteCompositionSpacesFromUser(event.getId(), event.getContext().getContextId(), writeCon);
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            Set<Integer> dedicatedFileStorageIds = deleteCompositionSpacesFromContext(event.getContext().getContextId(), writeCon);
            deleteDedicatedFileStorage(event.getContext().getContextId(), dedicatedFileStorageIds);
        }
    }

    private void deleteDedicatedFileStorage(int contextId, Set<Integer> dedicatedFileStorageIds) {
        for (Integer dedicatedFileStorageId : dedicatedFileStorageIds) {
            Pair<FileStorage, URI> fsAndUri = getFileStorage(dedicatedFileStorageId.intValue(), contextId);
            if (fsAndUri != null) {
                URI uri = fsAndUri.getSecond();
                if ("file".equals(uri.getScheme())) {
                    // Don't delete since a single static prefix is used
                } else {
                    FileStorage fileStorage = fsAndUri.getFirst();
                    try {
                        fileStorage.remove();
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete the filestore {}", uri, e);
                    }
                }
            }
        }
    }

    private Set<Integer> deleteCompositionSpacesFromContext(int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentBinary WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            {
                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceAttachmentMeta WHERE cid=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, KnownAttachmentStorageType.CONTEXT_ASSOCIATED_FILE_STORAGE.getType());
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
                                LoggerHolder.LOG.warn("Failed to delete the following files on context-associated filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LoggerHolder.LOG.warn("Failed to delete files on context-associated filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            Set<Integer> dedicatedFileStorageIds = null;
            {
                stmt = con.prepareStatement("SELECT refId, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE cid=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, KnownAttachmentStorageType.DEDICATED_FILE_STORAGE.getType());
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
                        if (dedicatedFileStorageIds == null) {
                            dedicatedFileStorageIds = new LinkedHashSet<>();
                        }
                        dedicatedFileStorageIds.add(I(fileStorageId));

                        Pair<FileStorage, URI> fsAndUri = getFileStorage(fileStorageId, contextId);
                        if (fsAndUri != null) {
                            try {
                                FileStorage fileStorage = fsAndUri.getFirst();
                                Set<String> undeletedFiles = fileStorage.deleteFiles(new String[] { storageIdentifier.getIdentifier() });
                                if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                                    LoggerHolder.LOG.warn("Failed to delete the following files on filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                                }
                            } catch (Exception e) {
                                LoggerHolder.LOG.warn("Failed to delete files on filestore {}", fsAndUri.getSecond(), e);
                            }
                        }
                    }
                    storageIdentifers = null;
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = con.prepareStatement("DELETE FROM compositionSpace WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            {
                FileStorageCompositionSpaceKeyStorage keyStorage = FileStorageCompositionSpaceKeyStorage.getInstance();
                if (null != keyStorage) {
                    keyStorage.clearCache();
                }

                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceKeyStorage WHERE cid=? AND dedicatedFileStorageId=0");
                stmt.setInt(1, contextId);
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
                                LoggerHolder.LOG.warn("Failed to delete the following files on context-associated filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LoggerHolder.LOG.warn("Failed to delete files on context-associated filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            {
                stmt = con.prepareStatement("SELECT refId, dedicatedFileStorageId FROM compositionSpaceKeyStorage WHERE cid=? AND dedicatedFileStorageId > 0");
                stmt.setInt(1, contextId);
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
                        if (dedicatedFileStorageIds == null) {
                            dedicatedFileStorageIds = new LinkedHashSet<>();
                        }
                        dedicatedFileStorageIds.add(I(fileStorageId));

                        Pair<FileStorage, URI> fsAndUri = getFileStorage(fileStorageId, contextId);
                        try {
                            FileStorage fileStorage = fsAndUri.getFirst();
                            Set<String> undeletedFiles = fileStorage.deleteFiles(new String[] { storageIdentifier.getIdentifier() });
                            if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                                LoggerHolder.LOG.warn("Failed to delete the following files on filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LoggerHolder.LOG.warn("Failed to delete files on filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            return dedicatedFileStorageIds == null ? Collections.emptySet() : dedicatedFileStorageIds;
        } catch (SQLException e) {
            throw DeleteFailedExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void deleteCompositionSpacesFromUser(int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentBinary WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            {
                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceAttachmentMeta WHERE cid=? AND user=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
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
                                LoggerHolder.LOG.warn("Failed to delete the following files on context-associated filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LoggerHolder.LOG.warn("Failed to delete files on context-associated filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            {
                stmt = con.prepareStatement("SELECT refId, dedicatedFileStorageId FROM compositionSpaceAttachmentMeta WHERE cid=? AND user=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
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
                                    LoggerHolder.LOG.warn("Failed to delete the following files on filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                                }
                            } catch (Exception e) {
                                LoggerHolder.LOG.warn("Failed to delete files on filestore {}", fsAndUri.getSecond(), e);
                            }
                        }
                    }
                    storageIdentifers = null;
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = con.prepareStatement("DELETE FROM compositionSpace WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            {
                FileStorageCompositionSpaceKeyStorage keyStorage = FileStorageCompositionSpaceKeyStorage.getInstance();
                if (null != keyStorage) {
                    keyStorage.clearCache();
                }

                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceKeyStorage WHERE cid=? AND user=? AND dedicatedFileStorageId=0");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
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
                                LoggerHolder.LOG.warn("Failed to delete the following files on context-associated filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LoggerHolder.LOG.warn("Failed to delete files on context-associated filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            {
                stmt = con.prepareStatement("SELECT refId, dedicatedFileStorageId FROM compositionSpaceKeyStorage WHERE cid=? AND user=? AND dedicatedFileStorageId>0");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
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
                                LoggerHolder.LOG.warn("Failed to delete the following files on filestore {}: {}", fsAndUri.getSecond(), undeletedFiles);
                            }
                        } catch (Exception e) {
                            LoggerHolder.LOG.warn("Failed to delete files on filestore {}", fsAndUri.getSecond(), e);
                        }
                    }
                    storageIdentifers = null;
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;
        } catch (SQLException e) {
            throw DeleteFailedExceptionCode.SQL_ERROR.create(e, e.getMessage());
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

}
