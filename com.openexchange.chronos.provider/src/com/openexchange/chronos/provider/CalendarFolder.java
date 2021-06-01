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

package com.openexchange.chronos.provider;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarFolder {

    /**
     * Gets the identifier of the calendar folder.
     *
     * @return The folder identifier
     */
    String getId();

    /**
     * Gets the name of the calendar folder.
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
     * Gets the last modification date of the calendar.
     *
     * @return The last modification date, or <code>null</code> if not defined
     */
    Date getLastModified();

    /**
     * Gets the permissions
     *
     * @return The permissions
     */
    List<CalendarPermission> getPermissions();

    /**
     * Gets the extended properties of the folder.
     * <p/>
     * See {@link CalendarFolderProperty} for a list of common folder properties evaluated by clients.
     *
     * @return The extended properties, or <code>null</code> if not defined
     */
    ExtendedProperties getExtendedProperties();

    /**
     * Gets the supported capabilities for a calendar access in this folder, describing the usable extended feature set.
     *
     * @return The supported calendar capabilities, or an empty set if no extended functionality is available
     */
    EnumSet<CalendarCapability> getSupportedCapabilites();

    /**
     * Gets a possible error in the underlying calendar account that prevents this calendar folder from operating normally.
     *
     * @return The account error, or <code>null</code> if there is none
     */
    OXException getAccountError();

}
