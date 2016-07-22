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

package com.openexchange.caldav.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Tools;
import com.openexchange.caldav.mixins.AllowedSharingModes;
import com.openexchange.caldav.mixins.CalendarOrder;
import com.openexchange.caldav.mixins.CalendarOwner;
import com.openexchange.caldav.mixins.CalendarTimezone;
import com.openexchange.caldav.mixins.Invite;
import com.openexchange.caldav.mixins.ManagedAttachmentsServerURL;
import com.openexchange.caldav.mixins.MaxDateTime;
import com.openexchange.caldav.mixins.MinDateTime;
import com.openexchange.caldav.mixins.Organizer;
import com.openexchange.caldav.mixins.ScheduleDefaultCalendarURL;
import com.openexchange.caldav.mixins.ScheduleDefaultTasksURL;
import com.openexchange.caldav.mixins.SupportedReportSet;
import com.openexchange.caldav.query.Filter;
import com.openexchange.caldav.query.FilterAnalyzer;
import com.openexchange.caldav.reports.FilteringResource;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.mixins.CalendarColor;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link CalDAVFolderCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <T>
 */
public abstract class CalDAVFolderCollection<T extends CalendarObject> extends CommonFolderCollection<T> implements FilteringResource {

    protected static final int NO_ORDER = -1;
    protected static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^\\d+$");

    protected final GroupwareCaldavFactory factory;
    protected final int folderID;

    private Date minDateTime;
    private Date maxDateTime;
    private Date lastModified;

