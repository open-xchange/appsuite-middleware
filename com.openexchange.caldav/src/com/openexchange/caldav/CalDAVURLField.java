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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.java.Strings;
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
    public Object getValue(FolderObject item, ServerSession session) {
        String resourceName = getCalDAVResourceName(session, item);
        if (null == resourceName) {
            return null;
        }
        return getURLTemplate(session).replaceAll("\\[folderId\\]", resourceName);
    }

    @Override
    public List<Object> getValues(List<FolderObject> items, ServerSession session) {
        List<Object> values = new ArrayList<Object>(items.size());
        String urlTemplate = getURLTemplate(session);
        for (FolderObject item : items) {
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

    private static String getCalDAVResourceName(ServerSession session, FolderObject folder) {
        switch (folder.getModule()) {
            case FolderObject.CALENDAR:
                if (false == session.getUserConfiguration().hasCalendar() || false == session.getUserConfiguration().hasPermission(Permission.CALDAV)) {
                    return null;
                }
                String id = folder.getFullName();
                return Tools.encodeFolderId(Strings.isEmpty(id) ? Tools.DEFAULT_ACCOUNT_PREFIX + folder.getObjectID() : id);
            case FolderObject.TASK:
                if (false == session.getUserConfiguration().hasTask() || false == session.getUserConfiguration().hasPermission(Permission.CALDAV)) {
                    return null;
                }
                return Tools.encodeFolderId(String.valueOf(folder.getObjectID()));
            default:
                return null;
        }
    }

}
