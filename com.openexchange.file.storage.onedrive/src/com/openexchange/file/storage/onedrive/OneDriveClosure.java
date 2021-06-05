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

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.microsoft.graph.api.exception.MicrosoftGraphAPIExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link OneDriveClosure}
 *
 * @param <R> - The return type
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.6.1
 */
public abstract class OneDriveClosure<R> {

    /**
     * Initializes a new {@link OneDriveClosure}.
     */
    public OneDriveClosure() {
        super();
    }

    /**
     * Performs the actual operation.
     * 
     * @return The return value
     * @throws OXException if an error is occurred
     */
    protected abstract R doPerform() throws OXException;

    /**
     * Performs the operation and handles any authentication errors
     * 
     * @param resourceAccess
     * @param session
     * @return
     * @throws OXException
     */
    public R perform(AbstractOneDriveResourceAccess resourceAccess, Session session) throws OXException {
        try {
            return doPerform();
        } catch (OXException e) {
            if (resourceAccess == null) {
                throw e;
            }
            if (MicrosoftGraphAPIExceptionCodes.ACCESS_DENIED.equals(e) || MicrosoftGraphAPIExceptionCodes.UNAUTHENTICATED.equals(e)) {
                resourceAccess.handleAuthError(e, session);
                return perform(resourceAccess, session);
            }
            throw e;
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
