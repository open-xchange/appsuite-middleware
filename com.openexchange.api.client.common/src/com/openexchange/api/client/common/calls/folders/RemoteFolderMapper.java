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

package com.openexchange.api.client.common.calls.folders;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.api.client.common.calls.folders.mapping.ExtendedPermissionMapping;
import com.openexchange.api.client.common.calls.folders.mapping.PermissionMapping;
import com.openexchange.api.client.common.calls.mapping.EntityInfoMapping;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.tools.mappings.json.BooleanMapping;
import com.openexchange.groupware.tools.mappings.json.DateMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;

/**
 * {@link RemoteFolderMapper}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class RemoteFolderMapper extends DefaultJsonMapper<RemoteFolder, RemoteFolderField> {

    @Override
    public RemoteFolder newInstance() {
        return new RemoteFolder();
    }

    @Override
    public RemoteFolderField[] newArray(int size) {
        return new RemoteFolderField[size];
    }

    @Override
    protected EnumMap<RemoteFolderField, ? extends JsonMapping<? extends Object, RemoteFolder>> createMappings() {
        //@formatter:off
        EnumMap<RemoteFolderField, JsonMapping<? extends Object, RemoteFolder>> mappings = new
            EnumMap<RemoteFolderField, JsonMapping<? extends Object, RemoteFolder>>(RemoteFolderField.class);
        //@formatter:on

        mappings.put(RemoteFolderField.ID, new StringMapping<RemoteFolder>(RemoteFolderField.ID.getName(), I(RemoteFolderField.ID.getColumn())) {

            @Override
            public void set(RemoteFolder object, String value) throws OXException {
                object.setID(value);
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setID(null);
            }

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getID() != null;
            }

            @Override
            public String get(RemoteFolder object) {
                return object.getID();
            }
        });

        mappings.put(RemoteFolderField.CREATED_BY, new StringMapping<RemoteFolder>(RemoteFolderField.CREATED_BY.getName(), I(RemoteFolderField.CREATED_BY.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getCreatedBy() != -1;
            }

            @Override
            public void set(RemoteFolder object, String value) throws OXException {
                if (value != null) {
                    object.setCreatedBy(Integer.parseInt(value));
                } else {
                    remove(object);
                }
            }

            @Override
            public String get(RemoteFolder object) {
                return String.valueOf(object.getCreatedBy());
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setCreatedBy(-1);
            }
        });

        mappings.put(RemoteFolderField.MODIFIED_BY, new StringMapping<RemoteFolder>(RemoteFolderField.MODIFIED_BY.getName(), I(RemoteFolderField.MODIFIED_BY.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getModifiedBy() != -1;
            }

            @Override
            public void set(RemoteFolder object, String value) throws OXException {
                if (value != null) {
                    object.setModifiedBy(Integer.parseInt(value));
                } else {
                    remove(object);
                }
            }

            @Override
            public String get(RemoteFolder object) {
                return String.valueOf(object.getModifiedBy());
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setModifiedBy(-1);
            }
        });

        mappings.put(RemoteFolderField.CREATION_DATE, new DateMapping<RemoteFolder>(RemoteFolderField.CREATION_DATE.getName(), I(RemoteFolderField.CREATION_DATE.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getCreationDate() != null;
            }

            @Override
            public void set(RemoteFolder object, Date value) throws OXException {
                object.setCreationDate(value);
            }

            @Override
            public Date get(RemoteFolder object) {
                return object.getCreationDate();
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setCreationDate(null);
            }
        });

        mappings.put(RemoteFolderField.LAST_MODIFIED, new DateMapping<RemoteFolder>(RemoteFolderField.LAST_MODIFIED.getName(), I(RemoteFolderField.LAST_MODIFIED.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getLastModified() != null;
            }

            @Override
            public void set(RemoteFolder object, Date value) throws OXException {
                object.setLastModified(value);
            }

            @Override
            public Date get(RemoteFolder object) {
                return object.getLastModified();
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setLastModified(null);
            }
        });

        mappings.put(RemoteFolderField.FODLER_ID, new StringMapping<RemoteFolder>(RemoteFolderField.FODLER_ID.getName(), I(RemoteFolderField.FODLER_ID.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return null != object.getParentID();
            }

            @Override
            public void set(RemoteFolder object, String value) throws OXException {
                object.setParentID("0".equals(value) ? null : value);
            }

            @Override
            public String get(RemoteFolder object) {
                return object.getParentID();
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setParentID(null);
            }
        });
        mappings.put(RemoteFolderField.TITLE, new StringMapping<RemoteFolder>(RemoteFolderField.TITLE.getName(), I(RemoteFolderField.TITLE.getColumn())) {

            @Override
            public void set(RemoteFolder object, String value) throws OXException {
                object.setName(value);
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setName(null);
            }

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getName() != null;
            }

            @Override
            public String get(RemoteFolder object) {
                return object.getName();
            }
        });

        mappings.put(RemoteFolderField.MODULE, new StringMapping<RemoteFolder>(RemoteFolderField.MODULE.getName(), I(RemoteFolderField.MODULE.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getModule() != null;
            }

            @Override
            public void set(RemoteFolder object, String value) throws OXException {
                object.setModule(value);
            }

            @Override
            public String get(RemoteFolder object) {
                return object.getModule();
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setModule(null);
            }
        });

        mappings.put(RemoteFolderField.PERMISSIONS, new PermissionMapping<RemoteFolder>(RemoteFolderField.PERMISSIONS.getName(), I(RemoteFolderField.PERMISSIONS.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.getPermissions() != null;
            }

            @Override
            public void set(RemoteFolder object, List<Permission> value) throws OXException {
                object.setPermissions(value.toArray(new Permission[value.size()]));
            }

            @Override
            public List<Permission> get(RemoteFolder object) {
                return Arrays.asList(object.getPermissions());
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setPermissions(null);
            }
        });

        mappings.put(RemoteFolderField.SUBFOLDERS, new BooleanMapping<RemoteFolder>(RemoteFolderField.SUBFOLDERS.getName(), I(RemoteFolderField.SUBFOLDERS.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.containsHasSubfolders();
            }

            @Override
            public void set(RemoteFolder object, Boolean value) throws OXException {
                if (value != null) {
                    object.setHasSubfolders(b(value));
                } else {
                    remove(object);
                }
            }

            @Override
            public Boolean get(RemoteFolder object) {
                return B(object.hasSubfolders());
            }

            @Override
            public void remove(RemoteFolder object) {
                object.removeHasSubfolders();
            }
        });

        mappings.put(RemoteFolderField.OWN_RIGHTS, new IntegerMapping<RemoteFolder>(RemoteFolderField.OWN_RIGHTS.getName(), I(RemoteFolderField.OWN_RIGHTS.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.containsOwnRights();
            }

            @Override
            public void set(RemoteFolder object, Integer value) throws OXException {
                if (value != null) {
                    object.setOwnRights(i(value));
                }
            }

            @Override
            public Integer get(RemoteFolder object) {
                return I(object.getOwnRights());
            }

            @Override
            public void remove(RemoteFolder object) {
                object.removeOwnRights();
            }
        });

        mappings.put(RemoteFolderField.SUBSCRIBED, new BooleanMapping<RemoteFolder>(RemoteFolderField.SUBSCRIBED.getName(), I(RemoteFolderField.SUBSCRIBED.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.containsSubscribed();
            }

            @Override
            public void set(RemoteFolder object, Boolean value) throws OXException {
                if (null != value) {
                    object.setSubscribed(b(value));
                } else {
                    remove(object);
                }
            }

            @Override
            public Boolean get(RemoteFolder object) {
                return B(object.isSubscribed());
            }

            @Override
            public void remove(RemoteFolder object) {
                object.removeSubscribed();
            }
        });

        mappings.put(RemoteFolderField.SUBSCR_SUBFLDS, new BooleanMapping<RemoteFolder>(RemoteFolderField.SUBSCR_SUBFLDS.getName(), I(RemoteFolderField.SUBSCR_SUBFLDS.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.containsSubscribedSubfolders();
            }

            @Override
            public void set(RemoteFolder object, Boolean value) throws OXException {
                if (null != value) {
                    object.setSubscribedSubfolders(b(value));
                } else {
                    remove(object);
                }
            }

            @Override
            public Boolean get(RemoteFolder object) {
                return B(object.hasSubscribedSubfolders());
            }

            @Override
            public void remove(RemoteFolder object) {
                object.removeSubscribedSubfolders();
            }
        });

        mappings.put(RemoteFolderField.EXTENDED_PERMISSIONS, new ExtendedPermissionMapping<RemoteFolder>(RemoteFolderField.EXTENDED_PERMISSIONS.getName(), I(RemoteFolderField.EXTENDED_PERMISSIONS.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return object.containsExtendedPermissions();
            }

            @Override
            public void set(RemoteFolder object, ExtendedPermission[] value) throws OXException {
                object.setExtendedPermissions(value);
            }

            @Override
            public ExtendedPermission[] get(RemoteFolder object) {
                return object.getExtendedPermissions();
            }

            @Override
            public void remove(RemoteFolder object) {
                object.removeExtendedPermissions();
            }

        });

        mappings.put(RemoteFolderField.CREATED_FROM, new EntityInfoMapping<RemoteFolder>(RemoteFolderField.CREATED_FROM.getName(), I(RemoteFolderField.CREATED_FROM.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return null != object.getCreatedFrom();
            }

            @Override
            public void set(RemoteFolder object, EntityInfo value) throws OXException {
                object.setCreatedFrom(value);
            }

            @Override
            public EntityInfo get(RemoteFolder object) {
                return object.getCreatedFrom();
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setCreatedFrom(null);
            }
        });

        mappings.put(RemoteFolderField.MODIFIED_FROM, new EntityInfoMapping<RemoteFolder>(RemoteFolderField.MODIFIED_FROM.getName(), I(RemoteFolderField.MODIFIED_FROM.getColumn())) {

            @Override
            public boolean isSet(RemoteFolder object) {
                return null != object.getModifiedFrom();
            }

            @Override
            public void set(RemoteFolder object, EntityInfo value) throws OXException {
                object.setModifiedFrom(value);
            }

            @Override
            public EntityInfo get(RemoteFolder object) {
                return object.getModifiedFrom();
            }

            @Override
            public void remove(RemoteFolder object) {
                object.setModifiedFrom(null);
            }
        });

        return mappings;
    }

}
