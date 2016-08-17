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

package com.openexchange.test;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.RootRequest;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.CommonUpdatesResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.arrays.Arrays;

/**
 * This class and FolderObject should be all that is needed to write folder-related tests. If multiple users are needed use multiple
 * instances of this class. Examples of tests using this class can be found in ExemplaryFolderTestManagerTest.java.
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - refactoring
 */
public class FolderTestManager implements TestManager{

    private AbstractAJAXResponse lastResponse;

    private List<FolderObject> createdItems;

    private final AJAXClient client;

    private boolean failOnError = true;

    private boolean ignoreMailFolders = true;

    private Throwable lastException;

    @Override
    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    public boolean getIgnoreMailFolders() {
        return ignoreMailFolders;
    }

    public void setIgnoreMailFolders(final boolean ignoreMailFolders) {
        this.ignoreMailFolders = ignoreMailFolders;
    }

    public void setLastResponse(final AbstractAJAXResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }


    public FolderTestManager(final AJAXClient client) {
        this.client = client;
        createdItems = new LinkedList<FolderObject>();
    }

    /**
     * Creates a folder via HTTP-API and updates it with new id, timestamp and all other information that is updated after such requests.
     * Remembers this folder for cleanup later.
     */
    public FolderObject insertFolderOnServer(final FolderObject folderToCreate) {
        final InsertRequest request = new InsertRequest(EnumAPI.OX_OLD, folderToCreate);
        CommonInsertResponse response = null;
        try {
            response = client.execute(request);
            setLastResponse(response);
            response.fillObject(folderToCreate);
        } catch (final Exception e) {
            doExceptionHandling(e, "NewRequest");
        }
        createdItems.add(folderToCreate);
        return folderToCreate;
    }

    /**
     * Create multiple folders via the HTTP-API at once
     */
    public void insertFoldersOnServer(final FolderObject[] folders) {
        for (FolderObject folder : folders) {
            this.insertFolderOnServer(folder);
        }
    }

    /**
     * Updates a folder via HTTP-API and returns the same folder for convenience
     */
    public FolderObject updateFolderOnServer(FolderObject folder) {
        return updateFolderOnServer(folder, true);
    }

    /**
     * Updates a folder via HTTP-API and returns the same folder for convenience
     */
    public FolderObject updateFolderOnServer(FolderObject folder, boolean failOnError) {
        final UpdateRequest request = new UpdateRequest(EnumAPI.OX_OLD, folder, failOnError);
        try {
            setLastResponse(client.execute(request));
            remember(folder);
        } catch (final Exception e) {
            doExceptionHandling(e, "UpdateRequest");
        }

        return folder;
    }

    /**
     * Deletes a folder via HTTP-API
     */
    public void deleteFolderOnServer(final FolderObject folderToDelete) throws OXException, IOException, SAXException, JSONException {
        deleteFolderOnServer(folderToDelete, Boolean.FALSE);
    }

    /**
     * Deletes a folder via HTTP-API
     */
    public void deleteFolderOnServer(final FolderObject folderToDelete, Boolean hardDelete) throws OXException, IOException, SAXException, JSONException {
        final DeleteRequest request = new DeleteRequest(EnumAPI.OX_OLD, folderToDelete);
        request.setHardDelete(hardDelete);
        setLastResponse(client.execute(request));
        if (hardDelete) {
            removeFolderFromCleanupList(folderToDelete);
        }
    }

    /**
     * Deletes a folder via HTTP-API
     */
    public void deleteFolderOnServer(final int folderID, final Date lastModified) throws OXException, IOException, SAXException, JSONException {
        final FolderObject fo = new FolderObject();
        fo.setObjectID(folderID);
        fo.setLastModified(lastModified);
        deleteFolderOnServer(fo);
    }

    /**
     * Removes a folder form the cleanup list. This is somewhat complicated, because FolderObject lacks a proper #equals() and because
     * identifying folders is different for normal folders and mail folders
     */
    private void removeFolderFromCleanupList(final FolderObject folderToDelete) {
        Iterator<FolderObject> it = createdItems.iterator();
        while (it.hasNext()) {
            FolderObject folder = it.next();
            // normal folder
            if (folder.getObjectID() == folderToDelete.getObjectID() && !folder.containsFullName() && !folderToDelete.containsFullName()) {
                it.remove();
            }
            // mail folder:
            if (folder.containsFullName() && folderToDelete.containsFullName() && !folder.containsObjectID() && !folderToDelete.containsObjectID() && folder.getFullName().equals(
                folderToDelete.getFullName())) {
                it.remove();
            }
        }
    }

