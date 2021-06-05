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

package com.openexchange.chronos.storage.rdb.replaying;

import static com.openexchange.chronos.common.CalendarUtils.ID_COMPARATOR;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RdbCalendarStorage implements CalendarStorage {

    private final CalendarStorage delegate;
    private final CalendarStorage legacyDelegate;

    /**
     * Initializes a new {@link RdbCalendarStorage}.
     *
     * @param delegate The delegate storage
     * @param legacyDelegate The legacy delegate storage
     */
    public RdbCalendarStorage(CalendarStorage delegate, CalendarStorage legacyDelegate) {
        super();
        this.delegate = delegate;
        this.legacyDelegate = legacyDelegate;
    }

    @Override
    public EventStorage getEventStorage() {
        return new RdbEventStorage(delegate.getEventStorage(), legacyDelegate.getEventStorage());
    }

    @Override
    public AlarmStorage getAlarmStorage() {
        return new RdbAlarmStorage(delegate.getAlarmStorage(), legacyDelegate.getAlarmStorage());
    }

    @Override
    public AttachmentStorage getAttachmentStorage() {
        return delegate.getAttachmentStorage();
    }

    @Override
    public AttendeeStorage getAttendeeStorage() {
        return new RdbAttendeeStorage(delegate.getAttendeeStorage(), legacyDelegate.getAttendeeStorage());
    }

    @Override
    public ConferenceStorage getConferenceStorage() {
        return delegate.getConferenceStorage();
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
        warnings.putAll(delegate.getWarnings());
        warnings.putAll(legacyDelegate.getWarnings());
        return warnings;
    }

    @Override
    public Map<String, List<OXException>> getAndFlushWarnings() {
        Map<String, List<OXException>> warnings = new TreeMap<String, List<OXException>>(ID_COMPARATOR);
        warnings.putAll(delegate.getAndFlushWarnings());
        warnings.putAll(legacyDelegate.getAndFlushWarnings());
        return warnings;
    }

    @Override
    public AlarmTriggerStorage getAlarmTriggerStorage() {
        return delegate.getAlarmTriggerStorage();
    }

}
