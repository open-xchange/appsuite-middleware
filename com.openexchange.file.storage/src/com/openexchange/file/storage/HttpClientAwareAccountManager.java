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

package com.openexchange.file.storage;

import java.net.CookieStore;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.httpclient.util.HttpContextUtils;
import com.openexchange.session.Session;

/**
 * {@link HttpClientAwareAccountManager} is a {@link SecretAwareFileStorageAccountManager} which also removes the {@link CookieStore} in case the account is deleted or updated.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class HttpClientAwareAccountManager extends SecretAwareFileStorageAccountManager {

    /**
     * Gets a new {@code HttpClientAwareAccountManager} instance.
     *
     * @param manager The backing account manager
     * @return The http client aware account manager or <code>null</code>
     */
    public static HttpClientAwareAccountManager newInstanceFor(FileStorageAccountManager manager) {
        return null == manager ? null : new HttpClientAwareAccountManager(manager);
    }

    /**
     * Initializes a new {@link HttpClientAwareAccountManager}.
     */
    private HttpClientAwareAccountManager(FileStorageAccountManager manager) {
        super(manager);
    }

    @Override
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        super.deleteAccount(account, session);
        HttpContextUtils.removeCookieStore(session, account.getId());
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        super.updateAccount(account, session);
        HttpContextUtils.removeCookieStore(session, account.getId());
    }

}
