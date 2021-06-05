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
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * The {@link ScheduleInboxURL}
 *
 * This property allows a client to determine where the scheduling Inbox
 * collection of the current user is located so that processing of
 * scheduling messages can occur. If not present, then the associated
 * calendar user is not enabled for reception of scheduling messages on the
 * server.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ScheduleInboxURL extends SingleXMLPropertyMixin {

	public static final String SCHEDULE_INBOX = "schedule-inbox";
	
	private final ServiceLookup serviceLookup;

    public ScheduleInboxURL(ServiceLookup serviceLookup) {
        super(CaldavProtocol.CAL_NS.getURI(), "schedule-inbox-URL");
        this.serviceLookup = serviceLookup;
    }

    @Override
    protected String getValue() {
        return "<D:href>" + getExternalPath(serviceLookup.getService(ConfigViewFactory.class), "/caldav/" + SCHEDULE_INBOX) + "</D:href>";
    }

}
