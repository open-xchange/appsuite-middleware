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

package com.openexchange.dav.caldav;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import com.openexchange.dav.caldav.ical.ICalUtils;
import com.openexchange.dav.caldav.ical.SimpleICal;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException;

/**
 * {@link ICalResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ICalResource {

    public static final String VFREEBUSY = "VFREEBUSY";
    public static final String VCALENDAR = "VCALENDAR";
    public static final String VEVENT = "VEVENT";
    public static final String VALARM = "VALARM";
    public static final String VTODO = "VTODO";
    public static final String VTIMEZONE = "VTIMEZONE";
    public static final String VAVAILABILITY = "VAVAILABILITY";

    private String eTag;
    private String href;
    private String scheduleTag;
    private final Component vCalendar;

    public ICalResource(String iCalString, String href, String eTag) throws IOException, SimpleICalException {
        super();
        this.href = href;
        this.eTag = eTag;
        this.vCalendar = SimpleICal.parse(iCalString);
    }

    public ICalResource(String iCalString) throws IOException, com.openexchange.dav.caldav.ical.SimpleICal.SimpleICalException {
        this(iCalString, null, null);
    }

    public Component getVCalendar() {
        return vCalendar;
    }

    public Component getVEvent() {
        List<Component> components = vCalendar.getComponents(VEVENT);
        return 0 < components.size() ? components.get(0) : null;
    }

    public Component getVEvent(String recurrenceId) {
        for (Component component : vCalendar.getComponents(VEVENT)) {
            if (Objects.equals(recurrenceId, component.getPropertyValue("RECURRENCE-ID"))) {
                return component;
            }
        }
        return null;
    }

    public Component getVTodo() {
        List<Component> components = vCalendar.getComponents(VTODO);
        return 0 < components.size() ? components.get(0) : null;
    }

    public Component getVFreeBusy() {
        List<Component> components = vCalendar.getComponents(VFREEBUSY);
        return 0 < components.size() ? components.get(0) : null;
    }

    public List<Component> getVEvents() {
        return vCalendar.getComponents(VEVENT);
    }

    public List<Component> getVFreeBusys() {
        return vCalendar.getComponents(VFREEBUSY);
    }

    public List<Component> getAvailabilities() {
        return vCalendar.getComponents(VAVAILABILITY);
    }

    public void addComponent(Component component) {
        vCalendar.getComponents().add(component);
    }

    @Override
    public String toString() {
        return ICalUtils.fold(this.vCalendar.toString());
    }

    /**
     * @return the eTag
     */
    public String getETag() {
        return eTag;
    }

    public void setEtag(String eTag) {
        this.eTag = eTag;
    }

    public String getScheduleTag() {
        return scheduleTag;
    }

    public void setScheduleTag(String scheduleTag) {
        this.scheduleTag = scheduleTag;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

}
