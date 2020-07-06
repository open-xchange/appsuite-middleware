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

package com.openexchange.file.storage.appsuite;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.openexchange.annotation.Nullable;
import com.openexchange.appsuite.client.AppsuiteClient;
import com.openexchange.appsuite.client.common.calls.folders.GetFolderCall;
import com.openexchange.appsuite.client.common.calls.folders.ListFoldersCall;
import com.openexchange.appsuite.client.common.calls.folders.RemoteFolder;
import com.openexchange.appsuite.client.common.calls.infostore.DocumentCall;
import com.openexchange.appsuite.client.common.calls.infostore.GetAllCall;
import com.openexchange.appsuite.client.common.calls.infostore.GetCall;
import com.openexchange.appsuite.client.common.calls.infostore.NewCall;
import com.openexchange.appsuite.client.common.calls.infostore.VersionsCall;
import com.openexchange.appsuite.client.common.calls.system.WhoamiCall;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

import static com.openexchange.java.Autoboxing.I;

/**
 * {@link ShareClient} a client for accessing remote shared on other Appsuite instances
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ShareClient {

    private final AppsuiteClient ajaxClient;
    private final Session session;

    private static final String USER_INFOSTORE_FOLDER = "10";

    /**
     * Initializes a new {@link ShareClient}.
     *
     * @param session A session
     * @param client The underlying {@link AppsuiteClient} to use
     */
    public ShareClient(Session session, AppsuiteClient client) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.ajaxClient = Objects.requireNonNull(client, "client must not be null");
    }

    private String getFolderId(String folderId) {
        return Strings.isEmpty(folderId) ? USER_INFOSTORE_FOLDER : folderId;
    }

    /**
     * Pings the remote OX
     *
     * @throws OXException if the ping failed
     */
    public void ping() throws OXException {
        ajaxClient.execute(new WhoamiCall());
    }

    /**
     * Internal method to return an array of IDs for the given fields
     *
     * @param fields The fields
     * @return An array of IDs for the given fields
     */
    private int[] toIdList(List<Field> fields) {
        return fields.stream().mapToInt(f -> f.getNumber()).toArray();
    }

    /**
     * Gets the folder identified through given identifier
     *
     * @param folderId The identifier
     * @return The corresponding instance of {@link AppsuiteFolder}
     * @throws OXException If either folder does not exist or could not be fetched
     */
    public AppsuiteFolder getFolder(String folderId) throws OXException {
        RemoteFolder remoteFolder = ajaxClient.execute(new GetFolderCall(folderId));
        final int userId = session.getUserId();
        return new AppsuiteFolder(userId, remoteFolder);
    }

    /**
     * Gets the first level subfolders located below the folder whose identifier matches given parameter <code>parentIdentifier</code>.
     *
     * @param parentIdentifier The parent identifier
     * @return An array of {@link FileStorageFolder} representing the subfolders
     * @throws OXException If either parent folder does not exist or its subfolders cannot be delivered
     */
    public AppsuiteFolder[] getSubFolders(String parentId) throws OXException {
        List<RemoteFolder> folders = ajaxClient.execute(new ListFoldersCall(getFolderId(parentId)));
        final int userId = session.getUserId();
        List<AppsuiteFolder> ret = folders.stream().map(f -> new AppsuiteFolder(userId, f)).collect(Collectors.toList());
        return ret.toArray(new AppsuiteFolder[ret.size()]);
    }

    /**
     * Gets the binary data from a document
     *
     * @param folderId The ID of the folder
     * @param id The ID of the item to get the data from
     * @param version The version to get the data for, or null to get the data for the current version
     * @return The binary data as {@link InputStream}
     * @throws OXException
     */
    public InputStream getDocument(String folderId, String id, @Nullable String version) throws OXException {
        return ajaxClient.execute(new DocumentCall(folderId, id, version));
    }

    public AppsuiteFile saveDocument(File file, InputStream data) throws OXException {
        DefaultFile newFile = ajaxClient.execute(new NewCall(new DefaultFile(file), data));
        return new AppsuiteFile(newFile);
    }

    /**
     * Gets given file meta data
     *
     * @param folderId The ID of the folder
     * @param id The ID of the file
     * @param version The version, or null to fetch the current version
     * @return The file
     * @throws OXException
     */
    public AppsuiteFile getMetaData(String folderId, String id, @Nullable String version) throws OXException {
        DefaultFile file = ajaxClient.execute(new GetCall(folderId, id, version));
        return new AppsuiteFile(file);
    }

    /**
     * Gets all documents in the given folder
     *
     * @param folderId The ID of the folder
     * @param fields The fields to get
     * @param sort the field to use for sorting
     * @param order The sort order
     * @return The documents of the given folder
     * @throws OXException
     */
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {

        //TODO: refine
        //Folders in the first layer are not accessible for file listing (infostore?action=all)
        //if(folderId.equals("44")) {
        //    return new FileTimedResult(Collections.emptyList());
        //}

        //@formatter:off
        List<? extends File> files = ajaxClient.execute(
            new GetAllCall(getFolderId(folderId),
                          toIdList(fields),
                          sort != null ? I(sort.getNumber()) : null,
                          order));
        //@formatter:on
        return new FileTimedResult((List<File>) files);
    }

    /**
     * Returns the versions for a given item
     *
     * @param id The ID of the item
     * @param fields The fields to return
     * @param sort The sorting field
     * @param order The sort direction
     * @return A list of versions
     * @throws OXException
     */
    public TimedResult<File> getVersions(String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        //@formatter:off
        List<? extends File> versions = ajaxClient.execute(
            new VersionsCall(id,
                             toIdList(fields),
                             sort != null ? I(sort.getNumber()) : null,
                             order));
        //@formatter:on
        return new FileTimedResult((List<File>) versions);
    }
}
