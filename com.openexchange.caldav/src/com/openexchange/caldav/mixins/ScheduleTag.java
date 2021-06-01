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

import com.openexchange.caldav.resources.EventResource;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link ScheduleTag}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ScheduleTag extends SingleXMLPropertyMixin {

    public static final int NO_ORDER = -1;

    private final String value;

    /**
     * Initializes a new {@link ScheduleTag}.
     *
     * @param eventResource The event resource to initialize with
     */
    public ScheduleTag(EventResource eventResource) {
        super(DAVProtocol.CAL_NS.getURI(), "schedule-tag");
        this.value = eventResource.getScheduleTag();
    }

    @Override
    protected String getValue() {
        return value;
    }

}
