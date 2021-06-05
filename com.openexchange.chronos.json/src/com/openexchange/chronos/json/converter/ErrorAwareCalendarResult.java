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

package com.openexchange.chronos.json.converter;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ErrorAwareCalendarResult}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ErrorAwareCalendarResult implements CalendarResult {

    private OXException error = null;
    private final EventID id;
    private final Session session;
    private final int user;
    private CalendarResult delegate;

    public ErrorAwareCalendarResult(OXException error, int calUser, EventID id, Session session) {
        this.error = error;
        this.id = id;
        this.session = session;
        user = calUser;
    }

    public ErrorAwareCalendarResult(CalendarResult delegate, int calUser, EventID id, Session session) {
        this.id = id;
        this.session = session;
        user = calUser;
        this.delegate = delegate;
    }

    public boolean hasError() {
        return error != null;
    }

    public OXException getError() {
        return error;
    }

    public EventID getId() {
        return id;
    }

    @Override
    public long getTimestamp() {
        if (delegate != null) {
            return delegate.getTimestamp();
        }
        return 0l;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public int getCalendarUser() {
        return user;
    }

    @Override
    public String getFolderID() {
        return id.getFolderID();
    }

    @Override
    public List<DeleteResult> getDeletions() {
        if (delegate != null) {
            return delegate.getDeletions();
        }
        return Collections.emptyList();
    }

    @Override
    public List<UpdateResult> getUpdates() {
        if (delegate != null) {
            return delegate.getUpdates();
        }
        return Collections.emptyList();
    }

    @Override
    public List<CreateResult> getCreations() {
        if (delegate != null) {
            return delegate.getCreations();
        }
        return Collections.emptyList();
    }

}
