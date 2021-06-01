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

package com.openexchange.file.storage.dropbox.access;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import com.dropbox.core.BadRequestException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.ProtocolException;
import com.dropbox.core.RateLimitException;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderContinueErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListRevisionsErrorException;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.RestoreErrorException;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.ThumbnailErrorException;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteConflictError;
import com.dropbox.core.v2.files.WriteError;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.session.Session;

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
     * @param session The groupware {@link Session}
     * @param oauthAccount The {@link OAuthAccount}
     * @return An {@link OXException}
     */
    static final OXException handle(Exception e, Session session, OAuthAccount oauthAccount) {
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
            API api = oauthAccount.getAPI();
            return OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(api.getDisplayName(), I(oauthAccount.getId()), I(session.getUserId()), I(session.getContextId()));
        }

        // Bad request
        if (BadRequestException.class.isInstance(e)) {
            String message = e.getMessage();
            if (null != message && message.indexOf("access token is malformed") >= 0) {
                API api = oauthAccount.getAPI();
                return OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(api.getDisplayName(), I(oauthAccount.getId()), I(session.getUserId()), L(session.getContextId()));
            }
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
     * Handles the {@link ListFolderContinueErrorException}
     *
     * @param e The {@link ListFolderContinueErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleListFolderContinueErrorException(ListFolderContinueErrorException e, String folderId) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapLookupError(e.errorValue.getPathValue(), folderId, "", e);
            case RESET:
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
     * @param accountDisplayName The display name of the associated account
     * @return The {@link OXException}
     */
    static final OXException handleRelocationErrorException(RelocationErrorException e, String folderId, String fileId, String accountDisplayName) {
        switch (e.errorValue.tag()) {
            case FROM_LOOKUP:
                return mapLookupError(e.errorValue.getFromLookupValue(), folderId, fileId, e);
            case FROM_WRITE:
                return mapWriteError(e.errorValue.getFromWriteValue(), folderId, accountDisplayName, e);
            case CANT_COPY_SHARED_FOLDER:
            case CANT_MOVE_FOLDER_INTO_ITSELF:
            case CANT_NEST_SHARED_FOLDER:
            case OTHER:
            case TO:
                {
                    WriteError error = e.errorValue.getToValue();
                    if (WriteConflictError.FILE == error.getConflictValue()) {
                        return mapWriteError(error, fileId, accountDisplayName, e);
                    }
                    return mapWriteError(error, folderId, accountDisplayName, e);
                }
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
     * @param accountDisplayName The display name of the associated account
     * @return The {@link OXException}
     */
    static final OXException handleRestoreErrorException(RestoreErrorException e, String folderId, String fileId, String accountDisplayName) {
        switch (e.errorValue.tag()) {
            case PATH_LOOKUP:
                return mapLookupError(e.errorValue.getPathLookupValue(), folderId, fileId, e);
            case PATH_WRITE:
                return mapWriteError(e.errorValue.getPathWriteValue(), folderId, accountDisplayName, e);
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
     * @param accountDisplayName The display name of the associated account
     * @return The {@link OXException}
     */
    static final OXException handleUploadErrorException(UploadErrorException e, String folderId, String accountDisplayName) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapWriteError(e.errorValue.getPathValue().getReason(), folderId, accountDisplayName, e);
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
     * @param accountDisplayName The display name of the associated account
     * @return The {@link OXException}
     */
    static final OXException handleDeleteErrorException(DeleteErrorException e, String folderId, String fileId, String accountDisplayName) {
        switch (e.errorValue.tag()) {
            case PATH_LOOKUP:
                return mapLookupError(e.errorValue.getPathLookupValue(), folderId, fileId, e);
            case PATH_WRITE:
                return mapWriteError(e.errorValue.getPathWriteValue(), folderId, accountDisplayName, e);
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

    /**
     * Handles the {@link CreateFolderErrorException}
     *
     * @param e The {@link CreateFolderErrorException}
     * @param folderId The folder identifier used to trigger the error
     * @param accountDisplayName The display name of the associated account
     * @return The {@link OXException}
     */
    static final OXException handleCreateFolderErrorException(CreateFolderErrorException e, String folderId, String accountDisplayName) {
        switch (e.errorValue.tag()) {
            case PATH:
                return mapWriteError(e.errorValue.getPathValue(), folderId, accountDisplayName, e);
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
     * @param id The object's identifier used to trigger the error
     * @param accountDisplayName The display name of the associated account
     * @param e The next {@link Exception}
     * @return An {@link OXException}
     */
    private static final OXException mapWriteError(WriteError error, String id, String accountDisplayName, Exception e) {
        switch (error.tag()) {
            case DISALLOWED_NAME:
                return FileStorageExceptionCodes.ILLEGAL_CHARACTERS.create(e, id);
            case INSUFFICIENT_SPACE:
                return FileStorageExceptionCodes.QUOTA_REACHED.create(e);
            case NO_WRITE_PERMISSION:
                return FileStorageExceptionCodes.NO_CREATE_ACCESS.create(e, id);
            case CONFLICT:
                {
                    if (WriteConflictError.FILE == error.getConflictValue()) {
                        return FileStorageExceptionCodes.FILE_ALREADY_EXISTS.create(e, id);
                    }

                    int slashPos = id.lastIndexOf('/');
                    String name;
                    String parentName;
                    if (slashPos > 0) {
                        String parentId = id.substring(0, slashPos);

                        name = id.substring(slashPos + 1);
                        parentName = parentId.substring(parentId.lastIndexOf('/') + 1);
                        if (Strings.isEmpty(parentName)) {
                            parentName = accountDisplayName;
                        }
                    } else {
                        name = id;
                        parentName = accountDisplayName;
                    }

                    return FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, name, parentName);
                }
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
        if (Strings.isNotEmpty(fileId)) {
            path = new StringBuilder(path).append('/').append(fileId).toString();
        }
        return path;
    }
}