    /**
     * Get a folder via HTTP-API with an existing FolderObject
     */
    public FolderObject getFolderFromServer(final FolderObject folder) {
        if (folder.getObjectID() == 0) {
            return getFolderFromServer(folder.getFullName(), getFailOnError());
        }
        return getFolderFromServer(folder.getObjectID(), getFailOnError());
    }

    public FolderObject getFolderFromServer(final FolderObject folder, final boolean failOnErrorOverride) {
        if (folder.getObjectID() == 0) {
            return getFolderFromServer(folder.getFullName(), failOnErrorOverride);
        }
        return getFolderFromServer(folder.getObjectID(), failOnErrorOverride);
    }

    public FolderObject getFolderFromServer(final int folderID, final boolean failOnErrorOverride, final int[] additionalColumns) {
        final boolean oldValue = getFailOnError();
        setFailOnError(failOnErrorOverride);
        FolderObject returnedFolder = null;
        final GetRequest request = new GetRequest(EnumAPI.OX_OLD, folderID, Arrays.addUniquely(FolderObject.ALL_COLUMNS,additionalColumns));
        GetResponse response = null;
        try {
            response = client.execute(request);
            setLastResponse(response);
            returnedFolder = response.getFolder();
            setFailOnError(oldValue);
        } catch (final Exception e) {
            doExceptionHandling(e, "GetRequest for folder with id " + folderID);
        }
        return returnedFolder;
    }

    public FolderObject getFolderFromServer(final String name) {
        return getFolderFromServer(name, getFailOnError());
    }

    /**
     * Get a folder via HTTP-API with no existing FolderObject and the folders name as identifier
     */
    public FolderObject getFolderFromServer(final String name, final boolean failOnErrorOverride) {
        FolderObject returnedFolder = null;
        final GetRequest request = new GetRequest(EnumAPI.OX_OLD, name, failOnErrorOverride);
        GetResponse response = null;
        try {
            response = client.execute(request);
            setLastResponse(response);
            returnedFolder = response.getFolder();
        } catch (final Exception e) {
            doExceptionHandling(e, "GetRequest");
        }
        return returnedFolder;
    }

    /**
     * Get a folder via HTTP-API with no existing FolderObject and the folders id as identifier
     */
    public FolderObject getFolderFromServer(final int folderId, final boolean failOnErrorOverride) {
        return getFolderByStringFromServer(Integer.toString(folderId), failOnErrorOverride);
    }

    /**
     * Get a folder via HTTP-API with no existing FolderObject and the folders id as identifier
     */
    public FolderObject getFolderByStringFromServer(final String folderId, final boolean failOnErrorOverride) {
        final boolean oldValue = getFailOnError();
        setFailOnError(failOnErrorOverride);
        FolderObject returnedFolder = null;
        final GetRequest request = new GetRequest(EnumAPI.OX_OLD, folderId, FolderObject.ALL_COLUMNS);
        GetResponse response = null;
        try {
            response = client.execute(request);
            setLastResponse(response);
            returnedFolder = response.getFolder();
            setFailOnError(oldValue);
        } catch (final Exception e) {
            doExceptionHandling(e, "GetRequest for folder with id " + folderId);
        }
        return returnedFolder;
    }

    public FolderObject getFolderFromServer(final int folderId) {
        return getFolderFromServer(folderId, getFailOnError());
    }

    /**
     * removes all folders inserted or updated by this Manager
     */
    @Override
    public void cleanUp() {
        final Vector<FolderObject> deleteMe = new Vector<FolderObject>(createdItems);
        try {
            for (final FolderObject folder : deleteMe) {
                folder.setLastModified(new Date(Long.MAX_VALUE));
                deleteFolderOnServer(folder, Boolean.TRUE);
            }
        } catch (final Exception e) {
            doExceptionHandling(e, "clean-up");
        }

        createdItems = new LinkedList<FolderObject>();
    }

    /**
     * get all folders in one parent folder via the HTTP-API (List-Request)
     */
    public FolderObject[] listFoldersOnServer(final int parentFolderId) {
        return listFoldersOnServer(parentFolderId, null);
    }

