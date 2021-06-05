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
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.microsoft.graph.onedrive.OneDriveFile;

/**
 * {@link OneDriveFileParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class OneDriveFileParser {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OneDriveFileParser.class);

    /**
     * Initialises a new {@link OneDriveFileParser}.
     */
    public OneDriveFileParser() {
        super();
    }

    /**
     * 
     * @param entity
     * @return
     */
    public OneDriveFile parseEntity(int userId, JSONObject entity) {
        if (!entity.hasAndNotNull("file")) {
            return null;
        }
        OneDriveFile file = new OneDriveFile(userId);
        String name = entity.optString("name");
        file.setId(entity.optString("id"));
        file.setUniqueId(entity.optString("id"));
        file.setFileName(name);
        file.setTitle(name);
        file.setFileSize(entity.optLong("size"));
        file.setURL(entity.optString("webUrl")); // FIXME: Use 'webUrl' or '@microsoft.graph.downloadUrl'
        file.setDescription(entity.optString("description"));

        file.setColorLabel(0);
        file.setCategories(null);
        file.setVersionComment(null);

        JSONObject parentRef = entity.optJSONObject("parentReference");
        if (parentRef != null && !parentRef.isEmpty()) {
            file.setFolderId(parentRef.optString("path").equals("/drive/root:") ? FileStorageFolder.ROOT_FULLNAME : parentRef.optString("id"));
        }

        JSONObject fileJson = entity.optJSONObject("file");
        if (fileJson != null && !fileJson.isEmpty()) {
            file.setFileMIMEType(fileJson.optString("mimeType"));
        }

        JSONObject fileSystemInfo = entity.optJSONObject("fileSystemInfo");
        if (fileSystemInfo != null && !fileSystemInfo.isEmpty()) {
            String createdAt = fileSystemInfo.optString("createdDateTime");
            try {
                file.setCreated(ISO8601DateParser.parse(createdAt));
            } catch (ParseException e) {
                LOG.warn("Could not parse date from: {}", createdAt, e);
            }
            String modifiedAt = fileSystemInfo.optString("lastModifiedDateTime");
            try {
                file.setLastModified(ISO8601DateParser.parse(createdAt));
            } catch (ParseException e) {
                LOG.warn("Could not parse date from: {}", modifiedAt, e);
            }
        }

        return file;
    }

    /**
     * 
     * @param entities
     * @return
     */
    public List<OneDriveFile> parseEntities(int userId, JSONArray entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<OneDriveFile> folders = new LinkedList<>();
        for (int index = 0; index < entities.length(); index++) {
            JSONObject entity = entities.optJSONObject(index);
            OneDriveFile file = parseEntity(userId, entity);
            if (file != null) {
                folders.add(file);
            }
        }
        return folders;
    }
}
