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

package com.openexchange.pop3.storage;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.pop3.POP3Access;

/**
 * {@link POP3StorageProvider} - Provider for POP3 storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface POP3StorageProvider {

    /**
     * Gets an appropriate POP3 storage.
     *
     * @param pop3Access The POP3 access to which the storage shall be bound
     * @param properties The properties for the storage; especially the POP3 {@link POP3StoragePropertyNames#PROPERTY_PATH path}
     * @return An appropriate POP3 storage
     * @throws OXException If no such storage can be found
     * @see POP3StoragePropertyNames
     */
    public POP3Storage getPOP3Storage(POP3Access pop3Access, POP3StorageProperties properties) throws OXException;

    /**
     * Gets the appropriate POP3 storage properties.
     *
     * @param pop3Access The POP3 access to which the storage properties belong
     * @return The appropriate POP3 storage properties
     * @throws OXException If no such storage properties can be found
     */
    public POP3StorageProperties getPOP3StorageProperties(POP3Access pop3Access) throws OXException;

    /**
     * Gets the POP3 storage name.
     *
     * @return The POP3 storage name
     */
    public String getPOP3StorageName();

    /**
     * Gets the {@link MailAccountDeleteListener delete listeners} for this provider.
     *
     * @return The {@link MailAccountDeleteListener delete listeners} or an empty list
     */
    public List<MailAccountDeleteListener> getDeleteListeners();

    /**
     * Indicates whether to unregister {@link MailAccountDeleteListener delete listeners} on provider's absence.
     *
     * @return <code>true</code> to unregister {@link MailAccountDeleteListener delete listeners} on provider's absence; otherwise
     *         <code>false</code>
     */
    public boolean unregisterDeleteListenersOnAbsence();

}
