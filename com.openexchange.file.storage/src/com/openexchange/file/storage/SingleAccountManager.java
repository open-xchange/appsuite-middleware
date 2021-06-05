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

import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SingleAccountManager} - Single account manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class SingleAccountManager extends AbstractFileStorageAccountManager {

    /**
     * Initializes a new {@link SingleAccountManager}.
     *
     * @param factory The factory
     */
    protected SingleAccountManager(final FileStorageServiceFactory factory) {
        super(factory);
    }

    @Override
    public String addAccount(final FileStorageAccount account, final Session session) throws OXException {
        return "1"; // IGNORE
    }

    @Override
    public void updateAccount(final FileStorageAccount account, final Session session) throws OXException {
        // IGNORE
    }

    @Override
    public void deleteAccount(final FileStorageAccount account, final Session session) throws OXException {
        // IGNORE
    }

    @Override
    public List<FileStorageAccount> getAccounts(final Session session) throws OXException {
        return Arrays.asList(getAccount(session));
    }

    @Override
    public FileStorageAccount getAccount(final String id, final Session session) throws OXException {
        return getAccount(session);
    }

    /**
     * Gets the account.
     *
     * @param session The session
     * @return The account
     */
    public abstract FileStorageAccount getAccount(Session session);

}
