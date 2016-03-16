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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.drive.impl.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Strings;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link JsonFileMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonFileMetadata extends AbstractJsonMetadata {

    private final File file;

    /**
     * Initializes a new {@link JsonFileMetadata}.
     *
     * @param session The sync session
     * @param folder The file to create the metadata for
     * @throws OXException
     */
    public JsonFileMetadata(SyncSession session, File file) throws OXException {
        super(session);
        this.file = file;
    }

    /**
     * Builds the JSON representation for a single file's metadata.
     *
     * @param file The file to get the JSON metadata for
     * @param specialCapabilities The special capabilities to include, depending on the underlying storage
     * @return The JSON file metadata
     */
    public JSONObject build(FileStorageCapability[] specialCapabilities) throws JSONException, OXException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", file.getFileName());
        jsonObject.put("created", file.getCreated().getTime());
        jsonObject.put("modified", file.getLastModified().getTime());
        jsonObject.put("created_by", file.getCreatedBy());
        jsonObject.put("modified_by", file.getModifiedBy());
        jsonObject.putOpt("content_type", DriveUtils.determineMimeType(file));
        jsonObject.putOpt("preview", session.getLinkGenerator().getFilePreviewLink(file));
        jsonObject.putOpt("thumbnail", session.getLinkGenerator().getFileThumbnailLink(file));
        for (FileStorageCapability capability : specialCapabilities) {
            switch (capability) {
            case FILE_VERSIONS:
                jsonObject.put("number_of_versions", file.getNumberOfVersions());
                jsonObject.put("version", file.getVersion());
                jsonObject.put("version_comment", file.getVersionComment());
                if (1 < file.getNumberOfVersions()) {
                    jsonObject.put("versions", getJSONFileVersions(file.getId()));
                }
                break;
            case OBJECT_PERMISSIONS:
                jsonObject.putOpt("object_permissions", getJSONObjectPermissions(file.getObjectPermissions(), false));
                jsonObject.putOpt("extended_object_permissions", getJSONObjectPermissions(file.getObjectPermissions(), true));
                if (isShared(file)) {
                    jsonObject.put("shared", true);
                }
                if (file.isShareable()) {
                    jsonObject.put("shareable", true);
                }
                break;
            case LOCKS:
                Date lockedUntil = file.getLockedUntil();
                if (null != lockedUntil && lockedUntil.getTime() > System.currentTimeMillis()) {
                    jsonObject.put("locked", true);
                }
                break;
            default:
                break;
            }
        }
        Set<String> jumpActions = getJumpActions(file, specialCapabilities);
        if (null != jumpActions && 0 < jumpActions.size()) {
            jsonObject.put("jump", new JSONArray(jumpActions));
        }
        return jsonObject;
    }

    /**
     * Builds the JSON representation for a single file's metadata.
     *
     * @param file The file to get the JSON metadata for
     * @return The JSON file metadata
     */
    public JSONObject build() throws JSONException, OXException {
        FileStorageCapability[] possibleCapabilities = new FileStorageCapability[] {
            FileStorageCapability.OBJECT_PERMISSIONS, FileStorageCapability.LOCKS, FileStorageCapability.FILE_VERSIONS
        };
        List<FileStorageCapability> specialCapabilities = new ArrayList<FileStorageCapability>();
        for (FileStorageCapability possibleCapability : possibleCapabilities) {
            if (session.getStorage().supports(session.getStorage().getRootFolderID(), possibleCapabilities)) {
                specialCapabilities.add(possibleCapability);
            }
        }
        return build(specialCapabilities.toArray(new FileStorageCapability[specialCapabilities.size()]));
    }

    /**
     * Gets the JSON representation for a file's versions. Version <code>"0"</code> id ignored implicitly, as the ajax action "versions"
     * action does.
     *
     * @param fileId The identifier of the file to get the versions for
     * @return A JSON array holding the file versions
     */
    private JSONArray getJSONFileVersions(String fileId) throws JSONException, OXException {
        JSONArray jsonArray = new JSONArray();
        List<Field> fields = Arrays.asList(Field.VERSION, Field.VERSION_COMMENT, Field.CREATED, Field.CREATED_BY,
            Field.LAST_MODIFIED, Field.MODIFIED_BY, Field.FILE_SIZE, Field.FILENAME);
        TimedResult<File> versions = session.getStorage().getFileAccess().getVersions(fileId, fields);
        if (null != versions) {
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = versions.results();
                while (searchIterator.hasNext()) {
                    File file = searchIterator.next();
                    if ("0".equals(file.getVersion())) {
                        continue; // skip version "0"
                    }
                    jsonArray.put(getJSONFileVersion(file));
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        return jsonArray;
    }

    private JSONObject getJSONFileVersion(File file) throws JSONException, OXException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", file.getFileName());
        jsonObject.put("file_size", file.getFileSize());
        jsonObject.put("created", file.getCreated().getTime());
        jsonObject.put("modified", file.getLastModified().getTime());
        jsonObject.put("created_by", file.getCreatedBy());
        jsonObject.put("modified_by", file.getModifiedBy());
        jsonObject.put("version", file.getVersion());
        jsonObject.put("version_comment", file.getVersionComment());
        return jsonObject;
    }

    private Set<String> getJumpActions(File file, FileStorageCapability[] specialCapabilities) {
        Set<String> jumpActions = new HashSet<String>();
        /*
         * statically add jump actions per file storage capability
         */
        for (FileStorageCapability capability : specialCapabilities) {
            switch (capability) {
            case FILE_VERSIONS:
                jumpActions.add("version_history");
                break;
            case OBJECT_PERMISSIONS:
                jumpActions.add("permissions");
                break;
            default:
                break;
            }
        }
        /*
         * dynamically add jump actions per file content type / user capabilities
         */
        String mimeType = DriveUtils.determineMimeType(file);
        if (false == Strings.isEmpty(mimeType)) {
            if (mimeType.matches("(?i)^text\\/.*(rtf|plain).*$")) {
                jumpActions.add("edit");
                jumpActions.add("preview");
            } else if (mimeType.matches(
                "(?i)^application\\/.*(ms-word|ms-excel|ms-powerpoint|msword|msexcel|mspowerpoint|openxmlformats|opendocument|pdf|rtf).*$")) {
                jumpActions.add("preview");
                if (mimeType.matches("(?i)^application\\/.*(ms-excel|msexcel|openxmlformats-officedocument.spreadsheetml).*$")) {
                    if (session.hasCapability("spreadsheet")) {
                        jumpActions.add("edit");
                    }
                } else if (mimeType.matches("(?i)^application\\/.*(ms-word|msword|openxmlformats-officedocument.wordprocessingml).*$")) {
                    if (session.hasCapability("text")) {
                        jumpActions.add("edit");
                    }
                }
            } else if (mimeType.matches("(?i)^(image\\/(gif|png|jpe?g|bmp|tiff))$") ||
                mimeType.matches("(?i)^audio\\/(mpeg|m4a|m4b|mp3|ogg|oga|opus|x-m4a)$")) {
                jumpActions.add("preview");
            }
        }
        return jumpActions;
    }

    private JSONArray getJSONObjectPermissions(List<FileStorageObjectPermission> permissions, boolean extended) throws JSONException, OXException {
        if (null == permissions) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(permissions.size());
        for (FileStorageObjectPermission permission : permissions) {
            jsonArray.put(extended ? getExtendedJSONObjectPermission(permission) : getJSONObjectPermission(permission));
        }
        return jsonArray;
    }

    private JSONObject getJSONObjectPermission(FileStorageObjectPermission permission) throws JSONException, OXException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("entity", permission.getEntity());
        jsonObject.put("group", permission.isGroup());
        jsonObject.put("bits", permission.getPermissions());
        return jsonObject;
    }

    private JSONObject getExtendedJSONObjectPermission(FileStorageObjectPermission permission) throws JSONException, OXException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("entity", permission.getEntity());
        jsonObject.put("bits", permission.getPermissions());
        if (permission.isGroup()) {
            jsonObject.put("type", "group");
            addGroupInfo(jsonObject, session.getPermissionResolver().getGroup(permission.getEntity()));
        } else {
            User user = session.getPermissionResolver().getUser(permission.getEntity());
            if (null == user) {
                org.slf4j.LoggerFactory.getLogger(JsonDirectoryMetadata.class).warn(
                    "Can't resolve uon entity {} for file {}", permission.getEntity(), file);
            } else if (user.isGuest()) {
                GuestInfo guest = session.getPermissionResolver().getGuest(user.getId());
                if (guest == null) {
                    int contextId = session.getServerSession().getContextId();
                    throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Could not resolve guest info for ID " + user.getId() + " in context " + contextId + ". " +
                        "It might have been deleted in the mean time or is in an inconsistent state.");
                }

                jsonObject.put("type", guest.getRecipientType().toString().toLowerCase());
                if (RecipientType.ANONYMOUS.equals(guest.getRecipientType())) {
                    addShareInfo(jsonObject, session.getPermissionResolver().getLink(file, permission.getEntity()));
                } else {
                    addUserInfo(jsonObject, user);
                }
            } else {
                jsonObject.put("type", "user");
                addUserInfo(jsonObject, user);
            }
        }
        return jsonObject;
    }

    private boolean isShared(File file) {
        List<FileStorageObjectPermission> permissions = file.getObjectPermissions();
        if (null != permissions && 0 < permissions.size()) {
            int userID = session.getServerSession().getUserId();
            for (FileStorageObjectPermission permission : permissions) {
                if (permission.getEntity() != userID) {
                    return true;
                }
            }
        }
        return false;
    }

}
