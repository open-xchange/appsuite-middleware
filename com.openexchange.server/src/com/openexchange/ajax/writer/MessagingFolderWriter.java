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

package com.openexchange.ajax.writer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.messaging.MessagingFolderIdentifier;
import com.openexchange.folderstorage.messaging.MessagingFolderImpl;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.messaging.DefaultMessagingPermission;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MessagingFolderWriter} - The messaging folder writer.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingFolderWriter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessagingFolderWriter.class);

    public interface JSONValuePutter {

        void put(String key, Object value) throws JSONException;
    }

    public static final class JSONArrayPutter implements JSONValuePutter {

        private JSONArray jsonArray;

        public JSONArrayPutter() {
            super();
        }

        public JSONArrayPutter(final JSONArray jsonArray) {
            this();
            this.jsonArray = jsonArray;
        }

        public JSONArrayPutter setJSONArray(final JSONArray jsonArray) {
            this.jsonArray = jsonArray;
            return this;
        }

        @Override
        public void put(final String key, final Object value) throws JSONException {
            jsonArray.put(value);
        }

    }

    public static final class JSONObjectPutter implements JSONValuePutter {

        private JSONObject jsonObject;

        public JSONObjectPutter() {
            super();
        }

        public JSONObjectPutter(final JSONObject jsonObject) {
            this();
            this.jsonObject = jsonObject;
        }

        public JSONObjectPutter setJSONObject(final JSONObject jsonObject) {
            this.jsonObject = jsonObject;
            return this;
        }

        @Override
        public void put(final String key, final Object value) throws JSONException {
            if ((null == value) || JSONObject.NULL.equals(value) || (null == key)) {
                // Don't write NULL value
                return;
            }
            jsonObject.put(key, value);
        }

    }

    public static abstract class MessagingFolderFieldWriter {

        public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder) throws OXException {
            writeField(jsonContainer, serviceId, accountId, folder, null, -1);
        }

        public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders) throws OXException {
            writeField(jsonContainer, serviceId, accountId, folder, name, hasSubfolders, null, -1, false);
        }

        public abstract void writeField(JSONValuePutter jsonContainer, String serviceId, int accountId, MessagingFolder folder, String name, int hasSubfolders, String id, int module, boolean all) throws OXException;
    }

    /**
     * Maps folder field constants to corresponding instance of {@link MessagingFolderFieldWriter}
     */
    private static final TIntObjectMap<MessagingFolderFieldWriter> WRITERS_MAP = new TIntObjectHashMap<MessagingFolderFieldWriter>(20);

    static {
        WRITERS_MAP.put(DataObject.OBJECT_ID, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(
                        DataFields.ID,
                        null == id ? MessagingFolderIdentifier.getFQN(serviceId, accountId, folder.getId()) : id);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.CREATED_BY, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(DataFields.CREATED_BY, Integer.valueOf(-1));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.MODIFIED_BY, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(DataFields.MODIFIED_BY, Integer.valueOf(-1));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.CREATION_DATE, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(DataFields.CREATION_DATE, Integer.valueOf(0));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.LAST_MODIFIED, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(DataFields.LAST_MODIFIED, Integer.valueOf(0));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderChildObject.FOLDER_ID, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    final Object parent;
                    if (null == folder.getParentId()) {
                        parent = Integer.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
                    } else {
                        parent = MessagingFolderIdentifier.getFQN(serviceId, accountId, folder.getParentId());
                    }
                    jsonContainer.put(FolderChildFields.FOLDER_ID, parent);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.FOLDER_NAME, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(FolderFields.TITLE, name == null ? folder.getName() : name);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.MODULE, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(FolderFields.MODULE, AJAXServlet.getModuleString(module == -1 ? FolderObject.MESSAGING : module, -1));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.TYPE, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    jsonContainer.put(FolderFields.TYPE, Integer.valueOf(FolderObject.MESSAGING));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUBFOLDERS, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    final boolean boolVal;
                    if (hasSubfolders == -1) {
                        boolVal = all ? folder.hasSubfolders() : folder.hasSubscribedSubfolders();
                    } else {
                        boolVal = hasSubfolders > 0;
                    }
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.SUBFOLDERS, Boolean.valueOf(boolVal));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.OWN_RIGHTS, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    final MessagingPermission mp;
                    if (folder.isRootFolder()) {
                        final MessagingPermission rootPermission = folder.getOwnPermission();
                        if (rootPermission == null) {
                            mp = DefaultMessagingPermission.newInstance();
                            mp.setAllPermissions(
                                OCLPermission.CREATE_SUB_FOLDERS,
                                OCLPermission.NO_PERMISSIONS,
                                OCLPermission.NO_PERMISSIONS,
                                OCLPermission.NO_PERMISSIONS);
                            mp.setAdmin(false);
                        } else {
                            mp = rootPermission;
                        }
                    } else {
                        mp = folder.getOwnPermission();
                    }
                    if (!folder.isHoldsFolders() && (mp.getFolderPermission() >= MessagingPermission.CREATE_SUB_FOLDERS)) {
                        // Cannot contain subfolders; therefore deny subfolder creation
                        mp.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
                    }
                    if (!folder.isHoldsMessages() && (mp.getReadPermission() >= MessagingPermission.READ_OWN_OBJECTS)) {
                        // Cannot contain messages; therefore deny read access. Folder is not selectable.
                        mp.setReadPermission(OCLPermission.NO_PERMISSIONS);
                    }
                    int permissionBits = createPermissionBits(mp);
                    if (folder.getCapabilities().contains(MessagingFolder.CAPABILITY_USER_FLAGS)) {
                        permissionBits |= BIT_USER_FLAG;
                    }
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.OWN_RIGHTS, Integer.valueOf(permissionBits));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.PERMISSIONS_BITS, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    final JSONArray ja = new JSONArray();
                    final List<MessagingPermission> perms = folder.getPermissions();
                    for (final MessagingPermission perm : perms) {
                        final JSONObject jo = new JSONObject();
                        jo.put(FolderFields.BITS, createPermissionBits(perm));
                        jo.put(FolderFields.ENTITY, perm.getEntity());
                        jo.put(FolderFields.GROUP, perm.isGroup());
                        ja.put(jo);
                    }
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.PERMISSIONS, ja);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUMMARY, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    final String value =
                        folder.isRootFolder() ? "" : new StringBuilder(16).append('(').append(folder.getMessageCount()).append('/').append(
                            folder.getUnreadMessageCount()).append(')').toString();
                    jsonContainer.put(FolderFields.SUMMARY, value);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.STANDARD_FOLDER, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    jsonContainer.put(
                        FolderFields.STANDARD_FOLDER,
                        Boolean.valueOf(folder.containsDefaultFolderType() ? folder.isDefaultFolder() : false));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.TOTAL, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.TOTAL, Integer.valueOf(folder.getMessageCount()));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.NEW, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.NEW, Integer.valueOf(folder.getNewMessageCount()));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.UNREAD, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.UNREAD, Integer.valueOf(folder.getUnreadMessageCount()));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.DELETED, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.DELETED, Integer.valueOf(folder.getDeletedMessageCount()));
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUBSCRIBED, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    final Object boolVal;
                    if (folder.getCapabilities().contains(MessagingFolder.CAPABILITY_SUBSCRIPTION)) {
                        boolVal = Boolean.valueOf(folder.isSubscribed());
                    } else {
                        boolVal = Boolean.TRUE;
                    }
                    jsonContainer.put(FolderFields.SUBSCRIBED, boolVal);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUBSCR_SUBFLDS, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    final Object boolVal;
                    if (!folder.getCapabilities().contains(MessagingFolder.CAPABILITY_SUBSCRIPTION)) {
                        boolVal = hasSubfolders == -1 ? Boolean.valueOf(folder.hasSubfolders()) : Boolean.valueOf(hasSubfolders > 0);
                    } else if (hasSubfolders == -1) {
                        boolVal = folder.hasSubfolders() ? Boolean.valueOf(folder.hasSubscribedSubfolders()) : Boolean.FALSE;
                    } else {
                        boolVal = Boolean.valueOf(hasSubfolders > 0);
                    }
                    /*
                     * Put value
                     */
                    jsonContainer.put(FolderFields.SUBSCR_SUBFLDS, boolVal);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.CAPABILITIES, new MessagingFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    final Integer caps = Integer.valueOf(MessagingFolderImpl.parseCaps(folder.getCapabilities()));
                    jsonContainer.put(FolderFields.CAPABILITIES, caps);
                } catch (final JSONException e) {
                    throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
    }

    /**
     * No instantiation
     */
    private MessagingFolderWriter() {
        super();
    }

    private static final int[] ALL_FLD_FIELDS = com.openexchange.ajax.writer.FolderWriter.getAllFolderFields();

    /**
     * Writes whole folder as a JSON object
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @param folder The folder to write
     * @param session The server session
     * @return The written JSON object
     * @throws OXException
     */
    public static JSONObject writeMessagingFolder(final String serviceId, final int accountId, final MessagingFolder folder, final ServerSession session) throws OXException {
        final JSONObject jsonObject = new JSONObject();
        final JSONValuePutter putter = new JSONObjectPutter(jsonObject);
        final MessagingFolderFieldWriter[] writers = getMessagingFolderFieldWriter(ALL_FLD_FIELDS, session);
        for (final MessagingFolderFieldWriter writer : writers) {
            writer.writeField(putter, serviceId, accountId, folder);
        }
        return jsonObject;
    }

    private static final int BIT_USER_FLAG = (1 << 29);

    // private static final String STR_UNKNOWN_COLUMN = "Unknown column";

    /**
     * Generates appropriate field writers for given mail folder fields
     *
     * @param fields The fields to write
     * @param session The server session
     * @return Appropriate field writers as an array of {@link MessagingFolderFieldWriter}
     */
    public static MessagingFolderFieldWriter[] getMessagingFolderFieldWriter(final int[] fields, final ServerSession session) {
        return getMessagingFolderFieldWriter(fields, session, Folder.getAdditionalFields());
    }

    /**
     * Generates appropriate field writers for given mail folder fields
     *
     * @param fields The fields to write
     * @param session The server session
     * @param additionalFields Additional fields
     * @return Appropriate field writers as an array of {@link MessagingFolderFieldWriter}
     */
    public static MessagingFolderFieldWriter[] getMessagingFolderFieldWriter(final int[] fields, final ServerSession session, final AdditionalFolderFieldList additionalFields) {
        final MessagingFolderFieldWriter[] retval = new MessagingFolderFieldWriter[fields.length];
        for (int i = 0; i < retval.length; i++) {
            final int curField = fields[i];
            final MessagingFolderFieldWriter mffw = WRITERS_MAP.get(curField);
            if (mffw == null) {
                if (!additionalFields.knows(curField)) {
                    LOG.warn("Unknown folder field: {}", curField);
                }

                final AdditionalFolderField folderField = additionalFields.get(curField);
                retval[i] = new MessagingFolderFieldWriter() {

                    @Override
                    public void writeField(final JSONValuePutter jsonContainer, final String serviceId, final int accountId, final MessagingFolder folder, final String name, final int hasSubfolders, final String id, final int module, final boolean all) throws OXException {
                        try {
                            /*
                             * Proper MessagingFolder-2-FolderObject conversion
                             */
                            final FolderObject fo = new FolderObject();
                            fo.setFullName(folder.getId());
                            fo.setFolderName(folder.getName());
                            fo.setModule(FolderObject.MESSAGING);
                            fo.setType(FolderObject.MESSAGING);
                            fo.setCreatedBy(-1);
                            jsonContainer.put(folderField.getColumnName(), folderField.renderJSON(null, folderField.getValue(fo, session)));
                        } catch (final JSONException e) {
                            throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                        }
                    }

                };
            } else {
                retval[i] = mffw;
            }
        }
        return retval;
    }

    private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };

    static int createPermissionBits(final MessagingPermission perm) throws OXException {
        return createPermissionBits(
            perm.getFolderPermission(),
            perm.getReadPermission(),
            perm.getWritePermission(),
            perm.getDeletePermission(),
            perm.isAdmin());
    }

    static int createPermissionBits(final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws OXException {
        final int[] perms = new int[5];
        perms[0] = fp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : fp;
        perms[1] = orp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : orp;
        perms[2] = owp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : owp;
        perms[3] = odp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : odp;
        perms[4] = adminFlag ? 1 : 0;
        return createPermissionBits(perms);
    }

    private static int createPermissionBits(final int[] permission) throws OXException {
        int retval = 0;
        boolean first = true;
        for (int i = permission.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            if (first) {
                retval += permission[i] << shiftVal;
                first = false;
            } else {
                if (permission[i] == OCLPermission.ADMIN_PERMISSION) {
                    retval += Folder.MAX_PERMISSION << shiftVal;
                } else {
                    try {
                        retval += mapping[permission[i]] << shiftVal;
                    } catch (final Exception e) {
                        throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }
            }
        }
        return retval;
    }

}
