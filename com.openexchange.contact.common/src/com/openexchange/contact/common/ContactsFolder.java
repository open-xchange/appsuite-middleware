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

package com.openexchange.contact.common;

import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ContactsFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface ContactsFolder {

    /**
     * Gets the identifier of the contacts folder.
     *
     * @return The folder identifier
     */
    String getId();

    /**
     * Gets the name of the contacts folder.
     *
     * @return The folder name
     */
    String getName();

    /**
     * Gets a value indicating whether the folder is actually subscribed or not.
     *
     * @return <code>true</code> if the folder is subscribed, <code>false</code>, otherwise
     */
    Boolean isSubscribed();

    /**
     * Gets a value indicating whether the folder is used for sync or not.
     *
     * @return the {@link UsedForSync} value
     */
    UsedForSync getUsedForSync();

    /**
     * Gets the last modification date of the contacts.
     *
     * @return The last modification date, or <code>null</code> if not defined
     */
    Date getLastModified();

    /**
     * Gets the extended properties of the folder.
     *
     * @return The extended properties, or <code>null</code> if not defined
     */
    ExtendedProperties getExtendedProperties();

    /**
     * Gets a possible error in the underlying contacts account that prevents this contacts folder from operating normally.
     *
     * @return The account error, or <code>null</code> if there is none
     */
    OXException getAccountError();

    /**
     * Gets the permissions
     *
     * @return The permissions
     */
    List<ContactsPermission> getPermissions();
}
