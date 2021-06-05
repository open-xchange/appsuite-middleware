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

import com.openexchange.session.Session;

/**
 * A {@link File} is usually requested in the name of a certain user. Thus the contained
 * information is potentially a user-centric view of that file and not globally valid. In
 * some situations it can become necessary to access parts of the contained information
 * from a non-user-centric perspective. This interface is meant to provide access to these
 * parts.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface UserizedFile extends File {

    /**
     * If this file is contained in a virtual folder of the user in question, it's global
     * ID might differ from the user-centric one. This method returns the original (i.e.
     * globally valid) ID. The user-centric ID is returned by {@link #getId()}.
     *
     * @return The original file ID; may be the same as {@link #getId()}, if the user-centric
     * ID is non-virtual.
     */
    String getOriginalId();

    /**
     * Sets the original file ID if the one set via {@link #setId(String)} is virtual.
     *
     * @param id The original ID; not <code>null</code>
     */
    void setOriginalId(String id);

    /**
     * If this file is contained in a virtual folder of the user in question, the folder
     * ID returned via {@link #getFolderId()} is the one of the virtual folder, but not the
     * one of the original folder where the file is physically located. This method returns
     * the original (i.e. physical) folder ID.
     *
     * @return The original folder ID; may be the same as {@link #getFolderId()}, if the user-
     * centric folder ID is non-virtual.
     */
    String getOriginalFolderId();

    /**
     * Sets the original folder ID if the one set via {@link #setFolderId(String)} is virtual.
     *
     * @param id The original ID; not <code>null</code>
     */
    void setOriginalFolderId(String id);

    /**
     * Gets the status of parsing/analyzing media meta-data from the media resource for the client
     *
     * @param session The client-associated session
     * @return The media status
     */
    MediaStatus getMediaStatusForClient(Session session);

}
