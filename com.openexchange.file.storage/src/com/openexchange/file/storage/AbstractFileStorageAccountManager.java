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

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AbstractFileStorageAccountManager} - The abstract account manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFileStorageAccountManager implements FileStorageAccountManager {

    /**
     * The factory.
     */
    protected final FileStorageServiceFactory factory;

    /**
     * Initializes a new {@link AbstractFileStorageAccountManager}.
     *
     * @param factory The factory
     */
    protected AbstractFileStorageAccountManager(final FileStorageServiceFactory factory) {
        this.factory = factory;
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        // Nope
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        // Nope
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        // Nope
    }

    @Override
    public boolean hasEncryptedItems(final Session session) throws OXException {
        return false;
    }

}
