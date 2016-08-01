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
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.WriteError;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.quota.QuotaExceptionCodes;

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
    static final OXException handleGetMetadataErrorException(GetMetadataErrorException e, boolean isFile, String folderId, String fileId) {
        return mapLookupError(e.errorValue.getPathValue(), isFile, folderId, fileId, e);
    }

    /**
     * Handles the {@link RelocationErrorException}
     * 
     * @param e The {@link RelocationErrorException}
     * @param isFile set to <code>true</code> if the exception was originated by a file lookup
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @return The {@link OXException}
     */
    static final OXException handleRelocationErrorException(RelocationErrorException e, boolean isFile, String folderId, String fileId) {
        switch (e.errorValue.tag()) {
            case FROM_LOOKUP:
                return mapLookupError(e.errorValue.getFromLookupValue(), isFile, folderId, fileId, e);
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
     * Maps the specified {@link LookupError} to a valid {@link FileStorageExceptionCodes}
     * 
     * @param error The {@link LookupError}
     * @param isFile set to <code>true</code> if the exception was originated by a file lookup
     * @param folderId The folder identifier used to trigger the error
     * @param fileId The file identifier used to trigger the error
     * @param e The nested {@link Exception}
     * @return An {@link OXException}
     */
    private static final OXException mapLookupError(LookupError error, boolean isFile, String folderId, String fileId, Exception e) {
        switch (error.tag()) {
            case NOT_FILE:
                return FileStorageExceptionCodes.NOT_A_FILE.create(e, DropboxConstants.ID, folderId + "/" + fileId);
            case NOT_FOLDER:
                return FileStorageExceptionCodes.NOT_A_FOLDER.create(e, DropboxConstants.ID, folderId);
            case NOT_FOUND:
                return FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, fileId, folderId);
            default:
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Maps the specified {@link WriteError} to a valid {@link FileStorageExceptionCodes} or {@link QuotaExceptionCodes}
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
                //FIXME: Is this useful or should we simply return an unexpected exception?
                return QuotaExceptionCodes.QUOTA_EXCEEDED.create(e);
            case NO_WRITE_PERMISSION:
                return FileStorageExceptionCodes.NO_CREATE_ACCESS.create(e, folderId);
            case CONFLICT:
                // Fall-through to 'unexpected error'
                // TODO: Maybe introduce a conflict exception
            default:
                return FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
