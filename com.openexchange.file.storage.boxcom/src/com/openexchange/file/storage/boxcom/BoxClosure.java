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

package com.openexchange.file.storage.boxcom;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import com.box.sdk.BoxAPIException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link BoxClosure}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public abstract class BoxClosure<R> {

    /**
     * Initializes a new {@link BoxClosure}.
     */
    public BoxClosure() {
        super();
    }

    /**
     * Performs the actual operation
     *
     * @return The return value
     * @throws OXException If an Open-Xchange error occurred
     * @throws BoxAPIException If a Box API error is occurred
     * @throws UnsupportedEncodingException If an encoding problem occurred
     */
    protected abstract R doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException;

    /**
     * Performs this closure's operation.
     *
     * @param resourceAccess The associated resource access or <code>null</code>
     * @param boxAccess The Box.com access to use
     * @param session The associated session
     * @return The return value
     * @throws OXException If operation fails
     */
    public R perform(AbstractBoxResourceAccess resourceAccess, BoxOAuthAccess boxAccess, Session session) throws OXException {
        return null == resourceAccess ? innerPerform(false, null, boxAccess, session) : innerPerform(true, resourceAccess, boxAccess, session);
    }

    /** Status code (401) indicating that the request requires HTTP authentication. */
    protected static final int SC_UNAUTHORIZED = 401;

    /** Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource. */
    protected static final int SC_CONFLICT = 409;

    /**
     * Performs the request
     *
     * @param <R> The result type
     * @param handleAuthError Whether to automatically handle any authentication errors
     * @param resourceAccess The resource access
     * @param boxAccess The Box access
     * @param session The groupware session
     * @return The result of the operation
     * @throws OXException if an error is occurred
     */
    private R innerPerform(boolean handleAuthError, AbstractBoxResourceAccess resourceAccess, BoxOAuthAccess boxAccess, Session session) throws OXException {
        try {
            return doPerform();
        } catch (BoxAPIException e) {
            int statusCode = e.getResponseCode();
            if (resourceAccess != null && statusCode == SC_UNAUTHORIZED) {
                if (handleAuthError) {
                    BoxOAuthAccess newBoxAccess = resourceAccess.handleAuthError(e, session);
                    return innerPerform(false, resourceAccess, newBoxAccess, session);
                }
                throw FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(e, resourceAccess.account.getId(), BoxConstants.ID, e.getMessage());
            }
            if (e.getCause() instanceof IOException) {
                throw handleRestError(e);
            }
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", statusCode + " " + e.getResponse());
        } catch (UnsupportedEncodingException | RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles given API error.
     *
     * @param e The {@link BoxAPIException} error
     * @return The resulting exception
     */
    private OXException handleRestError(BoxAPIException e) {
        Throwable cause = e.getCause();

        if (cause == null) {
            return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getResponseCode() + " " + e.getResponse());
        }

        if (cause instanceof IOException) {
            return FileStorageExceptionCodes.IO_ERROR.create(cause, cause.getMessage());
        }

        return FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, BoxConstants.ID, e.getMessage());
    }

}
