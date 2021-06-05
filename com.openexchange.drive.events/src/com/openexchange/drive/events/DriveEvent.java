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

package com.openexchange.drive.events;

import java.util.List;
import java.util.Set;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;

/**
 * {@link DriveEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveEvent {

    /**
     * Gets the consecutive actions to be executed by the client, based on the supplied root folder identifier(s).
     * <p/>
     * In case concrete directory versions are included in the actions, a <i>synthetic</i> random checksum may get applied as the actual
     * value is unknown at this stage.
     *
     * @param rootFolderIDs The root folder IDs the client is interested in.
     * @param useContentChanges <code>true</code> to prefer separate SYNC actions for content changes where possible, <code>false</code>, otherwise
     * @return The client actions
     */
    List<DriveAction<? extends DriveVersion>> getActions(List<String> rootFolderIDs, boolean useContentChanges);

    /**
     * Gets the context ID.
     *
     * @return The context ID
     */
    int getContextID();

    /**
     * Gets the IDs of all affected folders.
     *
     * @return The folder IDs
     */
    Set<String> getFolderIDs();

    /**
     * Gets all tracked content changes within specific folders.
     *
     * @return The folder content changes, or an empty collection if there were none
     */
    List<DriveContentChange> getContentChanges();

    /**
     * Gets a value indicating whether this drive event is about folder content changes only or not.
     *
     * @return <code>true</code> if there are content changes only, <code>false</code>, otherwise
     */
    boolean isContentChangesOnly();

    /**
     * Gets a value indicating whether this event is originated from a remote backend node or not.
     *
     * @return <code>true</code> it this event is 'remote', <code>false</code>, otherwise
     */
    boolean isRemote();

    /**
     * Gets the drive push token if this event originates in a drive client. Only applicable if available in the drive session. A token
     * reference is either the push token itself, or the md5 checksum of that token, expressed as a lowercase hexadecimal number string.
     *
     * @return The push token reference of the device causing the event, or <code>null</code> if not applicable
     */
    String getPushTokenReference();

}
