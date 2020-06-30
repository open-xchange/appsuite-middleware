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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.impl.rmi;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage.getDedicatedFileStorage;
import java.net.URI;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.java.util.Pair;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.mail.compose.rmi.RemoteCompositionSpaceService;
import com.openexchange.mail.compose.rmi.RemoteCompositionSpaceServiceException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;

/**
 * {@link RemoteCompositionSpaceServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class RemoteCompositionSpaceServiceImpl implements RemoteCompositionSpaceService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RemoteCompositionSpaceServiceImpl.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link RemoteCompositionSpaceServiceImpl}.
     */
    public RemoteCompositionSpaceServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void deleteOrphanedReferences(List<Integer> fileStorageIds) throws RemoteCompositionSpaceServiceException, RemoteException {
        try {
            DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
            ContextService contextService = services.getServiceSafe(ContextService.class);

            List<Integer> distinctContextsPerSchema = contextService.getDistinctContextsPerSchema();

            for (Integer fileStorageId : fileStorageIds) {
                deleteOrphanedReferences(fileStorageId.intValue(), distinctContextsPerSchema, databaseService);
            }
        } catch (OXException e) {
            throw convertException(e);
        } catch (RuntimeException e) {
            throw convertException(e);
        }
    }

    private RemoteCompositionSpaceServiceException convertException(Exception e) {
        LOGGER.error("Error during {} invocation", RemoteCompositionSpaceService.class.getSimpleName(), e);
        RemoteCompositionSpaceServiceException cme = new RemoteCompositionSpaceServiceException(e.getMessage());
        cme.setStackTrace(e.getStackTrace());
        return cme;
    }

    private void deleteOrphanedReferences(int fileStorageId, List<Integer> distinctContextsPerSchema, DatabaseService databaseService) throws  OXException {
        for (Integer representativeContextId : distinctContextsPerSchema) {
            deleteOrphanedReferencesForSchema(fileStorageId, representativeContextId.intValue(), databaseService);
        }
    }

    private void deleteOrphanedReferencesForSchema(int fileStorageId, int representativeContextId, DatabaseService databaseService) throws OXException {
        Connection writeCon = null;
        Connection readCon = databaseService.getReadOnly(representativeContextId);
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                if (!columnExists(readCon, "compositionSpaceAttachmentMeta", "dedicatedFileStorageId") || !columnExists(readCon, "compositionSpaceKeyStorage", "dedicatedFileStorageId")) {
                    return;
                }

                Set<Integer> consideredContextIds = new HashSet<Integer>();
                Map<Integer, Set<String>> attachmentIdentifiers;
                {
                    stmt = readCon.prepareStatement("SELECT cid, refId FROM compositionSpaceAttachmentMeta WHERE dedicatedFileStorageId=? AND refType=" + KnownAttachmentStorageType.DEDICATED_FILE_STORAGE.getType());
                    stmt.setInt(1, fileStorageId);
                    rs = stmt.executeQuery();
                    attachmentIdentifiers = new HashMap<>();
                    while (rs.next()) {
                        Integer contextId = I(rs.getInt(1));
                        consideredContextIds.add(contextId);
                        Set<String> storageIdentifiers = attachmentIdentifiers.get(contextId);
                        if (storageIdentifiers == null) {
                            storageIdentifiers = new HashSet<String>();
                            attachmentIdentifiers.put(contextId, storageIdentifiers);
                        }
                        storageIdentifiers.add(rs.getString(2));
                    }
                    Databases.closeSQLStuff(rs, stmt);
                    stmt = null;
                    rs = null;
                }

                Map<Integer, Set<String>> keyIdentifiers;
                {
                    stmt = readCon.prepareStatement("SELECT cid, refId FROM compositionSpaceKeyStorage WHERE dedicatedFileStorageId=?");
                    stmt.setInt(1, fileStorageId);
                    rs = stmt.executeQuery();
                    keyIdentifiers = new HashMap<>();
                    while (rs.next()) {
                        Integer contextId = I(rs.getInt(1));
                        consideredContextIds.add(contextId);
                        Set<String> storageIdentifiers = keyIdentifiers.get(contextId);
                        if (storageIdentifiers == null) {
                            storageIdentifiers = new HashSet<String>();
                            keyIdentifiers.put(contextId, storageIdentifiers);
                        }
                        storageIdentifiers.add(unobfuscate(rs.getString(2)));
                    }
                    Databases.closeSQLStuff(rs, stmt);
                    stmt = null;
                    rs = null;
                }

                // Release read-only database connection since no more needed
                databaseService.backReadOnly(representativeContextId, readCon);
                readCon = null;

                // Create a map to collect for a certain (dedicated) file storage all contexts that use that storage
                Map<URI, FileStorageAndContexts> fileStorage2Contexts = new HashMap<>();
                for (Integer contextId : consideredContextIds) {
                    Pair<FileStorage, URI> fileStorageAndUri = getDedicatedFileStorage(fileStorageId, contextId.intValue());
                    URI fileStorageUri = fileStorageAndUri.getSecond();

                    FileStorageAndContexts fileStorageAndContexts = fileStorage2Contexts.get(fileStorageUri);
                    if (fileStorageAndContexts == null) {
                        // No such file storage associated with determined URI, yet
                        FileStorage fileStorage = fileStorageAndUri.getFirst();
                        fileStorageAndContexts = new FileStorageAndContexts(fileStorage, contextId);
                        fileStorage2Contexts.put(fileStorageUri, fileStorageAndContexts);
                    } else {
                        // File storage already used by another context. So just add current context
                        fileStorageAndContexts.addContextId(contextId);
                    }
                }

                for (FileStorageAndContexts fileStorageAndContexts : fileStorage2Contexts.values()) {
                    FileStorage fileStorage = fileStorageAndContexts.fileStorage;

                    SortedSet<String> fileList = fileStorage.getFileList();

                    {
                        Set<String> nonReferenced = new HashSet<String>(fileList);
                        for (Integer contextId : fileStorageAndContexts.contextIds) {
                            {
                                Set<String> storageIdentifiers = attachmentIdentifiers.get(contextId);
                                if (storageIdentifiers != null) {
                                    nonReferenced.removeAll(storageIdentifiers);
                                }
                            }
                            {
                                Set<String> storageIdentifiers = keyIdentifiers.get(contextId);
                                if (storageIdentifiers != null) {
                                    nonReferenced.removeAll(storageIdentifiers);
                                }
                            }
                        }

                        if (!nonReferenced.isEmpty()) {
                            fileStorage.deleteFiles(nonReferenced.toArray(new String[nonReferenced.size()]));
                        }
                    }

                    {
                        Set<String> nonExisting = new HashSet<String>();
                        for (Integer contextId : fileStorageAndContexts.contextIds) {
                            {
                                Set<String> storageIdentifiers = attachmentIdentifiers.get(contextId);
                                if (storageIdentifiers != null) {
                                    nonExisting.addAll(storageIdentifiers);
                                }
                            }
                            {
                                Set<String> storageIdentifiers = keyIdentifiers.get(contextId);
                                if (storageIdentifiers != null) {
                                    nonExisting.addAll(storageIdentifiers);
                                }
                            }
                        }
                        nonExisting.removeAll(fileList);

                        if (!nonExisting.isEmpty()) {
                            if (writeCon == null) {
                                writeCon = databaseService.getWritable(representativeContextId);
                            }

                            for (String nonExistingStorageIdentifier : nonExisting) {
                                stmt = writeCon.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE dedicatedFileStorageId=? AND refType=" + KnownAttachmentStorageType.DEDICATED_FILE_STORAGE.getType() + " AND refId=?");
                                stmt.setInt(1, fileStorageId);
                                stmt.setString(2, nonExistingStorageIdentifier);
                                stmt.executeUpdate();
                                Databases.closeSQLStuff(stmt);
                                stmt = null;

                                stmt = writeCon.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE dedicatedFileStorageId=? AND refId=?");
                                stmt.setInt(1, fileStorageId);
                                stmt.setString(2, obfuscate(nonExistingStorageIdentifier));
                                stmt.executeUpdate();
                                Databases.closeSQLStuff(stmt);
                                stmt = null;
                            }
                        }
                    }
                }
            } catch (SQLSyntaxErrorException e) {
                // Assume that column 'dedicatedFileStorageId' does not exist in context-associated schema. Therefore ignore.
            } catch (SQLException e) {
                throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        } finally {
            if (readCon != null) {
                databaseService.backReadOnly(representativeContextId, readCon);
            }
            if (writeCon != null) {
                databaseService.backWritable(representativeContextId, writeCon);
            }
        }
    }

    /**
     * Obfuscates given string.
     *
     * @param s The string
     * @return The obfuscated string
     * @throws OXException If service is missing
     */
    private String obfuscate(String s) throws OXException {
        ObfuscatorService obfuscatorService = services.getOptionalService(ObfuscatorService.class);
        if (null == obfuscatorService) {
            throw ServiceExceptionCode.absentService(ObfuscatorService.class);
        }
        return obfuscatorService.obfuscate(s);
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

    /**
     * Checks if specified column exists.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @return <code>true</code> if specified column exists; otherwise <code>false</code>
     * @throws SQLException If an SQL error occurs
     */
    private boolean columnExists(Connection con, String table, String column) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equals(table));
            if (!retval) {
                return false;
            }
            closeSQLStuff(rs);
            rs = null;

            retval = false;
            rs = metaData.getColumns(null, null, table, column);
            while (rs.next()) {
                retval = rs.getString(4).equalsIgnoreCase(column);
            }
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

    private static class FileStorageAndContexts {

        final FileStorage fileStorage;
        final List<Integer> contextIds;

        FileStorageAndContexts(FileStorage fileStorage, Integer initialContextId) {
            super();
            this.fileStorage = fileStorage;
            this.contextIds = new ArrayList<>(2);
            contextIds.add(initialContextId);
        }

        void addContextId(Integer contextId) {
            contextIds.add(contextId);
        }
    }

}
