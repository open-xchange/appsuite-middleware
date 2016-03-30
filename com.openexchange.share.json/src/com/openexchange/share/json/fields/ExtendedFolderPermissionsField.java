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
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExtendedFolderPermissionsField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ExtendedFolderPermissionsField implements AdditionalFolderField {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ExtendedFolderPermissionsField}.
     *
     * @param services The service lookup reference
     */
    public ExtendedFolderPermissionsField(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public int getColumnID() {
        return 3060;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.share.extendedPermissions";
    }

    @Override
    public Object getValue(FolderObject folder, ServerSession session) {
        return getValues(Collections.singletonList(folder), session).iterator().next();
    }

    @Override
    public List<Object> getValues(List<FolderObject> folders, ServerSession session) {
        if (null == folders) {
            return null;
        }
        PermissionResolver resolver = new PermissionResolver(services, session);
        resolver.cacheFolderPermissionEntities(folders);
        List<Object> values = new ArrayList<Object>();
        for (FolderObject folder : folders) {
            List<OCLPermission> oclPermissions = folder.getPermissions();
            if (null != oclPermissions) {
                List<ExtendedFolderPermission> extendedPermissions = new ArrayList<ExtendedFolderPermission>(oclPermissions.size());
                for (OCLPermission oclPermission : oclPermissions) {
                    extendedPermissions.add(new ExtendedFolderPermission(resolver, folder, oclPermission));
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
                if (ExtendedFolderPermission.class.isInstance(item)) {
                    try {
                        jsonArray.put(((ExtendedFolderPermission) item).toJSON(requestData));
                    } catch (JSONException | OXException e) {
                        org.slf4j.LoggerFactory.getLogger(ExtendedFolderPermissionsField.class).error("Error serializing extended permissions", e);
                    }
                }
            }
            return jsonArray;
        }
        return null;
    }

}
