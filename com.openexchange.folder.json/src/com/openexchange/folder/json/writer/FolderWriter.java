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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folder.json.writer;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link FolderWriter} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderWriter {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FolderWriter.class);

    /**
     * Initializes a new {@link FolderWriter}.
     */
    private FolderWriter() {
        super();
    }

    private static interface JSONValuePutter {

        void put(String key, Object value) throws JSONException;
    }

    private static final class JSONArrayPutter implements JSONValuePutter {

        private final JSONArray jsonArray;

        public JSONArrayPutter(final JSONArray jsonArray) {
            super();
            this.jsonArray = jsonArray;
        }

        public void put(final String key, final Object value) throws JSONException {
            jsonArray.put(value);
        }

    }

    private static final class JSONObjectPutter implements JSONValuePutter {

        private final JSONObject jsonObject;

        public JSONObjectPutter(final JSONObject jsonObject) {
            super();
            this.jsonObject = jsonObject;
        }

        public void put(final String key, final Object value) throws JSONException {
            if (null == value || JSONObject.NULL.equals(value)) {
                // Don't write NULL value
                return;
            }
            jsonObject.put(key, value);
        }

    }

    private static interface FolderFieldWriter {

        void writeField(JSONValuePutter jsonValue, UserizedFolder folder) throws JSONException;
    }

    private static final Map<Integer, FolderFieldWriter> STATIC_WRITERS_MAP;

    static {
        final Map<Integer, FolderFieldWriter> m = new HashMap<Integer, FolderFieldWriter>();
        m.put(Integer.valueOf(FolderField.ID.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String id = folder.getID();
                jsonPutter.put(FolderField.ID.getName(), id);
            }
        });
        m.put(Integer.valueOf(FolderField.CREATED_BY.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int createdBy = folder.getCreatedBy();
                jsonPutter.put(FolderField.CREATED_BY.getName(), -1 == createdBy ? JSONObject.NULL : Integer.valueOf(createdBy));
            }
        });
        m.put(Integer.valueOf(FolderField.MODIFIED_BY.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int modifiedBy = folder.getModifiedBy();
                jsonPutter.put(FolderField.MODIFIED_BY.getName(), -1 == modifiedBy ? JSONObject.NULL : Integer.valueOf(modifiedBy));
            }
        });
        m.put(Integer.valueOf(FolderField.CREATION_DATE.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getCreationDate();
                jsonPutter.put(FolderField.CREATION_DATE.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(Integer.valueOf(FolderField.LAST_MODIFIED.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getLastModified();
                jsonPutter.put(FolderField.LAST_MODIFIED.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(Integer.valueOf(FolderField.LAST_MODIFIED_UTC.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getLastModifiedUTC();
                jsonPutter.put(FolderField.LAST_MODIFIED_UTC.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(Integer.valueOf(FolderField.FOLDER_ID.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String pid = folder.getParentID();
                jsonPutter.put(FolderField.FOLDER_ID.getName(), pid);
            }
        });
        m.put(Integer.valueOf(FolderField.FOLDER_NAME.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String name = folder.getLocalizedName(folder.getLocale());
                jsonPutter.put(FolderField.FOLDER_NAME.getName(), name);
            }
        });
        m.put(Integer.valueOf(FolderField.MODULE.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final ContentType obj = folder.getContentType();
                jsonPutter.put(FolderField.MODULE.getName(), null == obj ? JSONObject.NULL : obj.toString());
            }
        });
        m.put(Integer.valueOf(FolderField.TYPE.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Type obj = folder.getType();
                jsonPutter.put(FolderField.TYPE.getName(), null == obj ? JSONObject.NULL : Integer.valueOf(obj.getType()));
            }
        });
        m.put(Integer.valueOf(FolderField.SUBFOLDERS.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String[] obj = folder.getSubfolderIDs();
                jsonPutter.put(FolderField.SUBFOLDERS.getName(), null == obj ? JSONObject.NULL : Boolean.valueOf(obj.length > 0));
            }
        });
        m.put(Integer.valueOf(FolderField.OWN_RIGHTS.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Permission obj = folder.getOwnPermission();
                jsonPutter.put(FolderField.OWN_RIGHTS.getName(), null == obj ? JSONObject.NULL : Integer.valueOf(createPermissionBits(
                    obj.getFolderPermission(),
                    obj.getReadPermission(),
                    obj.getWritePermission(),
                    obj.getDeletePermission(),
                    obj.isAdmin())));
            }
        });
        m.put(Integer.valueOf(FolderField.PERMISSIONS_BITS.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final JSONArray ja;
                {
                    final Permission[] obj = folder.getPermissions();
                    if (null == obj) {
                        ja = null;
                    } else {
                        ja = new JSONArray();
                        for (final Permission permission : obj) {
                            final JSONObject jo = new JSONObject();
                            jo.put(FolderField.BITS.getName(), createPermissionBits(permission));
                            jo.put(FolderField.ENTITY.getName(), permission.getEntity());
                            jo.put(FolderField.GROUP.getName(), permission.isGroup());
                            ja.put(jo);
                        }
                    }
                }
                jsonPutter.put(FolderField.PERMISSIONS_BITS.getName(), null == ja ? JSONObject.NULL : ja);
            }
        });
        m.put(Integer.valueOf(FolderField.SUMMARY.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String obj = folder.getSummary();
                jsonPutter.put(FolderField.SUMMARY.getName(), null == obj ? JSONObject.NULL : obj);
            }
        });
        m.put(Integer.valueOf(FolderField.STANDARD_FOLDER.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(FolderField.STANDARD_FOLDER.getName(), Boolean.valueOf(folder.isDefault()));
            }
        });
        m.put(Integer.valueOf(FolderField.TOTAL.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getTotal();
                jsonPutter.put(FolderField.TOTAL.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(Integer.valueOf(FolderField.NEW.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getNew();
                jsonPutter.put(FolderField.NEW.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(Integer.valueOf(FolderField.UNREAD.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getUnread();
                jsonPutter.put(FolderField.UNREAD.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(Integer.valueOf(FolderField.DELETED.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getDeleted();
                jsonPutter.put(FolderField.DELETED.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(Integer.valueOf(FolderField.SUBSCRIBED.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(FolderField.SUBSCRIBED.getName(), Boolean.valueOf(folder.isSubscribed()));
            }
        });
        m.put(Integer.valueOf(FolderField.SUBSCR_SUBFLDS.getColumn()), new FolderFieldWriter() {

            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String[] obj = folder.getSubfolderIDs();
                jsonPutter.put(FolderField.SUBSCR_SUBFLDS.getName(), null == obj ? JSONObject.NULL : Boolean.valueOf(obj.length > 0));
            }
        });
        STATIC_WRITERS_MAP = Collections.unmodifiableMap(m);
    }

    /**
     * Writes requested fields of given folder into a JSON array.
     * 
     * @param fields The fields to write
     * @param folder The folder
     * @return The JSON array carrying requested fields of given folder
     * @throws FolderException If writing JSON array fails
     */
    public static JSONArray write2Array(final int[] fields, final UserizedFolder folder) throws FolderException {
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONValuePutter jsonPutter = new JSONArrayPutter(jsonArray);
            for (int i = 0; i < fields.length; i++) {
                final FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(Integer.valueOf(fields[i]));
                if (null == ffw) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Unknown field: " + fields[i], new Throwable());
                    }
                    jsonArray.put(JSONObject.NULL);
                } else {
                    ffw.writeField(jsonPutter, folder);
                }
            }
            return jsonArray;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Writes requested fields of given folder into a JSON object.
     * 
     * @param fields The fields to write
     * @param folder The folder
     * @return The JSON object carrying requested fields of given folder
     * @throws FolderException If writing JSON object fails
     */
    public static JSONObject write2Object(final int[] fields, final UserizedFolder folder) throws FolderException {
        try {
            final JSONObject jsonObject = new JSONObject();
            final JSONValuePutter jsonPutter = new JSONObjectPutter(jsonObject);
            for (int i = 0; i < fields.length; i++) {
                final FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(Integer.valueOf(fields[i]));
                if (null == ffw) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Unknown field: " + fields[i], new Throwable());
                    }
                } else {
                    ffw.writeField(jsonPutter, folder);
                }
            }
            return jsonObject;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /*
     * Helper methods
     */

    static int createPermissionBits(final Permission perm) {
        return createPermissionBits(
            perm.getFolderPermission(),
            perm.getReadPermission(),
            perm.getWritePermission(),
            perm.getDeletePermission(),
            perm.isAdmin());
    }

    /**
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    private static final int MAX_PERMISSION = 64;

    static int createPermissionBits(final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) {
        final int[] perms = new int[5];
        perms[0] = fp == MAX_PERMISSION ? Permission.MAX_PERMISSION : fp;
        perms[1] = orp == MAX_PERMISSION ? Permission.MAX_PERMISSION : orp;
        perms[2] = owp == MAX_PERMISSION ? Permission.MAX_PERMISSION : owp;
        perms[3] = odp == MAX_PERMISSION ? Permission.MAX_PERMISSION : odp;
        perms[4] = adminFlag ? 1 : 0;
        return createPermissionBits(perms);
    }

    private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };

    private static int createPermissionBits(final int[] permission) {
        int retval = 0;
        boolean first = true;
        for (int i = permission.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            if (first) {
                retval += permission[i] << shiftVal;
                first = false;
            } else {
                if (permission[i] == Permission.MAX_PERMISSION) {
                    retval += MAX_PERMISSION << shiftVal;
                } else {
                    retval += mapping[permission[i]] << shiftVal;
                }
            }
        }
        return retval;
    }
}
