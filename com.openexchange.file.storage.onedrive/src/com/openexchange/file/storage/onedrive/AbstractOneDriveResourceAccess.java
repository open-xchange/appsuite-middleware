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

package com.openexchange.file.storage.onedrive;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.onedrive.access.OneDriveOAuthAccess;
import com.openexchange.file.storage.onedrive.osgi.Services;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.session.Session;

/**
 * {@link AbstractOneDriveResourceAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOneDriveResourceAccess {

    private final OneDriveOAuthAccess oneDriveAccess;
    protected final Session session;
    protected final MicrosoftGraphDriveService driveService;

    /**
     * Initializes a new {@link AbstractOneDriveResourceAccess}.
     * 
     * @throws OXException if the {@link MicrosoftGraphDriveService} is absent
     */
    protected AbstractOneDriveResourceAccess(OneDriveOAuthAccess oneDriveAccess, Session session) throws OXException {
        super();
        this.oneDriveAccess = oneDriveAccess;
        this.session = session;
        this.driveService = Services.getServiceSafe(MicrosoftGraphDriveService.class);
    }

    /**
     * Performs given closure.
     *
     * @param closure The closure to perform
     * @param httpClient The client to use
     * @return The return value
     * @throws OXException If performing closure fails
     */
    protected <R> R perform(OneDriveClosure<R> closure) throws OXException {
        return closure.perform(this, session);
    }

    /**
     * Handles authentication error.
     *
     * @param e The authentication error
     * @param session The associated session
     * @throws OXException If authentication error could not be handled
     */
    protected void handleAuthError(OXException e, Session session) throws OXException {
        try {
            oneDriveAccess.initialize();
        } catch (OXException oxe) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractOneDriveResourceAccess.class);
            logger.warn("Could not re-initialize Microsoft Graph OneDrive access", oxe);

            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, OneDriveConstants.ID, e.getMessage());
        }
    }

    /**
     * Gets the OneDrive folder identifier from given file storage folder identifier
     *
     * @param folderId The file storage folder identifier
     * @return The appropriate OneDrive folder identifier
     * @throws OXException If operation fails
     */
    protected String toOneDriveFolderId(String folderId) throws OXException {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? driveService.getRootFolderId(getAccessToken()) : folderId;
    }

    /**
     * Gets the file storage folder identifier from given OneDrive folder identifier
     *
     * @param oneDriveId The OneDrive folder identifier
     * @return The appropriate file storage folder identifier
     * @throws OXException If operation fails
     */
    protected String toFileStorageFolderId(String oneDriveId) throws OXException {
        return driveService.getRootFolderId(getAccessToken()).equals(oneDriveId) ? FileStorageFolder.ROOT_FULLNAME : oneDriveId;
    }

    /**
     * Returns the OAuth access token
     * 
     * @return the OAuth access token
     */
    protected String getAccessToken() {
        return oneDriveAccess.getOAuthAccount().getToken();
    }
}
