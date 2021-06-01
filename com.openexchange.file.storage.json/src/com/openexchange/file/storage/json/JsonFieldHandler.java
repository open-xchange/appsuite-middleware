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

package com.openexchange.file.storage.json;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileFieldHandler;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageConstants;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageMediaUtility;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.actions.files.AJAXInfostoreRequest;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.file.storage.meta.FileFieldGet;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link JsonFieldHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonFieldHandler extends AbstractFileFieldHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JsonFieldHandler.class);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final FileFieldGet GETTER = new FileFieldGet();

    private static final String FIELD_ENCRYPTED = Strings.asciiLowerCase(FileStorageConstants.METADATA_KEY_ENCRYPTED);

    private static final String USER_INFOSTORE_FOLDER_ID   = Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
    private static final String PUBLIC_INFOSTORE_FOLDER_ID = Integer.toString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

    /**
     * A named value retrieved for a certain field when calling
     * {@link JsonFieldHandler#handle(Field, Object...) JsonFieldHandler's handle() method}.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: A named value can only be returned when JsonFieldHandler has been initialized through
     * {@link JsonFieldHandler#JsonFieldHandler(AJAXInfostoreRequest, JSONObject) this constructor}.
     * </div>
     */
    public static interface NamedValue<V> {

        /**
         * Gets the name
         *
         * @return The name
         */
        String getName() ;

        /**
         * Gets the value
         *
         * @return The value
         */
        V getValue();
    }

    private static class MediaNamedValue implements NamedValue<JSONObject> {

        private final String name;
        private final JSONObject value;

        /**
         * Initializes a new {@link MediaNamedValue}.
         */
        MediaNamedValue(String name, JSONObject value) {
            super();
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public JSONObject getValue() {
            return value;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(32);
            builder.append('[');
            if (name != null) {
                builder.append("name=").append(name).append(", ");
            }
            if (value != null) {
                builder.append("value=").append(value);
            }
            builder.append(']');
            return builder.toString();
        }

    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final AJAXInfostoreRequest request;
    private final JSONObject optJsonFile;
    private MediaNamedValue mediaStruct;
    private Map<String, Object> cache;
    private IDBasedFolderAccess folderAccess;

    /**
     * Initializes a new {@link JsonFieldHandler}.
     *
     * @param request The underlying request
     */
    public JsonFieldHandler(AJAXInfostoreRequest request) {
        this(request, null);
    }

    /**
     * Initializes a new {@link JsonFieldHandler}.
     *
     * @param request The underlying request
     * @param jFile The JSON object, to which file's JSON representation is serialized
     */
    public JsonFieldHandler(AJAXInfostoreRequest request, JSONObject jFile) {
        super();
        this.request = request;
        this.optJsonFile = jFile;
    }

    @Override
    public Object handle(final Field field, final Object... args) {
        Object value = field.doSwitch(GETTER, args);
        if (File.Field.FILE_MIMETYPE == field) {
            if (null == value) {
                return value;
            }
            final String ct = value.toString();
            if (ct.indexOf(';') <= 0) {
                return value;
            }
            try {
                return ContentType.getBaseType(ct);
            } catch (OXException e) {
                return value;
            }
        }
        if (Field.MEDIA_FIELDS.contains(field)) {
            try {
                if (null != optJsonFile && mediaStruct == null) {
                    // Output a NamedValue in case we're serializing to a JSON object
                    mediaStruct = new MediaNamedValue("media", new JSONObject(8));
                }

                if ((field == File.Field.MEDIA_STATUS) && (value == null)) {
                    if (args.length > 0 && (args[0] instanceof UserizedFile)) {
                        UserizedFile userizedFile = (UserizedFile) args[0];
                        value = userizedFile.getMediaStatusForClient(request.getSession());
                    }
                }

                if (value == null) {
                    if ((field == File.Field.MEDIA_STATUS) && (mediaStruct == null)) {
                        value = MediaStatus.Status.NONE.getIdentifier();
                    }
                } else {
                    if (Date.class.isInstance(value)) {
                        TimeZone tz = Field.LAST_MODIFIED_UTC == field ? UTC : request.getTimezone();
                        value = writeDate((Date) value, tz);
                    } else {
                        switch (field) {
                            case MEDIA_STATUS:
                                MediaStatus mediaStatus = (MediaStatus) value;
                                if (MediaStatus.Status.NONE == mediaStatus.getStatus()) {
                                    // Apparently no media information. Null'ify if we're serializing to media struct
                                    if (mediaStruct != null) {
                                        value = null;
                                    } else {
                                        value = mediaStatus.getStatus().getIdentifier();
                                    }
                                } else {
                                    value = mediaStatus.getStatus().getIdentifier();
                                }
                                break;
                            case MEDIA_META:
                                value = new JSONObject((Map<String, Object>) value);
                                break;
                            case GEOLOCATION:
                                value = value.toString();
                                break;
                            case CAMERA_APERTURE:
                                double aperture = ((Number) value).doubleValue();
                                value = FileStorageMediaUtility.getCameraApertureDescription(aperture, request.getSession().getUser().getLocale());
                                break;
                            case CAMERA_EXPOSURE_TIME:
                                double exposureTime = ((Number) value).doubleValue();
                                value = FileStorageMediaUtility.getCameraExposureTimeDescription(exposureTime, request.getSession().getUser().getLocale());
                                break;
                            case CAMERA_FOCAL_LENGTH:
                                double focalLength = ((Number) value).doubleValue();
                                value = FileStorageMediaUtility.getCameraFocalLengthDescription(focalLength, request.getSession().getUser().getLocale());
                                break;
                            default: // do nothing;
                        }

                        if (null != value && mediaStruct != null) {
                            mediaStruct.getValue().put(field.getName(), value);
                        }
                    }
                }

                if (mediaStruct == null || mediaStruct.getValue().isEmpty()) {
                    // We're either not serializing to a JSON object or nothing has been written to media struct
                    return value;
                }

                return mediaStruct;
            } catch (JSONException e) {
                LOG.error("Error writing field: {}", field.getName(), e);
            }
        }
        if (value == null) {
            if (field == File.Field.LOCKED_UNTIL) {
                return Integer.valueOf(0);
            }
            if (field == File.Field.FILENAME) {
                return "";
            }
        }
        if (Date.class.isInstance(value)) {
            Date d = Date.class.cast(value);
            if (field == File.Field.LOCKED_UNTIL && (d.getTime() <= System.currentTimeMillis())) {
                return Integer.valueOf(0);
            }
            TimeZone tz = Field.LAST_MODIFIED_UTC == field ? UTC : request.getTimezone();
            return writeDate(Date.class.cast(value), tz);
        }

        switch (field) {
        case CATEGORIES:
            return handleCategories((String) value);
        case META:
            try {
                if (value == null) {
                    return null;
                }

                if (null == optJsonFile) {
                    return JSONCoercion.coerceToJSON(value);
                }

                Map<String, Object> meta = (Map<String, Object>) value;
                // Add encrypted flag if appropriate
                Object oEncrypted = meta.get(FileStorageConstants.METADATA_KEY_ENCRYPTED);
                optJsonFile.put(FIELD_ENCRYPTED, Boolean.TRUE.equals(oEncrypted));
                return new JSONObject(meta);
            } catch (JSONException e) {
                LOG.error("", e);
                return null;
            }
        case OBJECT_PERMISSIONS:
            if (value != null && value instanceof List<?>) {
                List<?> list = (List<?>) value;
                JSONArray jPermissions = new JSONArray(list.size());
                for (Object obj : list) {
                    if (obj instanceof FileStorageGuestObjectPermission) {
                        FileStorageGuestObjectPermission permission = (FileStorageGuestObjectPermission) obj;
                        ShareRecipient recipient = permission.getRecipient();
                        JSONObject json = new JSONObject();
                        try {
                            json.put("type", recipient.getType().toString().toLowerCase());
                            json.put("bits", permission.getPermissions());
                            switch (recipient.getType()) {
                            case ANONYMOUS:
                                json.putOpt("password", ((AnonymousRecipient) recipient).getPassword());
                                break;
                            case GUEST:
                                GuestRecipient guestRecipient = (GuestRecipient) recipient;
                                json.putOpt("password", guestRecipient.getPassword());
                                json.putOpt("email_address", guestRecipient.getEmailAddress());
                                json.putOpt("display_name", guestRecipient.getDisplayName());
                                json.putOpt("contact_id", guestRecipient.getContactID());
                                json.putOpt("contact_folder", guestRecipient.getContactFolder());
                                break;
                            default:
                                throw new UnsupportedOperationException("Unable to write recipients of type " + recipient.getType());
                            }
                            jPermissions.put(json);
                        } catch (JSONException e) {
                            LOG.error("", e);
                            return null;
                        }
                    } else if (obj instanceof FileStorageObjectPermission) {
                        FileStorageObjectPermission permission = (FileStorageObjectPermission) obj;
                        JSONObject json = new JSONObject(4);
                        try {
                            json.put("identifier", permission.getIdentifier());
                            if (0 < permission.getEntity() || 0 == permission.getEntity() && permission.isGroup()) {
                                json.put("entity", permission.getEntity());
                            }
                            json.put("group", permission.isGroup());
                            json.put("bits", permission.getPermissions());
                            jPermissions.put(json);
                        } catch (JSONException e) {
                            LOG.error("", e);
                            return null;
                        }
                    }
                }

                return jPermissions;
            }

            return new JSONArray(0);
        case ORIGIN:
            return handleFolderPath((FolderPath) value);
        case CREATED_FROM:
            if (null != value) {
                return ((EntityInfo) value).toJSON();
            }
            return null;
        case MODIFIED_FROM:
            if (null != value) {
                return ((EntityInfo) value).toJSON();
            }
            return null;
        default: // do nothing;
        }

        return value;
    }

    private Object handleFolderPath(FolderPath folderPath) {
        if (null == folderPath) {
            return null;
        }

        try {
            FolderPath effectiveFolderPath = folderPath;
            String folderId;
            switch (effectiveFolderPath.getType()) {
                case PRIVATE:
                    folderId = getInfoStorePersonalFolder().getId();
                    break;
                case PUBLIC:
                    folderId = PUBLIC_INFOSTORE_FOLDER_ID;
                    break;
                case SHARED:
                    folderId = USER_INFOSTORE_FOLDER_ID;
                    break;
                case UNDEFINED: /* fall-through */
                default:
                    folderId = getInfoStorePersonalFolder().getId();
                    effectiveFolderPath = FolderPath.EMPTY_PATH;
            }

            FileStorageFolder folder = getFolderFor(folderId);
            Locale locale = request.getSession().getUser().getLocale();
            StringBuilder sb = new StringBuilder();
            sb.append(folder.getLocalizedName(locale));

            if (!effectiveFolderPath.isEmpty()) {
                boolean searchInSubfolders = true;
                for (String folderName : effectiveFolderPath.getPathForRestore()) {
                    boolean found = false;
                    if (searchInSubfolders) {
                        FileStorageFolder[] subfolders = getSubfoldersFor(folder.getId());
                        for (int i = 0; !found && i < subfolders.length; i++) {
                            FileStorageFolder subfolder = subfolders[i];
                            if (folderName.equals(subfolder.getName())) {
                                found = true;
                                sb.append("/").append(subfolder.getLocalizedName(locale));
                                folder = subfolder;
                            }
                        }
                    }

                    if (false == found) {
                        sb.append("/").append(folderName);
                        searchInSubfolders = false;
                    }
                }
            }

            return sb.toString();
        } catch (OXException e) {
            LOG.debug("Failed to determine original path", e);
            return null;
        }
    }

    private FileStorageFolder getInfoStorePersonalFolder() throws OXException {
        Map<String, Object> cache = this.cache;
        if (null == cache) {
            cache = new HashMap<>();
            this.cache = cache;
        }

        FileStorageFolder personalFolder = (FileStorageFolder) cache.get("__personal__");
        if (null == personalFolder) {
            IDBasedFolderAccess folderAccess = this.folderAccess;
            if (null == folderAccess) {
                folderAccess = Services.getFolderAccessFactory().createAccess(request.getSession());
            }

            personalFolder = folderAccess.getPersonalFolder(PUBLIC_INFOSTORE_FOLDER_ID); // An arbitrary InfoStore folder identifier
            cache.put(personalFolder.getId(), personalFolder);
            cache.put("__personal__", personalFolder);
        }
        return personalFolder;
    }

    private FileStorageFolder getFolderFor(String folderId) throws OXException {
        Map<String, Object> cache = this.cache;
        if (null == cache) {
            cache = new HashMap<>();
            this.cache = cache;
        }

        FileStorageFolder folder = (FileStorageFolder) cache.get(folderId);
        if (null == folder) {
            IDBasedFolderAccess folderAccess = this.folderAccess;
            if (null == folderAccess) {
                folderAccess = Services.getFolderAccessFactory().createAccess(request.getSession());
            }

            folder = folderAccess.getFolder(folderId);
            cache.put(folderId, folder);
        }
        return folder;
    }

    private FileStorageFolder[] getSubfoldersFor(String folderId) throws OXException {
        Map<String, Object> cache = this.cache;
        if (null == cache) {
            cache = new HashMap<>();
            this.cache = cache;
        }

        String key = "sub_" + folderId;
        FileStorageFolder[] subfolders = (FileStorageFolder[]) cache.get(key);
        if (null == subfolders) {
            IDBasedFolderAccess folderAccess = this.folderAccess;
            if (null == folderAccess) {
                folderAccess = Services.getFolderAccessFactory().createAccess(request.getSession());
            }

            subfolders = folderAccess.getSubfolders(folderId, true);
            cache.put(key, subfolders);
        }
        return subfolders;
    }

    private Object writeDate(final Date date, final TimeZone tz) {
        final int offset = (tz == null) ? 0 : tz.getOffset(date.getTime());
        long time = date.getTime() + offset;
        // Happens on infinite locks.
        if (time < 0) {
            time = Long.MAX_VALUE;
        }
        return Long.valueOf(time);
    }

    private JSONArray handleCategories(final String value) {
        if (value == null) {
            return null;
        }
        final String[] strings = Strings.splitByComma(value);
        final JSONArray array = new JSONArray();
        for (final String string : strings) {
            array.put(string);
        }

        return array;
    }

}
