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

import static com.openexchange.caldav.mixins.CalendarOrder.NO_ORDER;
import static com.openexchange.dav.DAVProtocol.protocolException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Namespace;
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
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.mixins.CalendarColor;
import com.openexchange.dav.mixins.CalendarDescription;
import com.openexchange.dav.mixins.CurrentUserPrivilegeSet;
import com.openexchange.dav.mixins.ScheduleCalendarTransp;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalendarFolderConverter;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.login.Interface;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link CalDAVFolderCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @param <T>
 * @since v7.10.2
 */
public abstract class CalDAVFolderCollection<T> extends FolderCollection<T> implements FilteringResource {

    protected final GroupwareCaldavFactory factory;
    protected final MinDateTime minDateTime;
    protected final MaxDateTime maxDateTime;

    /**
     * Initializes a new {@link CalDAVFolderCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     * @param order The calendar order to use, or {@value #NO_ORDER} for no specific order
     */
    protected CalDAVFolderCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder);
        this.factory = factory;
        this.minDateTime = new MinDateTime(factory);
        this.maxDateTime = new MaxDateTime(factory);
        includeProperties(
            minDateTime, 
            maxDateTime, 
            new SupportedReportSet(), 
            new ManagedAttachmentsServerURL(), 
            new ScheduleDefaultCalendarURL(factory), 
            new ScheduleDefaultTasksURL(factory)
        );
        if (null != folder) {
            includeProperties(
                new CurrentUserPrivilegeSet(folder.getOwnPermission()), 
                new Invite(factory, this), 
                new AllowedSharingModes(this), 
                new CalendarOwner(this),
                new Organizer(this), 
                new CalendarColor(this), 
                new CalendarTimezone(factory, this)
            );
            if (NO_ORDER != order) {
                includeProperties(new CalendarOrder(order));
            }
        }
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return new StringBuilder(super.getResourceType()).append('<').append(CaldavProtocol.CAL_NS.getPrefix()).append(":calendar/>").toString();
    }

    @Override
    public GroupwareCaldavFactory getFactory() {
        return factory;
    }

    @Override
    public String getPushTopic() {
        return null != folder ? "ox:" + Interface.CALDAV.toString().toLowerCase() + ":" + folder.getID() : null;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return null != folder ? folder.getLastModifiedUTC() : null;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        return Streams.EMPTY_INPUT_STREAM;
    }

    @Override
    public void setLength(Long l) throws WebdavProtocolException {
        // ignore
    }

    @Override
    public List<WebdavResource> filter(Filter filter) throws WebdavProtocolException {
        List<Object> arguments = new ArrayList<Object>(2);
        if (false == FilterAnalyzer.VEVENT_RANGE_QUERY_ANALYZER.match(filter, arguments) && false == FilterAnalyzer.VTODO_RANGE_QUERY_ANALYZER.match(filter, arguments)) {
            throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "supported-filter", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        Date from = 0 < arguments.size() ? toDate(arguments.get(0)) : null;
        Date minDateTime = this.minDateTime.getMinDateTime();
        Date rangeStart = null == from ? minDateTime : null == minDateTime ? from : from.before(minDateTime) ? minDateTime : from;
        Date until = 1 < arguments.size() ? toDate(arguments.get(1)) : null;
        Date maxDateTime = this.maxDateTime.getMaxDateTime();
        Date rangeEnd = null == until ? maxDateTime : null == maxDateTime ? until : until.after(maxDateTime) ? maxDateTime : until;
        try {
            Collection<T> objects = getObjects(rangeStart, rangeEnd);
            List<WebdavResource> resources = new ArrayList<WebdavResource>(objects.size());
            for (T object : objects) {
                resources.add(createResource(object, constructPathForChildResource(object)));
            }
            return resources;
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    protected abstract Collection<T> getObjects(Date rangeStart, Date rangeEnd) throws OXException;

    @Override
    protected String getFileExtension() {
        return Tools.EXTENSION_ICS;
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        if (null != folder && folder.isDefault()) {
            throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "default-calendar-needed", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        super.internalDelete();
    }

    @Override
    protected void internalPutProperty(WebdavProperty property) throws WebdavProtocolException {
        if (DAVProperty.class.isInstance(property)) {
            putProperty((DAVProperty) property);
            return;
        }
        throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "cannot-modify-protected-property", getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    protected void putProperty(DAVProperty property) throws WebdavProtocolException {
        ParameterizedFolder folderToUpdate = getFolderToUpdate();
        if (matches(property, CalendarColor.NAMESPACE, CalendarColor.NAME)) {
            String value = CalendarColor.parse(property);
            /*
             * apply color folder property
             */
            CalendarFolderConverter.setExtendedProperty(folderToUpdate, CalendarFolderProperty.COLOR(value));
            /*
             * also apply color in meta field for private folders
             */
            if (null != folder && PrivateType.getInstance().equals(folder.getType())) {
                Map<String, Object> meta = folderToUpdate.getMeta();
                if (Strings.isEmpty(value)) {
                    meta.remove("color");
                } else {
                    meta.put("color", value);
                }
                /*
                 * if possible, also try and match a specific color-label
                 */
                Map<String, String> attributes = property.getAttributes();
                if (null != attributes && attributes.containsKey(CalendarColor.SYMBOLIC_COLOR)) {
                    Integer uiValue = CalendarColor.mapColorLabel(attributes.get(CalendarColor.SYMBOLIC_COLOR));
                    if (uiValue != null) {
                        meta.put("color_label", uiValue);
                    }
                }
            }
        } else if (matches(property, CalendarDescription.NAMESPACE, CalendarDescription.NAME)) {
            /*
             * apply description folder property
             */
            String value = null != property.getElement() ? property.getElement().getText() : null;
            CalendarFolderConverter.setExtendedProperty(folderToUpdate, CalendarFolderProperty.DESCRIPTION(value));
        } else if (ScheduleCalendarTransp.NAMESPACE.getURI().equals(property.getNamespace()) && ScheduleCalendarTransp.NAME.equals(property.getName())) {
            /*
             * apply schedule transparency folder property
             */
            // ignore for now
            //            Transp value = TimeTransparency.OPAQUE;
            //            if (null != property.getElement() && null != property.getElement().getChild(Transp.TRANSPARENT.toLowerCase(), DAVProtocol.CAL_NS)) {
            //                value = TimeTransparency.TRANSPARENT;
            //            }
            //            CalendarFolderConverter.setExtendedProperty(folderToUpdate, CalendarFolderProperty.SCHEDULE_TRANSP(value));
        } else if (matches(property, CalendarTimezone.NAMESPACE, CalendarTimezone.NAME)) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "cannot-modify-protected-property", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    protected static boolean matches(WebdavProperty property, Namespace namespace, String name) {
        return null != property && namespace.getURI().equals(property.getNamespace()) && name.equals(property.getName());
    }

    private static Date toDate(Object object) {
        long tstamp = (Long) object;
        if (tstamp == -1) {
            return null;
        }
        return new Date(tstamp);
    }

}
