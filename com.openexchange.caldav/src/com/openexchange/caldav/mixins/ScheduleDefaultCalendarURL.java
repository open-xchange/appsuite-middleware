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

package com.openexchange.caldav.mixins;

import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
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
                value = defaultFolder.getID();
            }
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleDefaultCalendarURL.class).warn("Error determining 'schedule-default-calendar-URL'", e);
        }
        return null == value ? null : "<D:href>/caldav/" + value + "/</D:href>";
    }

}
