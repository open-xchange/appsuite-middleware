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

package com.openexchange.microsoft.graph.onedrive.parser;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.microsoft.graph.onedrive.OneDriveFolder;
import com.openexchange.microsoft.graph.onedrive.exception.MicrosoftGraphDriveServiceExceptionCodes;

/**
 * {@link OneDriveFolderParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class OneDriveFolderParser {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OneDriveFolderParser.class);

    /**
     * Initialises a new {@link OneDriveFolderParser}.
     */
    public OneDriveFolderParser() {
        super();
    }

    /**
     * Parses the specified {@link JSONObject} entity to an {@link OneDriveFolder}
     * 
     * @param userId The user identifier
     * @param hasSubFolders Whether the specified entity has any sub-folders
     * @param entity The {@link JSONObject}
     * @return The {@link OneDriveFolder} or <code>null</code> if the entity is not a folder
     * @throws OXException if the specified entity does not denote a folder
     */
    public @Nullable OneDriveFolder parseEntity(int userId, boolean hasSubFolders, @Nullable JSONObject entity) throws OXException {
        OneDriveFolder folder = new OneDriveFolder(userId);
        if (entity == null || entity.isEmpty()) {
            return folder;
        }
        if (false == entity.hasAndNotNull("folder")) {
            LOG.debug("The entity is missing the 'folder' field: {}", entity);
            throw MicrosoftGraphDriveServiceExceptionCodes.NOT_A_FOLDER.create();
        }
        folder.setId(entity.optString("id"));
        folder.setName(entity.optString("name"));
        folder.setRootFolder(entity.hasAndNotNull("root"));

        JSONObject parentRef = entity.optJSONObject("parentReference");
        if (parentRef != null && !parentRef.isEmpty()) {
            folder.setParentId(parentRef.optString("path").equals("/drive/root:") ? FileStorageFolder.ROOT_FULLNAME : parentRef.optString("id"));
        }

        if (folder.isRootFolder()) {
            // Override Microsoft's id with our default root id 
            // and retain the original in the metadata for future reference.
            folder.setMeta(Collections.singletonMap("id", folder.getId()));
            folder.setId(FileStorageFolder.ROOT_FULLNAME);
        }

        JSONObject fileSystemInfo = entity.optJSONObject("fileSystemInfo");
        if (fileSystemInfo != null && !fileSystemInfo.isEmpty()) {
            setDate(d -> folder.setCreationDate(d), fileSystemInfo.optString("createdDateTime"));
            setDate(d -> folder.setLastModifiedDate(d), fileSystemInfo.optString("lastModifiedDateTime"));
            folder.setSubfolders(hasSubFolders);
            folder.setSubscribedSubfolders(hasSubFolders);
        }
        return folder;
    }

    /**
     * Parses and sets the date via the specified consumer
     * 
     * @param date The date to parse and set
     */
    private void setDate(Consumer<Date> consumer, String date) {
        try {
            consumer.accept(ISO8601DateParser.parse(date));
        } catch (ParseException e) {
            LOG.warn("Could not parse date from: {}", date, e);
        }
    }
}
