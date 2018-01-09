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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider.caching.basic;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.SingleFolderCachingCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link BasicCachingCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class BasicCachingCalendarAccess implements BasicCalendarAccess {

    protected final Session session;
    protected CalendarAccount account;
    protected final CalendarParameters parameters;

    private final SingleFolderCachingCalendarAccess cachingBridge;

    /**
     * Initializes a new {@link BirthdaysCalendarAccess}.
     *
     * @param session The session
     * @param account The underlying calendar account
     * @param parameters Additional calendar parameters
     */
    protected BasicCachingCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super();
        this.account = account;
        this.session = session;
        this.parameters = parameters;
        cachingBridge = new CachingAccessBridge(this);
    }

    /**
     * Defines the refresh interval in minutes that has to be expired to contact the external event provider for the up-to-date calendar.<br>
     * <br>
     * If the value is <=0 the default of one day will be used.
     *
     * @return The interval that defines the expire of the caching in {@link TimeUnit#MINUTES}
     */
    protected abstract long getRefreshInterval() throws OXException;

    /**
     * Allows the underlying calendar provider to handle {@link OXException}s that might occur while retrieving data from the external source.
     *
     * @param e The {@link OXException} occurred
     */
    protected abstract void handleExceptions(OXException e);

    /**
     * Returns an {@link ExternalCalendarResult} containing all external {@link Event}s by querying the underlying calendar.<b>
     * <b>
     * Make sure not to consider client parameters (available via {@link CachingCalendarAccess#getParameters()}) while requesting events!
     *
     * @return {@link ExternalCalendarResult}
     */
    protected abstract ExternalCalendarResult getAllEvents() throws OXException;

    /**
     * Defines how long should be wait for the next request to the external calendar provider in case an error occurred.
     *
     * @return The time in {@link TimeUnit#MINUTES} that should be wait for contacting the external calendar provider for updates.
     */
    protected abstract long getRetryAfterErrorInterval();

    @Override
    public Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException {
        return cachingBridge.getEvent(FOLDER_ID, eventId, recurrenceId);
    }

    @Override
    public List<Event> getEvents() throws OXException {
        return cachingBridge.getEventsInFolder(FOLDER_ID);
    }

    @Override
    public List<Event> getChangeExceptions(String seriesId) throws OXException {
        return cachingBridge.getChangeExceptions(FOLDER_ID, seriesId);
    }

    @Override
    public void close() {
        // nothing to do
    }

    private static final class CachingAccessBridge extends SingleFolderCachingCalendarAccess {

        private final BasicCachingCalendarAccess basicAccess;

        protected CachingAccessBridge(BasicCachingCalendarAccess basicAccess) throws OXException {
            super(basicAccess.session, basicAccess.account, basicAccess.parameters, prepareFolder(basicAccess.session.getUserId(), basicAccess.getSettings()));
            this.basicAccess = basicAccess;
        }

        private static CalendarFolder prepareFolder(int userId, CalendarSettings settings) {
            DefaultCalendarFolder folder = new DefaultCalendarFolder(FOLDER_ID, settings.getName());
            folder.setLastModified(settings.getLastModified());
            folder.setExtendedProperties(settings.getExtendedProperties());
            folder.setPermissions(Collections.singletonList(new DefaultCalendarPermission(
                userId,
                CalendarPermission.MAX_PERMISSION,
                CalendarPermission.READ_ALL_OBJECTS,
                CalendarPermission.WRITE_ALL_OBJECTS,
                CalendarPermission.NO_PERMISSIONS,
                false,
                false,
                CalendarPermission.NO_PERMISSIONS)))
            ;
            return folder;
        }

        @Override
        public void close() {
            basicAccess.close();
        }

        @Override
        protected long getRefreshInterval() throws OXException {
            return basicAccess.getRefreshInterval();
        }

        @Override
        protected String updateFolder(CalendarFolder folder, long clientTimestamp) throws OXException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handleExceptions(OXException e) {
            basicAccess.handleExceptions(e);
        }

        @Override
        public ExternalCalendarResult getAllEvents() throws OXException {
            return basicAccess.getAllEvents();
        }

        @Override
        public long getRetryAfterErrorInterval() {
            return basicAccess.getRetryAfterErrorInterval();
        }

    }

}
