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
     * @throws OXException
     */
    public RdbCalendarStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        super();
        eventStorage = new RdbEventStorage(context, entityResolver, dbProvider, txPolicy);
        attendeeStorage = new RdbAttendeeStorage(context, entityResolver, dbProvider, txPolicy);
        alarmStorage = new RdbAlarmStorage(context, entityResolver, dbProvider, txPolicy);
        alarmTriggerStorage = new RdbAlarmTriggerStorage(context, entityResolver, dbProvider, txPolicy, eventStorage);
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
