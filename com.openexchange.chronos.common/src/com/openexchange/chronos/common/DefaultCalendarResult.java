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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.getMaximumTimestamp;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.session.Session;

/**
 * {@link DefaultCalendarResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarResult implements CalendarResult {

    private final Session session;
    private final int calendarUserId;
    private final String folderId;

    private final List<CreateResult> creations;
    private final List<UpdateResult> updates;
    private final List<DeleteResult> deletions;

    /**
     * Initializes a new {@link DefaultCalendarResult}.
     *
     * @param session The session
     * @param calendarUserId The actual calendar user
     * @param folderId The identifier of the targeted calendar folder
     * @param creations The create results, or <code>null</code> if there are none
     * @param updates The update results, or <code>null</code> if there are none
     * @param deletions The delete results, or <code>null</code> if there are none
     */
    public DefaultCalendarResult(Session session, int calendarUserId, String folderId, List<CreateResult> creations, List<UpdateResult> updates, List<DeleteResult> deletions) {
        super();
        this.session = session;
        this.calendarUserId = calendarUserId;
        this.folderId = folderId;
        this.creations = creations;
        this.updates = updates;
        this.deletions = deletions;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public int getCalendarUser() {
        return calendarUserId;
    }

    @Override
    public String getFolderID() {
        return folderId;
    }

    @Override
    public long getTimestamp() {
        return Math.max(getMaximumTimestamp(creations), Math.max(getMaximumTimestamp(deletions), getMaximumTimestamp(updates)));
    }

    @Override
    public List<DeleteResult> getDeletions() {
        return null == deletions ? Collections.<DeleteResult> emptyList() : Collections.unmodifiableList(deletions);
    }

    @Override
    public List<UpdateResult> getUpdates() {
        return null == updates ? Collections.<UpdateResult> emptyList() : Collections.unmodifiableList(updates);
    }

    @Override
    public List<CreateResult> getCreations() {
        return null == creations ? Collections.<CreateResult> emptyList() : Collections.unmodifiableList(creations);
    }

    @Override
    public String toString() {
        return "DefaultCalendarResult [session=" + session + ", calendarUserId=" + calendarUserId + ", folderId=" + folderId + ", creations=" + creations + ", updates=" + updates + ", deletions=" + deletions + "]";
    }

}
