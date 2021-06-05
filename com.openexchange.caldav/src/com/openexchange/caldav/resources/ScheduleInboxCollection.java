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

package com.openexchange.caldav.resources;

import static com.openexchange.dav.DAVProtocol.CALENDARSERVER_NS;
import static com.openexchange.dav.DAVProtocol.CAL_NS;
import static com.openexchange.dav.DAVProtocol.protocolException;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.mixins.ScheduleDefaultCalendarURL;
import com.openexchange.caldav.mixins.ScheduleDefaultTasksURL;
import com.openexchange.caldav.mixins.ScheduleInboxURL;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.query.Filter;
import com.openexchange.caldav.reports.FilteringResource;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.ical.ICalExceptionCodes;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.dav.mixins.SyncToken;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link ScheduleInboxCollection}
 *
 * A scheduling Inbox collection contains copies of incoming scheduling
 * messages. These can be requests sent by an Organizer, or replies sent by an
 * Attendee in response to a request. The scheduling Inbox collection is also
 * used to manage scheduling privileges.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ScheduleInboxCollection extends DAVCollection implements FilteringResource {

    /**
     * Initializes a new {@link ScheduleInboxCollection}.
     *
     * @param factory the factory
     */
    public ScheduleInboxCollection(GroupwareCaldavFactory factory) {
        super(factory, new WebdavPath(ScheduleInboxURL.SCHEDULE_INBOX));
        includeProperties(new SyncToken(this), new ScheduleDefaultCalendarURL(factory), new ScheduleDefaultTasksURL(factory), new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VAVAILABILITY));
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[] {
            new BasicPermission(getFactory().getUser().getId(), false, Permissions.createPermissionBits(
                Permission.READ_FOLDER, Permission.READ_ALL_OBJECTS, Permission.WRITE_ALL_OBJECTS, Permission.DELETE_ALL_OBJECTS, false))
        };
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType() + "<CAL:schedule-inbox />";
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {}

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return "Schedule Inbox";
    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void delete() throws WebdavProtocolException {}

    @Override
    public void setLanguage(String language) throws WebdavProtocolException {}

    @Override
    public void setLength(Long length) throws WebdavProtocolException {}

    @Override
    public void setContentType(String type) throws WebdavProtocolException {}

    @Override
    public String getSource() throws WebdavProtocolException {
        return null;
    }

    @Override
    public void setSource(String source) throws WebdavProtocolException {}

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return new Date(0);
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return new Date(0);
    }

    @Override
    public String getSyncToken() throws WebdavProtocolException {
        return "0";
    }

    @Override
    public AbstractResource getChild(String name) throws WebdavProtocolException {
        return new ScheduleInboxResource(getFactory(), constructPathForChildResource(name));
    }

    @Override
    public List<WebdavResource> filter(Filter filter) throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        if ("calendar-availability".equals(name) && (CALENDARSERVER_NS.getURI().equals(namespace) || CAL_NS.getURI().equals(namespace))) {
            try {
                return exportCalendarAvailability(namespace);
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
        }
        return null;
    }

    @Override
    protected void internalPutProperty(WebdavProperty property) throws WebdavProtocolException {
        if (null != property && "calendar-availability".equals(property.getName()) &&
            (CALENDARSERVER_NS.getURI().equals(property.getNamespace()) || CAL_NS.getURI().equals(property.getNamespace()))) {
            try {
                importCalendarAvailability(property);
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
        }
    }

    /**
     * Imports the calendar availability from the supplied WebDAV property.
     *
     * @param property The WebDAV property to import the calendar availability from, or <code>null</code> to delete the user's previously defined availability
     */
    private void importCalendarAvailability(WebdavProperty property) throws OXException {
        Availability calendarAvailability;
        if (null == property || Strings.isEmpty(property.getValue())) {
            calendarAvailability = null;
        } else {
            InputStream inputStream = null;
            try {
                inputStream = Streams.newByteArrayInputStream(property.getValue().getBytes(Charsets.UTF_8));
                calendarAvailability = getFactory().getService(ICalService.class).importICal(inputStream, null).getAvailability();
            } finally {
                Streams.close(inputStream);
            }
        }
        CalendarSession calendarSession = getFactory().getService(CalendarService.class).init(getFactory().getSession());
        CalendarAvailabilityService availabilityService = getFactory().getService(CalendarAvailabilityService.class);
        if (null == calendarAvailability || null == calendarAvailability.getAvailable() || calendarAvailability.getAvailable().isEmpty()) {
            availabilityService.deleteAvailability(calendarSession);
        } else {
            availabilityService.setAvailability(calendarSession, calendarAvailability);
        }
    }

    /**
     * Exports the calendar availability of the current session's user into a WebDAV property.
     *
     * @param namespace The target namespace for the WebDAV property
     * @return The exported calendar availability, or <code>null</code> if none is defined
     */
    private WebdavProperty exportCalendarAvailability(String namespace) throws OXException {
        CalendarSession calendarSession = getFactory().getService(CalendarService.class).init(getFactory().getSession());
        CalendarAvailabilityService availabilityService = getFactory().getService(CalendarAvailabilityService.class);
        Availability calendarAvailability = availabilityService.getAvailability(calendarSession);
        if (null == calendarAvailability || null == calendarAvailability.getAvailable() || calendarAvailability.getAvailable().isEmpty()) {
            WebdavProperty property = new WebdavProperty(namespace, "calendar-availability");
            property.setXML(true);
            property.setValue("");
            return property;
            //            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = getFactory().getService(ICalService.class).exportICal(null).add(calendarAvailability).getClosingStream();
            WebdavProperty property = new WebdavProperty(namespace, "calendar-availability");
            property.setXML(true);
            property.setValue("<![CDATA[" + Streams.stream2string(inputStream, Charsets.UTF_8_NAME) + "]]>");
            return property;
        } catch (IOException e) {
            LOG.warn("Error getting calendar availability for user '{}' in context '{}'",
                I(getFactory().getSession().getUserId()), I(getFactory().getSession().getContextId()), e);
            throw ICalExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(inputStream);
        }
    }
}
