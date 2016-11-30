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

package com.openexchange.file.storage.boxcom;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractBoxResourceAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractBoxResourceAccess {

    /** Status code (400) indicating a bad request. */
    protected static final int SC_BAD_REQUEST = 400;

    /** Status code (401) indicating that the request requires HTTP authentication. */
    protected static final int SC_UNAUTHORIZED = 401;

    /** Status code (404) indicating that the requested resource is not available. */
    protected static final int SC_NOT_FOUND = 404;

    protected final BoxOAuthAccess boxAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected final String rootFolderId;

    /**
     * Initializes a new {@link AbstractBoxResourceAccess}.
     */
    protected AbstractBoxResourceAccess(BoxOAuthAccess boxAccess, FileStorageAccount account, Session session) throws OXException {
        super();
        this.boxAccess = boxAccess;
        this.account = account;
        this.session = session;
        rootFolderId = "0";
    }

    /**
     * Performs given closure.
     *
     * @param closure The closure to perform
     * @return The return value
     * @throws OXException If performing closure fails
     */
    protected <R> R perform(BoxClosure<R> closure) throws OXException {
        return closure.perform(this, boxAccess, session);
    }

    /**
     * Checks if given typed object is trashed
     *
     * @param folder The typed object to check
     * @return <code>true</code> if typed object is trashed; otherwise <code>false</code>
     */
    protected boolean isFolderTrashed(BoxFolder.Info folder) {
        return hasTrashedParent(folder);
    }

    /**
     * Checks (recursively) whether the specified box folder has a trashed parent
     *
     * @param boxFolder The box folder
     * @return <code>true</code> if the parent folder is trashed; otherwise <code>false</code>
     */
    private boolean hasTrashedParent(BoxFolder.Info boxFolder) {
        BoxFolder.Info parent = boxFolder.getParent();
        if (null == parent) {
            return false;
        }
        if ("trash".equals(parent.getID())) {
            return true;
        }
        return hasTrashedParent(parent);
    }

    /**
     * Checks if given file is trashed
     *
     * @param fileInfo The file to check
     * @return <code>true</code> if the file is trashed; otherwise <code>false</code>
     */
    protected boolean isFileTrashed(BoxFile.Info fileInfo) {
        return fileInfo.getTrashedAt() != null;
    }

    /**
     * Checks the file's validity
     *
     * @param fileInfo The file's validity
     * @throws OXException if the specified file was trashed
     */
    protected void checkFileValidity(BoxFile.Info fileInfo) throws OXException {
        if (isFileTrashed(fileInfo)) {
            throw FileStorageExceptionCodes.NOT_A_FILE.create(BoxConstants.ID, fileInfo.getID());
        }
    }

    /**
     * Handles authentication error.
     *
     * @param e The authentication error
     * @param session The associated session
     * @return The re-initialized Box.com access
     * @throws OXException If authentication error could not be handled
     */
    protected BoxOAuthAccess handleAuthError(BoxAPIException e, Session session) throws OXException {
        try {
            boxAccess.initialize();
            return boxAccess;
        } catch (OXException oxe) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractBoxResourceAccess.class);
            logger.warn("Could not re-initialize Box.com access", oxe);

            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, BoxConstants.ID, e.getMessage());
        }
    }

    /**
     * Handles given HTTP response error.
     *
     * @param identifier The optional identifier for associated Box.com resource
     * @param e The HTTP error
     * @return The resulting exception
     */
    protected OXException handleHttpResponseError(String identifier, String accountId, BoxAPIException e) {
        if (null != identifier && SC_NOT_FOUND == e.getResponseCode()) {
            return FileStorageExceptionCodes.NOT_FOUND.create(e, "Box", identifier);
        }
        if (null != accountId && SC_UNAUTHORIZED == e.getResponseCode()) {
            return FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(e, accountId, BoxConstants.ID);
        }
        if (accountId != null && e.getResponseCode() == SC_BAD_REQUEST) {
            try {
                JSONObject responseBody = new JSONObject(e.getResponse());
                String errorDesc = responseBody.getString("error_description");
                if (errorDesc.equals("Refresh token has expired")) {
                    try {
                        //TODO: refresh token
                        boxAccess.initialize();
                    } catch (OXException ex) {
                        return ex;
                    }
                }
            } catch (JSONException e1) {
                return FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage());
            }
        }
        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getResponseCode() + " " + e.getResponse());
    }

    /**
     * Gets the Box.com folder identifier from given file storage folder identifier
     *
     * @param folderId The file storage folder identifier
     * @return The appropriate Box.com folder identifier
     */
    protected String toBoxFolderId(String folderId) {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? rootFolderId : folderId;
    }

    /**
     * Gets the file storage folder identifier from given Box.com folder identifier
     *
     * @param boxId The Box.com folder identifier
     * @return The appropriate file storage folder identifier
     */
    protected String toFileStorageFolderId(String boxId) {
        return rootFolderId.equals(boxId) || "0".equals(boxId) ? FileStorageFolder.ROOT_FULLNAME : boxId;
    }

    /**
     * Get a {@link BoxAPIConnection} from the {@link OAuthAccess}
     *
     * @return A {@link BoxAPIException}
     * @throws OXException if the API connection cannot be retrieved
     */
    protected BoxAPIConnection getAPIConnection() throws OXException {
        boxAccess.ensureNotExpired();
        return boxAccess.<BoxAPIConnection> getClient().client;
    }
}