    /**
     * Initializes a new {@link CalDAVFolderCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     */
    public CalDAVFolderCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, NO_ORDER);
    }

    /**
     * Initializes a new {@link CalDAVFolderCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     * @param order The calendar order to use, or {@value #NO_ORDER} for no specific order
     */
    public CalDAVFolderCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder);
        this.factory = factory;
        this.folderID = null != folder ? Tools.parse(folder.getID()) : 0;
        includeProperties(new SupportedReportSet(), new MinDateTime(this), new MaxDateTime(this), new Invite(factory, this),
            new AllowedSharingModes(factory.getSession()), new CalendarOwner(this), new Organizer(this),
            new ScheduleDefaultCalendarURL(factory), new ScheduleDefaultTasksURL(factory), new CalendarColor(this),
            new ManagedAttachmentsServerURL(), new CalendarTimezone(factory, this));
        if (NO_ORDER != order) {
            includeProperties(new CalendarOrder(order));
        }
    }

    protected abstract boolean isSupported(T object) throws OXException;

    protected abstract List<T> getObjectsInRange(Date from, Date until) throws OXException;

    @Override
    protected String getFileExtension() {
        return CalDAVResource.EXTENSION_ICS;
    }

    /**
     * Gets the start time of the configured synchronization timeframe for CalDAV.
     *
     * @return The start of the configured synchronization interval
     */
    public Date getIntervalStart() {
        if (null == minDateTime) {
            String value = null;
            try {
                value = factory.getConfigValue("com.openexchange.caldav.interval.start", "one_month");
            } catch (OXException e) {
                LOG.warn("falling back to 'one_month' as interval start", e);
                value = "one_month";
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            if ("one_year".equals(value)) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
            } else if ("six_months".equals(value)) {
                calendar.add(Calendar.MONTH, -6);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            } else {
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            }
            minDateTime = calendar.getTime();
        }
        return minDateTime;
    }

    /**
     * Gets the end time of the configured synchronization timeframe for CalDAV.
     *
     * @return The end of the configured synchronization interval
     */
    public Date getIntervalEnd() {
        if (null == maxDateTime) {
            String value = null;
            try {
                value = factory.getConfigValue("com.openexchange.caldav.interval.end", "one_year");
            } catch (OXException e) {
                LOG.warn("falling back to 'one_year' as interval end", e);
                value = "one_year";
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, "two_years".equals(value) ? 3 : 2);
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            maxDateTime = calendar.getTime();
        }
        return maxDateTime;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        if (null == this.lastModified) {
            lastModified = Tools.getLatestModified(getIntervalStart(), folder);
            try {
                /*
                 * new and modified objects
                 */
                Collection<T> modifiedObjects = getModifiedObjects(lastModified);
                for (T object : modifiedObjects) {
                    lastModified = Tools.getLatestModified(lastModified, object);
                }
                /*
                 * deleted objects
                 */
                Collection<T> deletedObjects = getDeletedObjects(lastModified);
                for (T object : deletedObjects) {
                    lastModified = Tools.getLatestModified(lastModified, object);
                }
            } catch (OXException e) {
                throw protocolException(e);
            }
        }
        return lastModified;
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
        if (CalendarColor.NAMESPACE.getURI().equals(prop.getNamespace()) && CalendarColor.NAME.equals(prop.getName())) {
            if (false == PrivateType.getInstance().equals(folder.getType())) {
                throw protocolException(HttpServletResponse.SC_FORBIDDEN);
            }
            /*
             * apply color to meta field
             */
            AbstractFolder folderToUpdate = getFolderToUpdate();
            Map<String, Object> meta = folderToUpdate.getMeta();
            String value = CalendarColor.parse(prop);
            if (Strings.isEmpty(value)) {
                meta.remove("color");
            } else {
                meta.put("color", value);
            }
            /*
             * if possible, also try and match a specific color-label
             */
            Map<String, String> attributes = prop.getAttributes();
            if (null != attributes && attributes.containsKey(CalendarColor.SYMBOLIC_COLOR)) {
                Integer uiValue = CalendarColor.mapColorLabel(attributes.get(CalendarColor.SYMBOLIC_COLOR));
                if (uiValue != null) {
                    meta.put("color_label", uiValue);
                }
            }
        }
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        if (null != folder && folder.isDefault()) {
            throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "default-calendar-needed", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        super.internalDelete();
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        return Streams.EMPTY_INPUT_STREAM;
    }

    @Override
    public void setLength(Long l) throws WebdavProtocolException {

    }

    protected static <T extends CalendarObject> boolean isInInterval(T object, Date intervalStart, Date intervalEnd) {
        return null != object && (null == object.getEndDate() || object.getEndDate().after(intervalStart)) && (null == object.getStartDate() || object.getStartDate().before(intervalEnd));
    }

    protected List<T> filter(SearchIterator<T> searchIterator) throws OXException {
        List<T> list = new ArrayList<T>();
        if (null != searchIterator) {
            try {
                while (searchIterator.hasNext()) {
                    T t = searchIterator.next();
                    if (isSupported(t)) {
                        list.add(t);
                    }
                }
            } finally {
                searchIterator.close();
            }
        }
        return list;
    }

    @Override
    protected T getObject(String resourceName) throws OXException {
        Collection<T> objects = this.getObjects();
        if (null != objects && 0 < objects.size()) {
            /*
             * try filename and uid properties
             */
            for (T t : objects) {
                if (resourceName.equals(t.getFilename()) || resourceName.equals(t.getUid())) {
                    return t;
                }
            }
            /*
             * try object id as fallback to support previous implementation
             */
            Matcher matcher = OBJECT_ID_PATTERN.matcher(resourceName);
            if (matcher.find()) {
                try {
                    int objectID = Integer.parseInt(matcher.group(0));
                    for (T t : objects) {
                        if (objectID == t.getObjectID()) {
                            return t;
                        }
                    }
                } catch (NumberFormatException e) {
                    // not an ID
                }
            }
        }
        return null;
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        StringBuilder stringBuilder = new StringBuilder(super.getResourceType());
        stringBuilder.append('<').append(CaldavProtocol.CAL_NS.getPrefix()).append(":calendar/>");
        if (null != this.folder) {
            if (SharedType.getInstance().equals(folder.getType())) {
                // used to indicate that the calendar is owned by another user and is being shared to the current user.
                stringBuilder.append('<').append(CaldavProtocol.CALENDARSERVER_NS.getPrefix()).append(":shared/>");
            } else if (PrivateType.getInstance().equals(folder.getType())) {
                // used to indicate that the calendar is owned by the current user and is being shared by them.
                stringBuilder.append('<').append(CaldavProtocol.CALENDARSERVER_NS.getPrefix()).append(":shared-owner/>");
            } else if (PublicType.getInstance().equals(folder.getType())) {
                // evaluate own permission if folder shares can be edited or not
                stringBuilder.append('<').append(CaldavProtocol.CALENDARSERVER_NS.getPrefix()).append(folder.getOwnPermission().isAdmin() ? ":shared-owner/>" : ":shared/>");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public List<WebdavResource> filter(Filter filter) throws WebdavProtocolException {
        List<Object> arguments = new ArrayList<Object>(2);
        if (FilterAnalyzer.VEVENT_RANGE_QUERY_ANALYZER.match(filter, arguments) || FilterAnalyzer.VTODO_RANGE_QUERY_ANALYZER.match(filter, arguments)) {
            Date from = arguments.isEmpty() ? null : toDate(arguments.get(0));
            if (null == from || from.before(getIntervalStart())) {
                from = getIntervalStart();
            }
            Date until = arguments.isEmpty() ? null : toDate(arguments.get(1));
            if (null == until || until.after(getIntervalEnd())) {
                until = getIntervalEnd();
            }
            try {
                List<T> objects = this.getObjectsInRange(from, until);
                if (null == objects || 0 == objects.size()) {
                    return Collections.emptyList();
                } else {
                    List<WebdavResource> resources = new ArrayList<WebdavResource>(objects.size());
                    for (T object : this.getObjectsInRange(from, until)) {
                        resources.add(createResource(object, constructPathForChildResource(object)));
                    }
                    return resources;
                }
            } catch (OXException e) {
                throw protocolException(e);
            }
        } else {
            throw protocolException(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    private static Date toDate(Object object) {
        long tstamp = (Long) object;
        if (tstamp == -1) {
            return null;
        }
        return new Date(tstamp);
    }

}
