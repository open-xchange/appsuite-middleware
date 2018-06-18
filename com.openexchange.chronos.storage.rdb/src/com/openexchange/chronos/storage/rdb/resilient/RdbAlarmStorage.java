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

package com.openexchange.chronos.storage.rdb.resilient;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbAlarmStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAlarmStorage extends RdbResilientStorage implements AlarmStorage {

    private final AlarmStorage delegate;

    /**
     * Initializes a new {@link RdbAlarmStorage}.
     *
     * @param services A service lookup reference
     * @param delegate The delegate storage
     * @param handleTruncations <code>true</code> to automatically handle data truncation warnings, <code>false</code>, otherwise
     * @param handleIncorrectStrings <code>true</code> to automatically handle incorrect string warnings, <code>false</code>, otherwise
     * @param unsupportedDataThreshold The threshold defining up to which severity unsupported data errors can be ignored, or <code>null</code> to not ignore any
     *            unsupported data error at all
     */
    public RdbAlarmStorage(ServiceLookup services, AlarmStorage delegate, boolean handleTruncations, boolean handleIncorrectStrings, ProblemSeverity unsupportedDataThreshold) {
        super(services, handleTruncations, handleIncorrectStrings);
        this.delegate = delegate;
        setUnsupportedDataThreshold(unsupportedDataThreshold, delegate);
    }

    @Override
    public int nextId() throws OXException {
        return delegate.nextId();
    }

    @Override
    public void insertAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        delegate.insertAlarms(event, userID, alarms);
    }

    @Override
    public void insertAlarms(Event event, Map<Integer, List<Alarm>> alarmsByUserId) throws OXException {
        delegate.insertAlarms(event, alarmsByUserId);
    }

    @Override
    public void insertAlarms(Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws OXException {
        delegate.insertAlarms(alarmsByUserByEventId);
    }

    @Override
    public Map<Integer, List<Alarm>> loadAlarms(Event event) throws OXException {
        return delegate.loadAlarms(event);
    }

    @Override
    public List<Alarm> loadAlarms(Event event, int userID) throws OXException {
        return delegate.loadAlarms(event, userID);
    }

    @Override
    public Map<String, List<Alarm>> loadAlarms(List<Event> events, int userID) throws OXException {
        return delegate.loadAlarms(events, userID);
    }

    @Override
    public Map<String, Map<Integer, List<Alarm>>> loadAlarms(List<Event> events) throws OXException {
        return delegate.loadAlarms(events);
    }

    @Override
    public void updateAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        delegate.updateAlarms(event, userID, alarms);
    }

    @Override
    public void deleteAlarms(String eventId) throws OXException {
        delegate.deleteAlarms(eventId);
    }

    @Override
    public void deleteAlarms(List<String> eventIds) throws OXException {
        delegate.deleteAlarms(eventIds);
    }

    @Override
    public void deleteAlarms(int userId) throws OXException {
        delegate.deleteAlarms(userId);
    }

    @Override
    public boolean deleteAllAlarms() throws OXException {
        return delegate.deleteAllAlarms();
    }

    @Override
    public void deleteAlarms(String eventId, int userId) throws OXException {
        delegate.deleteAlarms(eventId, userId);
    }

    @Override
    public void deleteAlarms(String eventId, int[] userIds) throws OXException {
        delegate.deleteAlarms(eventId, userIds);
    }

    @Override
    public void deleteAlarms(String eventId, int userId, int[] alarmIds) throws OXException {
        delegate.deleteAlarms(eventId, userId, alarmIds);
    }

}
