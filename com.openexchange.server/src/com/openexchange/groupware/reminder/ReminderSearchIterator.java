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
        } catch (final OXException exc) {
            next = null;
        } catch (final SQLException exc) {
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
        } catch (final OXException exc) {
            next = null;
        } catch (final SQLException exc) {
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
