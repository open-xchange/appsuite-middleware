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

package com.openexchange.ajax.folder.api2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link AbstractFolderTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AbstractFolderTest extends AbstractAJAXSession {

    protected AJAXClient client;

    protected int userId;

    /**
     * Initializes a new {@link AbstractFolderTest}.
     *
     * @param name
     */
    protected AbstractFolderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();
    }

    protected List<FolderObject> createAndPersistSeveral(final String title, int amount) throws Exception {
        List<FolderObject> severalFolders = createSeveral(title, amount);
        severalFolders = persistSeveral(severalFolders);
        return severalFolders;
    }

    /**
     * Create several simple folders whose type is chosen by rotating over calendar, contact and task.
     *
     * @param name The prefix of the folder's name, final name will be name-module-{0..amount-1}
     * @param amount The amount of folders to create
     * @return a list of created folders
     */
    private List<FolderObject> createSeveral(final String name, final int amount) {
        List<FolderObject> newFolders = new ArrayList<FolderObject>(amount);

        FolderType[] folderTypes = { FolderType.CALENDAR, FolderType.CONTACT, FolderType.TASK };
        int numberOfTypes = folderTypes.length;

        for (int i = 0; i < amount; i++) {
            int indexOfType = i % numberOfTypes;
            FolderType folderType = folderTypes[indexOfType];
            String serialName = name + "-" + folderType.getName() + "-" + i;
            FolderObject newFolder = createSingle(folderType.getId(), serialName);
            newFolders.add(newFolder);
        }
        return newFolders;
    }

    /**
     * Create a new folder in the private folder tree
     *
     * @return The new FolderObject
     */
    protected FolderObject createSingle(int folderModule, String folderName) {
        final FolderObject newFolder = new FolderObject();
        newFolder.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        newFolder.setFolderName(folderName);
        newFolder.setModule(folderModule);
        final OCLPermission permission = new OCLPermission();
        permission.setEntity(userId);
        permission.setGroupPermission(false);
        permission.setFolderAdmin(true);
        permission.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        newFolder.addPermission(permission);
        return newFolder;
    }

    /**
     * Persist several folders on the server.
     *
     * @param newFolders the folders to persist
     * @return the persisted folders with updated id and lastmodified infos
     * @throws Exception
     */
    public List<FolderObject> persistSeveral(List<FolderObject> newFolders) throws Exception {
        int numberOfFolders = newFolders.size();
        List<InsertRequest> insertFolderRequests = new ArrayList<InsertRequest>(numberOfFolders);
        for (FolderObject folder : newFolders) {
            insertFolderRequests.add(new InsertRequest(EnumAPI.OUTLOOK, folder));
        }
        MultipleRequest<InsertResponse> multipleRequest = MultipleRequest.create(insertFolderRequests.toArray(new InsertRequest[numberOfFolders]));
        MultipleResponse<InsertResponse> multipleResponse = client.execute(multipleRequest);
        return updateFoldersWithTimeAndId(newFolders, multipleResponse);
    }

    /**
     * Update the folders with the infos from the MultipleResponse
     *
     * @param folders the folders
     * @param insertResponses the MultipleResponse
     * @return the folders with updated id and lastmodified infos
     * @throws Exception
     */
    private List<FolderObject> updateFoldersWithTimeAndId(List<FolderObject> folders, MultipleResponse<InsertResponse> insertResponses) throws Exception {

        for (int i = 0; i < folders.size(); i++) {
            FolderObject currentFolder = folders.get(i);
            Response currentResponse = insertResponses.getResponse(i).getResponse();
            Date timestamp = currentResponse.getTimestamp();
            int objectID = Integer.parseInt((String) currentResponse.getData());
            currentFolder.setLastModified(timestamp);
            currentFolder.setObjectID(objectID);
        }
        return folders;
    }

    /**
     * Update one or several folders on the server and additionally update the lastmodified infos.
     *
     * @param folders The folders to update
     */
    public void updateFolders(List<FolderObject> folders) {
        updateFolders(folders.toArray(new FolderObject[folders.size()]));
    }

    /**
     * Update one or several folders on the server and additionally update the lastmodified infos.
     *
     * @param folders The folders to update
     */
    public void updateFolders(FolderObject... folders) {
        int numAppointments = folders.length;
        UpdateRequest[] updateRequests = new UpdateRequest[numAppointments];
        for (int i = 0; i < numAppointments; i++) {
            FolderObject folder = folders[i];
            folder.setFolderName(folder.getFolderName() + " was updated");
            updateRequests[i] = new UpdateRequest(EnumAPI.OUTLOOK, folder);
        }
        MultipleRequest<InsertResponse> multipleUpdate = MultipleRequest.create(updateRequests);
        MultipleResponse<InsertResponse> updateResponse = client.executeSafe(multipleUpdate);
        for (int i = 0; i < numAppointments; i++) {
            folders[i].setLastModified(updateResponse.getResponse(i).getTimestamp());
        }
    }

    /**
     * Delete one or several folders on the server.
     *
     * @param folders The folders to delete
     */
    public void deleteFolders(List<FolderObject> folders) {
        deleteFolders(folders.toArray(new FolderObject[folders.size()]));
    }

    /**
     * Delete one or several folders on the server.
     *
     * @param folders The folders to delete
     */
    public void deleteFolders(FolderObject... folders) {
        DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, folders);
        CommonDeleteResponse deleteResponse = client.executeSafe(deleteRequest);
        JSONArray failures = (JSONArray) deleteResponse.getData();
        assertTrue(failures.isEmpty());
    }

    /**
     * Delete one or several folders on the server.
     *
     * @param folders The folders to delete
     */
    public void deleteFolders(boolean hardDelete, FolderObject... folders) {
        DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, folders);
        if (hardDelete) {
            deleteRequest.setHardDelete(hardDelete);
        }
        CommonDeleteResponse deleteResponse = client.executeSafe(deleteRequest);
        JSONArray failures = (JSONArray) deleteResponse.getData();
        assertTrue(failures.isEmpty());
    }

    /**
     * @param cols Columns to use for the request
     * @param lastModified The timestamp of the last update of the requested folders
     * @param ignore what kind of updates to ignore
     * @return The UpdatesResponse containg new, modified and deleted folders
     * @throws Exception
     */
    public FolderUpdatesResponse listModifiedFolders(int[] cols, final Date lastModified, Ignore ignore) throws Exception {
        final UpdatesRequest request = new UpdatesRequest(EnumAPI.OX_NEW, cols, -1, null, lastModified, ignore);
        final FolderUpdatesResponse response = client.execute(request);
        return response;
    }

    /**
     * {@link FolderType} - Enumeration of the module ids and associated names.
     *
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    public enum FolderType {
        TASK(1, Folder.MODULE_TASK),
        CALENDAR(2, Folder.MODULE_CALENDAR),
        CONTACT(3, Folder.MODULE_CONTACT),
        UNBOUND(4, Folder.MODULE_UNBOUND),
        SYSTEM_MODULE(5, Folder.MODULE_SYSTEM),
        MAIL(7, Folder.MODULE_MAIL),
        INFOSTORE(8, Folder.MODULE_INFOSTORE),
        MESSAGING(13, Folder.MODULE_MESSAGING),
        FILE(14, Folder.MODULE_INFOSTORE);

        private final int id;

        private final String name;

        FolderType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

    }
}
