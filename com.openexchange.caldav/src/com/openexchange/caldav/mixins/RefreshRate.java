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

import static com.openexchange.chronos.provider.basic.CommonCalendarConfigurationFields.REFRESH_INTERVAL;
import java.util.concurrent.TimeUnit;
import org.jdom2.Namespace;
import org.json.JSONObject;
import com.openexchange.caldav.resources.EventCollection;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.folderstorage.CalendarFolderConverter;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link RefreshRate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class RefreshRate extends SingleXMLPropertyMixin {

    public static final String NAME = "refreshrate";
    public static final Namespace NAMESPACE = DAVProtocol.APPLE_NS;

    private final EventCollection collection;

    /**
     * Initializes a new {@link RefreshRate}.
     *
     * @param collection The event collection to initialize with
     */
    public RefreshRate(EventCollection collection) {
        super(NAMESPACE.getURI(), NAME);
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        JSONObject calendarConfig = CalendarFolderConverter.optCalendarConfig(collection.getFolder());
        if (null != calendarConfig) {
            long value = calendarConfig.optLong(REFRESH_INTERVAL, 0L);
            if (0L != value) {
                return AlarmUtils.getDuration(value, TimeUnit.MINUTES);
            }
        }
        return null;
    }

}
