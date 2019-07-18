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
import static com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage.getFileStorage;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
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
            deleteOrphanedReferences(fileStorageId, representativeContextId.intValue(), databaseService);
        }
    }

    private void deleteOrphanedReferences(int fileStorageId, int representativeContextId, DatabaseService databaseService) throws OXException {
        Connection con = databaseService.getReadOnly(representativeContextId);
        boolean readOnly = true;
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                if (!columnExists(con, "compositionSpaceAttachmentMeta", "dedicatedFileStorageId") || !columnExists(con, "compositionSpaceKeyStorage", "dedicatedFileStorageId")) {
                    return;
                }

                stmt = con.prepareStatement("SELECT cid, refId FROM compositionSpaceAttachmentMeta WHERE dedicatedFileStorageId=? AND refType="+KnownAttachmentStorageType.DEDICATED_FILE_STORAGE.getType());
                stmt.setInt(1, fileStorageId);
                rs = stmt.executeQuery();
                Set<Integer> contextIds = new HashSet<Integer>();
                Map<Integer, Set<String>> attachmentIdentifiers = new HashMap<>();
                while (rs.next()) {
                    Integer contextId = I(rs.getInt(1));
                    contextIds.add(contextId);
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

                stmt = con.prepareStatement("SELECT cid, refId FROM compositionSpaceKeyStorage WHERE dedicatedFileStorageId=?");
                stmt.setInt(1, fileStorageId);
                rs = stmt.executeQuery();
                Map<Integer, Set<String>> keyIdentifiers = new HashMap<>();
                while (rs.next()) {
                    Integer contextId = I(rs.getInt(1));
                    contextIds.add(contextId);
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

                for (Integer contextId : contextIds) {
                    FileStorage fileStorage = getFileStorage(fileStorageId, contextId.intValue());

                    SortedSet<String> fileList = fileStorage.getFileList();

                    {
                        Set<String> nonReferenced = new HashSet<String>(fileList);
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
                        if (!nonReferenced.isEmpty()) {
                            fileStorage.deleteFiles(nonReferenced.toArray(new String[nonReferenced.size()]));
                        }
                    }

                    {
                        Set<String> nonExisting = new HashSet<String>(fileList);
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
                        nonExisting.removeAll(fileList);
                        if (!nonExisting.isEmpty()) {
                            databaseService.backReadOnly(representativeContextId, con);
                            con = null;
                            con = databaseService.getWritable(representativeContextId);
                            readOnly = false;

                            for (String nonExistingStorageIdentifier : nonExisting) {
                                stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE cid=? AND dedicatedFileStorageId=? AND refType="+KnownAttachmentStorageType.DEDICATED_FILE_STORAGE.getType() + " AND refId=?");
                                stmt.setInt(1, contextId.intValue());
                                stmt.setInt(2, fileStorageId);
                                stmt.setString(3, nonExistingStorageIdentifier);
                                stmt.executeUpdate();
                                Databases.closeSQLStuff(stmt);
                                stmt = null;

                                stmt = con.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE cid=? AND dedicatedFileStorageId=? AND refId=?");
                                stmt.setInt(1, contextId.intValue());
                                stmt.setInt(2, fileStorageId);
                                stmt.setString(3, obfuscate(nonExistingStorageIdentifier));
                                stmt.executeUpdate();
                                Databases.closeSQLStuff(stmt);
                                stmt = null;
                            }
                        }
                    }
                } // End of loop traversing context identifiers
            } catch (SQLSyntaxErrorException e) {
                // Assume that column 'dedicatedFileStorageId' does not exist in context-associated schema. Therefore ignore.
            } catch (SQLException e) {
                throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        } finally {
            if (readOnly) {
                databaseService.backReadOnly(representativeContextId, con);
            } else {
                databaseService.backWritable(representativeContextId, con);
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

}