    public FolderObject[] listFoldersOnServer(final int parentFolderId, final int[] additionalFields) {
        final Vector<FolderObject> allFolders = new Vector<FolderObject>();
        final ListRequest request = new ListRequest(EnumAPI.OX_OLD, Integer.toString(parentFolderId), Arrays.addUniquely(new int[] { FolderObject.OBJECT_ID },additionalFields), getFailOnError());
        try {
            final ListResponse response = client.execute(request);
            final Iterator<FolderObject> iterator = response.getFolder();
            while (iterator.hasNext()) {
                allFolders.add(iterator.next());
            }
            setLastResponse(response);
        } catch (final Exception e) {
            doExceptionHandling(e, "ListRequest");
        }

        final FolderObject[] folderArray = new FolderObject[allFolders.size()];
        allFolders.copyInto(folderArray);
        return folderArray;
    }

    /**
     * get all folders in one parent folder via the HTTP-API
     */
    public FolderObject[] listFoldersOnServer(final FolderObject folder) {
        if (folder.getObjectID() != 0) {
            return listFoldersOnServer(folder.getObjectID());
        }
        final Vector<FolderObject> allFolders = new Vector<FolderObject>();
        // FolderObject parentFolder = this.getFolderFromServer(parentFolderId);
        final ListRequest request = new ListRequest(EnumAPI.OX_OLD, folder.getFullName(), new int[] { FolderObject.OBJECT_ID }, getFailOnError());
        try {
            final ListResponse response = client.execute(request);
            setLastResponse(response);
            final Iterator<FolderObject> iterator = response.getFolder();
            while (iterator.hasNext()) {
                allFolders.add(iterator.next());
            }
        } catch (final Exception e) {
            doExceptionHandling(e, "ListRequest");
        }

        final FolderObject[] folderArray = new FolderObject[allFolders.size()];
        allFolders.copyInto(folderArray);
        return folderArray;
    }

    public FolderObject[] listRootFoldersOnServer() {
        final Vector<FolderObject> allFolders = new Vector<FolderObject>();
        // FolderObject parentFolder = this.getFolderFromServer(parentFolderId);
        final RootRequest request = new RootRequest(EnumAPI.OX_OLD, new int[] { FolderObject.OBJECT_ID }, ignoreMailFolders);
        try {
            final ListResponse response = client.execute(request);
            setLastResponse(response);
            final Iterator<FolderObject> iterator = response.getFolder();
            while (iterator.hasNext()) {
                allFolders.add(iterator.next());
            }
        } catch (final Exception e) {
            doExceptionHandling(e, "ListRequest for root folders");
        }

        final FolderObject[] folderArray = new FolderObject[allFolders.size()];
        allFolders.copyInto(folderArray);
        return folderArray;
    }

    /**
     * Get folders in a parent folder that were updated since a specific date via the HTTP-API
     */
    public FolderObject[] getUpdatedFoldersOnServer(final Date lastModified) {
        return getUpdatedFoldersOnServer(lastModified, null);
    }

    public FolderObject[] getUpdatedFoldersOnServer(final Date lastModified, final int[] additionalFields) {
        final Vector<FolderObject> allFolders = new Vector<FolderObject>();
        final UpdatesRequest request = new UpdatesRequest(EnumAPI.OX_OLD, Arrays.addUniquely(new int[] { FolderObject.OBJECT_ID }, additionalFields), -1, null, lastModified);
        try {
            final CommonUpdatesResponse response = client.execute(request);
            final int idPos = findIDPosition(response.getColumns());
            final JSONArray data = (JSONArray) response.getResponse().getData();
            FolderObject fo = new FolderObject();
            for (int i = 0; i < data.length(); i++) {
                final JSONArray tempArray = data.getJSONArray(i);
                fo = this.getFolderByStringFromServer(tempArray.getString(idPos), getFailOnError());
                allFolders.add(fo);
            }
            setLastResponse(response);
        } catch (final Exception e) {
            doExceptionHandling(e, "AllRequest");
        }

        final FolderObject[] folderArray = new FolderObject[allFolders.size()];
        allFolders.copyInto(folderArray);
        return folderArray;
    }

