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

package com.openexchange.chronos.provider.groupware;

import java.util.Date;
import java.util.Map;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link GroupwareCalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public interface GroupwareCalendarFolder extends CalendarFolder {

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
     * Gets information about the entity which lastly modified this folder.
     *
     * @return The entity which lastly modified this folder, or <code>null</code> if not defined
     */
    EntityInfo getModifiedFrom();

    /**
     * Gets information about the entity which created this folder.
     *
     * @return The entity which created this folder, or <code>null</code> if not defined
     */
    EntityInfo getCreatedFrom();

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
