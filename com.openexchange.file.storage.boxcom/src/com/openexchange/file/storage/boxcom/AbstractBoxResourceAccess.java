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

import java.io.IOException;
import org.slf4j.Logger;
import com.box.boxjavalibv2.dao.BoxFile;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxTypedObject;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.restclientv2.exceptions.BoxRestException;
import com.box.restclientv2.exceptions.BoxSDKException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractBoxResourceAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractBoxResourceAccess {

    private static final String TYPE_FILE = BoxConstants.TYPE_FILE;

    private static final String TYPE_FOLDER = BoxConstants.TYPE_FOLDER;

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

        /*-
         *
        int keepOn = 1;
        while (keepOn > 0) {
            // Touch it...
            try {
                boxAccess.getBoxClient().getUsersManager().getCurrentUser(null);
                keepOn = 0;
            } catch (BoxRestException e) {
                throw handleRestError(e);
            } catch (BoxServerException e) {
                if (SC_UNAUTHORIZED != e.getStatusCode() || keepOn > 1) {
                    throw handleHttpResponseError(null, e);
                }

                keepOn = 2;
                boxAccess.reinit(session);
            } catch (AuthFatalFailureException e) {
                if (keepOn > 1) {
                    throw BoxExceptionCodes.UNLINKED_ERROR.create(e, new Object[0]);
                }

                keepOn = 2;
                boxAccess.reinit(session);
            }
        }
         *
         */
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
     * Checks if given typed object denotes a file
     *
     * @param typedObject The typed object to check
     * @return <code>true</code> if typed object denotes a file; otherwise <code>false</code>
     */
    protected static boolean isFile(BoxTypedObject typedObject) {
        return null != typedObject && TYPE_FILE.equals(typedObject.getType());
    }

    /**
     * Checks if given typed object denotes a folder
     *
     * @param typedObject The typed object to check
     * @return <code>true</code> if typed object denotes a folder; otherwise <code>false</code>
     */
    protected static boolean isFolder(BoxTypedObject typedObject) {
        return null != typedObject && TYPE_FOLDER.equals(typedObject.getType());
    }

    /**
     * Checks if given typed object is trashed
     *
     * @param boxFile The typed object to check
     * @return <code>true</code> if typed object is trashed; otherwise <code>false</code>
     */
    protected static boolean isTrashed(BoxFile boxFile) {
        return null != boxFile.getTrashedAt();
    }

    /**
     * Checks if given typed object is trashed
     *
     * @param folder The typed object to check
     * @return <code>true</code> if typed object is trashed; otherwise <code>false</code>
     */
    protected static boolean isTrashed(BoxFolder folder) {
        return hasTrashParent(folder);
    }

    /**
     * Checks if given typed object is trashed
     *
     * @param typedObject The typed object to check
     * @return <code>true</code> if typed object is trashed; otherwise <code>false</code>
     */
    protected static boolean isTrashed(BoxTypedObject typedObject) {
        if (isFile(typedObject)) {
            return null != ((BoxFile) typedObject).getTrashedAt();
        } else if (isFolder(typedObject)) {
            return hasTrashParent((BoxFolder) typedObject);
        }
        return false;
    }

    private static boolean hasTrashParent(BoxFolder boxFolder) {
        BoxFolder parent = boxFolder.getParent();
        if (null == parent) {
            return false;
        }
        if ("trash".equals(parent.getId())) {
            return true;
        }
        return hasTrashParent(parent);
    }

    /**
     * Handles authentication error.
     *
     * @param e The authentication error
     * @param session The associated session
     * @return The re-initialized Box.com access
     * @throws OXException If authentication error could not be handled
     */
    protected BoxOAuthAccess handleAuthError(BoxSDKException e, Session session) throws OXException {
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
     * Handles given REST error.
     *
     * @param e The REST error
     * @return The resulting exception
     */
    protected static OXException handleRestError(BoxRestException e) {
        Throwable cause = e.getCause();

        if (cause instanceof BoxServerException) {
            BoxServerException bx = (BoxServerException) cause;
            return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()) + " " + bx.getCustomMessage());
        }

        if (cause instanceof IOException) {
            return FileStorageExceptionCodes.IO_ERROR.create(cause, cause.getMessage());
        }

        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, BoxConstants.ID, e.getMessage());
    }

    /** Status code (401) indicating that the request requires HTTP authentication. */
    private static final int SC_UNAUTHORIZED = 401;

    /** Status code (404) indicating that the requested resource is not available. */
    private static final int SC_NOT_FOUND = 404;

    /**
     * Handles given HTTP response error.
     *
     * @param identifier The optional identifier for associated Box.com resource
     * @param e The HTTP error
     * @return The resulting exception
     */
    protected OXException handleHttpResponseError(String identifier, String accountId, BoxServerException e) {
        if (null != identifier && SC_NOT_FOUND == e.getStatusCode()) {
            return FileStorageExceptionCodes.NOT_FOUND.create(e, "Box", identifier);
        }
        if (null != accountId && SC_UNAUTHORIZED == e.getStatusCode()) {
            return FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(e, accountId, BoxConstants.ID);
        }
        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()) + " " + e.getCustomMessage());
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

}
