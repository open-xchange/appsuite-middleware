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

package com.openexchange.file.storage.googledrive;

import java.io.IOException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.Labels;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.googledrive.access.GoogleDriveOAuthAccess;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.scope.Module;
import com.openexchange.session.Session;

/**
 * {@link AbstractGoogleDriveAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractGoogleDriveAccess {

    private static final String MIME_TYPE_DIRECTORY = GoogleDriveConstants.MIME_TYPE_DIRECTORY;
    // private static final String QUERY_STRING_DIRECTORIES_ONLY = GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY;

    protected final GoogleDriveOAuthAccess googleDriveAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected String rootFolderIdentifier;

    /**
     * Initializes a new {@link AbstractGoogleDriveAccess}.
     */
    protected AbstractGoogleDriveAccess(final GoogleDriveOAuthAccess googleDriveAccess, final FileStorageAccount account, final Session session) {
        super();
        this.googleDriveAccess = googleDriveAccess;
        this.account = account;
        this.session = session;
    }

    /**
     * Gets the root folder identifier
     *
     * @return The root folder identifier
     * @throws OXException If root folder cannot be returned
     */
    protected String getRootFolderId() throws OXException {
        String rootFolderId = rootFolderIdentifier;
        if (null == rootFolderId) {
            String key = "com.openexchange.file.storage.googledrive.rootFolderId";
            rootFolderId = (String) session.getParameter(key);
            if (null == rootFolderId) {
                try {
                    Drive drive = googleDriveAccess.<Drive> getClient().client;
                    rootFolderId = drive.files().get("root").execute().getId();
                    session.setParameter(key, rootFolderId);
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(null, e);
                } catch (IOException e) {
                    throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
            rootFolderIdentifier = rootFolderId;
        }
        return rootFolderId;
    }

    /** Status code (400) indicating the request sent by the client was syntactically incorrect. */
    protected static final int SC_BAD_REQUEST = 400;

    /** Status code (401) indicating that the request requires HTTP authentication. */
    protected static final int SC_UNAUTHORIZED = 401;

    /** Status code (403) indicating the server understood the request but refused to fulfill it. */
    protected static final int SC_FORBIDDEN = 403;

    /** Status code (404) indicating that the requested resource is not available. */
    protected static final int SC_NOT_FOUND = 404;

    /** Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource. */
    protected static final int SC_CONFLICT = 409;

    /**
     * Handles given HTTP response error.
     *
     * @param identifier The option identifier for associated Google Drive resource
     * @param e The HTTP error
     * @return The resulting exception
     */
    public OXException handleHttpResponseError(String identifier, HttpResponseException e) {
        if (null != identifier && SC_NOT_FOUND == e.getStatusCode()) {
            return FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, identifier, "");
        }

        if (SC_BAD_REQUEST == e.getStatusCode()) {
            if (hasInvalidGrant(e)) {
                return createInvalidAccessTokenException();
            }
            return OAuthExceptionCodes.OAUTH_ERROR.create(e.getMessage(), e);
        }

        if (SC_UNAUTHORIZED == e.getStatusCode()) {
            if (hasInvalidGrant(e)) {
                return createInvalidAccessTokenException();
            }
            return FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(account.getId(), GoogleDriveConstants.ID, e.getMessage());
        }

        if (SC_FORBIDDEN == e.getStatusCode()) {
            // See https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors
            if (e.getMessage().indexOf("userRateLimitExceeded") > 0) {
                return FileStorageExceptionCodes.STORAGE_RATE_LIMIT.create(e, new Object[0]);
            }
            if (e.getMessage().indexOf("insufficientPermissions") > 0) {
                return OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(e, API.GOOGLE.getShortName(), Module.drive.getDisplayName());
            }
        }

        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()) + " " + e.getStatusMessage());
    }

    /**
     * Creates an access token invalid {@link OXException}
     * 
     * @return The {@link OXException}
     */
    private OXException createInvalidAccessTokenException() {
        String cburl = OAuthUtil.buildCallbackURL(session, googleDriveAccess.getOAuthAccount());
        return OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(googleDriveAccess.getOAuthAccount().getAPI().getFullName(), cburl);
    }

    /**
     * Determines whether the specified {@link HttpResponseException} is caused due to an 'invalid_grant'.
     * 
     * @param e The {@link HttpResponseException}
     * @return <code>true</code> if the exception was caused due to an 'invalid_grant'; <code>false</code>
     *         otherwise
     */
    private boolean hasInvalidGrant(HttpResponseException e) {
        String content = e.getContent();
        return !Strings.isEmpty(content) && content.contains("invalid_grant");
    }

    /**
     * Checks if specified {@link HttpResponseException} instance denoted a rate limit exception.
     *
     * @param e The exception to check
     * @return <code>true</code> if user hit a rate limit exception; otherwise <code>false</code>
     */
    protected static boolean isUserRateLimitExceeded(HttpResponseException e) {
        if (SC_FORBIDDEN == e.getStatusCode()) {
            // See https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors
            if (e.getMessage().indexOf("userRateLimitExceeded") > 0) {
                return true;
            }
        }
        return false;
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
        Labels labels = drive.files().get(id).setFields("labels/trashed").execute().getLabels();
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
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(file.getId(), "");
        }
    }

    /**
     * Gets the Google Drive folder identifier from given file storage folder identifier
     *
     * @param folderId The file storage folder identifier
     * @return The appropriate Google Drive folder identifier
     * @throws OXException If operation fails
     */
    protected String toGoogleDriveFolderId(String folderId) throws OXException {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? getRootFolderId() : folderId;
    }

    /**
     * Gets the file storage folder identifier from given Google Drive folder identifier
     *
     * @param googleId The Google Drive folder identifier
     * @return The appropriate file storage folder identifier
     * @throws OXException If operation fails
     */
    protected String toFileStorageFolderId(String googleId) throws OXException {
        return getRootFolderId().equals(googleId) || "root".equals(googleId) ? FileStorageFolder.ROOT_FULLNAME : googleId;
    }

}
