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

import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.optPropertyValue;
import org.jdom2.Namespace;
import com.openexchange.chronos.Transp;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.folderstorage.CalendarFolderConverter;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link ScheduleCalendarTransp}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ScheduleCalendarTransp extends SingleXMLPropertyMixin {

    public static final String NAME = "schedule-calendar-transp";
    public static final Namespace NAMESPACE = DAVProtocol.CAL_NS;

    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link ScheduleCalendarTransp}.
     *
     * @param collection The underlying collection
     */
    public ScheduleCalendarTransp(FolderCollection<?> collection) {
        super(NAMESPACE.getURI(), NAME);
        this.collection = collection;
    }

    @Override
    protected void configureProperty(WebdavProperty property) {
        property.setXML(true);
        property.setValue(getValue());
    }

    @Override
    protected String getValue() {
        if (null != collection.getFolder()) {
            Object value = optPropertyValue(CalendarFolderConverter.getExtendedProperties(collection.getFolder()), SCHEDULE_TRANSP_LITERAL);
            if (null != value) {
                value = Transp.TRANSPARENT.equals(value) ? Transp.TRANSPARENT : Transp.OPAQUE;
                return "<CAL:" + String.valueOf(value).toLowerCase() + "/>";
            }
        }
        return null;
    }

}
