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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;
import com.openexchange.ajax.customizer.folder.BulkFolderField;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.FolderFieldRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.log.LogProperties;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderWriter} - Write methods for folder module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderWriter {

    /**
     * The logger constant.
     */
    protected static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FolderWriter.class));

    protected static final boolean WARN = LOG.isWarnEnabled();

    /**
     * The default locale: en_US.
     */
    public static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * Initializes a new {@link FolderWriter}.
     */
    private FolderWriter() {
        super();
    }

    /**
     * {@link AdditionalFolderFieldWriter}
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class AdditionalFolderFieldWriter implements FolderFieldWriter {

        private final ServerSession serverSession;

        private final AdditionalFolderField aff;

        protected AdditionalFolderFieldWriter(final ServerSession serverSession, final AdditionalFolderField aff) {
            this.serverSession = serverSession;
            this.aff = aff;
        }

        @Override
        public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
            final FolderObject fo = new FolderObject();
            final int numFolderId = getUnsignedInteger(folder.getID());
            if (numFolderId < 0) {
                fo.setFullName(folder.getID());
            } else {
                fo.setObjectID(numFolderId);
            }
            fo.setFolderName(folder.getName());
            fo.setModule(folder.getContentType().getModule());
            fo.setType(folder.getType().getType());
            fo.setCreatedBy(folder.getCreatedBy());
            jsonPutter.put(aff.getColumnName(), aff.renderJSON(aff.getValue(fo, serverSession)));
        }
    }

    private static interface JSONValuePutter {

        void put(String key, Object value) throws JSONException;
    }

    private static final class JSONArrayPutter implements JSONValuePutter {

        private JSONArray jsonArray;

        public JSONArrayPutter() {
            super();
        }

        public JSONArrayPutter(final JSONArray jsonArray) {
            this();
            this.jsonArray = jsonArray;
        }

        public void setJSONArray(final JSONArray jsonArray) {
            this.jsonArray = jsonArray;
        }

        @Override
        public void put(final String key, final Object value) throws JSONException {
            jsonArray.put(value);
        }

    }

    private static final class JSONObjectPutter implements JSONValuePutter {

        private JSONObject jsonObject;

        public JSONObjectPutter() {
            super();
        }

        public JSONObjectPutter(final JSONObject jsonObject) {
            this();
            this.jsonObject = jsonObject;
        }

        public void setJSONObject(final JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        @Override
        public void put(final String key, final Object value) throws JSONException {
            if (null == value || JSONObject.NULL.equals(value)) {
                // Don't write NULL value
                return;
            }
            jsonObject.put(key, value);
        }

    }

    private static interface FolderFieldWriter {

        /**
         * Writes associated field's value to given JSON value.
         *
         * @param jsonValue The JSON value
         * @param folder The folder
         * @throws JSONException If a JSON error occurs
         * @throws NecessaryValueMissingException If a necessary value is missing; such as identifier
         */
        void writeField(JSONValuePutter jsonValue, UserizedFolder folder) throws JSONException;
    }

    protected static final FolderFieldWriter UNKNOWN_FIELD_FFW = new FolderFieldWriter() {

        @Override
        public void writeField(final JSONValuePutter jsonValue, final UserizedFolder folder) throws JSONException {
            jsonValue.put("unknown_field", JSONObject.NULL);
        }
    };

    private static final TIntObjectMap<FolderFieldWriter> STATIC_WRITERS_MAP;

    private static final int[] ALL_FIELDS;

    static {
        final TIntObjectMap<FolderFieldWriter> m = new TIntObjectHashMap<FolderFieldWriter>(32);
        m.put(FolderField.ID.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String id = folder.getID();
                if (null == id) {
                    throw new NecessaryValueMissingException("Missing folder identifier.");
                }
                jsonPutter.put(FolderField.ID.getName(), id);
            }
        });
        m.put(FolderField.CREATED_BY.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int createdBy = folder.getCreatedBy();
                jsonPutter.put(FolderField.CREATED_BY.getName(), -1 == createdBy ? JSONObject.NULL : Integer.valueOf(createdBy));
            }
        });
        m.put(FolderField.MODIFIED_BY.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int modifiedBy = folder.getModifiedBy();
                jsonPutter.put(FolderField.MODIFIED_BY.getName(), -1 == modifiedBy ? JSONObject.NULL : Integer.valueOf(modifiedBy));
            }
        });
        m.put(FolderField.CREATION_DATE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getCreationDate();
                jsonPutter.put(FolderField.CREATION_DATE.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(FolderField.LAST_MODIFIED.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getLastModified();
                jsonPutter.put(FolderField.LAST_MODIFIED.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(FolderField.LAST_MODIFIED_UTC.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getLastModifiedUTC();
                jsonPutter.put(FolderField.LAST_MODIFIED_UTC.getName(), null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(FolderField.FOLDER_ID.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String pid = folder.getParentID();
                jsonPutter.put(FolderField.FOLDER_ID.getName(), pid);
            }
        });
        m.put(FolderField.FOLDER_NAME.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Locale locale  = folder.getLocale();
                final String name = folder.getLocalizedName(locale == null ? DEFAULT_LOCALE : locale);
                jsonPutter.put(FolderField.FOLDER_NAME.getName(), name);
            }
        });
        m.put(FolderField.MODULE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final ContentType obj = folder.getContentType();
                jsonPutter.put(FolderField.MODULE.getName(), null == obj ? JSONObject.NULL : obj.toString());
            }
        });
        m.put(FolderField.TYPE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Type obj = folder.getType();
                jsonPutter.put(FolderField.TYPE.getName(), null == obj ? JSONObject.NULL : Integer.valueOf(obj.getType()));
            }
        });
        m.put(FolderField.SUBFOLDERS.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String[] obj = folder.getSubfolderIDs();
                if (null == obj) {
                    LOG.warn("Got null as subfolders for folder " + folder.getID() + ". Marking this folder to hold subfolders...");
                    jsonPutter.put(FolderField.SUBFOLDERS.getName(), Boolean.TRUE);
                } else {
                    jsonPutter.put(FolderField.SUBFOLDERS.getName(), Boolean.valueOf(obj.length > 0));
                }
            }
        });
        m.put(FolderField.OWN_RIGHTS.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int bits = folder.getBits();
                if (bits < 0) {
                    final Permission obj = folder.getOwnPermission();
                    jsonPutter.put(
                        FolderField.OWN_RIGHTS.getName(),
                        null == obj ? JSONObject.NULL : Integer.valueOf(createPermissionBits(
                            obj.getFolderPermission(),
                            obj.getReadPermission(),
                            obj.getWritePermission(),
                            obj.getDeletePermission(),
                            obj.isAdmin())));
                } else {
                    jsonPutter.put(FolderField.OWN_RIGHTS.getName(), Integer.valueOf(bits));
                }
            }
        });
        m.put(FolderField.PERMISSIONS_BITS.getColumn(), new FolderFieldWriter() {

            @Override
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
        m.put(FolderField.SUMMARY.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String obj = folder.getSummary();
                jsonPutter.put(FolderField.SUMMARY.getName(), null == obj ? JSONObject.NULL : obj);
            }
        });
        m.put(FolderField.STANDARD_FOLDER.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(FolderField.STANDARD_FOLDER.getName(), Boolean.valueOf(folder.isDefault()));
            }
        });
        m.put(FolderField.STANDARD_FOLDER_TYPE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(FolderField.STANDARD_FOLDER_TYPE.getName(), Integer.valueOf(folder.getDefaultType()));
            }
        });
        m.put(FolderField.TOTAL.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                LogProperties.putLogProperty(LogProperties.Name.SESSION_SESSION, folder.getSession());
                final int obj = folder.getTotal();
                jsonPutter.put(FolderField.TOTAL.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.NEW.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getNew();
                jsonPutter.put(FolderField.NEW.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.UNREAD.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getUnread();
                jsonPutter.put(FolderField.UNREAD.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.DELETED.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getDeleted();
                jsonPutter.put(FolderField.DELETED.getName(), -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.SUBSCRIBED.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(FolderField.SUBSCRIBED.getName(), Boolean.valueOf(folder.isSubscribed()));
            }
        });
        m.put(FolderField.SUBSCR_SUBFLDS.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                /*-
                 *
                final String[] obj = folder.getSubfolderIDs();
                jsonPutter.put(FolderField.SUBSCR_SUBFLDS.getName(), null == obj ? JSONObject.NULL : Boolean.valueOf(obj.length > 0));
                 */
                jsonPutter.put(FolderField.SUBSCR_SUBFLDS.getName(), Boolean.valueOf(folder.hasSubscribedSubfolders()));
            }
        });
        m.put(FolderField.CAPABILITIES.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int caps = folder.getCapabilities();
                jsonPutter.put(FolderField.CAPABILITIES.getName(), -1 == caps ? JSONObject.NULL : Integer.valueOf(caps));
            }
        });
        STATIC_WRITERS_MAP = m;

        final FolderField[] all = FolderField.values();
        final int[] allFields = new int[all.length];
        int j = 0;
        for (int i = 0; i < allFields.length; i++) {
            final int val = all[i].getColumn();
            if (val > 0) {
                allFields[j++] = val;
            }
        }
        ALL_FIELDS = new int[j];
        System.arraycopy(allFields, 0, ALL_FIELDS, 0, j);
    }

    private static List<FolderObject> turnIntoFolderObjects(final UserizedFolder[] folders) {
        final List<FolderObject> retval = new ArrayList<FolderObject>(folders.length);
        for (final UserizedFolder folder : folders) {
            final FolderObject fo = new FolderObject();
            final int numFolderId = getUnsignedInteger(folder.getID());
            if (numFolderId < 0) {
                fo.setFullName(folder.getID());
            } else {
                fo.setObjectID(numFolderId);
            }
            fo.setFolderName(folder.getName());
            fo.setModule(folder.getContentType().getModule());
            fo.setType(folder.getType().getType());
            fo.setCreatedBy(folder.getCreatedBy());
            retval.add(fo);
        }
        return retval;
    }

    /**
     * Writes requested fields of given folder into a JSON array.
     *
     * @param fields The fields to write or <code>null</code> to write all
     * @param folder The folder
     * @return The JSON array carrying requested fields of given folder
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeSingle2Array(final int[] fields, final UserizedFolder folder) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final FolderFieldWriter[] ffws = new FolderFieldWriter[cols.length];
        final TIntObjectMap<com.openexchange.folderstorage.FolderField> fieldSet = FolderFieldRegistry.getInstance().getFields();
        for (int i = 0; i < ffws.length; i++) {
            FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(cols[i]);
            if (null == ffw) {
                ffw = getPropertyByField(cols[i], fieldSet);
            }
            ffws[i] = ffw;
        }
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONValuePutter jsonPutter = new JSONArrayPutter(jsonArray);
            for (final FolderFieldWriter ffw : ffws) {
                ffw.writeField(jsonPutter, folder);
            }
            return jsonArray;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        } catch (final NecessaryValueMissingException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Writes requested fields of given folders into a JSON array consisting of JSON arrays.
     *
     * @param fields The fields to write to each JSON array or <code>null</code> to write all
     * @param folders The folders
     * @return The JSON array carrying JSON arrays of given folders
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeMultiple2Array(final int[] fields, final UserizedFolder[] folders, final ServerSession serverSession, final AdditionalFolderFieldList additionalFolderFieldList) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final FolderFieldWriter[] ffws = new FolderFieldWriter[cols.length];
        final TIntObjectMap<com.openexchange.folderstorage.FolderField> fieldSet = FolderFieldRegistry.getInstance().getFields();
        for (int i = 0; i < ffws.length; i++) {
            final int curCol = cols[i];
            FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(curCol);
            if (null == ffw) {
                if (additionalFolderFieldList.knows(curCol)) {
                    final AdditionalFolderField aff = new BulkFolderField(additionalFolderFieldList.get(curCol));
                    aff.getValues(turnIntoFolderObjects(folders), serverSession);
                    ffw = new AdditionalFolderFieldWriter(serverSession, aff);
                } else {
                    ffw = getPropertyByField(curCol, fieldSet);
                }
            }
            ffws[i] = ffw;
        }
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONArrayPutter jsonPutter = new JSONArrayPutter();
            for (final UserizedFolder folder : folders) {
                try {
                    final JSONArray folderArray = new JSONArray();
                    jsonPutter.setJSONArray(folderArray);
                    for (final FolderFieldWriter ffw : ffws) {
                        ffw.writeField(jsonPutter, folder);
                    }
                    jsonArray.put(folderArray);
                } catch (final NecessaryValueMissingException e) {
                    LOG.warn(e.getMessage());
                }
            }
            return jsonArray;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Writes requested fields of given folders into a JSON array consisting of JSON arrays.
     *
     * @param fields The fields to write to each JSON array or <code>null</code> to write all
     * @param folders The folders
     * @return The JSON array carrying JSON arrays of given folders
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeMultiple2Array(final int[] fields, final Collection<UserizedFolder> folders) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final FolderFieldWriter[] ffws = new FolderFieldWriter[cols.length];
        final TIntObjectMap<com.openexchange.folderstorage.FolderField> fieldSet = FolderFieldRegistry.getInstance().getFields();
        for (int i = 0; i < ffws.length; i++) {
            FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(cols[i]);
            if (null == ffw) {
                ffw = getPropertyByField(cols[i], fieldSet);
            }
            ffws[i] = ffw;
        }
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONArrayPutter jsonPutter = new JSONArrayPutter();
            for (final UserizedFolder folder : folders) {
                try {
                    final JSONArray folderArray = new JSONArray();
                    jsonPutter.setJSONArray(folderArray);
                    for (final FolderFieldWriter ffw : ffws) {
                        ffw.writeField(jsonPutter, folder);
                    }
                    jsonArray.put(folderArray);
                } catch (final NecessaryValueMissingException e) {
                    LOG.warn(e.getMessage());
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
     * @param fields The fields to write or <code>null</code> to write all
     * @param folder The folder
     * @return The JSON object carrying requested fields of given folder
     * @throws OXException If writing JSON object fails
     */
    public static JSONObject writeSingle2Object(final int[] fields, final UserizedFolder folder, final ServerSession serverSession, final AdditionalFolderFieldList additionalFolderFieldList) throws OXException {
        final int[] cols = null == fields ? getAllFields(additionalFolderFieldList) : fields;
        final FolderFieldWriter[] ffws = new FolderFieldWriter[cols.length];
        final TIntObjectMap<com.openexchange.folderstorage.FolderField> fieldSet = FolderFieldRegistry.getInstance().getFields();
        for (int i = 0; i < ffws.length; i++) {
            final int curCol = cols[i];
            FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(curCol);
            if (null == ffw) {
                if (additionalFolderFieldList.knows(curCol)) {
                    final AdditionalFolderField aff = additionalFolderFieldList.get(curCol);
                    ffw = new AdditionalFolderFieldWriter(serverSession, aff);
                } else {
                    ffw = getPropertyByField(curCol, fieldSet);
                }
            }
            ffws[i] = ffw;
        }
        try {
            final JSONObject jsonObject = new JSONObject();
            final JSONValuePutter jsonPutter = new JSONObjectPutter(jsonObject);
            for (final FolderFieldWriter ffw : ffws) {
                ffw.writeField(jsonPutter, folder);
            }
            return jsonObject;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        } catch (final NecessaryValueMissingException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static int[] getAllFields(final AdditionalFolderFieldList additionalFolderFieldList) {
        final TIntList list = new TIntArrayList();
        list.add(ALL_FIELDS);
        list.add(additionalFolderFieldList.getKnownFields());
        return list.toArray();
    }

    /**
     * Writes requested fields of given folders into a JSON array consisting of JSON objects.
     *
     * @param fields The fields to write to each JSON object or <code>null</code> to write all
     * @param folders The folders
     * @return The JSON array carrying JSON objects of given folders
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeMultiple2Object(final int[] fields, final UserizedFolder[] folders) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final FolderFieldWriter[] ffws = new FolderFieldWriter[cols.length];
        final TIntObjectMap<com.openexchange.folderstorage.FolderField> fieldSet = FolderFieldRegistry.getInstance().getFields();
        for (int i = 0; i < ffws.length; i++) {
            FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(cols[i]);
            if (null == ffw) {
                ffw = getPropertyByField(cols[i], fieldSet);
            }
            ffws[i] = ffw;
        }
        try {
            final JSONArray jsonArray = new JSONArray();
            final JSONObjectPutter jsonPutter = new JSONObjectPutter();
            for (final UserizedFolder folder : folders) {
                try {
                    final JSONObject folderObject = new JSONObject();
                    jsonPutter.setJSONObject(folderObject);
                    for (final FolderFieldWriter ffw : ffws) {
                        ffw.writeField(jsonPutter, folder);
                    }
                    jsonArray.put(folderObject);
                } catch (final NecessaryValueMissingException e) {
                    LOG.warn(e.getMessage());
                }
            }
            return jsonArray;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /*
     * Helper methods
     */

    private static final class PropertyFieldWriter implements FolderFieldWriter {

        private final com.openexchange.folderstorage.FolderField field;

        private final String name;

        protected PropertyFieldWriter(final com.openexchange.folderstorage.FolderField field) {
            super();
            this.field = field;
            final String name = field.getName();
            this.name = null == name ? Integer.toString(field.getField()) : name;
        }

        @Override
        public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
            final FolderProperty property = folder.getProperties().get(field);
            jsonPutter.put(name, null == property ? field.getDefaultValue() : property.getValue());
        }

    }

    private static final ConcurrentTIntObjectHashMap<PropertyFieldWriter> PROPERTY_WRITERS = new ConcurrentTIntObjectHashMap<PropertyFieldWriter>(4);

    private static FolderFieldWriter getPropertyByField(final int field, final TIntObjectMap<com.openexchange.folderstorage.FolderField> fields) {
        final com.openexchange.folderstorage.FolderField fieldNamePair = fields.get(field);
        if (null == fieldNamePair) {
            if (WARN) {
                LOG.warn("Unknown field: " + field, new Throwable());
            }
            return UNKNOWN_FIELD_FFW;
        }
        PropertyFieldWriter pw = PROPERTY_WRITERS.get(field);
        if (null == pw) {
            final PropertyFieldWriter npw = new PropertyFieldWriter(fieldNamePair);
            pw = PROPERTY_WRITERS.putIfAbsent(field, npw);
            if (null == pw) {
                pw = npw;
            }
        }
        return pw;
    }

    /**
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    private static final int MAX_PERMISSION = 64;

    private static final TIntIntHashMap MAPPING = new TIntIntHashMap(6) {

        { // Unnamed Block.
            put(Permission.MAX_PERMISSION, MAX_PERMISSION);
            put(MAX_PERMISSION, MAX_PERMISSION);
            put(0, 0);
            put(2, 1);
            put(4, 2);
            put(8, 4);
        }
    };

    static int createPermissionBits(final Permission perm) {
        return createPermissionBits(
            perm.getFolderPermission(),
            perm.getReadPermission(),
            perm.getWritePermission(),
            perm.getDeletePermission(),
            perm.isAdmin());
    }

    static int createPermissionBits(final int fp, final int rp, final int wp, final int dp, final boolean adminFlag) {
        int retval = 0;
        int i = 4;
        retval += (adminFlag ? 1 : 0) << (i-- * 7)/* Number of bits to be shifted */;
        retval += MAPPING.get(dp) << (i-- * 7);
        retval += MAPPING.get(wp) << (i-- * 7);
        retval += MAPPING.get(rp) << (i-- * 7);
        retval += MAPPING.get(fp) << (i * 7);
        return retval;
    }

    /**
     * The radix for base <code>10</code>.
     */
    private static final int RADIX = 10;

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    static final int getUnsignedInteger(final String s) {
        if (s == null) {
            return -1;
        }

        final int max = s.length();

        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        int result = 0;
        int i = 0;

        final int limit = -Integer.MAX_VALUE;
        final int multmin = limit / RADIX;
        int digit;

        if (i < max) {
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return -1;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return -1;
            }
            if (result < multmin) {
                return -1;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

}
