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
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.FileStorageConstants;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.json.actions.files.AJAXInfostoreRequest;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.file.storage.meta.FileFieldGet;
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

    private final AJAXInfostoreRequest request;
    private final JSONObject optJsonFile;
    private Map<String, Object> cache;
    private IDBasedFolderAccess folderAccess;

    /**
     * Initializes a new {@link JsonFieldHandler}.
     *
     * @param request The underlying infostore request
     */
    public JsonFieldHandler(AJAXInfostoreRequest request) {
        this(request, null);
    }

    /**
     * Initializes a new {@link JsonFieldHandler}.
     *
     * @param request The underlying infostore request
     * @param jFile The JSON file representation
     */
    public JsonFieldHandler(AJAXInfostoreRequest request, JSONObject optJsonFile) {
        super();
        this.request = request;
        this.optJsonFile = optJsonFile;
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
            } catch (final OXException e) {
                return value;
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
            Date d = (Date) value;
            if (field == File.Field.LOCKED_UNTIL && (d == null || d.getTime() <= System.currentTimeMillis())) {
                return Integer.valueOf(0);
            }
            TimeZone tz = Field.LAST_MODIFIED_UTC == field ? UTC : request.getTimezone();
            return writeDate((Date) value, tz);
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
                optJsonFile.put(FIELD_ENCRYPTED, ((oEncrypted instanceof Boolean) && ((Boolean) oEncrypted).booleanValue()));
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
                        JSONObject json = new JSONObject(3);
                        try {
                            json.put("entity", permission.getEntity());
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
            cache = new HashMap<String, Object>();
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
            cache = new HashMap<String, Object>();
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
            cache = new HashMap<String, Object>();
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
