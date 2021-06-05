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

package com.openexchange.chronos.impl.session;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarConfigImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarConfigImpl implements CalendarConfig {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarConfigImpl.class);

    private final CalendarSession optSession;
    private final ServiceLookup services;
    private final int contextId;

    /**
     * Initializes a new {@link CalendarConfigImpl}.
     *
     * @param session The underlying calendar session
     * @param services A service lookup reference
     */
    public CalendarConfigImpl(CalendarSession session, ServiceLookup services) {
        this(session, session.getContextId(), services);
    }

    /**
     * Initializes a new {@link CalendarConfigImpl}.
     *
     * @param contextId The context identifier
     * @param services A service lookup reference
     */
    public CalendarConfigImpl(int contextId, ServiceLookup services) {
        this(null, contextId, services);
    }

    private CalendarConfigImpl(CalendarSession session, int contextId, ServiceLookup services) {
        super();
        this.contextId = contextId;
        this.services = services;
        this.optSession = session;
    }

    @Override
    public String getDefaultFolderId(int userId) throws OXException {
        return getUserSettings(userId).getDefaultFolderId();
    }

    @Override
    public ParticipationStatus getInitialPartStat(int userId, boolean inPublicFolder) {
        return getUserSettings(userId).getInitialPartStat(inPublicFolder);
    }

    @Override
    public List<Alarm> getDefaultAlarmDate(int userId) throws OXException {
        return getUserSettings(userId).getDefaultAlarmDate();
    }

    @Override
    public List<Alarm> getDefaultAlarmDateTime(int userId) throws OXException {
        return getUserSettings(userId).getDefaultAlarmDateTime();
    }

    @Override
    public Available[] getAvailability(int userId) throws OXException {
        return getUserSettings(userId).getAvailability();
    }

    @Override
    public boolean isNotifyOnCreate(int userId) {
        return getUserSettings(userId).isNotifyOnCreate();
    }

    @Override
    public boolean isNotifyOnUpdate(int userId) {
        return getUserSettings(userId).isNotifyOnUpdate();
    }

    @Override
    public boolean isNotifyOnDelete(int userId) {
        return getUserSettings(userId).isNotifyOnDelete();
    }

    @Override
    public boolean isNotifyOnReply(int userId) {
        return getUserSettings(userId).isNotifyOnReply();
    }

    @Override
    public boolean isNotifyOnReplyAsAttendee(int userId) {
        return getUserSettings(userId).isNotifyOnReplyAsAttendee();
    }

    @Override
    public int getMsgFormat(int userId) {
        return getUserSettings(userId).getMsgFormat();
    }

    @Override
    public boolean isResolveGroupAttendees() {
        return getConfigValue("com.openexchange.calendar.resolveGroupAttendees", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean isNotifyResourceAttendees() {
        return getConfigValue("com.openexchange.calendar.notifyResourceAttendees", Boolean.class, Boolean.TRUE).booleanValue();
    }

    @Override
    public int getMinimumSearchPatternLength() throws OXException {
        return ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
    }

    @Override
    public int getMaxConflictsPerRecurrence() {
        return getConfigValue("com.openexchange.calendar.maxConflictsPerRecurrence", Integer.class, I(5)).intValue();
    }

    @Override
    public int getMaxAttendeesPerConflict() {
        return getConfigValue("com.openexchange.calendar.maxAttendeesPerConflict", Integer.class, I(5)).intValue();
    }

    @Override
    public int getMaxConflicts() {
        return getConfigValue("com.openexchange.calendar.maxConflicts", Integer.class, I(100)).intValue();
    }

    @Override
    public int getMaxOccurrencesForConflicts() {
        return getConfigValue("com.openexchange.calendar.maxOccurrencesForConflicts", Integer.class, I(1000)).intValue();
    }

    @Override
    public int getMaxSeriesUntilForConflicts() {
        return getConfigValue("com.openexchange.calendar.maxSeriesUntilForConflicts", Integer.class, I(10)).intValue();
    }

    @Override
    public boolean isSkipExternalAttendeeURIChecks() {
        return getConfigValue("com.openexchange.calendar.skipExternalAttendeeURIChecks", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean isRestrictAllowedAttendeeChanges() {
        return getConfigValue("com.openexchange.calendar.restrictAllowedAttendeeChanges", Boolean.class, Boolean.TRUE).booleanValue();
    }

    @Override
    public boolean isOrganizerChangeAllowed() {
        return getConfigValue("com.openexchange.calendar.allowChangeOfOrganizer", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean isAllowOrganizerPartStatChanges() {
        return b(getConfigValue("com.openexchange.calendar.allowOrganizerPartStatChanges", Boolean.class, Boolean.FALSE));
    }

    @Override
    public boolean isLookupPeerAttendeesForSameMailDomainOnly() {
        return b(getConfigValue("com.openexchange.calendar.lookupPeerAttendeesForSameMailDomainOnly", Boolean.class, Boolean.TRUE));
    }

    private CalendarUserSettings getUserSettings(int userId) {
        if (null != optSession) {
            return new CalendarUserSettings(optSession, userId, services);
        }
        return new CalendarUserSettings(contextId, userId, services);
    }

    private <T> T getConfigValue(String property, Class<T> coerceTo, T defaultValue) {
        int userId = null == optSession ? -1 : optSession.getUserId();
        try {
            ConfigView configView = Services.getService(ConfigViewFactory.class, true).getView(userId, contextId);
            return configView.opt(property, coerceTo, defaultValue);
        } catch (OXException e) {
            LOG.warn("Error getting \"{}\", falling back to \"{}\"", property, defaultValue, e);
            return defaultValue;
        }
    }

}
