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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.caldav.mixins.ScheduleOutboxURL;
import com.openexchange.caldav.resources.CommonCollection;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.loader.BulkLoader;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractWebdavFactory;

/**
 * The {@link GroupwareCaldavFactory} holds access to all external groupware services and acts as the factory for CaldavResources and
 * CaldavCollections
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GroupwareCaldavFactory extends AbstractWebdavFactory implements BulkLoader {

    private static final Log LOG = LogFactory.getLog(GroupwareCaldavFactory.class);
    private static final CaldavProtocol PROTOCOL = new CaldavProtocol();
    public static final WebdavPath ROOT_URL = new WebdavPath();

    private final SessionHolder sessionHolder;
    private final AppointmentSqlFactoryService appointments;
    private final FolderService folders;
    private final ICalEmitter icalEmitter;
    private final ICalParser icalParser;
    private final ThreadLocal<State> stateHolder = new ThreadLocal<State>();
    private final UserService users;
    private final CalendarCollectionService calendarUtils;
    private final ConfigViewFactory configs;
    
    public GroupwareCaldavFactory(SessionHolder sessionHolder, AppointmentSqlFactoryService appointments, FolderService folders, 
        ICalEmitter icalEmitter, ICalParser icalParser, UserService users, CalendarCollectionService calendarUtils, ConfigViewFactory configs) {
        this.sessionHolder = sessionHolder;
        this.appointments = appointments;
        this.folders = folders;
        this.icalEmitter = icalEmitter;
        this.icalParser = icalParser;
        this.users = users;
        this.calendarUtils = calendarUtils;
        this.configs = configs;
    }

    @Override
    public void beginRequest() {
        super.beginRequest();
        stateHolder.set(new State(this));
    }

    @Override
    public void endRequest(final int status) {
        stateHolder.set(null);
        super.endRequest(status);
    }

    @Override
	public CaldavProtocol getProtocol() {
        return PROTOCOL;
    }

    @Override
    public WebdavCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
        return getState().resolveCollection(url);
    }

    @Override
    public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
        return getState().resolveResource(url);
    }

    public AppointmentSQLInterface getAppointmentInterface() {
        return appointments.createAppointmentSql(sessionHolder.getSessionObject());
    }

    public TasksSQLInterface getTaskInterface() {
        return new TasksSQLImpl(getSession());
    }
    
    public FolderService getFolderService() {
        return folders;
    }

    public Session getSession() {
        return sessionHolder.getSessionObject();
    }

    public ICalEmitter getIcalEmitter() {
        return icalEmitter;
    }

    public ICalParser getIcalParser() {
        return icalParser;
    }

    public Context getContext() {
        return sessionHolder.getContext();
    }

    public User getUser() {
        return sessionHolder.getUser();
    }

    public String getLoginName() {
        return sessionHolder.getSessionObject().getLoginName();
    }

    public SessionHolder getSessionHolder() {
        return sessionHolder;
    }

    public CalendarCollectionService getCalendarUtilities() {
        return calendarUtils;
    }

    public String getConfigValue(String key, String defaultValue) throws WebdavProtocolException {
        try {
            ConfigView view = configs.getView(sessionHolder.getUser().getId(), sessionHolder.getContext().getContextId());
            ComposedConfigProperty<String> property = view.property(key, String.class);
            if (false == property.isDefined()) {
                return defaultValue;
            }
            return property.get();
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public State getState() {
        return stateHolder.get();
    }
    public Date end() throws WebdavProtocolException {
        final String value = getConfigValue("com.openexchange.caldav.interval.end", "one_year");
        int addYears = 2;
        if (value.equals("two_years")) {
            addYears = 3;
        }
        final Calendar instance = Calendar.getInstance();
        instance.add(Calendar.YEAR, addYears);
        instance.set(Calendar.DAY_OF_YEAR, 0);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);

        return instance.getTime();
    }

    public Date start() throws WebdavProtocolException {
        final String value = getConfigValue("com.openexchange.caldav.interval.start", "one_month");

        if (value.equals("one_year")) {
            final Calendar instance = Calendar.getInstance();
            instance.add(Calendar.YEAR, -1);
            instance.set(Calendar.DAY_OF_YEAR, 0);
            instance.set(Calendar.HOUR_OF_DAY, 0);
            instance.set(Calendar.MINUTE, 0);
            instance.set(Calendar.SECOND, 0);
            instance.set(Calendar.MILLISECOND, 0);

            return instance.getTime();
        }

        final Calendar instance = Calendar.getInstance();

        if (value.equals("six_months")) {
            instance.add(Calendar.MONTH, -6);
        } else {
            instance.add(Calendar.MONTH, -1);
        }
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTime();

    }
    
    /**
     * Gets the used folder tree identifier for folder operations.
     */
    public String getTreeID() {
        try {
			return this.getConfigValue("com.openexchange.caldav.tree", FolderStorage.REAL_TREE_ID);
		} catch (final WebdavProtocolException e) {
			LOG.warn("falling back to tree id '" + FolderStorage.REAL_TREE_ID +"'.", e);
			return FolderStorage.REAL_TREE_ID;
		}
    }

    public User resolveUser(int userID) throws WebdavProtocolException {
        try {
            return users.getUser(userID, getContext());
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Efficient loading based on loading hints

    @Override
	public void load(LoadingHints hint) {
        //TODO check if needed
    }

    @Override
	public void load(List<LoadingHints> hints) {
        for (LoadingHints loadingHints : hints) {
            load(loadingHints);
        }
    }

    public static final class State {
        
        private final Map<WebdavPath, CommonCollection> knownCollections;
        private final GroupwareCaldavFactory factory;

        public State(final GroupwareCaldavFactory factory) {
            super();
            this.factory = factory;
            this.knownCollections = new HashMap<WebdavPath, CommonCollection>();
        }

        public CommonCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
            url = sanitize(url);
            if (url.size() > 1) {
                throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_NOT_FOUND);
            }
            if (this.knownCollections.containsKey(url)) {
                return knownCollections.get(url);
            }
            
            CommonCollection collection = null;
            if (isRoot(url)) {
                collection = new com.openexchange.caldav.resources.CalDAVRootCollection(factory);
            } else if (ScheduleOutboxURL.SCHEDULE_OUTBOX.equals(url.name())) {
                collection = factory.mixin(new com.openexchange.caldav.resources.ScheduleOutboxCollection(factory));
            } else {
                CommonCollection rootCollection = this.resolveCollection(ROOT_URL);
                collection = (CommonCollection)rootCollection.getChild(url.name());                
            }
            
            if (null == collection) {
                throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_NOT_FOUND);
            } else {
                collection = factory.mixin(collection);
                this.knownCollections.put(url, collection);                
                return collection;
            }
        }
        
        public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
            url = sanitize(url);
            if (2 == url.size()) {
                return factory.mixin(this.resolveCollection(url.parent()).getChild(url.name()));
            } else {
                return this.resolveCollection(url);
            }
        }

        private static boolean isRoot(WebdavPath url) {
            return 0 == url.size();
        }

        private static WebdavPath sanitize(WebdavPath url) {
            return url.startsWith(new WebdavPath("caldav")) ? url.subpath(1) : url;
        }
        
    }
}
