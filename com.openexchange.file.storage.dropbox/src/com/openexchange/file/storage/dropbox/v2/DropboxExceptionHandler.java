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

package com.openexchange.file.storage.dropbox.v2;

import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.ProtocolException;
import com.dropbox.core.RateLimitException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListRevisionsErrorException;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.RestoreErrorException;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.ThumbnailErrorException;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteError;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.java.Strings;

/**
 * {@link DropboxExceptionHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class DropboxExceptionHandler {

    /**
     * Handles the specified Exception and returns an appropriate {@link OXException}
     * 
     * @param e The {@link Exception} to handle
     * @return An {@link OXException}
     */
    //TODO: Complete exception handling
    static final OXException handle(Exception e) {
        // It's an OXException, so return it
        if (OXException.class.isInstance(e)) {
            return (OXException) e;
        }

        // Rate limiting
        if (RateLimitException.class.isInstance(e)) {
            return FileStorageExceptionCodes.STORAGE_RATE_LIMIT.create();
        }

        // Invalid token or account was unlinked
        if (InvalidAccessTokenException.class.isInstance(e)) {
            return FileStorageExceptionCodes.UNLINKED_ERROR.create(e, new Object[0]);
        }

        // Protocol error
        if (ProtocolException.class.isInstance(e)) {
            return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, DropboxConstants.ID, e.getMessage());
        }

        // Fall-back
        return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
    }

    /**
     * Handles the {@link GetMetadataErrorException}
     * 
     * @param e The {@link GetMetadataErrorException}
     * @param folderId The folder identifier used to trigger the {@link GetMetadataErrorException}
     * @param fileId The file identifier used to trigger the {@link GetMetadataErrorException}
     * @return An {@link OXException}
     */
    static final OXException handleGetMetadataErrorException(GetMetadataErrorException e, String folderId, String fileId) {
        return mapLookupError(e.errorValue.getPathValue(), folderId, fileId, e);
    }

    /**
     * Handles the {@link ListFolderErrorException}
     * 
     * @param e The {@link ListFolderErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleListFolderErrorException(ListFolderErrorException e, String folderId) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapLookupError(e.errorValue.getPathValue(), folderId, "", e);
            case OTHER:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link RelocationErrorException}
     * 
     * @param e The {@link RelocationErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleRelocationErrorException(RelocationErrorException e, String folderId, String fileId) {
        switch (e.errorValue.tag()) {
            case FROM_LOOKUP:
                return mapLookupError(e.errorValue.getFromLookupValue(), folderId, fileId, e);
            case FROM_WRITE:
                return mapWriteError(e.errorValue.getFromWriteValue(), folderId, e);
            case CANT_COPY_SHARED_FOLDER:
            case CANT_MOVE_FOLDER_INTO_ITSELF:
            case CANT_NEST_SHARED_FOLDER:
            case OTHER:
            case TO:
            case TOO_MANY_FILES:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link RestoreErrorException}
     * 
     * @param e The {@link RestoreErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleRestoreErrorException(RestoreErrorException e, String folderId, String fileId) {
        switch (e.errorValue.tag()) {
            case PATH_LOOKUP:
                return mapLookupError(e.errorValue.getPathLookupValue(), folderId, fileId, e);
            case PATH_WRITE:
                return mapWriteError(e.errorValue.getPathWriteValue(), folderId, e);
            case INVALID_REVISION:
            case OTHER:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link UploadErrorException}
     * 
     * @param e The {@link UploadErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleUploadErrorException(UploadErrorException e, String folderId) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapWriteError(e.errorValue.getPathValue().getReason(), folderId, e);
            case OTHER:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link DownloadErrorException}
     * 
     * @param e The {@link DownloadErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleDownloadErrorException(DownloadErrorException e, String folderId, String fileId) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapLookupError(e.errorValue.getPathValue(), folderId, fileId, e);
            case OTHER:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link ListRevisionsErrorException}
     * 
     * @param e The {@link ListRevisionsErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleListRevisionsErrorException(ListRevisionsErrorException e, String folderId, String fileId) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapLookupError(e.errorValue.getPathValue(), folderId, fileId, e);
            case OTHER:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link SearchErrorException}
     * 
     * @param e The {@link SearchErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleSearchErrorException(SearchErrorException e, String folderId, String pattern) {
        switch (e.errorValue.tag()) {
            case PATH:
                // FIXME: Use the 'pattern' for 'fileId' in the mapLookupError?
                return mapLookupError(e.errorValue.getPathValue(), folderId, pattern, e);
            case OTHER:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link DeleteErrorException}
     * 
     * @param e The {@link DeleteErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleDeleteErrorException(DeleteErrorException e, String folderId, String fileId) {
        switch (e.errorValue.tag()) {
            case PATH_LOOKUP:
                return mapLookupError(e.errorValue.getPathLookupValue(), folderId, fileId, e);
            case PATH_WRITE:
                return mapWriteError(e.errorValue.getPathWriteValue(), folderId, e);
            case OTHER:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles the {@link ThumbnailErrorException}
     * 
     * @param e The {@link ThumbnailErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleThumbnailErrorException(ThumbnailErrorException e, String folderId, String fileId) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapLookupError(e.errorValue.getPathValue(), folderId, fileId, e);
            case UNSUPPORTED_EXTENSION:
            case UNSUPPORTED_IMAGE:
            case CONVERSION_ERROR:
            default:
                // Everything else falls through to 'unexpected error' 
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    //////////////////////////////////////////// HELPERS //////////////////////////////////////////////////

    /**
     * Maps the specified {@link LookupError} to a valid {@link FileStorageExceptionCodes}
     * 
     * @param error The {@link LookupError}
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @param e The nested {@link Exception}
     * @return An {@link OXException}
     */
    private static final OXException mapLookupError(LookupError error, String folderId, String fileId, Exception e) {
        switch (error.tag()) {
            case NOT_FILE:
                return FileStorageExceptionCodes.NOT_A_FILE.create(e, DropboxConstants.ID, toPath(folderId, fileId));
            case NOT_FOLDER:
                return FileStorageExceptionCodes.NOT_A_FOLDER.create(e, DropboxConstants.ID, folderId);
            case NOT_FOUND:
                return FileStorageExceptionCodes.NOT_FOUND.create(e, DropboxConstants.ID, toPath(folderId, fileId));
            default:
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Maps the specified {@link WriteError} to a valid {@link FileStorageExceptionCodes}
     * 
     * @param error The {@link WriteError}
     * @param folderId The folder identifier used to trigger the error
     * @param e The next {@link Exception}
     * @return An {@link OXException}
     */
    private static final OXException mapWriteError(WriteError error, String folderId, Exception e) {
        switch (error.tag()) {
            case DISALLOWED_NAME:
                return FileStorageExceptionCodes.ILLEGAL_CHARACTERS.create(e, folderId);
            case INSUFFICIENT_SPACE:
                return FileStorageExceptionCodes.QUOTA_REACHED.create(e);
            case NO_WRITE_PERMISSION:
                return FileStorageExceptionCodes.NO_CREATE_ACCESS.create(e, folderId);
            case CONFLICT:
                // Fall through to 'unexpected error'
                // TODO: Maybe introduce a conflict exception
            default:
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Concatenates the specified folder identifier and the optional file identifier into a path
     * 
     * @param folderId The folder identifier
     * @param fileId The optional file identifier
     * @return The concatenated path
     */
    private static final String toPath(String folderId, String fileId) {
        String path = folderId;
        if (!Strings.isEmpty(fileId)) {
            path = new StringBuilder(path).append('/').append(fileId).toString();
        }
        return path;
    }
}
