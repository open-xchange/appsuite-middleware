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

package com.openexchange.caldav;

import javax.servlet.http.HttpServletResponse;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.caldav.resources.CalDAVRootCollection;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * The {@link GroupwareCaldavFactory} holds access to all external groupware services and acts as the factory for CaldavResources and
 * CaldavCollections
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GroupwareCaldavFactory extends DAVFactory {

    private final AppointmentSqlFactoryService appointments;
    private final FolderService folders;
    private final ICalEmitter icalEmitter;
    private final ICalParser icalParser;
    private final UserService users;
    private final CalendarCollectionService calendarUtils;

    /**
     * Initializes a new {@link GroupwareCaldavFactory}.
     *
     * @param protocol The underlying protocol
     * @param services A service lookup reference
     * @param sessionHolder The session holder
     */
    public GroupwareCaldavFactory(Protocol protocol, ServiceLookup services, SessionHolder sessionHolder) {
        super(protocol, services, sessionHolder);
        this.appointments = services.getService(AppointmentSqlFactoryService.class);
        this.folders = services.getService(FolderService.class);
        this.icalEmitter = services.getService(ICalEmitter.class);
        this.icalParser = services.getService(ICalParser.class);
        this.users = services.getService(UserService.class);
        this.calendarUtils = services.getService(CalendarCollectionService.class);
    }

    @Override
    protected String getURLPrefix() {
        return "/caldav/";
    }

    @Override
    public DAVCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
        WebdavPath path = sanitize(url);
        if (isRoot(path)) {
            return mixin(new CalDAVRootCollection(this));
        }
        if (1 == path.size()) {
            return mixin(new CalDAVRootCollection(this).getChild(path.name()));
        }
        throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
        WebdavPath path = sanitize(url);
        if (isRoot(path)) {
            return mixin(new CalDAVRootCollection(this));
        }
        if (1 == path.size()) {
            return mixin(new CalDAVRootCollection(this).getChild(path.name()));
        }
        if (2 == path.size()) {
            return mixin(new CalDAVRootCollection(this).getChild(path.parent().name()).getChild(path.name()));
        }
        throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Gets a configuration property value through the config-cascade.
     *
     * @param key The configuration property name
     * @param defaultValue The default value to use as fallback
     * @return The value, or the passed fallback if not defined
     */
    public String getConfigValue(String key, String defaultValue) throws OXException {
        ConfigView view = requireService(ConfigViewFactory.class).getView(getUser().getId(), getContext().getContextId());
        ComposedConfigProperty<String> property = view.property(key, String.class);
        if (null == property || false == property.isDefined()) {
            return defaultValue;
        }
        return property.get();
    }


    public AppointmentSQLInterface getAppointmentInterface() {
        return appointments.createAppointmentSql(getSessionObject());
    }

    public TasksSQLInterface getTaskInterface() {
        return new TasksSQLImpl(getSession());
    }

    public FolderService getFolderService() {
        return folders;
    }

    public ICalEmitter getIcalEmitter() {
        return icalEmitter;
    }

    public ICalParser getIcalParser() {
        return icalParser;
    }

    public CalendarCollectionService getCalendarUtilities() {
        return calendarUtils;
    }

    public User resolveUser(int userID) throws OXException {
        return users.getUser(userID, getContext());
    }

}
