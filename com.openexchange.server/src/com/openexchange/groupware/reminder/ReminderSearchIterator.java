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

package com.openexchange.groupware.reminder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.iterator.SearchIterator;

class ReminderSearchIterator implements SearchIterator<ReminderObject> {

    private ReminderObject next;
    private boolean closed = false;
    private final ResultSet rs;
    private final PreparedStatement preparedStatement;
    private final Connection readCon;
    private final List<OXException> warnings;
    private final Context ctx;

    ReminderSearchIterator(final Context ctx, final PreparedStatement preparedStatement, final ResultSet rs, final Connection readCon) throws OXException {
        super();
        this.ctx = ctx;
        this.warnings =  new ArrayList<OXException>(2);
        this.rs = rs;
        this.readCon = readCon;
        this.preparedStatement = preparedStatement;
        try {
            next = ReminderHandler.result2Object(ctx, rs, preparedStatement, false);
        } catch (OXException exc) {
            next = null;
        } catch (SQLException exc) {
            throw ReminderExceptionCode.SQL_ERROR.create(exc, exc.getMessage());
        }
    }

    @Override
    public boolean hasNext() throws OXException {
        return closed ? false : next != null;
    }

    @Override
    public ReminderObject next() throws OXException {
        final ReminderObject reminderObj = next;
        try {
            if (closed) {
                next = null;
            } else {
                next = ReminderHandler.result2Object(ctx, rs, preparedStatement, false);
            }
        } catch (OXException exc) {
            next = null;
        } catch (SQLException exc) {
            throw ReminderExceptionCode.SQL_ERROR.create(exc, exc.getMessage());
        }
        return reminderObj;
    }

    @Override
    public void close() {
        Databases.closeSQLStuff(rs, preparedStatement);
        DBPool.closeReaderSilent(ctx, readCon);
        closed = true;
    }

    @Override
    public int size() {
        return -1;
    }

    public boolean hasSize() {
        return false;
    }

    @Override
    public void addWarning(final OXException warning) {
        warnings.add(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
