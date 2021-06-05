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

package com.openexchange.chronos.storage.rdb.legacy;

import static com.openexchange.chronos.common.CalendarUtils.ID_COMPARATOR;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.chronos.storage.AttachmentStorage;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageUtilities;
import com.openexchange.chronos.storage.ConferenceStorage;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.chronos.storage.rdb.RdbCalendarStorageUtilities;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RdbCalendarStorage implements CalendarStorage {

    private final RdbEventStorage eventStorage;
    private final RdbAttendeeStorage attendeeStorage;
    private final RdbAlarmStorage alarmStorage;
    private final RdbAlarmTriggerStorage alarmTriggerStorage;
    private final RdbConferenceStorage conferenceStorage;
    private final RdbAttachmentStorage attachmentStorage;
    private final CalendarAccountStorage accountStorage;
    private final CalendarStorageUtilities storageUtilities;

    /**
     * Initializes a new {@link RdbCalendarStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbCalendarStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super();
        eventStorage = new RdbEventStorage(context, entityResolver, dbProvider, txPolicy);
        attendeeStorage = new RdbAttendeeStorage(context, entityResolver, dbProvider, txPolicy);
        alarmStorage = new RdbAlarmStorage(context, entityResolver, dbProvider, txPolicy);
        alarmTriggerStorage = new RdbAlarmTriggerStorage(context, entityResolver, dbProvider, txPolicy, eventStorage);
        this.conferenceStorage = new RdbConferenceStorage(context, dbProvider, txPolicy);
        attachmentStorage = new RdbAttachmentStorage(context, dbProvider, txPolicy);
        accountStorage = com.openexchange.chronos.storage.rdb.RdbCalendarAccountStorage.init(context, dbProvider, txPolicy);
        storageUtilities = new RdbCalendarStorageUtilities(this);
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
        return attachmentStorage;
    }

    @Override
    public AttendeeStorage getAttendeeStorage() {
        return attendeeStorage;
    }

    @Override
    public ConferenceStorage getConferenceStorage() {
        return conferenceStorage;
    }

    @Override
    public CalendarAccountStorage getAccountStorage() {
        return accountStorage;
    }

    @Override
    public CalendarStorageUtilities getUtilities() {
        return storageUtilities;
    }

    @Override
    public Map<String, List<OXException>> getWarnings() {
        Map<String, List<OXException>> warnings = new TreeMap<String, List<OXException>>(ID_COMPARATOR);
        warnings.putAll(eventStorage.getWarnings());
        warnings.putAll(attendeeStorage.getWarnings());
        warnings.putAll(alarmStorage.getWarnings());
        if (null != attachmentStorage) {
            warnings.putAll(attachmentStorage.getWarnings());
        }
        return warnings;
    }

    @Override
    public Map<String, List<OXException>> getAndFlushWarnings() {
        Map<String, List<OXException>> warnings = new TreeMap<String, List<OXException>>(ID_COMPARATOR);
        warnings.putAll(eventStorage.getAndFlushWarnings());
        warnings.putAll(attendeeStorage.getAndFlushWarnings());
        warnings.putAll(alarmStorage.getAndFlushWarnings());
        if (null != attachmentStorage) {
            warnings.putAll(attachmentStorage.getAndFlushWarnings());
        }
        return warnings;
    }

    @Override
    public AlarmTriggerStorage getAlarmTriggerStorage() {
        return alarmTriggerStorage;
    }

}
