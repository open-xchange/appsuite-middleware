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

package com.openexchange.chronos.provider.composition.impl.idmangling;

import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.chronos.service.UpdateResult;

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
