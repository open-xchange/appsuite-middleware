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

package com.openexchange.chronos.storage.rdb.resilient;

import static com.openexchange.chronos.common.CalendarUtils.ID_COMPARATOR;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.chronos.storage.AttachmentStorage;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageUtilities;
import com.openexchange.chronos.storage.ConferenceStorage;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbCalendarStorage implements CalendarStorage {

    private final CalendarStorage delegate;
    private final RdbEventStorage eventStorage;
    private final RdbAttendeeStorage attendeeStorage;
    private final RdbAlarmStorage alarmStorage;
    private final RdbConferenceStorage conferenceStorage;

    /**
     * Initializes a new {@link RdbCalendarStorage}.
     *
     * @param services A service lookup reference
     * @param delegate The delegate storage
     * @param handleTruncations <code>true</code> to automatically handle data truncation warnings, <code>false</code>, otherwise
     * @param handleIncorrectStrings <code>true</code> to automatically handle incorrect string warnings, <code>false</code>, otherwise
     * @param unsupportedDataThreshold The threshold defining up to which severity unsupported data errors can be ignored, or
     *            <code>null</code> to not ignore any unsupported data error at all
     */
    public RdbCalendarStorage(ServiceLookup services, CalendarStorage delegate, boolean handleTruncations, boolean handleIncorrectStrings, ProblemSeverity unsupportedDataThreshold) {
        super();
        this.delegate = delegate;
        this.eventStorage = new RdbEventStorage(services, delegate.getEventStorage(), handleTruncations, handleIncorrectStrings, unsupportedDataThreshold);
        this.attendeeStorage = new RdbAttendeeStorage(services, delegate.getAttendeeStorage(), handleTruncations, handleIncorrectStrings, unsupportedDataThreshold);
        this.alarmStorage = new RdbAlarmStorage(services, delegate.getAlarmStorage(), handleTruncations, handleIncorrectStrings, unsupportedDataThreshold);
        this.conferenceStorage = new RdbConferenceStorage(services, delegate.getConferenceStorage(), handleTruncations, handleIncorrectStrings, unsupportedDataThreshold);
    }

    @Override
    public EventStorage getEventStorage() {
        return eventStorage;
    }

    @Override
    public AlarmStorage getAlarmStorage() {
        return alarmStorage;
    }

    @Override
    public AttachmentStorage getAttachmentStorage() {
        return delegate.getAttachmentStorage();
    }

    @Override
    public AttendeeStorage getAttendeeStorage() {
        return attendeeStorage;
    }

    @Override
    public AlarmTriggerStorage getAlarmTriggerStorage() {
        return delegate.getAlarmTriggerStorage();
    }

    @Override
    public ConferenceStorage getConferenceStorage() {
        return conferenceStorage;
    }

    @Override
    public CalendarAccountStorage getAccountStorage() {
        return delegate.getAccountStorage();
    }

    @Override
    public CalendarStorageUtilities getUtilities() {
        return delegate.getUtilities();
    }

    @Override
    public Map<String, List<OXException>> getWarnings() {
        Map<String, List<OXException>> warnings = new TreeMap<String, List<OXException>>(ID_COMPARATOR);
        warnings.putAll(eventStorage.getWarnings());
        warnings.putAll(attendeeStorage.getWarnings());
        warnings.putAll(alarmStorage.getWarnings());
        warnings.putAll(conferenceStorage.getWarnings());
        warnings.putAll(delegate.getWarnings());
        return warnings;
    }

    @Override
    public Map<String, List<OXException>> getAndFlushWarnings() {
        Map<String, List<OXException>> warnings = new TreeMap<String, List<OXException>>(ID_COMPARATOR);
        warnings.putAll(eventStorage.getAndFlushWarnings());
        warnings.putAll(attendeeStorage.getAndFlushWarnings());
        warnings.putAll(alarmStorage.getAndFlushWarnings());
        warnings.putAll(conferenceStorage.getAndFlushWarnings());
        warnings.putAll(delegate.getAndFlushWarnings());
        return warnings;
    }

}
