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

import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.user.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CalendarOwner}<p/>
 *
 * <b>Name:</b>   calendar-owner<p/>
 * <b>Location:</b>   MUST appear on a calendar if there is a principal resources (user or group) with which it is associated.<p/>
 * <b>Purpose:</b>   This property is used for browsing clients to find out the user, group or resource for which the calendar events are
 * scheduled.  Sometimes the calendar is a user's calendar, in which case the value SHOULD be the user's principal URL from WebDAV ACL.
 * (In this case the DAV:owner property probably has the same principal URL value.) If the calendar is a group calendar the value SHOULD be
 * the group's principal URL.  (In this case the DAV:owner property probably specifies one user who manages this group calendar.) If the
 * calendar is a resource calendar (e.g.  for a room, or a projector) there won't be a principal URL, so some other URL SHOULD be used.
 * A LDAP URL could be useful in this case.  This property contains one 'href' element in the "DAV:" namespace.</p>
 * <b>Declaration:</b>   <code><!ELEMENT calendar-owner (href) ></code>
 * <b>Extensibility:</b>   MAY contain additional elements, which MUST be ignored if not understood.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarOwner extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarOwner.class);

    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link CalendarOwner}.
     *
     * @param collection The underlying folder collection to represent the calendar owner for
     */
    public CalendarOwner(FolderCollection<?> collection) {
        super(CaldavProtocol.CAL_NS.getURI(), "calendar-owner");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        User owner = null;
        try {
            owner = collection.getOwner();
        } catch (OXException e) {
            LOG.warn("error determining owner from folder collection '{}'", collection.getFolder(), e);
        }
        return null != owner ? "<D:href>" + PrincipalURL.forUser(owner.getId(), collection.getFactory().getService(ConfigViewFactory.class)) + "</D:href>" : null;
    }

}
