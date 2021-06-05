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

package com.openexchange.caldav;

import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalDAVURLField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalDAVURLField implements AdditionalFolderField {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalDAVURLField}.
     *
     * @param services A service lookup reference
     */
    public CalDAVURLField(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public int getColumnID() {
        return 3220;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.caldav.url";
    }

    @Override
    public Object getValue(Folder item, ServerSession session) {
        String resourceName = getCalDAVResourceName(session, item);
        if (null == resourceName) {
            return null;
        }
        return getURLTemplate(session).replaceAll("\\[folderId\\]", resourceName);
    }

    @Override
    public List<Object> getValues(List<Folder> items, ServerSession session) {
        List<Object> values = new ArrayList<Object>(items.size());
        String urlTemplate = getURLTemplate(session);
        for (Folder item : items) {
            String resourceName = getCalDAVResourceName(session, item);
            if (null != resourceName) {
                values.add(urlTemplate.replaceAll("\\[folderId\\]", resourceName));
            } else {
                values.add(null);
            }
        }
        return values;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        if (null == value || false == String.class.isInstance(value) || null == requestData || null == requestData.getHostname()) {
            return value;
        }
        return ((String) value).replaceAll("\\[hostname\\]", requestData.getHostname());
    }

    private String getURLTemplate(ServerSession session) {
        String defaultValue = "https://[hostname]/caldav/[folderId]";
        try {
            ConfigView view = requireService(ConfigViewFactory.class, services).getView(session.getUserId(), session.getContextId());
            ComposedConfigProperty<String> property = view.property("com.openexchange.caldav.url", String.class);
            if (null != property && property.isDefined()) {
                return property.get();
            }
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(CalDAVURLField.class).warn("Error getting property \"com.openexchange.caldav.url\", falling back to {}.", defaultValue, e);
        }
        return defaultValue;
    }

    private static String getCalDAVResourceName(ServerSession session, Folder folder) {
        ContentType contentType = folder.getContentType();
        if (null == contentType) {
            return null;
        }
        switch (contentType.getModule()) {
            case FolderObject.CALENDAR:
                if (false == session.getUserConfiguration().hasCalendar() || false == session.getUserConfiguration().hasPermission(Permission.CALDAV)) {
                    return null;
                }
                String id = folder.getID();
                return Tools.encodeFolderId(id);
            case FolderObject.TASK:
                if (false == session.getUserConfiguration().hasTask() || false == session.getUserConfiguration().hasPermission(Permission.CALDAV)) {
                    return null;
                }
                return Tools.encodeFolderId(folder.getID());
            default:
                return null;
        }
    }

}
