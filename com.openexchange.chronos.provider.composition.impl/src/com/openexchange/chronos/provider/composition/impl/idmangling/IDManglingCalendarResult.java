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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.session.Session;

/**
 * {@link IDManglingCalendarResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingCalendarResult implements CalendarResult {

    protected final CalendarResult delegate;
    protected final int accountId;

    /**
     * Initializes a new {@link IDManglingCalendarResult}.
     *
     * @param delegate The result delegate
     * @param accountId The identifier of the calendar account the result originates in
     */
    public IDManglingCalendarResult(CalendarResult delegate, int accountId) {
        super();
        this.delegate = delegate;
        this.accountId = accountId;
    }

    @Override
    public Session getSession() {
        return delegate.getSession();
    }

    @Override
    public int getCalendarUser() {
        return delegate.getCalendarUser();
    }

    @Override
    public long getTimestamp() {
        return delegate.getTimestamp();
    }

    @Override
    public String getFolderID() {
        return IDMangling.getUniqueFolderId(accountId, delegate.getFolderID());
    }

    @Override
    public List<DeleteResult> getDeletions() {
        List<DeleteResult> deletions = delegate.getDeletions();
        if (null == deletions) {
            return Collections.emptyList();
        }
        List<DeleteResult> idManglingDeletions = new ArrayList<DeleteResult>(deletions.size());
        for (DeleteResult deletion : deletions) {
            idManglingDeletions.add(new IDManglingDeleteResult(deletion, accountId));
        }
        return idManglingDeletions;
    }

    @Override
    public List<UpdateResult> getUpdates() {
        List<UpdateResult> updates = delegate.getUpdates();
        if (null == updates) {
            return Collections.emptyList();
        }
        List<UpdateResult> idManglingUpdates = new ArrayList<UpdateResult>(updates.size());
        for (UpdateResult update : updates) {
            idManglingUpdates.add(new IDManglingUpdateResult(update, accountId));
        }
        return idManglingUpdates;
    }

    @Override
    public List<CreateResult> getCreations() {
        List<CreateResult> creations = delegate.getCreations();
        if (null == creations) {
            return Collections.emptyList();
        }
        List<CreateResult> idManglingCreations = new ArrayList<CreateResult>(creations.size());
        for (CreateResult creation : creations) {
            idManglingCreations.add(new IDManglingCreateResult(creation, accountId));
        }
        return idManglingCreations;
    }

    @Override
    public String toString() {
        return "IDManglingCalendarResult [accountId=" + accountId + ", delegate=" + delegate + "]";
    }

}
