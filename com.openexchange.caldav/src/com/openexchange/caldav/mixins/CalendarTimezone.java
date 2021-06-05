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

import org.jdom2.Namespace;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.user.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CalendarTimezone}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class CalendarTimezone extends SingleXMLPropertyMixin {

    public static final String NAME = "calendar-timezone";
    public static final Namespace NAMESPACE = DAVProtocol.CAL_NS;

    private final GroupwareCaldavFactory factory;
    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link CalendarTimezone}.
     *
     * @param factory The CalDAV factory
     * @param collection The collection
     */
    public CalendarTimezone(GroupwareCaldavFactory factory, FolderCollection<?> collection) {
        super(NAMESPACE.getURI(), NAME);
        this.factory = factory;
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        try {
            User user = collection.getOwner();
            if (null == user || Strings.isEmpty(user.getTimeZone())) {
                user = factory.getUser();
            }
            if (Strings.isNotEmpty(user.getTimeZone())) {
                ICalService iCalService = factory.getService(ICalService.class);
                byte[] iCal = iCalService.exportICal(iCalService.initParameters()).add(user.getTimeZone()).toByteArray();
                return "<![CDATA[" + new String(iCal, Charsets.UTF_8) + "]]>";
            }
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(CalendarTimezone.class).warn("Error serializing calendar-timezone", e);
        }
        return null;
    }

}
