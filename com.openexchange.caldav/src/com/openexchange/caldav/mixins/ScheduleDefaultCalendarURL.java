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

package com.openexchange.caldav.mixins;

import static com.openexchange.dav.DAVTools.getExternalPath;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.calendar.contentType.CalendarContentType;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * The {@link ScheduleDefaultCalendarURL}
 *
 * This property MAY be defined on a scheduling Inbox
 * collection.  If present, it contains zero or one DAV:href XML
 * elements.  When a DAV:href element is present, its value indicates
 * a URL to a calendar collection that is used as the default
 * calendar.  When no DAV:href element is present, it indicates that
 * there is no default calendar.  In the absence of this property,
 * there is no default calendar.  When there is no default calendar,
 * the server is free to choose the calendar in which a new
 * scheduling object resource is created.
 * <p/>
 * https://tools.ietf.org/html/rfc6638#section-9.2
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ScheduleDefaultCalendarURL extends SingleXMLPropertyMixin {

    private final GroupwareCaldavFactory factory;

    /**
     * Initializes a new {@link ScheduleDefaultCalendarURL}.
     *
     * @param factory The CalDAV factory
     */
    public ScheduleDefaultCalendarURL(GroupwareCaldavFactory factory) {
        super(CaldavProtocol.CAL_NS.getURI(), "schedule-default-calendar-URL");
        this.factory = factory;
    }

    @Override
    protected String getValue() {
        String value = null;
        try {
            String treeID = factory.getConfigValue("com.openexchange.caldav.tree", FolderStorage.REAL_TREE_ID);
            UserizedFolder defaultFolder = factory.getFolderService().getDefaultFolder(
                factory.getUser(), treeID, CalendarContentType.getInstance(), factory.getSession(), null);
            if (null != defaultFolder) {
                value = Tools.encodeFolderId(defaultFolder.getID());
            }
            return null == value ? null : "<D:href>" + getExternalPath(factory.getConfigViewFactory(), "/caldav/" + value + "/") + "</D:href>";
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleDefaultCalendarURL.class).warn("Error determining 'schedule-default-calendar-URL'", e);
            return null;
        }
    }

}
