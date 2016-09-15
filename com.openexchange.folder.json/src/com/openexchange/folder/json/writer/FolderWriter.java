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

package com.openexchange.folder.json.writer;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;
import com.openexchange.ajax.customizer.folder.BulkFolderField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.FolderFieldRegistry;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Streams;
import com.openexchange.java.util.Tools;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link FolderWriter} - Write methods for folder module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderWriter {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderWriter.class);

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

        private final AdditionalFolderField aff;
        private final AJAXRequestData requestData;

        /**
         * Initializes a new {@link AdditionalFolderFieldWriter}.
         *
         * @param requestData The underlying request data
         * @param aff The additional folder field
         */
        protected AdditionalFolderFieldWriter(AJAXRequestData requestData, final AdditionalFolderField aff) {
            super();
            this.requestData = requestData;
            this.aff = aff;
        }

        @Override
        public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
            FolderObject fo = turnIntoFolderObjects(new UserizedFolder[] { folder }).iterator().next();
            Object value = aff.getValue(fo, requestData.getSession());
            jsonPutter.put(jsonPutter.withKey() ? aff.getColumnName() : null, aff.renderJSON(requestData, value));
        }
    }

    private static interface JSONValuePutter {

        void put(String key, Object value) throws JSONException;

        /**
         * Puts given name-value-pair into this data's parameters.
         * <p>
         * A <code>null</code> value removes the mapping.
         *
         * @param name The parameter name
         * @param value The parameter value
         * @throws NullPointerException If name is <code>null</code>
         */
        void putParameter(String name, Object value);

        /**
         * Gets specified parameter's value.
         *
         * @param name The parameter name
         * @return The <code>String</code> representing the single value of the parameter
         * @throws NullPointerException If name is <code>null</code>
         */
        <V> V getParameter(String name);

        /**
         * Gets the available parameters' names.
         *
         * @return The parameter names
         */
        Set<String> getParamterNames();

        /**
         * Gets the parameters reference.
         *
         * @return The parameters reference.
         */
        ConcurrentMap<String, Object> parameters();

        /**
         * Signals whether a key is required
         *
         * @return <code>true</code> if a key is required; otherwise <code>false</code>
         */
        boolean withKey();
    }

    private static abstract class AbstractJSONValuePutter implements JSONValuePutter {

        /** The parameters map */
        protected final ConcurrentMap<String, Object> parameters;

        protected AbstractJSONValuePutter() {
            super();
            parameters = new ConcurrentHashMap<String, Object>(4, 0.9f, 1);
        }

        @Override
        public Set<String> getParamterNames() {
            return new LinkedHashSet<String>(parameters.keySet());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V> V getParameter(String name) {
            return (V) parameters.get(name);
        }

        @Override
        public void putParameter(String name, Object value) {
            if (null == value) {
                parameters.remove(name);
            } else {
                parameters.put(name, value);
            }
        }

        @Override
        public ConcurrentMap<String, Object> parameters() {
            return parameters;
        }
    }

    private static final class JSONArrayPutter extends AbstractJSONValuePutter {

        private JSONArray jsonArray;

        public JSONArrayPutter(final Map<String, Object> parameters) {
            super();
            if (null != parameters) {
                this.parameters.putAll(parameters);
            }
        }

        @Override
        public boolean withKey() {
            return false;
        }

        public JSONArrayPutter(final JSONArray jsonArray, final Map<String, Object> parameters) {
            this(parameters);
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

    private static final class JSONObjectPutter extends AbstractJSONValuePutter {

        private JSONObject jsonObject;

        public JSONObjectPutter(final Map<String, Object> parameters) {
            super();
            if (null != parameters) {
                this.parameters.putAll(parameters);
            }
        }

        @Override
        public boolean withKey() {
            return true;
        }

        public JSONObjectPutter(final JSONObject jsonObject, final Map<String, Object> parameters) {
            this(parameters);
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
                jsonPutter.put(jsonPutter.withKey() ? FolderField.ID.getName() : null, id);
            }
        });
        m.put(FolderField.CREATED_BY.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int createdBy = folder.getCreatedBy();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.CREATED_BY.getName() : null, -1 == createdBy ? JSONObject.NULL : Integer.valueOf(createdBy));
            }
        });
        m.put(FolderField.MODIFIED_BY.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int modifiedBy = folder.getModifiedBy();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.MODIFIED_BY.getName() : null, -1 == modifiedBy ? JSONObject.NULL : Integer.valueOf(modifiedBy));
            }
        });
        m.put(FolderField.CREATION_DATE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getCreationDate();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.CREATION_DATE.getName() : null, null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(FolderField.LAST_MODIFIED.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getLastModified();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.LAST_MODIFIED.getName() : null, null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(FolderField.LAST_MODIFIED_UTC.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Date d = folder.getLastModifiedUTC();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.LAST_MODIFIED_UTC.getName() : null, null == d ? JSONObject.NULL : Long.valueOf(d.getTime()));
            }
        });
        m.put(FolderField.FOLDER_ID.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String pid = folder.getParentID();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.FOLDER_ID.getName() : null, pid);
            }
        });
        m.put(FolderField.ACCOUNT_ID.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String accountId = folder.getAccountID();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.ACCOUNT_ID.getName() : null, accountId == null ? JSONObject.NULL : accountId);
            }
        });
        m.put(FolderField.FOLDER_NAME.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Locale locale = folder.getLocale();
                if (folder.supportsAltName()) {
                    jsonPutter.put(jsonPutter.withKey() ? FolderField.FOLDER_NAME.getName() : null, folder.getLocalizedName(locale == null ? DEFAULT_LOCALE : locale, folder.isAltNames()));
                } else {
                    jsonPutter.put(jsonPutter.withKey() ? FolderField.FOLDER_NAME.getName() : null, folder.getLocalizedName(locale == null ? DEFAULT_LOCALE : locale));
                }
            }
        });
        m.put(FolderField.MODULE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final ContentType obj = folder.getContentType();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.MODULE.getName() : null, null == obj ? JSONObject.NULL : obj.toString());
            }
        });
        m.put(FolderField.TYPE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Type obj = folder.getType();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.TYPE.getName() : null, null == obj ? JSONObject.NULL : Integer.valueOf(obj.getType()));
            }
        });
        m.put(FolderField.SUBFOLDERS.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String[] obj = folder.getSubfolderIDs();
                if (null == obj) {
                    LOG.warn("Got null as subfolders for folder {}. Marking this folder to hold subfolders...", folder.getID());
                    jsonPutter.put(jsonPutter.withKey() ? FolderField.SUBFOLDERS.getName() : null, Boolean.TRUE);
                } else {
                    jsonPutter.put(jsonPutter.withKey() ? FolderField.SUBFOLDERS.getName() : null, Boolean.valueOf(obj.length > 0));
                }
            }
        });
        m.put(FolderField.OWN_RIGHTS.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int bits = folder.getBits();
                if (bits < 0) {
                    final Permission obj = folder.getOwnPermission();
                    jsonPutter.put(jsonPutter.withKey() ? FolderField.OWN_RIGHTS.getName() : null,
                        null == obj ? JSONObject.NULL : Integer.valueOf(Permissions.createPermissionBits(
                            obj.getFolderPermission(),
                            obj.getReadPermission(),
                            obj.getWritePermission(),
                            obj.getDeletePermission(),
                            obj.isAdmin())));
                } else {
                    jsonPutter.put(jsonPutter.withKey() ? FolderField.OWN_RIGHTS.getName() : null, Integer.valueOf(bits));
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
                            final JSONObject jo = new JSONObject(4);
                            jo.put(FolderField.BITS.getName(), Permissions.createPermissionBits(permission));
                            jo.put(FolderField.ENTITY.getName(), permission.getEntity());
                            jo.put(FolderField.GROUP.getName(), permission.isGroup());
                            ja.put(jo);
                        }
                    }
                }
                jsonPutter.put(jsonPutter.withKey() ? FolderField.PERMISSIONS_BITS.getName() : null, null == ja ? JSONObject.NULL : ja);
            }
        });
        m.put(FolderField.SUMMARY.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final String obj = folder.getSummary();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.SUMMARY.getName() : null, null == obj ? JSONObject.NULL : obj);
            }
        });
        m.put(FolderField.STANDARD_FOLDER.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(jsonPutter.withKey() ? FolderField.STANDARD_FOLDER.getName() : null, Boolean.valueOf(folder.isDefault()));
            }
        });
        m.put(FolderField.STANDARD_FOLDER_TYPE.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(jsonPutter.withKey() ? FolderField.STANDARD_FOLDER_TYPE.getName() : null, Integer.valueOf(folder.getDefaultType()));
            }
        });
        m.put(FolderField.TOTAL.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getTotal();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.TOTAL.getName() : null, -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.NEW.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getNew();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.NEW.getName() : null, -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.UNREAD.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getUnread();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.UNREAD.getName() : null, -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.DELETED.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int obj = folder.getDeleted();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.DELETED.getName() : null, -1 == obj ? JSONObject.NULL : Integer.valueOf(obj));
            }
        });
        m.put(FolderField.SUBSCRIBED.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                jsonPutter.put(jsonPutter.withKey() ? FolderField.SUBSCRIBED.getName() : null, Boolean.valueOf(folder.isSubscribed()));
            }
        });
        m.put(FolderField.SUBSCR_SUBFLDS.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                /*-
                 *
                final String[] obj = folder.getSubfolderIDs();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.SUBSCR_SUBFLDS.getName() : null, null == obj ? JSONObject.NULL : Boolean.valueOf(obj.length > 0));
                 */
                jsonPutter.put(jsonPutter.withKey() ? FolderField.SUBSCR_SUBFLDS.getName() : null, Boolean.valueOf(folder.hasSubscribedSubfolders()));
            }
        });
        // Capabilities
        m.put(FolderField.CAPABILITIES.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final int caps = folder.getCapabilities();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.CAPABILITIES.getName() : null, -1 == caps ? JSONObject.NULL : Integer.valueOf(caps));
            }
        });
        // Meta
        m.put(FolderField.META.getColumn(), new FolderFieldWriter() {
            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                // Get meta map
                Map<String, Object> map = folder.getMeta();
                jsonPutter.put(jsonPutter.withKey() ? FolderField.META.getName() : null, null == map || map.isEmpty() ? JSONObject.NULL : JSONCoercion.coerceToJSON(map));
            }

        });
        // Supported capabilities
        m.put(FolderField.SUPPORTED_CAPABILITIES.getColumn(), new FolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter jsonPutter, final UserizedFolder folder) throws JSONException {
                final Set<String> caps = getSupportedCapabilities(folder);
                jsonPutter.put(jsonPutter.withKey() ? FolderField.SUPPORTED_CAPABILITIES.getName() : null, null == caps ? JSONObject.NULL : new JSONArray(caps));
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
            final int numFolderId = Tools.getUnsignedInteger(folder.getID());
            if (numFolderId < 0) {
                fo.setFullName(folder.getID());
            } else {
                fo.setObjectID(numFolderId);
            }
            fo.setFolderName(folder.getName());
            fo.setModule(folder.getContentType().getModule());
            if (null != folder.getType()) {
                fo.setType(folder.getType().getType());
            }
            fo.setCreatedBy(folder.getCreatedBy());
            fo.setPermissions(turnIntoOCLPermissions(numFolderId, folder.getPermissions()));
            retval.add(fo);
        }
        return retval;
    }

    /**
     * Converts an array of permissions as used in userized folders into a list of OCL permissions as used by folder objects.
     *
     * @param folderID The folder identifier
     * @param permissions The permissions
     * @return The OXL permissions
     */
    private static List<OCLPermission> turnIntoOCLPermissions(int folderID, Permission[] permissions) {
        if (null == permissions) {
            return null;
        }
        List<OCLPermission> oclPermissions = new ArrayList<OCLPermission>(permissions.length);
        for (Permission permission : permissions) {
            OCLPermission oclPermission = new OCLPermission(permission.getEntity(), folderID);
            oclPermission.setAllPermission(permission.getFolderPermission(), permission.getReadPermission(),
                permission.getWritePermission(), permission.getDeletePermission());
            oclPermission.setFolderAdmin(permission.isAdmin());
            oclPermission.setGroupPermission(permission.isGroup());
            oclPermissions.add(oclPermission);
        }
        return oclPermissions;
    }

    /**
     * Writes requested fields of given folders into a JSON array consisting of JSON arrays.
     *
     * @param requestData The underlying request data
     * @param fields The fields to write or <code>null</code> to write all
     * @param folders The folders
     * @param additionalFolderFieldList The additional folder fields to write
     * @return The JSON array carrying JSON arrays of given folders
     * @throws OXException If writing JSON array fails
     */
    public static JSONArray writeMultiple2Array(AJAXRequestData requestData, final int[] fields, final UserizedFolder[] folders, final AdditionalFolderFieldList additionalFolderFieldList) throws OXException {
        final int[] cols = null == fields ? ALL_FIELDS : fields;
        final FolderFieldWriter[] ffws = new FolderFieldWriter[cols.length];
        final TIntObjectMap<com.openexchange.folderstorage.FolderField> fieldSet = FolderFieldRegistry.getInstance().getFields();
        for (int i = 0; i < ffws.length; i++) {
            final int curCol = cols[i];
            FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(curCol);
            if (null == ffw) {
                if (additionalFolderFieldList.knows(curCol)) {
                    final AdditionalFolderField aff = new BulkFolderField(additionalFolderFieldList.get(curCol));
                    aff.getValues(turnIntoFolderObjects(folders), requestData.getSession());
                    ffw = new AdditionalFolderFieldWriter(requestData, aff);
                } else {
                    ffw = getPropertyByField(curCol, fieldSet);
                }
            }
            ffws[i] = ffw;
        }
        ConcurrentMap<String, Object> params = null;
        try {
            final JSONArray jsonArray = new JSONArray(folders.length);
            final JSONArrayPutter jsonPutter = new JSONArrayPutter(null);
            // params = jsonPutter.parameters();
            for (final UserizedFolder folder : folders) {
                // folder.setParameters(params);
                try {
                    final JSONArray folderArray = new JSONArray(ffws.length);
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
        } finally {
            if (params != null) {
                for (final Object param : params.values()) {
                    if (param instanceof Closeable) {
                        Streams.close((Closeable) param);
                    }
                }
            }
        }
    }

    /**
     * Writes requested fields of given folder into a JSON object.
     *
     * @param requestData The underlying request data
     * @param fields The fields to write or <code>null</code> to write all
     * @param folder The folder
     * @param additionalFolderFieldList The additional folder fields to write
     * @return The JSON object carrying requested fields of given folder
     * @throws OXException If writing JSON object fails
     */
    public static JSONObject writeSingle2Object(AJAXRequestData requestData, final int[] fields, final UserizedFolder folder, final AdditionalFolderFieldList additionalFolderFieldList) throws OXException {
        final int[] cols = null == fields ? getAllFields(additionalFolderFieldList) : fields;
        final FolderFieldWriter[] ffws = new FolderFieldWriter[cols.length];
        final TIntObjectMap<com.openexchange.folderstorage.FolderField> fieldSet = FolderFieldRegistry.getInstance().getFields();
        for (int i = 0; i < ffws.length; i++) {
            final int curCol = cols[i];
            FolderFieldWriter ffw = STATIC_WRITERS_MAP.get(curCol);
            if (null == ffw) {
                if (additionalFolderFieldList.knows(curCol)) {
                    final AdditionalFolderField aff = additionalFolderFieldList.get(curCol);
                    ffw = new AdditionalFolderFieldWriter(requestData, aff);
                } else {
                    ffw = getPropertyByField(curCol, fieldSet);
                }
            }
            ffws[i] = ffw;
        }
        ConcurrentMap<String, Object> params = null;
        try {
            final JSONObject jsonObject = new JSONObject(ffws.length);
            final JSONValuePutter jsonPutter = new JSONObjectPutter(jsonObject, null);
            // params = jsonPutter.parameters();
            for (final FolderFieldWriter ffw : ffws) {
                // folder.setParameters(params);
                ffw.writeField(jsonPutter, folder);
            }
            return jsonObject;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        } catch (final NecessaryValueMissingException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (params != null) {
                for (final Object param : params.values()) {
                    if (param instanceof Closeable) {
                        Streams.close((Closeable) param);
                    }
                }
            }
        }
    }

    private static int[] getAllFields(final AdditionalFolderFieldList additionalFolderFieldList) {
        final TIntList list = new TIntArrayList();
        list.add(ALL_FIELDS);
        list.add(additionalFolderFieldList.getKnownFields());
        return list.toArray();
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
            jsonPutter.put(jsonPutter.withKey() ? name : null, null == property ? field.getDefaultValue() : property.getValue());
        }

    }

    private static final ConcurrentTIntObjectHashMap<PropertyFieldWriter> PROPERTY_WRITERS = new ConcurrentTIntObjectHashMap<PropertyFieldWriter>(4);

    private static FolderFieldWriter getPropertyByField(final int field, final TIntObjectMap<com.openexchange.folderstorage.FolderField> fields) {
        final com.openexchange.folderstorage.FolderField fieldNamePair = fields.get(field);
        if (null == fieldNamePair) {
            LOG.warn("Unknown field: {}", field, new Throwable());
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
     * Determines the supported capabilities for a userized folder to indicate to clients. This is based on the storage folder
     * capabilities, but may be restricted further based on the availability of further services.
     *
     * @param folder The folder to determine the supported capabilities for
     * @return The supported capabilities
     */
    static Set<String> getSupportedCapabilities(UserizedFolder folder) {
        Set<String> capabilities = folder.getSupportedCapabilities();
        if (null == capabilities || 0 == capabilities.size()) {
            return capabilities;
        }
        Set<String> supportedCapabilities = new HashSet<String>(capabilities.size());
        for (String capability : capabilities) {
            try {
                switch (capability) {
                    case "publication":
                        if (supportsPublications(folder)) {
                            supportedCapabilities.add(capability);
                        }
                        break;
                    case "subscription":
                        if (supportsSubscriptions(folder)) {
                            supportedCapabilities.add(capability);
                        }
                        break;
                    default:
                        supportedCapabilities.add(capability);
                        break;
                }
            } catch (OXException e) {
                LOG.warn("Error evaluating capability '{}' for folder {}", capability, folder.getID(), e);
            }
        }
        return supportedCapabilities;
    }

    /**
     * Gets a value indicating whether a specific folder supports publications.
     *
     * @param folder The folder to check
     * @return <code>true</code> if publications are supported, <code>false</code>, otherwise
     */
    private static boolean supportsPublications(UserizedFolder folder) throws OXException {
        PublicationTargetDiscoveryService targetDiscoveryService = ServiceRegistry.getInstance().getService(PublicationTargetDiscoveryService.class);
        if (null != targetDiscoveryService) {
            String module = String.valueOf(folder.getContentType());
            Collection<PublicationTarget> targets = targetDiscoveryService.getTargetsForEntityType(module);
            if (null != targets && 0 < targets.size()) {
                for (PublicationTarget target : targets) {
                    if (target.getPublicationService().isCreateModifyEnabled()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a specific folder supports subscriptions.
     *
     * @param folder The folder to check
     * @return <code>true</code> if subscriptions are supported, <code>false</code>, otherwise
     */
    private static boolean supportsSubscriptions(UserizedFolder folder) throws OXException {
        SubscriptionSourceDiscoveryService sourceDiscoveryService = ServiceRegistry.getInstance().getService(SubscriptionSourceDiscoveryService.class);
        if (null != sourceDiscoveryService) {
            sourceDiscoveryService = sourceDiscoveryService.filter(folder.getUser().getId(), folder.getContext().getContextId());
            List<SubscriptionSource> sources;
            if (null != folder.getContentType()) {
                sources = sourceDiscoveryService.getSources(folder.getContentType().getModule());
            } else {
                sources = sourceDiscoveryService.getSources();
            }
            if (null != sources && 0 < sources.size()) {
                for (SubscriptionSource source : sources) {
                    if (source.getSubscribeService().isCreateModifyEnabled()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
