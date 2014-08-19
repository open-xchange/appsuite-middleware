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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.file.storage.googledrive;

import java.io.IOException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.Labels;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.googledrive.access.GoogleDriveAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractGoogleDriveAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractGoogleDriveAccess {

    private static final String MIME_TYPE_DIRECTORY = GoogleDriveConstants.MIME_TYPE_DIRECTORY;
    // private static final String QUERY_STRING_DIRECTORIES_ONLY = GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY;

    protected final GoogleDriveAccess googleDriveAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected final String rootFolderId;

    /**
     * Initializes a new {@link AbstractGoogleDriveAccess}.
     */
    protected AbstractGoogleDriveAccess(final GoogleDriveAccess googleDriveAccess, final FileStorageAccount account, final Session session) throws OXException {
        super();
        this.googleDriveAccess = googleDriveAccess;
        this.account = account;
        this.session = session;

        try {
            Drive drive = googleDriveAccess.getDrive();
            rootFolderId = drive.files().get("root").execute().getId();
        } catch (HttpResponseException e) {
            throw handleHttpResponseError(null, e);
        } catch (IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles given HTTP response error.
     *
     * @param identifier The option identifier for associated Google Drive resource
     * @param e The HTTP error
     * @return The resulting exception
     */
    protected OXException handleHttpResponseError(String identifier, HttpResponseException e) {
        if (null != identifier && 404 == e.getStatusCode()) {
            return GoogleDriveExceptionCodes.NOT_FOUND.create(e, identifier);
        }

        return GoogleDriveExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(e.getStatusCode()), e.getStatusMessage());
    }

    /**
     * Checks if given Google Drive resource is trashed.
     *
     * @param id The Google Drive identifier
     * @param drive The drive reference
     * @return <code>true</code> if trashed; otherwise <code>false</code>
     * @throws IOException If check fails
     */
    protected static boolean isTrashed(String id, Drive drive) throws IOException {
        Labels labels = drive.files().get(id).execute().getLabels();
        if (null == labels) {
            return false;
        }
        Boolean trashed = labels.getTrashed();
        return null != trashed && trashed.booleanValue();
    }

    /**
     * Checks if given file is a directory.
     *
     * @param file The file to check
     * @return <code>true</code> if file is a directory, otherwise <code>false</code>
     */
    protected static boolean isDir(File file) {
        return MIME_TYPE_DIRECTORY.equals(file.getMimeType());
    }

    /**
     * Checks if given file/directory is trashed
     *
     * @param file The file/directory to check
     * @throws OXException If file/directory is trashed
     */
    protected void checkIfTrashed(com.google.api.services.drive.model.File file) throws OXException {
        Boolean explicitlyTrashed = file.getExplicitlyTrashed();
        if (null != explicitlyTrashed && explicitlyTrashed.booleanValue()) {
            throw GoogleDriveExceptionCodes.NOT_FOUND.create(file.getId());
        }
    }

    /**
     * Gets the Google Drive folder identifier from given file storage folder identifier
     *
     * @param folderId The file storage folder identifier
     * @return The appropriate Google Drive folder identifier
     */
    protected String toGoogleDriveFolderId(String folderId) {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? rootFolderId : folderId;
    }

    /**
     * Gets the file storage folder identifier from given Google Drive folder identifier
     *
     * @param googleId The Google Drive folder identifier
     * @return The appropriate file storage folder identifier
     */
    protected String toFileStorageFolderId(String googleId) {
        return rootFolderId.equals(googleId) || "root".equals(googleId) ? FileStorageFolder.ROOT_FULLNAME : googleId;
    }

}
