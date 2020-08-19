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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client.common.calls.folders;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.api.client.common.calls.folders.mapping.PermissionMapper;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.tools.mappings.json.BooleanMapping;
import com.openexchange.groupware.tools.mappings.json.DateMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.b;

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
                object.setCreatedBy(Integer.parseInt(value));
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

        mappings.put(RemoteFolderField.PERMISSIONS, new PermissionMapper<RemoteFolder>(RemoteFolderField.PERMISSIONS.getName(), I(RemoteFolderField.PERMISSIONS.getColumn())) {

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
                if(value != null) {
                    object.setHasSubfolders(b(value));
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

        return mappings;
    }

}
