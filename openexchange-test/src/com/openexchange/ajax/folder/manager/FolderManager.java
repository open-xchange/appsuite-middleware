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

package com.openexchange.ajax.folder.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersVisibilityData;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;

/**
 * {@link FolderManager}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class FolderManager {

    private final FolderApi folderApi;
    private final List<String> foldersToDelete = new ArrayList<>();
    private long lastTimestamp;
    private final String tree;

    /**
     * Initializes a new {@link FolderManager}.
     */
    public FolderManager(FolderApi api, String tree) {
        super();
        this.tree = tree;
        this.folderApi = api;
        this.lastTimestamp = 0l;
    }

    public String createFolder(String parent, String name, String module) throws ApiException {
        NewFolderBody body = new NewFolderBody();
        body.setFolder(FolderFactory.getSimpleFolder(name, module));
        FolderUpdateResponse createFolder = folderApi.getFoldersApi().createFolder(parent, getSession(), body, tree, null);
        checkResponse(createFolder.getError(), createFolder.getErrorDesc(), createFolder.getData());
        rememberFolder(createFolder.getData());
        lastTimestamp = createFolder.getTimestamp();
        return createFolder.getData();
    }

    public FoldersVisibilityData getAllFolders(String contentType, String columns) throws ApiException {
        FoldersVisibilityResponse visibleFolders = folderApi.getFoldersApi().getVisibleFolders(getSession(), contentType, columns, tree, null);
        checkResponse(visibleFolders.getError(), visibleFolders.getErrorDesc(), visibleFolders.getData());
        return visibleFolders.getData();
    }

    public String getSession() {
        return folderApi.getSession();
    }

    private void rememberFolder(String folderId) {
        foldersToDelete.add(folderId);
    }

    public void cleanUp() throws ApiException {
        folderApi.getFoldersApi().deleteFolders(folderApi.getSession(), foldersToDelete, tree, lastTimestamp, null, true, false, false);
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    <T> T checkResponse(String error, String errorDesc, T data) {
        assertNull(errorDesc, error);
        assertNotNull(data);
        return data;
    }

    /**
     * @return
     * @throws ApiException
     *
     */
    public FolderData getFolder(String id) throws ApiException {
        FolderResponse folderResponse = folderApi.getFoldersApi().getFolder(getSession(), id, tree, null, null);
        checkResponse(folderResponse.getError(), folderResponse.getErrorDesc(), folderResponse.getData());
        if (folderResponse.getTimestamp() != null && folderResponse.getTimestamp() > lastTimestamp) {
            lastTimestamp = folderResponse.getTimestamp();
        }
        return folderResponse.getData();

    }

    public void moveFolder(String folderId, String newParent) throws ApiException {
        FolderBody body = new FolderBody();
        FolderData folder = new FolderData();
        folder.setId(folderId);
        folder.setFolderId(newParent);
        folder.setPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse updateFolder = folderApi.getFoldersApi().updateFolder(getSession(), folderId, body, Boolean.FALSE, Long.valueOf(lastTimestamp), tree, null, Boolean.TRUE);
        checkResponse(updateFolder.getError(), updateFolder.getErrorDesc(), updateFolder.getData());
        lastTimestamp = updateFolder.getTimestamp();
    }

    public void setLastTimestamp(Long timestamp) {
        lastTimestamp = timestamp;
    }

}
