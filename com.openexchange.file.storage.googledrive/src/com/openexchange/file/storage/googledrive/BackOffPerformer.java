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

package com.openexchange.file.storage.googledrive;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.googledrive.access.GoogleDriveOAuthAccess;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.session.Session;

/**
 * {@link BackOffPerformer} - Implements the <a href="https://developers.google.com/analytics/devguides/reporting/core/v3/errors#backoff">exponential backoff</a> as described by Google.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
abstract class BackOffPerformer<T> {

    protected final GoogleDriveOAuthAccess googleDriveAccess;

    protected final Session session;

    protected final FileStorageAccount account;

    private int retryCount;

    /**
     * Initializes a new {@link BackOffPerformer}.
     * 
     * @param googleDriveAccess The {@link GoogleDriveAccountAccess} to use
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     */
    public BackOffPerformer(@NonNull GoogleDriveOAuthAccess googleDriveAccess, @NonNull FileStorageAccount account, @NonNull Session session) {
        super();
        this.googleDriveAccess = googleDriveAccess;
        this.account = account;
        this.session = session;
        this.retryCount = 0;
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Performs the actual request to the Drive API. Do <b>NOT</b> implement the backoff mechanism on your own. Use {@link #perform(String)}
     * 
     * @return The result for the Drive API call
     * @throws OXException In case some rate limit is exceeded. Wraps exceptions send by Google into OXExceptions.
     * @throws IOException In case call to Drive API fails
     * @throws RuntimeException In case call to Drive API fails
     */
    abstract T perform() throws OXException, IOException, RuntimeException;

    // ------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Performs the request to the Drive API by utilizing {@link #perform()}. <p>
     * If the request fails we try again after
     * <code>number_off_tries * 1 second + random_milliseconds()</code>
     * implementing the backoff mechanism described by Google.
     * 
     * @param identifier Identifier to use in the error {@link FileStorageExceptionCodes#FILE_NOT_FOUND}.
     *            If not set the error {@link FileStorageExceptionCodes#PROTOCOL_ERROR} is used.
     * @return The result for the Drive API call
     * @throws OXException In case some rate limit is exceeded. Wraps exceptions send by Google into OXExceptions.
     */
    public T perform(@Nullable String identifier) throws OXException {
        try {
            return perform();
        } catch (IOException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleIOError(identifier, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            if (++retryCount > 5) {
                // Exceeded max. retry count
                throw handleIOError(identifier, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return perform(identifier);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    //@formatter:off
    /**
     * Handles an {@link IOException} for a failed request <p>
     * 
     * The errors message is expected to look like
     * <pre> 
     * 400 Bad Request
     * {
     *  "code" : 400,
     *  "errors" : [ {
     *      "domain" : "global",
     *      "location" : "fields",
     *      "locationType" : "parameter",
     *      "message" : "Invalid field selection id.parents",
     *      "reason" : "invalidParameter"
     *  } ],
     *  "message" : "Invalid field selection id.parents"
     * }
     * </pre> 
     * 
     * 
     * 
     * @param identifier The identifier of the resource that was asked fore, e.g. the folder identifier. <p>
     *              For the error code {@link GoogleDriveConstants#SC_NOT_FOUND} following applies:
     *              If set the exception {@link FileStorageExceptionCodes#FILE_NOT_FOUND} is used, 
     *              If the identifier is <code>null</code> the exception {@link FileStorageExceptionCodes#PROTOCOL_ERROR} is used.
     * @param e The {@link IOException} to handle
     * @return An fitting {@link OXException}
     */
    //@formatter:on
    private OXException handleIOError(@Nullable String identifier, @NonNull IOException e) {
        String errorMessage = e.getMessage();
        int statusCode = GoogleDriveUtil.getStatusCode(e);
        switch (statusCode) {
            case GoogleDriveConstants.SC_CONFLICT:
                return FileStorageExceptionCodes.FILE_ALREADY_EXISTS.create();
            case GoogleDriveConstants.SC_BAD_REQUEST:
                if (hasInvalidGrant(errorMessage)) {
                    return createInvalidAccessTokenException();
                }
                return OAuthExceptionCodes.OAUTH_ERROR.create(e, errorMessage);
            case GoogleDriveConstants.SC_UNAUTHORIZED:
                if (hasInvalidGrant(errorMessage)) {
                    return createInvalidAccessTokenException();
                }
                return FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(account.getId(), GoogleDriveConstants.ID, errorMessage);
            case GoogleDriveConstants.SC_FORBIDDEN:
                // See https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors
                if (errorMessage.indexOf("userRateLimitExceeded") > 0) {
                    return FileStorageExceptionCodes.STORAGE_RATE_LIMIT.create(e, new Object[0]);
                }
                if (errorMessage.indexOf("insufficientPermissions") > 0) {
                    return OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(e, KnownApi.GOOGLE.getDisplayName(), OXScope.drive.getDisplayName());
                }
                break;
            case GoogleDriveConstants.SC_NOT_FOUND:
                if (null != identifier) {
                    return FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, identifier, "");
                }
                break;
            default:
                // Use fallback
                break;
        }
        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", statusCode + " " + e.getMessage());
    }

    /**
     * Creates an access token invalid {@link OXException}
     *
     * @return The {@link OXException}
     */
    private OXException createInvalidAccessTokenException() {
        OAuthAccount oAuthAccount = googleDriveAccess.getOAuthAccount();
        API api = oAuthAccount.getAPI();
        return OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(api.getDisplayName(), I(oAuthAccount.getId()), I(session.getUserId()), I(session.getContextId()));
    }

    /**
     * Determines whether the specified {@link IOException} is caused due to an 'invalid_grant'.
     *
     * @param content The optional exceptions content
     * @return <code>true</code> if the exception was caused due to an 'invalid_grant'; <code>false</code>
     *         otherwise
     */
    private boolean hasInvalidGrant(String content) {
        return Strings.isNotEmpty(content) && content.contains("invalid_grant");
    }

    /**
     * Checks if specified {@link IOException} instance denoted a rate limit exception.
     *
     * @param e The exception to check
     * @return <code>true</code> if user hit a rate limit exception; otherwise <code>false</code>
     */
    private boolean isUserRateLimitExceeded(IOException e) {
        if (GoogleDriveConstants.SC_FORBIDDEN == GoogleDriveUtil.getStatusCode(e)) {
            // See https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors
            if (e.getMessage().indexOf("userRateLimitExceeded") > 0) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

}
