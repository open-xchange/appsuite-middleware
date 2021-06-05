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
import java.util.Map;

/**
 * {@link GroupwareContactsFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface GroupwareContactsFolder extends ContactsFolder {

    /**
     * Indicates if this folder is a default folder.
     *
     * @return <code>true</code> if this folder is a default folder; otherwise <code>false</code>
     */
    boolean isDefaultFolder();

    /**
     * Get the identifier of the parent folder
     *
     * @return The identifier of the parent or <code>null</code>
     */
    String getParentId();

    /**
     * Gets the entity which lastly modified this folder.
     *
     * @return The entity which lastly modified this folder or <code>-1</code>
     */
    int getModifiedBy();

    /**
     * Gets the entity which created this folder.
     *
     * @return The entity which created this folder or <code>-1</code>
     */
    int getCreatedBy();

    /**
     * Gets the creation date as {@link Date}.
     *
     * @return The creation date
     */
    Date getCreationDate();

    /**
     * The {@link GroupwareFolderType} of this folder. Possible values are
     * <ul>
     * <li> {@link GroupwareFolderType#PRIVATE} </li>
     * <li> {@link GroupwareFolderType#PUBLIC} </li>
     * <li> {@link GroupwareFolderType#SHARED} </li>
     * </ul>
     *
     * @return A {@link GroupwareFolderType}
     */
    GroupwareFolderType getType();

    /**
     * Gets additional arbitrary metadata associated with the folder.
     * 
     * @return The additional metadata, or <code>null</code> if not set
     */
    Map<String, Object> getMeta();
}
