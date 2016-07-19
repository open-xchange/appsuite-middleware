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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.caldav.mixins.ScheduleInboxURL;
import com.openexchange.caldav.mixins.ScheduleOutboxURL;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.freebusy.service.FreeBusyService;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.loader.BulkLoader;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
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
public class GroupwareCaldavFactory extends DAVFactory implements BulkLoader {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GroupwareCaldavFactory.class);
    private static final CaldavProtocol PROTOCOL = new CaldavProtocol();
    public static final WebdavPath ROOT_URL = new WebdavPath();

    private final AppointmentSqlFactoryService appointments;
    private final FolderService folders;
    private final ICalEmitter icalEmitter;
    private final ICalParser icalParser;
    private final ThreadLocal<State> stateHolder = new ThreadLocal<State>();
    private final UserService users;
    private final CalendarCollectionService calendarUtils;
    private final ConfigViewFactory configs;
    private final FreeBusyService freeBusyService;

    public GroupwareCaldavFactory(Protocol protocol, ServiceLookup services, SessionHolder sessionHolder) {
        super(protocol, services, sessionHolder);
        this.appointments = services.getService(AppointmentSqlFactoryService.class);
        this.folders = services.getService(FolderService.class);
        this.icalEmitter = services.getService(ICalEmitter.class);
        this.icalParser = services.getService(ICalParser.class);
        this.users = services.getService(UserService.class);
        this.calendarUtils = services.getService(CalendarCollectionService.class);
        this.configs = services.getService(ConfigViewFactory.class);
        this.freeBusyService = services.getService(FreeBusyService.class);
    }

    @Override
    protected String getURLPrefix() {
        return "/caldav/";
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
        return appointments.createAppointmentSql(getSessionObject());
    }

    public FreeBusyService getFreeBusyService() {
        return freeBusyService;
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

    public String getConfigValue(String key, String defaultValue) throws OXException {
        ConfigView view = configs.getView(getUser().getId(), getContext().getContextId());
        ComposedConfigProperty<String> property = view.property(key, String.class);
        if (null == property || false == property.isDefined()) {
            return defaultValue;
        }
        return property.get();
    }

    public State getState() {
        return stateHolder.get();
    }

    public User resolveUser(int userID) throws OXException {
        return users.getUser(userID, getContext());
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

    /**
     * {@link State} - Thread-local CalDAV state
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class State {

        private final Map<WebdavPath, DAVCollection> knownCollections;
        private final GroupwareCaldavFactory factory;

        private Date minDateTime = null;
        private Date maxDateTime = null;
        private String treeID;
        private UserizedFolder defaultFolder = null;

        public State(final GroupwareCaldavFactory factory) {
            super();
            this.factory = factory;
            this.knownCollections = new HashMap<WebdavPath, DAVCollection>();
        }

        /**
         * Gets the user's default calendar folder.
         *
         * @return The default folder
         */
        public UserizedFolder getDefaultFolder() throws OXException {
            if (null == defaultFolder) {
                defaultFolder = factory.getFolderService().getDefaultFolder(
                    factory.getUser(), getTreeID(), CalendarContentType.getInstance(), factory.getSession(), null);
            }
            return defaultFolder;
        }

        /**
         * Gets the used folder tree identifier.
         *
         * @return the folder tree ID
         */
        public String getTreeID() {
            if (null == this.treeID) {
                try {
                    treeID = factory.getConfigValue("com.openexchange.caldav.tree", FolderStorage.REAL_TREE_ID);
                } catch (OXException e) {
                    LOG.warn("falling back to tree id ''{}''.", FolderStorage.REAL_TREE_ID, e);
                    treeID = FolderStorage.REAL_TREE_ID;
                }
            }
            return treeID;
        }

        /**
         * Gets the configured minimum date-time for the CalDAV interface
         *
         * @return the minimum date
         */
        public Date getMinDateTime() {
            if (null == this.minDateTime) {
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
                this.minDateTime = calendar.getTime();
            }
            return minDateTime;
        }

        /**
         * Gets the configured maximum date-time for the CalDAV interface
         *
         * @return the maximum date
         */
        public Date getMaxDateTime() {
            if (null == this.maxDateTime) {
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
                this.maxDateTime = calendar.getTime();
            }
            return maxDateTime;
        }

        public DAVCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
            url = sanitize(url);
            if (url.size() > 1) {
                throw WebdavProtocolException.generalError(url, HttpServletResponse.SC_NOT_FOUND);
            }
            if (this.knownCollections.containsKey(url)) {
                return knownCollections.get(url);
            }

            DAVCollection collection = null;
            if (isRoot(url)) {
                collection = new com.openexchange.caldav.resources.CalDAVRootCollection(factory);
            } else if (ScheduleOutboxURL.SCHEDULE_OUTBOX.equals(url.name())) {
                collection = factory.mixin(new com.openexchange.caldav.resources.ScheduleOutboxCollection(factory));
            } else if (ScheduleInboxURL.SCHEDULE_INBOX.equals(url.name())) {
                collection = factory.mixin(new com.openexchange.caldav.resources.ScheduleInboxCollection(factory));
            } else {
                DAVCollection rootCollection = this.resolveCollection(ROOT_URL);
                collection = (DAVCollection)rootCollection.getChild(url.name());
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
