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

package com.openexchange.file.storage.onedrive;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
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
    private final Session session;
    protected final MicrosoftGraphDriveService driveService;

    /**
     * Initializes a new {@link AbstractOneDriveResourceAccess}.
     */
    protected AbstractOneDriveResourceAccess(OneDriveOAuthAccess oneDriveAccess, FileStorageAccount account, Session session) {
        super();
        this.oneDriveAccess = oneDriveAccess;
        this.session = session;
        this.driveService = Services.getService(MicrosoftGraphDriveService.class);
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
