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

package com.openexchange.share.json.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExtendedObjectPermissionsField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ExtendedObjectPermissionsField implements AdditionalFileField {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ExtendedObjectPermissionsField}.
     *
     * @param services The service lookup reference
     */
    public ExtendedObjectPermissionsField(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Field[] getRequiredFields() {
        return new Field[] { Field.OBJECT_PERMISSIONS, Field.FOLDER_ID, Field.ID, Field.CREATED_BY };
    }

    @Override
    public int getColumnID() {
        return 7010;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.share.extendedObjectPermissions";
    }

    @Override
    public Object getValue(File file, ServerSession session) {
        return getValues(Collections.singletonList(file), session).iterator().next();
    }

    @Override
    public List<Object> getValues(List<File> files, ServerSession session) {
        if (null == files) {
            return null;
        }
        PermissionResolver resolver = new PermissionResolver(services, session);
        resolver.cacheFilePermissionEntities(files);
        List<Object> values = new ArrayList<Object>();
        for (File file : files) {
            List<FileStorageObjectPermission> objectPermissions = file.getObjectPermissions();
            if (null != objectPermissions) {
                List<ExtendedObjectPermission> extendedPermissions = new ArrayList<ExtendedObjectPermission>(objectPermissions.size());
                for (FileStorageObjectPermission objectPermission : objectPermissions) {
                    extendedPermissions.add(new ExtendedObjectPermission(resolver, file, objectPermission));
                }
                values.add(extendedPermissions);
            } else {
                values.add(null);
            }
        }
        return values;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        if (null != value && List.class.isInstance(value)) {
            List<?> values = (List<?>) value;
            JSONArray jsonArray = new JSONArray(values.size());
            for (Object item : values) {
                if (ExtendedObjectPermission.class.isInstance(item)) {
                    try {
                        jsonArray.put(((ExtendedObjectPermission) item).toJSON(requestData));
                    } catch (JSONException | OXException e) {
                        org.slf4j.LoggerFactory.getLogger(ExtendedObjectPermissionsField.class).error("Error serializing extended permissions", e);
                    }
                }
            }
            return jsonArray;
        }
        return new JSONArray(0);
    }

}
