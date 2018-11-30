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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
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
 * {@link BackOffPerformer}
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
    public BackOffPerformer(final GoogleDriveOAuthAccess googleDriveAccess, final FileStorageAccount account, final Session session) {
        super();
        this.googleDriveAccess = googleDriveAccess;
        this.account = account;
        this.session = session;
        this.retryCount = 0;
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    abstract T perform() throws OXException, IOException, RuntimeException;

    // ------------------------------------------------------------------------------------------------------------------------------- //

    protected T perform(String identifier) throws OXException {
        try {
            return perform();
        } catch (final IOException e) {
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
        } catch (final RuntimeException e) {
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
     * @param identifier The identifier of the resource that was asked fore, e.g. the folder identifier
     * @param e The {@link IOException}
     * @return An fitting {@link OXException}
     */
    //@formatter:on
    private OXException handleIOError(String identifier, IOException e) {
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
     * @param content The exceptions content
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
