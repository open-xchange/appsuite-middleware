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

package com.openexchange.mail.json.writer;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderWriter} - Writes {@link MailFolder} instances as JSON strings.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderWriter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderWriter.class);

    public interface JSONValuePutter {

        void put(String key, Object value) throws JSONException;

        boolean withKey();
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

        @Override
        public boolean withKey() {
            return false;
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

        @Override
        public boolean withKey() {
            return true;
        }

    }

    public static abstract class MailFolderFieldWriter {

        public void writeField(final JSONValuePutter jsonContainer, final int accountId, final MailFolder folder) throws OXException {
            writeField(jsonContainer, accountId, folder, null, -1);
        }

        public void writeField(final JSONValuePutter jsonContainer, final int accountId, final MailFolder folder, final String name, final int hasSubfolders) throws OXException {
            writeField(jsonContainer, accountId, folder, name, hasSubfolders, null, -1, false);
        }

        public abstract void writeField(JSONValuePutter putter, int accountId, MailFolder folder, String name, int hasSubfolders, String fullName, int module, boolean all) throws OXException;
    }

    private static abstract class ExtendedMailFolderFieldWriter extends MailFolderFieldWriter {

        final MailConfig mailConfig;

        public ExtendedMailFolderFieldWriter(final MailConfig mailConfig) {
            super();
            this.mailConfig = mailConfig;
        }

    }

    /**
     * Maps folder field constants to corresponding instance of {@link MailFolderFieldWriter}.
     */
    private static final TIntObjectMap<MailFolderFieldWriter> WRITERS_MAP = new TIntObjectHashMap<MailFolderFieldWriter>(20);

    static {
        WRITERS_MAP.put(DataObject.OBJECT_ID, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    if (null == fullName) {
                        final String value = accountId >= 0 ? prepareFullname(accountId, folder.getFullname()) : folder.getFullname();
                        if (null == value) {
                            throw MailExceptionCode.MISSING_FULLNAME.create();
                        }
                        putter.put(putter.withKey() ? DataFields.ID : null, value);
                    } else {
                        putter.put(putter.withKey() ? DataFields.ID : null, fullName);
                    }
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.CREATED_BY, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    putter.put(putter.withKey() ? DataFields.CREATED_BY : null, Integer.valueOf(-1));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.MODIFIED_BY, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    putter.put(putter.withKey() ? DataFields.MODIFIED_BY : null, Integer.valueOf(-1));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.CREATION_DATE, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    putter.put(putter.withKey() ? DataFields.CREATION_DATE : null, Integer.valueOf(0));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(DataObject.LAST_MODIFIED, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    putter.put(putter.withKey() ? DataFields.LAST_MODIFIED : null, Integer.valueOf(0));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderChildObject.FOLDER_ID, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    final Object parent;
                    if (null == folder.getParentFullname()) {
                        parent = Integer.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
                    } else {
                        parent = accountId >= 0 ? prepareFullname(accountId, folder.getParentFullname()) : folder.getParentFullname();
                    }
                    putter.put(putter.withKey() ? FolderChildFields.FOLDER_ID : null, parent);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.FOLDER_NAME, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    putter.put(putter.withKey() ? FolderFields.TITLE : null, name == null ? folder.getName() : name);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.MODULE, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    putter.put(putter.withKey() ? FolderFields.MODULE : null, AJAXServlet.getModuleString(module == -1 ? FolderObject.MAIL : module, -1));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.TYPE, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    putter.put(putter.withKey() ? FolderFields.TYPE : null, Integer.valueOf(FolderObject.MAIL));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUBFOLDERS, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
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
                    putter.put(putter.withKey() ? FolderFields.SUBFOLDERS : null, Boolean.valueOf(boolVal));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.OWN_RIGHTS, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    final MailPermission mp;
                    if (folder.isRootFolder()) {
                        final MailPermission rootPermission = folder.getOwnPermission();
                        if (rootPermission == null) {
                            mp = new DefaultMailPermission();
                            mp.setAllPermission(
                                OCLPermission.CREATE_SUB_FOLDERS,
                                OCLPermission.NO_PERMISSIONS,
                                OCLPermission.NO_PERMISSIONS,
                                OCLPermission.NO_PERMISSIONS);
                            mp.setFolderAdmin(false);
                        } else {
                            mp = rootPermission;
                        }
                    } else {
                        mp = folder.getOwnPermission();
                    }
                    if (!folder.isHoldsFolders() && mp.canCreateSubfolders()) {
                        // Cannot contain subfolders; therefore deny subfolder creation
                        mp.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
                    }
                    if (!folder.isHoldsMessages() && mp.canReadOwnObjects()) {
                        // Cannot contain messages; therefore deny read access. Folder is not selectable.
                        mp.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
                    }
                    int permissionBits = createPermissionBits(mp);
                    if (folder.isSupportsUserFlags()) {
                        permissionBits |= BIT_USER_FLAG;
                    }
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.OWN_RIGHTS : null, Integer.valueOf(permissionBits));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.PERMISSIONS_BITS, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    final JSONArray ja = new JSONArray();
                    final OCLPermission[] perms = folder.getPermissions();
                    for (int j = 0; j < perms.length; j++) {
                        final JSONObject jo = new JSONObject();
                        jo.put(FolderFields.BITS, createPermissionBits(perms[j]));
                        jo.put(FolderFields.ENTITY, perms[j].getEntity());
                        jo.put(FolderFields.GROUP, perms[j].isGroupPermission());
                        ja.put(jo);
                    }
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.PERMISSIONS : null, ja);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUMMARY, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    final String value =
                        folder.isRootFolder() ? "" : new StringBuilder(16).append('(').append(folder.getMessageCount()).append('/').append(
                            folder.getUnreadMessageCount()).append(')').toString();
                    putter.put(putter.withKey() ? FolderFields.SUMMARY : null, value);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.STANDARD_FOLDER, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.STANDARD_FOLDER : null, Boolean.valueOf(folder.containsDefaultFolder() ? folder.isDefaultFolder() : false));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.TOTAL, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.TOTAL : null, Integer.valueOf(folder.getMessageCount()));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.NEW, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.NEW : null, Integer.valueOf(folder.getNewMessageCount()));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.UNREAD, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.UNREAD : null, Integer.valueOf(folder.getUnreadMessageCount()));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.DELETED, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.DELETED : null, Integer.valueOf(folder.getDeletedMessageCount()));
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUBSCRIBED, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    final Object boolVal;
                    if (MailProperties.getInstance().isIgnoreSubscription()) {
                        boolVal = Boolean.TRUE;
                    } else {
                        boolVal = folder.containsSubscribed() ? Boolean.valueOf(folder.isSubscribed()) : JSONObject.NULL;
                    }
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.SUBSCRIBED : null, boolVal);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
        WRITERS_MAP.put(FolderObject.SUBSCR_SUBFLDS, new MailFolderFieldWriter() {

            @Override
            public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                try {
                    final Object boolVal;
                    if (MailProperties.getInstance().isIgnoreSubscription()) {
                        boolVal = hasSubfolders == -1 ? Boolean.valueOf(folder.hasSubfolders()) : Boolean.valueOf(hasSubfolders > 0);
                    } else if (hasSubfolders == -1) {
                        boolVal = folder.hasSubfolders() ? Boolean.valueOf(folder.hasSubscribedSubfolders()) : Boolean.FALSE;
                    } else {
                        boolVal = Boolean.valueOf(hasSubfolders > 0);
                    }
                    /*
                     * Put value
                     */
                    putter.put(putter.withKey() ? FolderFields.SUBSCR_SUBFLDS : null, boolVal);
                } catch (final JSONException e) {
                    throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                }
            }
        });
    }

    /**
     * No instantiation
     */
    private FolderWriter() {
        super();
    }

    private static final int[] ALL_FLD_FIELDS = com.openexchange.ajax.writer.FolderWriter.getAllFolderFields();

    /**
     * Writes whole folder as a JSON object
     *
     * @param accountId The account ID
     * @param folder The folder to write
     * @param session The server session
     * @return The written JSON object
     * @throws OXException
     */
    public static JSONObject writeMailFolder(final int accountId, final MailFolder folder, final MailConfig mailConfig, final ServerSession session) throws OXException {
        final JSONObject jsonObject = new JSONObject();
        final JSONValuePutter putter = new JSONObjectPutter(jsonObject);
        final MailFolderFieldWriter[] writers = getMailFolderFieldWriter(ALL_FLD_FIELDS, mailConfig, session);
        for (final MailFolderFieldWriter writer : writers) {
            writer.writeField(putter, accountId, folder);
        }
        return jsonObject;
    }

    private static final int BIT_USER_FLAG = (1 << 29);

    // private static final String STR_UNKNOWN_COLUMN = "Unknown column";

    /**
     * Generates appropriate field writers for given mail folder fields
     *
     * @param fields The fields to write
     * @param mailConfig Current mail configuration
     * @param session The server session
     * @return Appropriate field writers as an array of {@link MailFolderFieldWriter}
     */
    public static MailFolderFieldWriter[] getMailFolderFieldWriter(final int[] fields, final MailConfig mailConfig, final ServerSession session) {
        return getMailFolderFieldWriter(fields, mailConfig, session, Folder.getAdditionalFields());
    }

    /**
     * Generates appropriate field writers for given mail folder fields
     *
     * @param fields The fields to write
     * @param mailConfig Current mail configuration
     * @param session The server session
     * @param additionalFields Additional fields
     * @return Appropriate field writers as an array of {@link MailFolderFieldWriter}
     */
    public static MailFolderFieldWriter[] getMailFolderFieldWriter(final int[] fields, final MailConfig mailConfig, final ServerSession session, final AdditionalFolderFieldList additionalFields) {
        final MailFolderFieldWriter[] retval = new MailFolderFieldWriter[fields.length];
        for (int i = 0; i < retval.length; i++) {
            final int curField = fields[i];
            final MailFolderFieldWriter mffw = WRITERS_MAP.get(curField);
            if (mffw == null) {
                if (FolderObject.CAPABILITIES == curField) {
                    retval[i] = new ExtendedMailFolderFieldWriter(mailConfig) {

                        @Override
                        public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                            try {
                                /*
                                 * Put value
                                 */
                                putter.put(FolderFields.CAPABILITIES, Integer.valueOf(mailConfig.getCapabilities().getCapabilities()));
                            } catch (final JSONException e) {
                                throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                            }
                        }
                    };
                } else {

                    if (!additionalFields.knows(curField)) {
                        LOG.warn("Unknown folder field: {}", curField);
                    }

                    final AdditionalFolderField folderField = additionalFields.get(curField);
                    retval[i] = new MailFolderFieldWriter() {

                        @Override
                        public void writeField(final JSONValuePutter putter, final int accountId, final MailFolder folder, final String name, final int hasSubfolders, final String fullName, final int module, final boolean all) throws OXException {
                            try {
                                /*
                                 * Proper MailFolder-2-FolderObject conversion
                                 */
                                final FolderObject fo = new FolderObject();
                                fo.setFullName(folder.getFullname());
                                fo.setFolderName(folder.getName());
                                fo.setModule(FolderObject.MAIL);
                                fo.setType(FolderObject.PRIVATE);
                                fo.setCreatedBy(-1);
                                putter.put(folderField.getColumnName(), folderField.renderJSON(null, folderField.getValue(fo, session)));
                            } catch (final JSONException e) {
                                throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
                            }
                        }

                    };
                }
            } else {
                retval[i] = mffw;
            }
        }
        return retval;
    }

    private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };

    static int createPermissionBits(final OCLPermission perm) throws OXException {
        return createPermissionBits(
            perm.getFolderPermission(),
            perm.getReadPermission(),
            perm.getWritePermission(),
            perm.getDeletePermission(),
            perm.isFolderAdmin());
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
                        throw OXFolderExceptionCode.MAP_PERMISSION_FAILED.create(e, Integer.valueOf(permission[i]));
                    }
                }
            }
        }
        return retval;
    }

}
