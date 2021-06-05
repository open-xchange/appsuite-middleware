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

package com.openexchange.mail.api;

import javax.mail.Store;
import com.openexchange.exception.OXException;


/**
 * {@link IMailStoreAware} - Additional interface for {@link MailAccess} to get associated {@link Store} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public interface IMailStoreAware {

    /**
     * Checks if this instance supports {@link #getStore()} method.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isStoreSupported() throws OXException;

    /**
     * Gets the associated {@link Store} instance
     *
     * @return The associated {@link Store} instance
     * @throws OXException If {@link Store} instance cannot be returned; e.g. {@link MailAccess} is not connected
     */
    Store getStore() throws OXException;

}
