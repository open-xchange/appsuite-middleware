/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2018-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.onedrive.OneDriveFolder;

/**
 * {@link OneDriveFolderParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
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
     * @param entity The {@link JSONObject}
     * @return The {@link OneDriveFolder} or <code>null</code> if the entity is not a folder
     */
    public OneDriveFolder parseEntity(int userId, JSONObject entity) {
        if (!entity.hasAndNotNull("folder")) {
            return null;
        }
        OneDriveFolder folder = new OneDriveFolder(userId);
        if (entity == null || entity.isEmpty()) {
            return folder;
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
            folder.setId(FileStorageFolder.ROOT_FULLNAME);
        }

        JSONObject fileSystemInfo = entity.optJSONObject("fileSystemInfo");
        if (fileSystemInfo != null && !fileSystemInfo.isEmpty()) {
            String createdAt = fileSystemInfo.optString("createdDateTime");
            try {
                folder.setCreationDate(ISO8601DateParser.parse(createdAt));
            } catch (ParseException e) {
                LOG.warn("Could not parse date from: {}", createdAt, e);
            }
            String modifiedAt = fileSystemInfo.optString("lastModifiedDateTime");
            try {
                folder.setLastModifiedDate(ISO8601DateParser.parse(createdAt));
            } catch (ParseException e) {
                LOG.warn("Could not parse date from: {}", modifiedAt, e);
            }
            JSONObject folderMetaData = entity.optJSONObject("folder");
            if (folderMetaData != null && !folderMetaData.isEmpty()) {
                int childCount = folderMetaData.optInt("childCount");
                folder.setSubfolders(childCount > 0);
                folder.setSubscribedSubfolders(folder.hasSubfolders());
            }
        }
        return folder;
    }

    /**
     * Parses the specified {@link JSONArray} of entities in to a {@link List}
     * of {@link OneDriveFolder}s
     * 
     * @param userId the user identifier
     * @param entities The {@link JSONArray} with the entities
     * @return A {@link List} with the {@link OneDriveFolder}s
     */
    public List<OneDriveFolder> parseEntities(int userId, JSONArray entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<OneDriveFolder> folders = new LinkedList<>();
        for (int index = 0; index < entities.length(); index++) {
            JSONObject entity = entities.optJSONObject(index);
            OneDriveFolder folder = parseEntity(userId, entity);
            if (folder != null) {
                folders.add(folder);
            }
        }
        return folders;
    }
}
