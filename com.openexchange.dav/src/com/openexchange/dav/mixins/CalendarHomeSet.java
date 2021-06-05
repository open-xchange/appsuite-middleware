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

package com.openexchange.dav.mixins;

import static com.openexchange.dav.DAVTools.getExternalPath;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CalendarHomeSet}
 *
 * Identifies the URL of any WebDAV collections that contain calendar collections owned by the associated principal resource.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarHomeSet extends SingleXMLPropertyMixin {

    /** The static path to a user's calendar home */
    public static final WebdavPath CALENDAR_HOME = new WebdavPath("caldav");
    
    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link CalendarHomeSet}.
     * 
     * @param configViewFactory The configuration view
     */
    public CalendarHomeSet(ConfigViewFactory configViewFactory) {
        super("urn:ietf:params:xml:ns:caldav", "calendar-home-set");
        this.configViewFactory = configViewFactory;
    }

    @Override
    protected String getValue() {
        return "<D:href>" + getExternalPath(configViewFactory, CALENDAR_HOME.toString() + "/") + "</D:href>";
    }

}
