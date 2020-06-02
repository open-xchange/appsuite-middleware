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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.SchemaInfo;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.KnownAttachmentStorageType;
import com.openexchange.mail.compose.rmi.RemoteCompositionSpaceService;
import com.openexchange.mail.compose.rmi.RemoteCompositionSpaceServiceException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

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
        String exceptionId = UUIDs.getUnformattedStringFromRandom();
        try {
            DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
            ContextService contextService = services.getServiceSafe(ContextService.class);
            ThreadPoolService threadPool = services.getServiceSafe(ThreadPoolService.class);

            List<Integer> distinctContextsPerSchema = contextService.getDistinctContextsPerSchema();

            for (Integer fileStorageId : fileStorageIds) {
                deleteOrphanedReferences(fileStorageId.intValue(), distinctContextsPerSchema, databaseService, threadPool, exceptionId);
            }
        } catch (OXException e) {
            throw convertException(e, exceptionId);
        } catch (RuntimeException e) {
            throw convertException(e, exceptionId);
        }
    }

    private RemoteCompositionSpaceServiceException convertException(Exception e, String exceptionId) {
        LOGGER.error("Error while deleting orphaned composition space references; exceptionId={}", exceptionId, e);
        RemoteCompositionSpaceServiceException cme = new RemoteCompositionSpaceServiceException(e.getMessage());
        cme.setStackTrace(e.getStackTrace());
        return cme;
    }

    private void deleteOrphanedReferences(int fileStorageId, List<Integer> distinctContextsPerSchema, DatabaseService databaseService, ThreadPoolService threadPool, String exceptionId) throws  OXException {
        try {
            Semaphore semaphore = new Semaphore(10);
            List<FutureAndContext> futures = new ArrayList<>(distinctContextsPerSchema.size());
            for (Integer representativeContextId : distinctContextsPerSchema) {
                semaphore.acquire();
                try {
                    Future<Void> future = threadPool.submit(new AbstractTask<Void>() {

                        @Override
                        public Void call() throws Exception {
                            try {
                                deleteOrphanedReferencesForSchema(fileStorageId, representativeContextId.intValue(), databaseService);
                                return null;
                            } finally {
                                semaphore.release();
                            }
                        }
                    }, CallerRunsBehavior.getInstance());
                    futures.add(new FutureAndContext(future, representativeContextId));
                } catch (RejectedExecutionException e) {
                    semaphore.release();
                    logError(e, exceptionId, representativeContextId, databaseService);
                }
            }

            boolean anyErrors = false;
            for (FutureAndContext futureAndContext : futures) {
                try {
                    futureAndContext.future.get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    logError(cause == null ? e : cause, exceptionId, futureAndContext.representativeContextId, databaseService);
                    anyErrors = true;
                }
            }
            if (anyErrors) {
                throw OXException.general("Errors occurred while deleting orphaned composition space references. Please check log files for exception identifier: " + exceptionId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OXException.general("Deleting orphaned composition space references has been interrupted");
        }
    }

    void deleteOrphanedReferencesForSchema(int fileStorageId, int representativeContextId, DatabaseService databaseService) throws OXException {
        // Examine current schema
        Optional<SchemaExaminationResult> optionalResult = examineSchema(fileStorageId, representativeContextId, databaseService);
        if (!optionalResult.isPresent()) {
            return;
        }

        // Get schema result
        SchemaExaminationResult result = optionalResult.get();

        // Create a collection to collect for a certain (dedicated) file storage all contexts that use that storage
        Collection<FileStorageAndContexts> collecton = determineFileStorageUsingContexts(fileStorageId, result.consideredContextIds);

        // Create difference sets to determine unreferenced and non-existing file storage resources
        Map<Integer, Set<String>> attachmentIdentifiers = result.attachmentIdentifiers;
        Map<Integer, Set<String>> keyIdentifiers = result.keyIdentifiers;
        for (FileStorageAndContexts fileStorageAndContexts : collecton) {
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
                    deleteObsoleteDatabaseEntries(nonExisting, fileStorageId, representativeContextId, databaseService);
                }
            }
        }
    }

    private void deleteObsoleteDatabaseEntries(Set<String> nonExisting, int fileStorageId, int representativeContextId, DatabaseService databaseService) throws OXException {
        PreparedStatement stmt = null;
        Connection writeCon = databaseService.getWritable(representativeContextId);
        try {
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
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            databaseService.backWritable(representativeContextId, writeCon);
        }
    }

    private Collection<FileStorageAndContexts> determineFileStorageUsingContexts(int fileStorageId, Set<Integer> consideredContextIds) throws OXException {
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
        return fileStorage2Contexts.values();
    }

    private Optional<SchemaExaminationResult> examineSchema(int fileStorageId, int representativeContextId, DatabaseService databaseService) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection readCon = databaseService.getReadOnly(representativeContextId);
        try {
            if (!columnExists(readCon, "compositionSpaceAttachmentMeta", "dedicatedFileStorageId") || !columnExists(readCon, "compositionSpaceKeyStorage", "dedicatedFileStorageId")) {
                return Optional.empty();
            }

            stmt = readCon.prepareStatement("SELECT cid, refId FROM compositionSpaceAttachmentMeta WHERE dedicatedFileStorageId=? AND refType=" + KnownAttachmentStorageType.DEDICATED_FILE_STORAGE.getType());
            stmt.setInt(1, fileStorageId);
            rs = stmt.executeQuery();
            Set<Integer> consideredContextIds = new HashSet<Integer>();
            Map<Integer, Set<String>> attachmentIdentifiers = new HashMap<>();
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

            stmt = readCon.prepareStatement("SELECT cid, refId FROM compositionSpaceKeyStorage WHERE dedicatedFileStorageId=?");
            stmt.setInt(1, fileStorageId);
            rs = stmt.executeQuery();
            Map<Integer, Set<String>> keyIdentifiers = new HashMap<>();
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

            return Optional.of(new SchemaExaminationResult(consideredContextIds, attachmentIdentifiers, keyIdentifiers));
        } catch (SQLSyntaxErrorException e) {
            // Assume that column 'dedicatedFileStorageId' does not exist in context-associated schema. Therefore ignore.
            return Optional.empty();
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (readCon != null) {
                databaseService.backReadOnly(representativeContextId, readCon);
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

    private void logError(Throwable e, String exceptionId, Integer representativeContextId, DatabaseService databaseService) {
        SchemaInfo schemaInfo = getSchemaInfoSafe(databaseService, representativeContextId);
        if (schemaInfo == null) {
            LOGGER.error("Failed to delete orphaned composition space references; exceptionId={}", exceptionId, e);
        } else {
            LOGGER.error("Failed to delete orphaned composition space references for schema {} in database {}; exceptionId={}", schemaInfo.getSchema(), I(schemaInfo.getPoolId()), exceptionId, e);
        }
    }

    private SchemaInfo getSchemaInfoSafe(DatabaseService databaseService, Integer representativeContextId) {
        try {
            return databaseService.getSchemaInfo(representativeContextId.intValue());
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class SchemaExaminationResult {

        final Set<Integer> consideredContextIds;
        final Map<Integer, Set<String>> attachmentIdentifiers;
        final Map<Integer, Set<String>> keyIdentifiers;

        SchemaExaminationResult(Set<Integer> consideredContextIds, Map<Integer, Set<String>> attachmentIdentifiers, Map<Integer, Set<String>> keyIdentifiers) {
            super();
            this.consideredContextIds = consideredContextIds;
            this.attachmentIdentifiers = attachmentIdentifiers;
            this.keyIdentifiers = keyIdentifiers;
        }
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

    private static class FutureAndContext {

        final Future<Void> future;
        final Integer representativeContextId;

        FutureAndContext(Future<Void> future, Integer representativeContextId) {
            super();
            this.future = future;
            this.representativeContextId = representativeContextId;
        }
    }

}
