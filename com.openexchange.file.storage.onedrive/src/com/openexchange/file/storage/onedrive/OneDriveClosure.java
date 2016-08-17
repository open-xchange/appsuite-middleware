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

import java.io.IOException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link OneDriveClosure}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
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
     * Performs the actual operation
     *
     * @param httpClient The HTTP client to use
     * @return The return value
     * @throws OXException If an Open-Xchange error occurred
     * @throws JSONException If a JSON error occurs
     * @throws IOException If an I/O error occurred
     */
    protected abstract R doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException;

    /**
     * Performs this closure's operation.
     *
     * @param resourceAccess The associated resource access
     * @param httpClient The HTTP client to use
     * @param session The associated session
     * @return The return value
     * @throws OXException If operation fails
     */
    public R perform(AbstractOneDriveResourceAccess resourceAccess, DefaultHttpClient httpClient, Session session) throws OXException {
        return null == resourceAccess ? innerPerform(false, null, httpClient, session) : innerPerform(true, resourceAccess, httpClient, session);
    }

    private R innerPerform(boolean handleAuthError, AbstractOneDriveResourceAccess resourceAccess, DefaultHttpClient httpClient, Session session) throws OXException {
        try {
            return doPerform(httpClient);
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed -- recreate token
                if (!handleAuthError) {
                    throw FileStorageExceptionCodes.AUTHENTICATION_FAILED.create(e, resourceAccess.account.getId(), OneDriveConstants.ID, e.getMessage());
                }
                resourceAccess.handleAuthError(e, session);
                return innerPerform(false, resourceAccess, httpClient, session);
            }
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()), e.getMessage());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
