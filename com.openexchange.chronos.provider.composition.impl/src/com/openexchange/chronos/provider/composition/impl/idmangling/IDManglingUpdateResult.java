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

package com.openexchange.chronos.provider.composition.impl.idmangling;

import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;

/**
 * {@link IDManglingUpdateResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingUpdateResult implements UpdateResult {

    private final UpdateResult delegate;
    private final int accountId;

    /**
     * Initializes a new {@link IDManglingUpdateResult}.
     *
     * @param delegate The result delegate
     * @param accountId The identifier of the calendar account the result originates in
     */
    public IDManglingUpdateResult(UpdateResult delegate, int accountId) {
        super();
        this.delegate = delegate;
        this.accountId = accountId;
    }

    @Override
    public CollectionUpdate<Attendee, AttendeeField> getAttendeeUpdates() {
        return delegate.getAttendeeUpdates();
    }

    @Override
    public CollectionUpdate<Alarm, AlarmField> getAlarmUpdates() {
        return delegate.getAlarmUpdates();
    }

    @Override
    public SimpleCollectionUpdate<Attachment> getAttachmentUpdates() {
        return delegate.getAttachmentUpdates();
    }

    @Override
    public CollectionUpdate<Conference, ConferenceField> getConferenceUpdates() {
        return delegate.getConferenceUpdates();
    }

    @Override
    public Event getOriginal() {
        Event original = delegate.getOriginal();
        return null == original ? null : IDMangling.withUniqueID(original, accountId);
    }

    @Override
    public Event getUpdate() {
        Event update = delegate.getUpdate();
        return null == update ? null : IDMangling.withUniqueID(update, accountId);
    }

    @Override
    public Set<EventField> getUpdatedFields() {
        return delegate.getUpdatedFields();
    }

    @Override
    public boolean containsAnyChangeOf(EventField[] fields) {
        return delegate.containsAnyChangeOf(fields);
    }

    @Override
    public long getTimestamp() {
        return delegate.getTimestamp();
    }

}
