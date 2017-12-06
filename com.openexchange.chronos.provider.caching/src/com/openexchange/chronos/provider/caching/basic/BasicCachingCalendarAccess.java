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

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.basic.DefaultCalendarSettings;
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

    protected abstract String getName();

    protected abstract long getRefreshInterval() throws OXException;

    protected abstract void handleExceptions(OXException e);

    protected abstract ExternalCalendarResult getAllEvents() throws OXException;

    protected abstract long getRetryAfterErrorInterval();

    @Override
    public CalendarSettings getSettings() {
        DefaultCalendarSettings settings = new DefaultCalendarSettings();
        settings.setName(getName());
        settings.setLastModified(account.getLastModified());
        settings.setConfig(account.getUserConfiguration());
        JSONObject internalConfig = account.getInternalConfiguration();
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, true));
        extendedProperties.add(DESCRIPTION(internalConfig.optString("description", null)));
        extendedProperties.add(USED_FOR_SYNC(Boolean.FALSE, true));
        extendedProperties.add(COLOR(internalConfig.optString("color", null), false));
        settings.setExtendedProperties(extendedProperties);
        return settings;
    }

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
                CalendarPermission.READ_FOLDER,
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