    private int findPositionOfColumn(final int[] haystack, final int needle) {
        for(int i = 0; i < haystack.length; i++) {
            if(haystack[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    private int findIDPosition(final int[] columns) {
        return findPositionOfColumn(columns, Appointment.OBJECT_ID);
    }

    private void remember(final FolderObject folder) {
        for (final FolderObject tempFolder : createdItems) {
            if (tempFolder.getObjectID() == folder.getObjectID()) {
                createdItems.set(createdItems.indexOf(tempFolder), folder);
            } else {
                createdItems.add(folder);
            }
        }
    }

    /**
     * get all folders in one parent folder via the HTTP-API (All-Request)
     */
    // TODO: It would be nice if the fields of the returned FolderObjects were filled by the original AllRequest, not by separate
    // GetRequests
    public FolderObject[] getAllFoldersOnServer(final int folderId) {
        final Vector<FolderObject> allFolders = new Vector<FolderObject>();
        final CommonAllRequest request = new CommonAllRequest("/ajax/folders", folderId, new int[] { FolderObject.OBJECT_ID }, 0, null, getFailOnError());
        try {
            final CommonAllResponse response = client.execute(request);
            final JSONArray data = (JSONArray) response.getResponse().getData();
            for (int i = 0; i < data.length(); i++) {
                final JSONArray temp = data.optJSONArray(i);
                final int tempFolderId = temp.getInt(0);
                final FolderObject tempFolder = getFolderFromServer(tempFolderId);
                allFolders.add(tempFolder);
            }
            setLastResponse(response);
        } catch (final Exception e) {
            doExceptionHandling(e, "AllRequest");
        }
        final FolderObject[] folderArray = new FolderObject[allFolders.size()];
        allFolders.copyInto(folderArray);
        return folderArray;
    }

    protected void doExceptionHandling(final Exception exception, final String action) {
        try {
            lastException = exception;
            throw exception;
        } catch (final OXException e) {
            if (getFailOnError()) {
                fail("AjaxException occured during " + action + ": " + e.getMessage());
            }
        } catch (final IOException e) {
            if (getFailOnError()) {
                fail("IOException occured during " + action + ": " + e.getMessage());
            }
        } catch (final SAXException e) {
            if (getFailOnError()) {
                fail("SAXException occured during " + action + ": " + e.getMessage());
            }
        } catch (final JSONException e) {
            if (getFailOnError()) {
                fail("JSONException occured during " + action + ": " + e.getMessage());
            }
        } catch (final Exception e) {
            fail("Unexpected error occured during " + action + ": " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    public FolderObject generatePrivateFolder(final String name, final int moduleID, final int parentID, final int userID) {
        final FolderObject folder = new FolderObject();
        folder.setFolderName(name);
        folder.setType(FolderObject.PRIVATE);
        folder.setParentFolderID(parentID);
        folder.setModule(moduleID);
        final OCLPermission permissions = new OCLPermission();
        permissions.setEntity(userID);
        permissions.setGroupPermission(false);
        permissions.setFolderAdmin(true);
        permissions.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);

        folder.addPermission(permissions);
        return folder;
    }

    /**
     * Generates a folder with admin permissions for all given userIDs.
     * @param name Name of the folder
     * @param moduleID moduleID of the folder (calendar, task, etc... from FolderObject, not from any other class)
     * @param parentID the parent folder's ID
     * @param userIDs the IDs of the users that have admin permission on this one
     * @return a fodler object according to the input parameters
     */
    public FolderObject generatePublicFolder(final String name, final int moduleID, final int parentID, final int... userIDs){
        //create a folder
        final FolderObject folder = new FolderObject();
        folder.setFolderName(name);
        folder.setType(FolderObject.PUBLIC);
        folder.setParentFolderID(parentID);
        folder.setModule(moduleID);
        // create permissions
        final ArrayList<OCLPermission> allPermissions = new ArrayList<OCLPermission>();
        for(final int userID: userIDs){
            final OCLPermission permissions = new OCLPermission();
            permissions.setEntity(userID);
            permissions.setGroupPermission(false);
            permissions.setFolderAdmin(true);
            permissions.setAllPermission(
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION);
            allPermissions.add(permissions);
        }
        folder.setPermissions(allPermissions);
        return folder;
    }

    public FolderObject generateSharedFolder(final String name, final int moduleID, final int parentID, final int... userIDs){
        //create a folder
        final FolderObject folder = new FolderObject();
        folder.setFolderName(name);
        folder.setType(FolderObject.SHARED);
        folder.setParentFolderID(parentID);
        folder.setModule(moduleID);
        // create permissions
        final ArrayList<OCLPermission> allPermissions = new ArrayList<OCLPermission>();
        boolean firstUser = true;
        for(final int userID: userIDs){
            final OCLPermission permissions = new OCLPermission();
            permissions.setEntity(userID);
            permissions.setGroupPermission(false);
            permissions.setFolderAdmin(firstUser);
            permissions.setAllPermission(
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION);
            allPermissions.add(permissions);
            firstUser = false;
        }
        folder.setPermissions(allPermissions);
        return folder;
    }

    @Override
    public boolean doesFailOnError() {
        return getFailOnError();
    }

    @Override
    public Throwable getLastException() {
        return this.lastException;
    }

    @Override
    public boolean hasLastException() {
        return this.lastException != null;
    }
}
